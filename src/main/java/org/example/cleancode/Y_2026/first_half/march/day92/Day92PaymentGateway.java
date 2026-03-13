package org.example.cleancode.Y_2026.first_half.march.day92;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.core.functions.CheckedSupplier;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.vavr.control.Try;

import java.time.Duration;

/**
 * Day 92 — Resilience4j: Circuit Breaker & Retry
 *
 * | 1 | 일시 장애에도 즉시 실패 | `Retry` — 최대 3회, 500ms 간격 재시도 |
 * | 2 | 계속 실패 중인 API에 반복 호출 | `CircuitBreaker` — OPEN 상태 전환으로 호출 차단 |
 * | 3 | 복구 감지 불가 | `HALF_OPEN` 상태로 자동 탐색 |
 * | 4 | 상태 변화 모니터링 없음 | `EventConsumer`로 상태 전환 로깅 |
 */
public class Day92PaymentGateway {

    private final ExternalPaymentApi api;
    private final Retry retry;
    private final CircuitBreaker circuitBreaker;


    public Day92PaymentGateway(ExternalPaymentApi api) {
        this.api = api;
        this.retry = Retry.of("payment-retry", RetryConfig.custom()
                    .maxAttempts(3)
                    .waitDuration(Duration.ofMillis(500))
                    .retryExceptions(RuntimeException.class)
                    .build());

        // 설정 분리
        CircuitBreakerConfig cbConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slidingWindowSize(4)
                .waitDurationInOpenState(Duration.ofSeconds(2))
                .permittedNumberOfCallsInHalfOpenState(2)
                .build();

        this.circuitBreaker = CircuitBreaker.of("payment-cb", cbConfig);

        // EventConsumer
        this.circuitBreaker.getEventPublisher()
                .onStateTransition(event -> System.out.printf("[CB 상태 변화] %s → %s%n",
                        event.getStateTransition().getFromState(),
                        event.getStateTransition().getToState()))
                .onCallNotPermitted(event ->
                        System.out.println("[CB OPEN] 호출 차단됨"))
                .onError(event ->  System.out.printf("[CB 실패] %s%n", event.getThrowable().getMessage()));

    }

    // Retry 재시도 적용
    //  CircuitBreaker를 적용하여 연속 실패 시 회로를 열어(OPEN) 호출 자체 차단
    // CLOSED → 실패율 임계치 초과 → OPEN(호출 차단) → 대기 후 → HALF_OPEN(탐색) → 성공 시 CLOSED 복귀
    public PaymentResult charge(String userId, int amount) {
        // 1단계 CircuitBreaker로 먼저 감싸기
        CheckedSupplier<PaymentResult> cbDecorated =
                CircuitBreaker.decorateCheckedSupplier(circuitBreaker, () -> api.requestPayment(userId, amount));

        // 2단계 Retry로 한번 더 감싸기
        CheckedSupplier<PaymentResult> retryDecorated =
                Retry.decorateCheckedSupplier(retry, cbDecorated);

        // 3단계 실행
        return Try.of(retryDecorated::get)
                .getOrElse(PaymentResult.failure("차단됨: " + circuitBreaker.getState()));

    }

    public static void main(String[] args) {
        ExternalPaymentApi api = new ExternalPaymentApi();
        Day92PaymentGateway gateway = new Day92PaymentGateway(api);

        // 외부 API가 불안정한 상황 시뮬레이션
        for (int i = 1; i <= 8; i++) {
            PaymentResult result = gateway.charge("user-" + i, 10000 * i);
            System.out.printf("시도 %d: %s%n", i, result.getMessage());
        }
    }
}

class ExternalPaymentApi {
    private int callCount = 0;

    public PaymentResult requestPayment(String userId, int amount) {
        callCount++;
        // 처음 5번은 실패, 이후 성공
        if (callCount <= 5) throw new RuntimeException("Payment API timeout");
        return PaymentResult.success("결제 완료: " + amount + "원");
    }
}

class PaymentResult {
    private final boolean success;
    private final String message;

    private PaymentResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static PaymentResult success(String msg) { return new PaymentResult(true, msg); }
    public static PaymentResult failure(String msg)  { return new PaymentResult(false, msg); }
    public String getMessage() { return (success ? "✅ " : "❌ ") + message; }
}