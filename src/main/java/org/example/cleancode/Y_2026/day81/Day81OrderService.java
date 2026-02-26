package org.example.cleancode.Y_2026.day81;

import lombok.Value;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Day 81 과제 — 비동기 서비스 오케스트레이션 리팩터링
 *
 * 블로킹 호출 — get(timeout)으로 스레드를 직접 블로킹해 비동기의 이점을 버림
 * 무분별한 예외 — 모든 오류를 RuntimeException + 문자열로 처리해 예외 구분 불가
 * 반환 타입 불일치 — 비동기 메서드가 String을 반환해 호출자가 결과를 체이닝할 수 없음
 * 순차 알림 전송 — 이메일·푸시를 순서대로 실행해 불필요한 지연 발생
 * 단일 catch로 모든 예외 흡수 — 타임아웃, 결제 실패, 재고 부족을 동일하게 처리해 디버깅·복구 불가
 */
public class Day81OrderService {

    private UserService userService;
    private InventoryService inventoryService;
    private PaymentService paymentService;
    private NotificationService notificationService;

    public CompletableFuture<OrderResult> processOrder(OrderRequest request) {
            CompletableFuture<User> userFuture = userService.getUser(request.getUserId())
                    .thenApply(user -> {
                        if (user == null) {
                            throw new UserNotFoundException(request.getUserId());
                        }

                        return user;
                    });

            CompletableFuture<Product> productFuture = inventoryService.getProduct(request.getProductId())
                    .thenApply(item -> {
                        if(item == null) {
                            throw new OutOfStockException(request.getProductId());
                        }
                        if(item.getStock() < request.getQuantity()) {
                            throw new OutOfStockException(request.getProductId());
                        }

                        return item;
                    });


            // 오늘 소스에서 가장 중요한 부분 (메서드 체이닝 -> 결과를 합치고 다음 로직에 결과를 넘기고 알림 처리와 예외 처리까지)
            // thenCombine은 두 결과를 합칠때, then Compose는 앞 결과를 다음 비동기 결과에 넘길 때 사용
            return userFuture.thenCombine(productFuture, (user, product) -> {
                // 두 값을 묶어서 다음 도큐먼트로 전달
                return Map.entry(user, product);
            }).thenCompose(entry -> {
                User user = entry.getKey();
                Product product = entry.getValue();
                long amount = product.getPrice() * request.getQuantity();

                return paymentService.charge(user.getPaymentMethod(), amount)
                        .thenCompose(result -> {
                            if(!result.isSuccess()) {
                                throw new PaymentFailedException(result.getErrorCode());
                            }

                            return inventoryService.decreaseStock(request.getProductId(), request.getQuantity())
                                    .thenApply(v -> new PaymentContext(user, OrderResult.success(result.getTransactionId())));
                        });

            }).thenCompose(ctx -> {
                // 알림 병렬 발송
                CompletableFuture<Void> emailFuture = notificationService.sendEmail(ctx.user().getEmail(), "주문 완료");
                CompletableFuture<Void> pushFuture = notificationService.sendPush(ctx.user().getDeviceToken(), "주문 완료");
                
                return CompletableFuture.allOf(emailFuture, pushFuture)
                        .thenApply(v -> ctx.orderResult())
                        .orTimeout(10, TimeUnit.SECONDS) // 전체 10초 초과 시 타임아웃 처리
                        .exceptionally(ex -> {
                            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                            if (cause instanceof PaymentFailedException)  return OrderResult.failure("PAYMENT_FAILED");
                            if (cause instanceof OutOfStockException)     return OrderResult.failure("OUT_OF_STOCK");
                            if (cause instanceof UserNotFoundException)   return OrderResult.failure("USER_NOT_FOUND");
                            if (cause instanceof TimeoutException)        return OrderResult.failure("TIMEOUT");
                            return OrderResult.failure("UNKNOWN");
                        });
            });


    }
}


@Value
class OrderRequest {
    Long userId;
    Long productId;
    int quantity;
}

@Value
class OrderResult {
    String transactionId;
    OrderStatus status;

    public static OrderResult success(String transactionId) {
        return new OrderResult(transactionId, OrderStatus.SUCCESS);
    }

    public static OrderResult failure(String errorCode) {
        return new OrderResult(errorCode, OrderStatus.FAILED);
    }
}

@Value
class PaymentResult {
    String transactionId;
    boolean success;
    String errorCode;
}

@Value
class User {
    Long id;
    String email;
    String deviceToken;
    String paymentMethod;
}

@Value
class Product {
    Long id;
    int stock;
    long price;
}

enum OrderStatus {
    SUCCESS, FAILED, TIMEOUT
}


interface UserService {
    CompletableFuture<User> getUser(Long userId);
}

interface InventoryService {
    CompletableFuture<Product> getProduct(Long productId);
    CompletableFuture<Void> decreaseStock(Long productId, int quantity);
}

interface PaymentService {
    CompletableFuture<PaymentResult> charge(String paymentMethod, long amount);
}

interface NotificationService {
    CompletableFuture<Void> sendEmail(String email, String message);
    CompletableFuture<Void> sendPush(String deviceToken, String message);
}

interface OrderService {
    CompletableFuture<OrderResult> processOrder(OrderRequest request);
}

class OrderServiceImpl implements OrderService {
    @Override
    public CompletableFuture<OrderResult> processOrder(OrderRequest request) {
        return null;
    }
}

class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long userId) {
        super("User not found: " + userId);
    }
}

class OutOfStockException extends RuntimeException {
    public OutOfStockException(Long productId) {
        super("Out of stock: " + productId);
    }
}

class PaymentFailedException extends RuntimeException {
    public PaymentFailedException(String errorCode) {
        super("Payment failed: " + errorCode);
    }
}

record PaymentContext(User user, OrderResult orderResult) {};
