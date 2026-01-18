package org.example.cleancode.Y_2026.day55;


import java.util.ArrayList;
import java.util.List;

/**
 * Day 55: 이벤트 기반 아키텍처 맛보기
 *
 * Event, EventPublisher, EventListener 설계
 * 핵심 로직(재고, 결제)과 부가 로직(이메일, 로그) 분리
 * OrderCreatedEvent 발행 → 각 리스너가 독립 처리
 * 실패 시 재시도/보상 트랜잭션 고려
 */
public class Day55OrderService {

    private InventoryService inventoryService;
    private PaymentService paymentService;
    private EventPublisher eventPublisher;

    public Day55OrderService(InventoryService inventoryService,
                             PaymentService paymentService,
                             EventPublisher eventPublisher) {
        this.inventoryService = inventoryService;
        this.paymentService = paymentService;
        this.eventPublisher = eventPublisher;
    }

    public static void main(String[] args) {
        // 서비스 생성
        InventoryService inventoryService = new InventoryService();
        PaymentService paymentService = new PaymentService();
        EmailService emailService = new EmailService();
        LogService logService = new LogService();

        // EventPublisher 객체 생성
        EventPublisher eventPublisher = new SimpleEventPublisher();

        // 리스너 생성 및 구독 등록
        eventPublisher.subscribe(new EmailEventListener(emailService));
        eventPublisher.subscribe(new LogEventListener(logService));

        // OrderService 생성
        Day55OrderService orderService = new Day55OrderService(
                inventoryService,
                paymentService,
                eventPublisher
        );

        // 주문 테스트
        Order order = new Order(
                "ORD-001",
                "LAPTOP",
                1,
                "user@example.com",
                new PaymentInfo("1234567890123456", 1500.0)
        );

        orderService.createOrder(order);
    }


    public void createOrder(Order order) {
        // 1. 재고 확인 및 차감
        inventoryService.reduceStock(order.getProductId(), order.getQuantity());

        // 2. 결제 처리
        paymentService.processPayment(order.getPaymentInfo());

        // 3. 주문 저장
        order.setStatus("COMPLETED");
        saveOrder(order);

        // 4. 이벤트 발행 (한번만) -> 모든 리스너가 독립적 처리
        eventPublisher.publish(new OrderCreatedEvent(order));
    }

    private void saveOrder(Order order) {
        // DB 저장
    }

}
// Event 클래스와 EventPublisher 설계
class OrderCreatedEvent {
    private final Order order;
    private final long timestamp;

    public OrderCreatedEvent(Order order) {
        this.order = order;
        this.timestamp = System.currentTimeMillis();
    }

    public Order getOrder() {
        return order;
    }

    public long getTimestamp() {
        return timestamp;
    }
}

interface EventPublisher {
    void publish(Object event);
    void subscribe(EventListener listener);
}

interface EventListener<T> {
    void onEvent(T event);
    Class<T> getEventType();
}

// 이벤트 퍼블리셔 구현체 
class SimpleEventPublisher implements EventPublisher {
    private List<EventListener<?>> listeners = new ArrayList<>();

    @Override
    public void publish(Object event) {
        for (EventListener listener : listeners) {
            // OrderCreatedEvent를 처리하는 리스너만 필터링
            if(listener.getEventType().isInstance(event)) {
                // instanceof와 동일하지만 동적 타입 체크
                // 타입 캐스팅의 이유(Object) -> 제네릭 타입 소거 때문에 필요
                ((EventListener<Object>) listener).onEvent(event);
            }
        }
    }

    @Override
    public void subscribe(EventListener listener) {
        // 리스너를 리스너 목록에 삽입
        listeners.add(listener);
    }
}

// 구체적인 EventListener 구현
class EmailEventListener implements EventListener<OrderCreatedEvent> {
    private EmailService emailService;

    public EmailEventListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void onEvent(OrderCreatedEvent event) {
        Order order = event.getOrder();
        emailService.sendOrderConfirmation(order.getCustomerEmail());
    }

    @Override
    public Class<OrderCreatedEvent> getEventType() {
        return OrderCreatedEvent.class;
    }
}


class LogEventListener implements EventListener<OrderCreatedEvent> {
    private LogService logService;

    public LogEventListener(LogService logService) {
        this.logService = logService;
    }

    @Override
    public void onEvent(OrderCreatedEvent event) {
        Order order = event.getOrder();
        logService.log("Order created: " + order.getId());
    }

    @Override
    public Class<OrderCreatedEvent> getEventType() {
        // OrderCreatedEvent만 처리한다고 명시적 선언
        // SimpleEventPublisher가 이 정보로 라우팅
        return OrderCreatedEvent.class;
    }
}


// 주문 객체
class Order {
    private String id;
    private String productId;
    private int quantity;
    private String customerEmail;
    private PaymentInfo paymentInfo;
    private String status;

    public Order(String id, String productId, int quantity,
                 String customerEmail, PaymentInfo paymentInfo) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.customerEmail = customerEmail;
        this.paymentInfo = paymentInfo;
        this.status = "PENDING";
    }

    // Getters & Setters
    public String getId() { return id; }
    public String getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public String getCustomerEmail() { return customerEmail; }
    public PaymentInfo getPaymentInfo() { return paymentInfo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

class PaymentInfo {
    private String cardNumber;
    private double amount;

    public PaymentInfo(String cardNumber, double amount) {
        this.cardNumber = cardNumber;
        this.amount = amount;
    }

    public String getCardNumber() { return cardNumber; }
    public double getAmount() { return amount; }
}


class InventoryService {
    public void reduceStock(String productId, int quantity) {
        System.out.println("Stock reduced: " + productId + " x " + quantity);
    }
}

class PaymentService {
    public void processPayment(PaymentInfo paymentInfo) {
        System.out.println("Payment processed: " + paymentInfo.getAmount());
    }
}

class EmailService {
    public void sendOrderConfirmation(String email) {
        System.out.println("Email sent to: " + email);
    }
}

class LogService {
    public void log(String message) {
        System.out.println("LOG: " + message);
    }
}