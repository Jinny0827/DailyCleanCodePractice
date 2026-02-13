package org.example.cleancode.Y_2026.day74;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Day 74: 상태 패턴으로 주문 처리 로직 개선
 *
 * 상태 패턴 적용 - 상태별 행동을 객체로 캡슐화
 * 불가능한 상태 전이 방지 - 타입 시스템으로 보장
 * 확장성 - 새 상태 추가 시 기존 코드 수정 최소화
 */
public class Day74OrderProcessor {
    private final StateContext context;
    private OrderState currentState;

    public Day74OrderProcessor(NotificationService notificationService,
                               InventoryService inventoryService,
                               RefundService refundService) {
       this.context = new StateContext(
               notificationService,
               inventoryService,
               refundService
       );
       this.currentState = new PendingState();
    }

    public void pay(Order order) {
        currentState.pay(order, context)
                .ifPresent(newState -> currentState = newState);
    }

    public void ship(Order order) {
        currentState.ship(order, context)
                .ifPresent(newState -> currentState = newState);
    }

    public void deliver(Order order) {
        currentState.deliver(order, context)
                .ifPresent(newState -> currentState = newState);
    }

    public void cancel(Order order) {
        currentState.cancel(order, context)
                .ifPresent(newState -> currentState = newState);
    }

    public String getCurrentState() {
        return currentState.getStateName();
    }

    public boolean canCancel() {
        return currentState.canCancel();
    }

    public boolean canPay() { return currentState.canPay(); }
    public boolean canShip() { return currentState.canShip(); }
    public boolean canDeliver() { return currentState.canDeliver(); }

}

interface NotificationService {
    void send(String message);
}

interface InventoryService {
    void reserve(List<OrderItem> items);
    void release(List<OrderItem> items);
}

interface RefundService {
    void process(Order order);
}

// 주문 상태 추상화 객체
interface OrderState {
    // 각 액션 메서드는 Optional<OrderState>를 반환
    // - 성공 시: 새로운 상태 반환
    // - 실패/불가능 시: Optional.empty()

    Optional<OrderState> pay(Order order, StateContext context);
    Optional<OrderState> ship(Order order, StateContext context);
    Optional<OrderState> deliver(Order order, StateContext context);
    Optional<OrderState> cancel(Order order, StateContext context);

    // 가능 여부 메서드
    boolean canPay();
    boolean canShip();
    boolean canDeliver();
    boolean canCancel();

    String getStateName();
}

class PendingState implements OrderState {

    @Override
    public Optional<OrderState> pay(Order order, StateContext context) {
        // 유효성 검사
        if (order.getAmount() <= 0 || order.getPaymentMethod() == null) {
            return Optional.empty();
        }

        // 비지니스 로직
        order.setPaymentDate(LocalDateTime.now());
        context.getNotificationService().send("Payment confirmed");
        context.getInventoryService().reserve(order.getItems());

        return Optional.of(new PaidState());
    }

    @Override
    public Optional<OrderState> ship(Order order, StateContext context) {
        // PENDING 상태에서는 배송 불가
        return Optional.empty();
    }

    @Override
    public Optional<OrderState> deliver(Order order, StateContext context) {
        // PENDING 상태에서는 배송완료 불가
        return Optional.empty();
    }

    @Override
    public Optional<OrderState> cancel(Order order, StateContext context) {
        context.getNotificationService().send("Order cancelled");
        return Optional.of(new CancelledState());
    }

    @Override
    public String getStateName() {
        return "PENDING";
    }

    @Override
    public boolean canPay() { return true; }

    @Override
    public boolean canShip() { return false; }

    @Override
    public boolean canDeliver() { return false; }

    @Override
    public boolean canCancel() { return true; }
}

class PaidState implements OrderState {
    @Override
    public Optional<OrderState> pay(Order order, StateContext context) {
        return Optional.empty();
    }

    @Override
    public Optional<OrderState> ship(Order order, StateContext context) {
        order.setShippingDate(LocalDateTime.now());
        context.getNotificationService().send("Order shipped");
        return Optional.of(new ShippedState());
    }

    @Override
    public Optional<OrderState> deliver(Order order, StateContext context) {
        return Optional.empty();
    }

    @Override
    public Optional<OrderState> cancel(Order order, StateContext context) {
        context.getInventoryService().release(order.getItems());
        context.getRefundService().process(order);
        return Optional.of(new CancelledState());
    }

    @Override
    public String getStateName() {
        return "PAID";
    }

    @Override
    public boolean canPay() { return false; }

    @Override
    public boolean canShip() { return true; }

    @Override
    public boolean canDeliver() { return false; }

    @Override
    public boolean canCancel() { return true; }
}

class ShippedState implements OrderState{
    @Override
    public Optional<OrderState> pay(Order order, StateContext context) {
        return Optional.empty();
    }

    @Override
    public Optional<OrderState> ship(Order order, StateContext context) {
        return Optional.empty();
    }

    @Override
    public Optional<OrderState> deliver(Order order, StateContext context) {
        order.setDeliveryDate(LocalDateTime.now());
        context.getNotificationService().send("Order delivered");
        return Optional.of(new DeliveredState());
    }

    @Override
    public Optional<OrderState> cancel(Order order, StateContext context) {
        return Optional.empty();
    }

    @Override
    public String getStateName() {
        return "SHIPPED";
    }

    @Override
    public boolean canPay() { return false; }

    @Override
    public boolean canShip() { return false; }

    @Override
    public boolean canDeliver() { return true; }

    @Override
    public boolean canCancel() { return false; }
}

class DeliveredState implements OrderState {
    @Override
    public Optional<OrderState> pay(Order order, StateContext context) {
        return Optional.empty();
    }

    @Override
    public Optional<OrderState> ship(Order order, StateContext context) {
        return Optional.empty();
    }

    @Override
    public Optional<OrderState> deliver(Order order, StateContext context) {
        return Optional.empty();
    }

    @Override
    public Optional<OrderState> cancel(Order order, StateContext context) {
        return Optional.empty();
    }

    @Override
    public String getStateName() {
        return "DELIVERED";
    }

    @Override
    public boolean canPay() { return false; }

    @Override
    public boolean canShip() { return false; }

    @Override
    public boolean canDeliver() { return false; }

    @Override
    public boolean canCancel() { return false; }
}

class CancelledState implements OrderState {
    @Override
    public Optional<OrderState> pay(Order order, StateContext context) {
        return Optional.empty();
    }

    @Override
    public Optional<OrderState> ship(Order order, StateContext context) {
        return Optional.empty();
    }

    @Override
    public Optional<OrderState> deliver(Order order, StateContext context) {
        return Optional.empty();
    }

    @Override
    public Optional<OrderState> cancel(Order order, StateContext context) {
        return Optional.empty();
    }

    @Override
    public String getStateName() {
        return "Cancelled";
    }

    @Override
    public boolean canPay() { return false; }

    @Override
    public boolean canShip() { return false; }

    @Override
    public boolean canDeliver() { return false; }

    @Override
    public boolean canCancel() { return false; }
}



// 서비스들을 상태 객체에 전달하기 위한 컨텍스트
class StateContext {
    private final NotificationService notificationService;
    private final InventoryService inventoryService;
    private final RefundService refundService;

    public StateContext(NotificationService notificationService, InventoryService inventoryService, RefundService refundService) {
        this.notificationService = notificationService;
        this.inventoryService = inventoryService;
        this.refundService = refundService;
    }

    public NotificationService getNotificationService() {
        return notificationService;
    }

    public InventoryService getInventoryService() {
        return inventoryService;
    }

    public RefundService getRefundService() {
        return refundService;
    }
}


class Order {
    private String id;
    private List<OrderItem> items;
    private double amount;
    private String paymentMethod;
    private LocalDateTime orderDate;
    private LocalDateTime paymentDate;
    private LocalDateTime shippingDate;
    private LocalDateTime deliveryDate;

    public Order(String id, List<OrderItem> items, double amount) {
        this.id = id;
        this.items = items;
        this.amount = amount;
        this.orderDate = LocalDateTime.now();
    }

    // Getters
    public String getId() { return id; }
    public List<OrderItem> getItems() { return items; }
    public double getAmount() { return amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public LocalDateTime getPaymentDate() { return paymentDate; }
    public LocalDateTime getShippingDate() { return shippingDate; }
    public LocalDateTime getDeliveryDate() { return deliveryDate; }

    // Setters
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }
    public void setShippingDate(LocalDateTime shippingDate) {
        this.shippingDate = shippingDate;
    }
    public void setDeliveryDate(LocalDateTime deliveryDate) {
        this.deliveryDate = deliveryDate;
    }
}

class OrderItem {
    private String productId;
    private int quantity;
    private double price;

    public OrderItem(String productId, int quantity, double price) {
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }

    public String getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
}