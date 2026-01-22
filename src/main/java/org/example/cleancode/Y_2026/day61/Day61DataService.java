package org.example.cleancode.Y_2026.day61;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Day 61: 캐시 전략 리팩터링
 *
 *
 *  전략 패턴 - 캐시 알고리즘을 독립적인 전략으로 분리
 * 조건문 제거 - 타입 코드 대신 다형성 활용
 * 단일 책임 - 캐시 로직과 데이터 로직 분리
 * 불변성 - 설정값을 변경 불가능하게
 */
public class Day61DataService {
    private final CacheStrategy cacheStrategy;

    public Day61DataService(CacheStrategy cacheStrategy) {
        this.cacheStrategy = cacheStrategy;
    }

    public Object getData(String key) {
        // 캐시에서 먼저 조회
        Object cachedData = cacheStrategy.get(key);
        if(cachedData != null) {
            return cachedData;
        }
        
        // DB에서 조회(캐시가 없는 경우)
        Object data = fetchFromDatabase(key);

        // 캐시가 가득찰 경우 캐시 삭제
        if(cacheStrategy.isFull()) {
            cacheStrategy.evict();
        }
        
        // 캐시에 저장
        cacheStrategy.put(key, data);

        return data;
    }

    private Object fetchFromDatabase(String key) {
        return "Data for " + key;
    }


    public static void main(String[] args) {
        System.out.println("=== LRU Cache Test ===");
        Day61DataService lruService = new Day61DataService(new LRUCache(3));

        System.out.println(lruService.getData("A")); // DB 조회
        System.out.println(lruService.getData("B")); // DB 조회
        System.out.println(lruService.getData("C")); // DB 조회
        System.out.println(lruService.getData("A")); // 캐시 hit (A 최신으로)
        System.out.println(lruService.getData("D")); // B 제거됨 (가장 오래 사용 안됨)


        System.out.println("\n=== FIFO Cache Test ===");
        Day61DataService fifoService = new Day61DataService(new FIFOCache(3));

        System.out.println(fifoService.getData("X")); // DB 조회
        System.out.println(fifoService.getData("Y")); // DB 조회
        System.out.println(fifoService.getData("Z")); // DB 조회
        System.out.println(fifoService.getData("X")); // 캐시 hit (순서 변경 없음)
        System.out.println(fifoService.getData("W")); // X 제거됨 (가장 먼저 들어옴)
    }
}

// 캐시 전략 인터페이스
interface CacheStrategy {

    // 캐시에서 값 조회(있으면 반환, 없으면 Null)
    Object get(String key);

    // 캐시에 값 저장
    void put(String key, Object value);

    // 캐시가 가득찼는지 확인
    boolean isFull();
    
    // 제거할 항목 삭제(eviction)
    void evict();
}

// FIFO 캐시 구현체
class FIFOCache implements CacheStrategy {
    private final Map<String, Object> cache;
    private final int maxSize;

    public FIFOCache(int maxSize) {
        this.maxSize = maxSize;
        
        // LinkedHashMap은 삽입 순서 유지
        this.cache = new LinkedHashMap<>();
    }

    @Override
    public Object get(String key) {
        return cache.get(key);
    }

    @Override
    public void put(String key, Object value) {
        cache.put(key, value);
    }

    @Override
    public boolean isFull() {
        return cache.size() >= maxSize;
    }

    @Override
    public void evict() {
        String firstKey = cache.keySet().iterator().next();
        // 캐시의 가장 오래된 항목 제거
        cache.remove(firstKey);
    }
}

// LRU 캐시 구현체
class LRUCache implements CacheStrategy {
    private final Map<String, Object> cache;
    private final int maxSize;

    public LRUCache(int maxSize) {
        this.maxSize = maxSize;
        // accessOrder를 true 설정 시 get 호출시 해당 항목이 맨 뒤로 이동
        this.cache = new LinkedHashMap<>(maxSize, 0.75f, true);
    }

    @Override
    public Object get(String key) {
        return cache.get(key);
    }

    @Override
    public void put(String key, Object value) {
        cache.put(key, value);
    }

    @Override
    public boolean isFull() {
        return cache.size() >= maxSize;
    }

    @Override
    public void evict() {
        String firstKey = cache.keySet().iterator().next();
        cache.remove(firstKey);
    }
}