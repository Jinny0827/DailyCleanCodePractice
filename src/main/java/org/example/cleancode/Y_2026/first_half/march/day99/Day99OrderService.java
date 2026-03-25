package org.example.cleancode.Y_2026.first_half.march.day99;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Day 99 — Micrometer: 서비스 계측(Instrumentation)
 *
 * 1. int 필드로 카운터 관리 — 스레드 안전하지 않음 -> Counter, Gauge
 * 2. 처리 시간을 직접 계산 -> Timer.record()
 * 3. 성공/실패 구분 없이 단순 카운트 -> tag("result", "success/fail")
 * 4. 통계 조회를 위한 별도 메서드 -> MeterRegistry에서 일괄 조회
 * 5. 활성 주문 수 직접 관리 -> Gauge.builder(..., () -> activeOrders)
 *
 */
public class Day99OrderService {

    // 직접 관리하는 카운터들

    MeterRegistry registry = new SimpleMeterRegistry();

    Counter orderCounter = Counter.builder("orders.total")
            .description("전체 주문 수")
            .register(registry);

    Counter successCounter = Counter.builder("orders.success")
            .description("성공 주문 수")
            .register(registry);

    Counter failedCounter = Counter.builder("order.failed")
            .description("실패 주문 수")
            .register(registry);

    Timer timer = Timer.builder("order.duration")
            .description("주문 처리 시간")
            .register(registry);

    AtomicInteger activeOrders = new AtomicInteger(0);


    public Day99OrderService() {
        Gauge.builder("orders.active", activeOrders, AtomicInteger::get)
                .description("현재 처리 중인 주문 수")
                .register(registry);
    }

    public String processOrder(String userId, int amount) {
        orderCounter.increment();
        activeOrders.incrementAndGet();

        try {
            // recordCallable이 자동으로 시작/종료 시간 측정
            return timer.recordCallable(() -> {

                if (userId == null || userId.isBlank()) {
                    failedCounter.increment();
                    return "FAIL: invalid user";
                }
                if (amount <= 0 || amount > 1_000_000) {
                    failedCounter.increment();
                    return "FAIL: invalid amount";
                }

                Thread.sleep((long)(Math.random() * 200));
                successCounter.increment();
                return "OK: " + userId + " / " + amount;
            });

        } catch (Exception e) {
            failedCounter.increment();
            return "FAIL: " + e.getMessage();
        } finally {
            activeOrders.decrementAndGet();
        }
    }

    public static void main(String[] args) {
        Day99OrderService service = new Day99OrderService();
        service.processOrder("user1", 50000);
        service.processOrder(null, 50000);
        service.processOrder("user2", 2000000);
        service.printStats();
    }


    public void printStats() {
        System.out.println("전체: " + registry.counter("orders.total").count());
        System.out.println("성공: " + registry.counter("orders.success").count());
        System.out.println("실패: " + registry.counter("order.failed").count());
        System.out.println("평균처리시간: " +
                timer.mean(java.util.concurrent.TimeUnit.MILLISECONDS) + "ms");
        System.out.println("처리중: " + activeOrders.get());
    }
}
