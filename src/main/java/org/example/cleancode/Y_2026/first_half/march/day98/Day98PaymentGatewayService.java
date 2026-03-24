package org.example.cleancode.Y_2026.first_half.march.day98;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.vavr.control.Try;
import lombok.Value;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * Day 98 — Resilience4j: CircuitBreaker로 외부 서비스 장애 대응
 *
 * 1. failureCount 수동 관리, 경쟁 조건 -> CircuitBreaker 객체가 상태 관리
 * 2. 모든 예외를 동일하게 처리 -> </>recordExceptions / ignoreExceptions 설정
 * 3. OPEN 상태 판단 로직 직접 구현  -> cb.getState() 로 CLOSED / OPEN / HALF_OPEN 확인
 * 4. fallback 로직 없음 -> CallNotPermittedException 에서 fallback 반환
 * 5. PaymentResult 생성자 노출 -> Lombok @Value 로 불변 객체화
 */
public class Day98PaymentGatewayService {

    private final ExternalPaymentApi paymentApi;
    private final CircuitBreaker circuitBreaker;

    public Day98PaymentGatewayService(ExternalPaymentApi paymentApi) {
        this.paymentApi = paymentApi;

        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)                        // 실패율 50% 초과 시 OPEN
                .waitDurationInOpenState(Duration.ofSeconds(10)) // 10초 후 HALF_OPEN 시도
                .slidingWindowSize(4)                            // 최근 4건 기준으로 실패율 계산
                .minimumNumberOfCalls(4)
                .recordExceptions(RuntimeException.class)        // 이 예외만 실패로 카운트
                .ignoreExceptions(IllegalArgumentException.class)// 이 예외는 무시
                .build();
        this.circuitBreaker = CircuitBreakerRegistry.of(config)
                .circuitBreaker("payment");

    }

    public PaymentResult processPayment(String userId, long amount) {

        Supplier<PaymentResult> decorated = CircuitBreaker.decorateSupplier(
                circuitBreaker,
                () -> Try.of(() -> paymentApi.charge(userId, amount))
                        .map(PaymentResult::success)
                        .getOrElseThrow(e -> new RuntimeException(e))
        );

        return Try.ofSupplier(decorated)
                .recover(CallNotPermittedException.class, e -> PaymentResult.failure("서킷 OPEN — 결제 서비스 일시 중단"))
                .recover(Exception.class, e -> PaymentResult.failure("결제 실패: " + e.getMessage()))
                .get();
    }

    public String getCircuitState() {
        return circuitBreaker.getState().name();
    }

    public static void main(String[] args) {
        ExternalPaymentApi fakeApi = (userId, amount) -> {
            throw new RuntimeException("외부 API 다운");
        };

        Day98PaymentGatewayService service = new Day98PaymentGatewayService(fakeApi);

        for (int i = 1; i <= 6; i++) {
            PaymentResult result = service.processPayment("user-1", 10000L);
            System.out.println(i + "번째 | 상태: " + service.getCircuitState() + " | " + result);
        }
    }
}

@Value
class PaymentResult {
    boolean success;
    String transactionId;
    String errorMessage;

    public static PaymentResult success(String txId) {
        return new PaymentResult(true, txId, null);
    }

    public static PaymentResult failure(String message) {
        return new PaymentResult(false, null, message);
    }
}

interface ExternalPaymentApi {
    String charge(String userId, long amount) throws Exception;
}
