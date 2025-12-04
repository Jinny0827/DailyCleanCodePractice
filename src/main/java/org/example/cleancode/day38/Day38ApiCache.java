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

        //-------------------------------------------------------------------------------------------------------


        System.out.println("\n=== 캐시 키 생성 테스트 ===");
        CacheKeyGenerator keyGen = new CacheKeyGenerator();
        
        // 쿼리 파라미터 순서가 달라도 같은 키 생성
        Map<String, String> params1 = new HashMap<>();
        params1.put("id", "123");
        params1.put("sort", "name");

        Map<String, String> params2 = new HashMap<>();
        params2.put("sort", "name");
        params2.put("id", "123");

        HttpRequest req1 = new HttpRequest("GET", "/users", params1, null, null);
        HttpRequest req2 = new HttpRequest("GET", "/users", params2, null, null);

        String key1 = keyGen.generate(req1);
        String key2 = keyGen.generate(req2);

        System.out.println("Key 1: " + key1);
        System.out.println("Key 2: " + key2);
        System.out.println("동일한가? " + key1.equals(key2)); // true 여야 함

        //-------------------------------------------------------------------------------------------------------

        System.out.println("\n=== Step 2: 캐시 정책 테스트 ===");
        CachePolicy policy = new DefaultCachePolicy();

        // 1.GET 요청
        HttpRequest getReq = new HttpRequest("GET", "/users", null, null, null);
        System.out.println("GET 캐싱 가능? " + policy.shouldCache(getReq)); // true
        System.out.println("TTL: " + policy.getTtl(getReq) + "초");

        // 2. POST 요청
        HttpRequest postReq = new HttpRequest("POST", "/users", null, null, "{}");
        System.out.println("POST 캐싱 가능? " + policy.shouldCache(postReq)); // false

        // 3. Cache-Contro: no-cache
        Map<String, String> noCacheHeaders = new HashMap<>();
        noCacheHeaders.put("Cache-Control", "no-cache");
        HttpRequest noCacheReq = new HttpRequest("GET", "/users", null, noCacheHeaders, null);
        System.out.println("no-cache 캐싱 가능? " + policy.shouldCache(noCacheReq)); // false


        // 4. Cache-Control: max-age=600
        Map<String, String> maxAgeHeaders = new HashMap<>();
        maxAgeHeaders.put("Cache-Control", "max-age=600");
        HttpRequest maxAgeReq = new HttpRequest("GET", "/users", null, maxAgeHeaders, null);
        System.out.println("max-age TTL: " + policy.getTtl(maxAgeReq) + "초"); // 600

        //-------------------------------------------------------------------------------------------------------

        System.out.println("\n=== Step 3: CachedResponse 테스트 ===");
        
        // 1. 캐시생성(ttl = 2초)
        CachedResponse cached = new CachedResponse(
                "{\"id\":123}",
                "etag-abc123",
                System.currentTimeMillis(),
                2 
        );

        System.out.println("생성 직후 만료? " + cached.isExpired()); // false
        System.out.println("재검증 필요? " + cached.needsRevalidation()); // false

        // 2초 ttl을 3초 슬립으로 만료시키기 (ttl 초과)
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("3초 후 만료? " + cached.isExpired()); // true
        System.out.println("재검증 필요? " + cached.needsRevalidation()); // true (ETag 있음)

        // 3. HTTP 응답 테스트
        Map<String, String> responseHeaders = new HashMap<>();
        responseHeaders.put("ETag", "etag-xyz789");
        responseHeaders.put("Last-Modified", String.valueOf(System.currentTimeMillis()));

        HttpResponse response = new HttpResponse(200, "{\"data\":\"ok\"}", responseHeaders);
        System.out.println("응답 상태: " + response.getStatusCode());
        System.out.println("ETag: " + response.getHeader("ETag"));

        // 304 Not Modified 처리
        HttpResponse notModified = new HttpResponse(304, "", null);
        System.out.println("304 응답? " + notModified.isNotModified());
    }
}

// 캐시 정책 인터페이스
interface CachePolicy {
    boolean shouldCache(HttpRequest request);

    // 초단위
    long getTtl(HttpRequest request);
}


class DefaultCachePolicy implements CachePolicy {
    
    // 5분 설정
    private static final long DEFAULT_TTL = 300;

    @Override
    public boolean shouldCache(HttpRequest request) {
        String method = request.getMethod();

        // GET, HEAD만 캐싱
        if(!method.equals("GET") && !method.equals("HEAD")) {
            return false;
        }
        
        // Cache-Control: no-cache 체크
        Map<String, String> headers = request.getHeaders();
        if(headers != null) {
            String cacheControl = headers.get("Cache-Control");
            if(cacheControl != null && cacheControl.contains("no-cache")) {
                return false;
            }
        }

        return true;
    }

    @Override
    public long getTtl(HttpRequest request) {
        // Cache max-age 파싱
        Map<String, String> headers = request.getHeaders();
        if(headers != null) {
            String cacheControl = headers.get("Cache-Control");
            if(cacheControl != null && cacheControl.contains("max-age=")) {
                //max-age=600 에서 숫자 추출
                String[] parts = cacheControl.split("max-age=");
                if(parts.length > 1) {
                    try {
                        String maxAge = parts[1].split(",")[0].trim();
                        return Long.parseLong(maxAge);
                    }
                    catch (NumberFormatException e) {
                        // 파싱 실패시 기본 값
                    }
                }
            }
        }
        
        
        
        return DEFAULT_TTL;
    }
}

// 캐시 정보 질의 응답 클래스
class CachedResponse {
    private final String body;
    private final String etag;
    private final long lastModified;
    private final long  cachedAt;
    private final long ttl;

    public CachedResponse(String body, String etag, long lastModified, long ttl) {
        this.body = body;
        this.etag = etag;
        this.lastModified = lastModified;
        this.cachedAt = System.currentTimeMillis();
        this.ttl = ttl;
    }

    // 캐시 만료 여부
    public boolean isExpired() {
        long now = System.currentTimeMillis();
        // 현재시간 - 캐시된시간 / 1000
        long elapsedSeconds = (now - cachedAt / 1000);
        return elapsedSeconds > ttl;
    }
    
    // 재검증이 필요한지 (만료되었지만 eTag 존재)
    public boolean needsRevalidation() {
        return isExpired() && (etag != null || lastModified > 0);
    }

    public String getBody() {
        return body;
    }

    public String getEtag() {
        return etag;
    }

    public long getLastModified() {
        return lastModified;
    }

    @Override
    public String toString() {
        return String.format("CachedResponse{etag='%s', expired=%b}",
                etag, isExpired());
    }
}



// 요청에 대한 객체 생성
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

class HttpResponse {
    private final int statusCode;
    private final String body;
    private final Map<String, String> headers;

    public HttpResponse(int statusCode, String body, Map<String, String> headers) {
        this.statusCode = statusCode;
        this.body = body;
        this.headers = headers != null ? headers : new HashMap<>();
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public boolean isNotModified() {
        return statusCode == 304;
    }

}

class CacheKeyGenerator {
    public String generate(HttpRequest request) {
        StringBuilder keyBuilder = new StringBuilder();

        // HTTP 메서드 추가
        keyBuilder.append(request.getMethod()).append(".");
        
        // URL 추가
        keyBuilder.append(request.getUrl());

        // 쿼리 파라미터 추가
        Map<String, String> params = request.getQueryParams();
        if(params != null && !params.isEmpty()) {

            // url?key=value&key=value 형태로 저장
            keyBuilder.append("?");

            params.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> {
                        keyBuilder.append(entry.getKey())
                                .append("=")
                                .append(entry.getValue())
                                .append("&");
                    });
            
            // 마지막 key,value는 & 제거 (마지막 글자 삭제 deleteCharAt)
            keyBuilder.deleteCharAt(keyBuilder.length() - 1);
        }

        // body가 있으면 추가 (POST, PUT 등)
        if(request.getBody() != null && !request.getBody().isEmpty()) {
            keyBuilder.append(":").append(request.getBody());
        }

        return keyBuilder.toString();
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