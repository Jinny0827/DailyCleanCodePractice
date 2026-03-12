package org.example.cleancode.Y_2026.first_half.march.day90;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Day 90 과제 — Guava EventBus 활용
 *
 * 문제점:
 * 1. 수동 리스너 관리 — addListener() 직접 구현, 타입 구분 없음
 * 2. 이벤트 타입 분기 — instanceof로 이벤트 종류 판별
 * 3. 리스너 인터페이스 강제 — 모든 구독자가 동일 인터페이스 구현
 * 4. 직접 호출 — 리스너를 일일이 순회하며 notify()
 */
public class Day90OrderEventSystem {

    public static void main(String[] args) {
        EventBus eventBus = new EventBus();

        eventBus.register(new EmailListener());
        eventBus.register(new LogListener());
        eventBus.register(new PointListener());

        // 주문 완료
        eventBus.post(new OrderCompletedEvent("ORD-001", "user@test.com", 50000));

        // 주문 취소
        eventBus.post(new OrderCancelledEvent("ORD-002", "user@test.com"));
    }
}

// 이벤트 클래스
@Data
@AllArgsConstructor
class OrderCompletedEvent {
    private String orderId;
    private String email;
    private int amount;
}

@Data
@AllArgsConstructor
class OrderCancelledEvent {
    private String orderId;
    private String email;
}

class EmailListener {
    @Subscribe
    public void onOrderCompleted(OrderCompletedEvent e) {
        System.out.println("📧 이메일 발송: " + e.getEmail() + " → 주문 " + e.getOrderId() + " 완료");
    }

    @Subscribe
    public void onOrderCancelled(OrderCancelledEvent e) {
        System.out.println("📧 이메일 발송: " + e.getEmail() + " → 주문 " + e.getOrderId() + " 취소");
    }
}

class LogListener  {
    @Subscribe
    public void onOrderCompleted(OrderCompletedEvent e) {
        System.out.println("📝 로그: 주문완료 " + e.getOrderId() + " / " + e.getAmount() + "원");
    }

    @Subscribe
    public void onOrderCancelled(OrderCancelledEvent e) {
        System.out.println("📝 로그: 주문취소 " + e.getOrderId());
    }
}

class PointListener  {
    @Subscribe
    public void onOrderCompleted(OrderCompletedEvent e) {
        int points = e.getAmount() / 100;
        System.out.println("⭐ 포인트 적립: " + points + "점");
    }
}