package org.example.cleancode.day38;

import java.util.HashMap;
import java.util.Map;

/**
 * Day 38: API 응답 캐싱 시스템
 *
 * 문제점:
 * - 캐시 키 생성 로직이 반복됨
 * - HTTP 메서드별 캐싱 정책 없음
 * - 헤더 기반 캐시 제어 미지원
 * - 조건부 요청(ETag, Last-Modified) 없음
 * - 캐시 무효화 패턴 부재
 */
public class Day38ApiCache {
    public static void main(String[] args) {
        ApiClient client = new ApiClient();

        // GET 요청 (캐시 가능)
        client.request("GET", "/users/123");
        client.request("GET", "/users/123");  // 캐시 히트 예상

        // POST 요청 (캐시 불가능)
        client.request("POST", "/users", "{\"name\":\"John\"}");

        // 캐시 무효화 후 재조회
        client.invalidateUrl("/users/123");
        client.request("GET", "/users/123");
    }
}

// 캐시 키 생성 전략
class HttpRequest {
    private String method;
    private String url;
    private Map<String, String> queryParams;
    private Map<String, String> headers;
    private String body;

    public HttpRequest(String method, String url, Map<String, String> queryParams, Map<String, String> headers, String body) {
        this.method = method;
        this.url = url;
        this.queryParams = queryParams;
        this.headers = headers;
        this.body = body;
    }

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "HttpRequest{" +
                "method='" + method + '\'' +
                ", url='" + url + '\'' +
                ", queryParams=" + queryParams +
                ", headers=" + headers +
                ", body='" + body + '\'' +
                '}';
    }
}

class ApiClient {
    private Map<String, String> cache = new HashMap<>();

    public String request(String method, String url) {
        return request(method, url, null);
    }

    public String request(String method, String url, String body) {
        // 문제 1: 캐시 키 생성이 중복됨
        String cacheKey = method + ":" + url;
        if (body != null) {
            cacheKey += ":" + body;
        }

        // 문제 2: GET만 캐싱 (메서드별 정책 없음)
        if (method.equals("GET") && cache.containsKey(cacheKey)) {
            System.out.println("💾 캐시 히트: " + url);
            return cache.get(cacheKey);
        }

        // 실제 API 호출
        System.out.println("🌐 API 호출: " + method + " " + url);
        String response = callApi(method, url, body);

        // 문제 3: 모든 GET을 무조건 캐싱
        if (method.equals("GET")) {
            cache.put(cacheKey, response);
        }

        return response;
    }

    private String callApi(String method, String url, String body) {
        // API 호출 시뮬레이션
        return "{\"id\":123,\"name\":\"John\"}";
    }

    // 문제 4: URL만으로 무효화 (쿼리 파라미터 고려 안함)
    public void invalidateUrl(String url) {
        cache.remove("GET:" + url);
        System.out.println("🗑️ 캐시 무효화: " + url);
    }
}