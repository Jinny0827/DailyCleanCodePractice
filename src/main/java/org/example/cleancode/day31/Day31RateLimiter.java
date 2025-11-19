package org.example.cleancode.day31;

import java.util.HashMap;
import java.util.Map;

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


        for (int i = 1; i <= 4; i++) {
            RateLimitResult result = limiter.checkLimit("user-A");

            if(result.isAllowed()) {
                System.out.println("✅ 요청 허용 - 남은 횟수: " + result.getRemaining());
            } else {
                System.out.println("❌ 요청 거부 - "
                        + result.getResetTimeInSeconds() + "초 후 재시도");
            }
        }

        System.out.println("\n⏳ 60초 대기...\n");
        Thread.sleep(60000);

        RateLimitResult result = limiter.checkLimit("user-A");
        if(result.isAllowed()) {
            System.out.println("✅ 리셋 후 요청 허용 - 남은 횟수: " + result.getRemaining());
        }
    }

}

class RateLimiter {
    private Map<String, UserRateLimit> userRequestCounts = new HashMap<>();

    private static final int MAX_REQUESTS = 3;
    private static final int WINDOW_SIZE_MS = 60000;

    // 시간 체크 로직으로 변경
    public RateLimitResult checkLimit(String userId) {
        UserRateLimit limit = userRequestCounts.getOrDefault(userId, new UserRateLimit());

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