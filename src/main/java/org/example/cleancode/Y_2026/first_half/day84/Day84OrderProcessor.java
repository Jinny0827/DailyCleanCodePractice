package org.example.cleancode.Y_2026.first_half.day84;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Day 84 과제 — CompletableFuture 체이닝 정리
 *
 * 예외 처리 - RuntimeException 남용, 의미 불명확
 * 메서드 길이 - 람다 내부 로직이 너무 두꺼움
 * 에러 복구 - exceptionally에서 null 반환
 * 알림 메시지 - 문자열 직접 조합
 */
public class Day84OrderProcessor {

    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    private final PaymentService paymentService;
    private final ShippingService shippingService;
    private final NotificationService notificationService;

    public Day84OrderProcessor(OrderRepository orderRepository, InventoryService inventoryService, PaymentService paymentService, ShippingService shippingService, NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.inventoryService = inventoryService;
        this.paymentService = paymentService;
        this.shippingService = shippingService;
        this.notificationService = notificationService;
    }

    public void processOrder(String orderId) {

        // 람다 메서드화
        CompletableFuture.supplyAsync(() -> findOrder(orderId))
                .thenApply(this::checkInventory)
                .thenApply(this::processPayment)
                .thenApply(this::requestShipping)
                .thenAccept(this::notifyUser)
                .handle((order, ex) -> {
                     if(ex != null) {
                         handleError(ex);
                     }

                    return null;
                });
    }

    private Order findOrder(String orderId) {
        Order order = orderRepository.findById(orderId);
        if (order == null) throw new OrderNotFoundException(orderId);
        return order;
    }

    private Order checkInventory(Order order) {
        boolean ok = inventoryService.check(order.getProductId(), order.getQuantity());
        if (!ok) throw new OutOfStockException(order.getProductId());
        return order;
    }

    private Order processPayment(Order order) {
        boolean paid = paymentService.pay(order.getUserId(), order.getAmount());
        if (!paid) throw new PaymentFailedException(order.getUserId());
        return order;
    }

    private Order requestShipping(Order order) {
        String trackingId = shippingService.ship(order);
        if (trackingId == null) throw new ShippingFailedException(order.getId());
        order.setTrackingId(trackingId);
        return order;
    }

    private void notifyUser(Order order) {
        String message = "주문 완료: %s, 운송장: %s"
                .formatted(order.getId(), order.getTrackingId());
        notificationService.notify(order.getUserId(), message);
    }
    
    // 예외 처리 메서드
    private void handleError(Throwable ex) {

        if (ex instanceof OrderNotFoundException) {
            System.out.println("[주문오류] " + ex.getMessage());
        } else if (ex instanceof OutOfStockException) {
            System.out.println("[재고오류] " + ex.getMessage());
        } else if (ex instanceof PaymentFailedException) {
            System.out.println("[결제오류] " + ex.getMessage());
        } else if (ex instanceof ShippingFailedException) {
            System.out.println("[배송오류] " + ex.getMessage());
        } else {
            System.out.println("[알 수 없는 오류] " + ex.getMessage());
        }
    }

}

@Data
class Order {
    private String id;
    private String userId;
    private String productId;
    private int quantity;
    private double amount;
    private String trackingId;

    public Order(String id, String userId, String productId, int quantity, double amount) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.amount = amount;
    }
}

class OrderRepository {
    private static final Map<String, Order> store = new HashMap<>();

    static {
        store.put("ORDER-001", new Order("ORDER-001", "user-1", "prod-A", 2, 39000));
        store.put("ORDER-002", new Order("ORDER-002", "user-2", "prod-B", 1, 15000));
    }

    public Order findById(String orderId) {
        return store.get(orderId); // 없으면 null 반환 (의도적)
    }
}

class InventoryService {
    public boolean check(String productId, int quantity) {
        return true; // "prod-C"이면 false 리턴하도록 바꿔서 실패 케이스 테스트 가능
    }
}

class PaymentService {
    public boolean pay(String userId, double amount) {
        return true;
    }
}

class ShippingService {
    public String ship(Order order) {
        return "TRACK-" + UUID.randomUUID().toString().substring(0, 8);
    }
}

class NotificationService {
    public void notify(String userId, String message) {
        System.out.println("[알림→" + userId + "] " + message);
    }
}

// 예외 클래스
class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String orderId) {
        super("주문을 찾을 수 없습니다: " + orderId);
    }
}

class OutOfStockException extends RuntimeException {
    public OutOfStockException(String productId) {
        super("재고가 부족합니다: " + productId);
    }
}

class PaymentFailedException extends RuntimeException {
    public PaymentFailedException(String userId) {
        super("결제가 실패하였습니다: " + userId);
    }
}

class ShippingFailedException extends RuntimeException {
        public ShippingFailedException(String orderId) {
                super("배송 처리 실패: " + orderId);
        }
}