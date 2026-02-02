package org.example.cleancode.Y_2026.day65;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 *  Day 65 - 병렬 처리와 예외 안전성
 *
 *  병렬 처리: 여러 주문을 동시에 처리
 *  보상 트랜잭션 패턴: 실패 시 롤백 로직 체계화
 *  함수 분리: 각 단계별 책임 분리
 *  예외 안전성: 부분 실패 상황 명확히 처리
 *  불변성: 중간 상태 안전하게 관리
 */
public class Day65OrderProcessor {

    private PaymentService paymentService;
    private InventoryService inventoryService;
    private EmailService emailService;

    // 단건 주문 처리
    private OrderResult processOrder(Order order) {
        OrderResult result = new OrderResult(order.getId());
        CompensationManager cm = new CompensationManager();

        // 결제 처리
        Payment payment = processPayment(order, result, cm);
        if(payment == null) {
            cm.compensateAll();
            return result;
        };

        // 재고 차감
        boolean inventoryOk = processInventory(order, payment, result, cm);
        if (!inventoryOk) {
            cm.compensateAll();
            return result;
        };

        // 이메일 발송
        boolean emailOk = processEmail(order, payment, result, cm);
        if(!emailOk) {
            cm.compensateAll();
            return result;
        };

        result.setStatus("SUCCESS");

        return result;
    }

    // 단건 주문처리를 통한 다건 주문처리
    public List<OrderResult> processOrders(List<Order> orders) {
        // 각 주문 비동기  처리
        List<CompletableFuture<OrderResult>> futures = orders.stream()
                .map(order -> CompletableFuture.supplyAsync(() -> processOrder(order)))
                .collect(Collectors.toList());

        // 모든 결과를 기다림
        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

    }

    // 결제 처리
    private Payment processPayment(Order order, OrderResult result, CompensationManager cm) {

        Result<Payment, PaymentException> paymentResult = paymentService.charge(order.getAmount());

        if(paymentResult.isFailure()) {
            result.setStatus("PAYMENT_FAILED");
            result.setError(paymentResult.getError().getMessage());
            return null;
        }

        Payment payment = paymentResult.getValue();
        result.setPaymentId(payment.getId());

        cm.add(() -> paymentService.refund(payment.getId()));

        return payment;
    }

    // 재고 차감
    private boolean processInventory(Order order, Payment payment, OrderResult result, CompensationManager cm) {

        for (OrderItem item : order.getItems()) {
            Result<Void, InventoryException> inventoryResult =
                    inventoryService.decreaseStock(item.getProductId(), item.getQuantity());

            if(inventoryResult.isFailure()) {
                result.setStatus("INVENTORY_FAILED");
                result.setError(inventoryResult.getError().getMessage());
                return false;
            }
        }

        result.setInventoryUpdated(true);
        
        // 보상 작업 등록
        for(OrderItem item : order.getItems()) {
            String productId = item.getProductId();
            int quantity = item.getQuantity();
            cm.add(() -> inventoryService.increaseStock(productId, quantity));
        }

        
        return true;
    }

    // 이메일 발송
    private boolean processEmail(Order order, Payment payment, OrderResult result, CompensationManager cm) {

        Result<Void, Exception> emailResult =
                emailService.sendConfirmation(order.getCustomerEmail(), order.getId());

        if(emailResult.isFailure()) {
            result.setStatus("EMAIL_FAILED");
            result.setError(emailResult.getError().getMessage());
            return false;
        }

        result.setEmailSent(true);
        return true;

    }
}

// 서비스 인터페이스
interface PaymentService {
    Result<Payment, PaymentException> charge(BigDecimal amount);
    Result<Void, PaymentException> refund(String paymentId);
}

interface InventoryService {
    Result<Void, InventoryException> decreaseStock(String productId, int quantity);
    Result<Void, InventoryException> increaseStock(String productId, int quantity);
}

interface EmailService {
    Result<Void, Exception> sendConfirmation(String email, String orderId);
}


// 보상작업들을 저장할 클래스
// 결제 취소 등 여러가지 상황에 대한 보상 처리 결합체 & 구현체
@FunctionalInterface
interface CompensatingAction {
    void compensate();
}

// 보상 작업 관리자
class CompensationManager {
    private Stack<CompensatingAction> compensations = new Stack<>();
    
    // 보상 작업 추가
    public void add(CompensatingAction action) {
        compensations.add(action);
    }
    
    // 모든 보상 실행
    public void compensateAll() {
        while (!compensations.isEmpty()) {
            CompensatingAction action = compensations.pop();
            action.compensate();
        }
    }
}


// Result 타입 도입 -> 성공/실패를 타입으로 명시, 예외 대신 값으로 에러 전달
class Result<T, E>  {
    // 성공 시 값
    private final T value;
    //실패 시 에러
    private final E error;
    // 성공 여부
    private final boolean isSuccess;

    // 외부 직접 생성 X
    private Result(T value, E error, boolean isSuccess) {
        this.value = value;
        this.error = error;
        this.isSuccess = isSuccess;
    }

    // 성공 객체 생성 메서드 (값, 에러내용null, 참)
    public static <T, E extends Exception> Result<T,E> success(T value) {
        return new Result<>(value, null, true);
    }
    
    // 실패시 객체 생성 메서드 (값null, error내용, 거짓)
    public static <T, E extends Exception> Result<T,E> failure(E error) {
        return new Result<>(null, error, false);
    }

    // 상태 확인
    public boolean isSuccess() {
        return isSuccess;
    }

    public boolean isFailure() {
        return !isSuccess;
    }

    public T getValue() {
        return value;
    }

    public E getError() {
        return error;
    }
}

// 아래로 객체 구현체
class Order {
    private String id;
    private String customerEmail;
    private BigDecimal amount;
    private List<OrderItem> items;

    public Order(String id, String customerEmail, BigDecimal amount, List<OrderItem> items) {
        this.id = id;
        this.customerEmail = customerEmail;
        this.amount = amount;
        this.items = items;
    }


    public String getId() {
        return id;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public List<OrderItem> getItems() {
        return items;
    }
}


class OrderItem {
    private String productId;
    private int quantity;

    public OrderItem(String productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public String getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }
}

class Payment {
    private String id;
    private BigDecimal amount;

    public Payment(String id, BigDecimal amount) {
        this.id = id;
        this.amount = amount;
    }


    public String getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}

class OrderResult {
    private String orderId;
    private String status;
    private String paymentId;
    private boolean inventoryUpdated;
    private boolean emailSent;
    private String error;

    public OrderResult(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getStatus() {
        return status;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public boolean isInventoryUpdated() {
        return inventoryUpdated;
    }

    public boolean isEmailSent() {
        return emailSent;
    }

    public String getError() {
        return error;
    }


    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public void setInventoryUpdated(boolean inventoryUpdated) {
        this.inventoryUpdated = inventoryUpdated;
    }

    public void setEmailSent(boolean emailSent) {
        this.emailSent = emailSent;
    }

    public void setError(String error) {
        this.error = error;
    }
}

// 예외 구현 추가
class PaymentException extends Exception {
    public PaymentException(String message) {
        super(message);
    }
}

class InventoryException extends Exception {
    public InventoryException(String message) {
        super(message);
    }
}
