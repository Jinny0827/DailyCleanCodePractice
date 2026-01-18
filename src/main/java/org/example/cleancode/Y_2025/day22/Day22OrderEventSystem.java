package org.example.cleancode.Y_2025.day22;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Day 22: 이벤트 기반 시스템 - 옵저버 패턴
 *
 * 문제점:
 * - 주문 처리 로직이 모든 알림 로직과 강하게 결합
 * - 새로운 알림 채널 추가 시 OrderService 수정 필요
 * - 알림 실패가 주문 처리를 중단시킬 수 있음
 * - 이벤트 처리 순서/조건 제어 불가
 */
public class Day22OrderEventSystem {

    public static void main(String[] args) {
        EventPublisher publisher = new EventPublisher();

        publisher.subscribe(new EmailNotificationListener());
        publisher.subscribe(new SmsNotificationListener());
        publisher.subscribe(new InventoryListener());
        publisher.subscribe(new LoggingListener());

        OrderService service = new OrderService(publisher);
        service.processOrder("ORDER-001", "user@test.com", 50000);
        service.processOrder("ORDER-002", "vip@test.com", 150000);
    }

}

interface OrderEventListener {
    void onOrderCreated(OrderEvent event);
}

class OrderEvent {
    private String orderId;
    private String email;
    private int amount;
    private LocalDateTime timestamp;

    public OrderEvent(String orderId, String email, int amount, LocalDateTime timestamp) {
        this.orderId = orderId;
        this.email = email;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getEmail() {
        return email;
    }

    public int getAmount() {
        return amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}

class EventPublisher {
    private List<OrderEventListener> listeners = new ArrayList<>();

    public void subscribe(OrderEventListener listener) {
        listeners.add(listener);
    }

    public void publish(OrderEvent event) {
        for (OrderEventListener listener : listeners){
            try {
                listener.onOrderCreated(event);
            } catch (Exception e) {
                System.out.println("리스너 처리 실패 " + e.getMessage());
            }
        }
    }
}

class EmailNotificationListener implements OrderEventListener {
    private EmailService emailService = new EmailService();

    @Override
    public void onOrderCreated(OrderEvent event) {
        emailService.sendOrderConfirmation(
                event.getEmail(),
                event.getOrderId(),
                event.getAmount()
        );

        if(event.getEmail().contains("vip")) {
            emailService.sendVipGift(event.getEmail());
        }
    }
}

class SmsNotificationListener implements OrderEventListener {
    private static final int HIGH_VALUE_THRESHOLD = 100000;
    private SmsService smsService = new SmsService();

    @Override
    public void onOrderCreated(OrderEvent event) {
        if (event.getAmount() >= HIGH_VALUE_THRESHOLD) {
            smsService.sendHighValueAlert(
                    event.getEmail(),
                    event.getOrderId(),
                    event.getAmount()
            );
        }
    }
}

class InventoryListener implements OrderEventListener {
    private InventoryService inventoryService = new InventoryService();

    @Override
    public void onOrderCreated(OrderEvent event) {
        inventoryService.decreaseStock(event.getOrderId());
    }
}

class LoggingListener implements OrderEventListener {
    private LoggingService loggingService = new LoggingService();

    @Override
    public void onOrderCreated(OrderEvent event) {
        loggingService.logOrder(event.getOrderId(), event.getAmount());
    }
}




class OrderService {
    private EventPublisher eventPublisher;

    public OrderService(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void processOrder(String orderId, String email, int amount) {
        System.out.println("=== 주문 처리: " + orderId + " ===");

        saveOrder(orderId, email, amount);

        OrderEvent event = new OrderEvent(orderId, email, amount, LocalDateTime.now());
        eventPublisher.publish(event);
    }

    private void saveOrder(String orderId, String email, int amount) {
        System.out.println("💾 주문 저장: " + orderId);
    }
}

class EmailService {
    public void sendOrderConfirmation(String email, String orderId, int amount) {
        System.out.println("📧 주문 확인 이메일 발송: " + email);
    }

    public void sendVipGift(String email) {
        System.out.println("🎁 VIP 선물 안내 발송: " + email);
    }
}

class SmsService {
    public void sendHighValueAlert(String email, String orderId, int amount) {
        System.out.println("📱 고액 주문 SMS 발송");
    }
}

class InventoryService {
    public void decreaseStock(String orderId) {
        System.out.println("📦 재고 감소 처리");
    }
}

class LoggingService {
    public void logOrder(String orderId, int amount) {
        System.out.println("📝 주문 로그 저장");
    }
}