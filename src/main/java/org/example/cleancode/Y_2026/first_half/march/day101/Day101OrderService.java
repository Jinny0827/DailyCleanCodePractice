package org.example.cleancode.Y_2026.first_half.march.day101;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * Day 101 — SLF4J + Logback MDC: 요청 컨텍스트 로깅
 *
 * 1. System.out.println 남발 -> Logger + 로그 레벨 분리
 * 2. requestId, userId를 매 메서드 파라미터로 전달 -> MDC.put()으로 스레드 로컬에 저장
 * 3. 로그 포맷이 코드에 하드코딩 -> logback.xml 패턴으로 통일
 * 4. MDC 정리(clear) 없음 -> try-finally로 MDC.clear() 보장
 */
public class Day101OrderService {

    private static final Logger log = LoggerFactory.getLogger(Day101OrderService.class);

    public void processOrder(String userId, String itemId, int qty) {
        MDC.put("requestId", UUID.randomUUID().toString());
        MDC.put("userId", userId);

        log.info("주문 시작 - item: {}", itemId);

        try {
            validateUser(userId);
            reserveStock(itemId, qty);
            chargePayment(qty * 1000);

            log.info("주문 완료");

        } catch (Exception e) {
            log.error("주문 실패: {}", e.getMessage());
        } finally {
            // 스레드 상태 초기화용 (I/O 정리랑 다른 개념 / 다음 사용자를 위해 흔적을 지우는거라고 생각)
            MDC.clear();
        }
    }

    private void validateUser(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("유저 없음");
        }
    }

    private void reserveStock(String itemId, int qty) {
        log.info("재고 확인: {} x{}", itemId, qty);
        if (qty > 100) throw new IllegalStateException("재고 부족");
    }

    private void chargePayment(int amount) {
        log.info("결제 처리 : {} 원", amount);
    }

    public static void main(String[] args) {
        Day101OrderService service = new Day101OrderService();
        service.processOrder("user-001", "item-A", 3);
        service.processOrder("", "item-B", 5);
    }
}
