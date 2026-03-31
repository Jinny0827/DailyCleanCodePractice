package org.example.cleancode.Y_2026.first_half.march.day102;


import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.decorators.Decorators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * Day 102 과제 — Resilience4j: Circuit Breaker
 *
 *
 *  1. 실패해도 계속 외부 호출 -> CircuitBreaker — 임계치 초과 시 OPEN으로 차단
 *  2. 실패율 추적 없음 -> slidingWindowSize + failureRateThreshold
 *  3. 복구 확인 로직 없음 -> waitDurationInOpenState + HALF_OPEN 자동 전환
 *  4. fallback 없음 -> CallNotPermittedException 처리로 대체 응답 반환
 *  5. 상태 변화 모니터링 없음 -> cb.getEventPublisher().onStateTransition() 로그
 */
public class Day102PaymentClient {

    private static final Logger log = LoggerFactory.getLogger(Day102PaymentClient.class);
    private CircuitBreaker circuitBreaker;
    private Retry retry;

    public Day102PaymentClient() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slidingWindowSize(6)
                .waitDurationInOpenState(Duration.ofSeconds(3))
                .build();

        this.circuitBreaker = CircuitBreakerRegistry.of(config)
                .circuitBreaker("payment");

        this.circuitBreaker.getEventPublisher()
                .onStateTransition(event ->
                        log.info("상태 변경: {} → {}",
                                event.getStateTransition().getFromState(),
                                event.getStateTransition().getToState()))
                .onCallNotPermitted(event -> log.warn("차단된 요청 발생"));

        this.retry =  Retry.of("payment", RetryConfig.custom()
                .maxAttempts(2)
                .waitDuration(Duration.ofMillis(100))
                .build());

    }

    // 외부 PG사 API 호출 (불안정한 서비스 가정)
    public String requestPayment(String orderId, int amount) {
        log.info("결제 요청: orderId={}, amount={}", orderId, amount);

        try {
            // 외부 API 호출 시뮬레이션 (30% 확률로 실패)
            Supplier<String> decorated = Decorators.ofSupplier(() -> callExternalPgApi(orderId, amount))
                    .withRetry(retry)
                    .withCircuitBreaker(circuitBreaker)
                    .decorate();
            String result = decorated.get();

            log.info("결제 성공: {}", result);
            return result;
        } catch (CallNotPermittedException e) {
            log.warn("Circuit OPEN — 결제 서비스 일시 중단");
            return "결제 서비스 점검 중입니다. 잠시 후 다시 시도해주세요.";
        } catch (Exception e) {
            log.error("결제 실패: {}", e.getMessage());
            // 문제 1: 실패해도 계속 외부 API를 호출함 (장애 전파)
            // 문제 2: 실패 횟수/비율 추적 없음
            // 문제 3: 서비스 복구 여부 확인 로직 없음
            throw new RuntimeException("결제 실패: " + e.getMessage());
        }
    }

    private String callExternalPgApi(String orderId, int amount) {
        // 50% 확률로 외부 API 실패 시뮬레이션
        if (Math.random() < 0.5) {
            throw new RuntimeException("PG사 연결 타임아웃");
        }
        return "PAY-" + orderId + "-" + amount;
    }

    public static void main(String[] args) {
        Day102PaymentClient client = new Day102PaymentClient();

        for (int i = 1; i <= 10; i++) {
            try {
                System.out.println(i + ". " + client.requestPayment("ORD-" + i, 10000 * i));
            } catch (Exception e) {
                System.out.println(i + ". 오류: " + e.getMessage());
            }
        }
    }
}
