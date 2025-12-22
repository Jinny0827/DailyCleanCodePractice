package org.example.cleancode.day42;


import java.util.concurrent.Callable;

/**
 * Day 42: 외부 API 호출 시스템
 *
 * 문제점:
 * - 외부 API 장애 시 무한 재시도
 * - 장애 전파로 전체 시스템 다운 위험
 * - 복구 지연 감지 불가
 * - 실패율 추적 없음
 * - Half-Open 상태 미지원
 */
public class Day42ExternalApiClient {

    public static void main(String[] args) {
        PaymentApiClient client = new PaymentApiClient();

        System.out.println("=== 서킷 브레이커 테스트 시작 ===\n");

        // 10번 호출 시도
        for (int i = 1; i <= 10; i++) {
            System.out.println("\n[요청 #" + i + "]");

            try {
                String result = client.processPayment("TXN-" + i, 1000);
                System.out.println("✅ 성공: " + result);
            } catch (CircuitBreakerOpenException e) {
                System.out.println("⚡ Fast Fail: " + e.getMessage());
            } catch (Exception e){
                System.out.println("❌ API 실패: " + e.getMessage());
            }
        }

        // 호출 간 대기
        try {
            Thread.sleep(1000);  // 1초 대기
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("=== 서킷 브레이커 테스트 종료 ===\n");
    }

}

// 서킷 브레이커 상태
enum CircuitState {
    CLOSED,
    OPEN,
    HALF_OPEN
}

class CircuitBreakerConfig {
    private final int failureThreshold;
    private final long timeoutMillis;
    private final int halfOpenMaxCalls;

    public CircuitBreakerConfig(int failureThreshold, long timeoutMillis, int halfOpenMaxCalls) {
        this.failureThreshold = failureThreshold;
        this.timeoutMillis = timeoutMillis;
        this.halfOpenMaxCalls = halfOpenMaxCalls;
    }

    public int getFailureThreshold() {
        return failureThreshold;
    }

    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    public int getHalfOpenMaxCalls() {
        return halfOpenMaxCalls;
    }
}

// 서킷 브레이커 로직
class CircuitBreaker {
    private final CircuitBreakerConfig config;
    private CircuitState state;
    private int failureCount;
    private int successCount;
    private long lastFailureTime;

    public CircuitBreaker(CircuitBreakerConfig config) {
        this.config = config;
        this.state = CircuitState.CLOSED;
        this.failureCount = 0;
        this.successCount = 0;
        this.lastFailureTime = 0;
    }

    public <T> T call(Callable<T> operation) throws Exception {
        
        // 1단계 현재 상태에서 호출 가능 여부
        if(state == CircuitState.OPEN) {
            // OPEN 상태 -> 타임아웃 지났는지 확인
            if(shouldAttemptReset()) {
                transitionToHalfOpen();
            } else {
                // 아직 타임아웃 안지났음 -> 즉시 예외처리
                throw new CircuitBreakerOpenException(
                        "서킷 브레이커 OPEN 상태 (복구 대기 중)"
                );
            }
        }

        // 2단계 API 호출 시도
        try {
            T result = operation.call();
            onSuccess();
            return result;
        } catch (Exception e) {
            // 실패 처리
            onFailure();
            throw e;
        }
    }

    // 성공 시 처리
    private void onSuccess() {
        if (state == CircuitState.HALF_OPEN) {
            successCount++;
            System.out.println("✓ HALF_OPEN 성공 (" + successCount + "회)");

            if(successCount >= config.getHalfOpenMaxCalls()) {
                transitionToClosed();
            }
        } else {
            failureCount = 0;
        }
    }

    // 실패 시 처리
    private void onFailure() {
        failureCount++;
        lastFailureTime = System.currentTimeMillis();

        System.out.println("❌ 실패 (" + failureCount + "/" +
                config.getFailureThreshold() + ")");

        if(state == CircuitState.HALF_OPEN) {
            //HALF_OPEN에서 실패 시 즉시 OPEN 처리
            transitionToOpen();
        } else if(failureCount >= config.getFailureThreshold()) {
            // 임계값 초과 시 OPEN 처리
            transitionToOpen();
        }
    }

    // 타임아웃 지났는지 확인
    private boolean shouldAttemptReset() {
        long elapsed = System.currentTimeMillis() - lastFailureTime;
        return elapsed >= config.getTimeoutMillis();
    }

    // 상태 전이 메서드
    private void transitionToOpen() {
        System.out.println("🔴 CLOSED/HALF_OPEN → OPEN");
        state = CircuitState.OPEN;
        successCount = 0;
    }

    private void transitionToHalfOpen() {
        System.out.println("🟡 OPEN → HALF_OPEN (복구 테스트)");
        state = CircuitState.HALF_OPEN;
        successCount = 0;
    }

    private void transitionToClosed() {
        System.out.println("🟢 HALF_OPEN → CLOSED (복구 완료)");
        state = CircuitState.CLOSED;
        failureCount = 0;
        successCount = 0;
    }

    public CircuitState getState() {
        return state;
    }
}

class CircuitBreakerOpenException extends Exception {
    public CircuitBreakerOpenException(String message) {
        super(message);
    }
}


class PaymentApiClient {
    private final CircuitBreaker circuitBreaker;

    public PaymentApiClient() {
       CircuitBreakerConfig config = new CircuitBreakerConfig(
               3,
               3000,
               2
       );
       this.circuitBreaker = new CircuitBreaker(config);
    }

    public String processPayment(String txnId, int amount) throws Exception {
       return circuitBreaker.call(() -> {
           System.out.println("  💳 실제 API 호출: " + txnId);
           
           //외부 APi 시뮬레이션 (70% 실패율)
           if(Math.random() < 0.7) {
               throw new Exception("Connection timeout");
           }

           return "Payment processed: " + amount;
       });
    }
}