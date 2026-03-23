package org.example.cleancode.Y_2026.first_half.march.day97;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Day 97 — Bucket4j: API Rate Limiting
 *
 *
 * 1. requestCounts + windowStart 두 맵 관리 -> Bucket 객체 하나로 통합
 * 2. 윈도우 만료 직접 계산 -> Bandwidth.classic(5, Refill.greedy(...))
 * 3. 동시성 안전하지 않음 -> Bucket은 thread-safe 내장
 * 4. 유저별 버킷 직접 생성/관리 -> Map<String, Bucket> + computeIfAbsent
 */
public class Day97ApiRateLimiter {

    // 멀티스레드 환경에서 동시 접근이 안전한 Map == ConcurrentHashMap
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    //
    private Bucket resolveBucket(String userId) {
        return buckets.computeIfAbsent(userId, id -> Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(5)
                        .refillGreedy(5, Duration.ofSeconds(10))
                        .build())
                .build());
    }

    public ApiResponse<String> handleRequest(String userId, String endpoint) {
        if (resolveBucket(userId).tryConsume(1)) {
            return ApiResponse.success(endpoint + " processed for " + userId);
        }
        return ApiResponse.error(ErrorCode.TOO_MANY_REQUESTS);
    }



    public static void main(String[] args) throws InterruptedException {
        Day97ApiRateLimiter limiter = new Day97ApiRateLimiter();

        System.out.println("=== user-A: 7번 요청 ===");
        for (int i = 1; i <= 7; i++) {
            System.out.println(i + ". " + limiter.handleRequest("user-A", "/api/orders"));
        }

        System.out.println("\n=== user-B: 3번 요청 ===");
        for (int i = 1; i <= 3; i++) {
            System.out.println(i + ". " + limiter.handleRequest("user-B", "/api/orders"));
        }
    }

}

enum ErrorCode {
    TOO_MANY_REQUESTS("E429", "요청 한도를 초과했습니다."),
    SERVER_ERROR("E999", "서버 내부 오류가 발생했습니다.");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

class ApiResponse<T> {
    private final boolean success;
    private final T data;
    private final String code;
    private final String message;

    public ApiResponse(boolean success, T data, String code, String message) {
        this.success = success;
        this.data = data;
        this.code = code;
        this.message = message;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, "200", "OK");
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode){
        return new ApiResponse<>(false, null, errorCode.getCode(), errorCode.getMessage());
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "success=" + success +
                ", data=" + data +
                ", code='" + code + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}

