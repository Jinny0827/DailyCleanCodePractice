package org.example.cleancode.day31;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Day 31: API 레이트 리미터
 * API 요청을 제한하는 레이트 리미터 시스템
 *
 * 문제점:
 * - 시간 윈도우 없음 (카운터만 증가)
 * - 사용자 구분 없음
 * - 제한 초과 정보 부족
 * - 자동 리셋 없음
 * - Thread-safe하지 않음
 */

public class Day31RateLimiter {

    public static void main(String[] args) throws InterruptedException {
        RateLimiter limiter = new RateLimiter();

        System.out.println("🧪 동시성 테스트 시작\n");

        // 10개 스레드가 동시에 같은 사용자로 요청
        for (int i = 0; i < 10; i++) {
            final int threadNum = i + 1;
            new Thread(() -> {
                RateLimitResult result = limiter.checkLimit("user-A");
                System.out.println("Thread-" + threadNum + ": "
                        + (result.isAllowed() ? "✅ 허용" : "❌ 거부")
                        + " (남은: " + result.getRemaining() + ")");
            }).start();
        }

        Thread.sleep(1000);  // 모든 스레드 완료 대기

        System.out.println("\n📊 최종 확인");
        RateLimitResult finalResult = limiter.checkLimit("user-A");
        System.out.println("최종 상태: " + (finalResult.isAllowed() ? "허용" : "거부"));
    }
}

class RateLimiter {

    // 동시성 버그 시나리오 (ConcurrentHashMap 사용 처리)
    private Map<String, UserRateLimit> userRequestCounts = new ConcurrentHashMap<>();

    private static final int MAX_REQUESTS = 3;
    private static final int WINDOW_SIZE_MS = 60000;

    // 시간 체크 로직으로 변경
    public RateLimitResult checkLimit(String userId) {

        UserRateLimit limit = userRequestCounts.computeIfAbsent(
                userId,
                k -> new UserRateLimit()
        );

        synchronized (limit) {
            long currentTime = System.currentTimeMillis();

            if(currentTime - limit.windowStart > WINDOW_SIZE_MS) {
                limit.requestCount = 0;
                limit.windowStart = currentTime;
            }

            limit.requestCount++;
            userRequestCounts.put(userId, limit);

            boolean allowed = limit.requestCount <= MAX_REQUESTS;
            int remaining = Math.max(0, MAX_REQUESTS - limit.requestCount);
            long resetTime = limit.windowStart + WINDOW_SIZE_MS;

            return new RateLimitResult(allowed, remaining, resetTime);
        }
    }

    // 리셋 메서드가 수동임
    public void reset() {
        userRequestCounts.clear();
    }
}

class UserRateLimit {
    public int requestCount;
    public long windowStart;

    public UserRateLimit() {
        this.requestCount = 0;
        this.windowStart = System.currentTimeMillis();
    }
}

// 결과 반환 클래스 생성
class RateLimitResult {
    // 허용 여부
    private final boolean allowed;

    // 남은 요청 수
    private final int remaining;
    // 리셋시간 (밀리초)
    private final long resetTime;

    public RateLimitResult(boolean allowed, int remaining, long resetTime) {
        this.allowed = allowed;
        this.remaining = remaining;
        this.resetTime = resetTime;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public int getRemaining() {
        return remaining;
    }

    public long getResetTime() {
        return resetTime;
    }

    public long getResetTimeInSeconds() {
        return (resetTime - System.currentTimeMillis()) / 1000;
    }
}