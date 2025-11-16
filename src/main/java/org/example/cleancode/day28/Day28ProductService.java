package org.example.cleancode.day28;


import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Day 28: 캐싱 시스템
 *
 * 문제점:
 * - 캐시 만료 시간 없음 (무한 증가)
 * - 메모리 제한 없음
 * - 캐시 무효화 전략 부재
 * - 통계 정보 부재
 */
public class Day28ProductService {
    private static final int MAX_CACHE_SIZE = 3; // 테스트용 작은 크기
    private static final long DEFAULT_TTL = 30000; // 30초로 늘림

    private CacheStatistics statistics = new CacheStatistics();

    private Map<String, CacheEntry<Product>> cache = new LinkedHashMap<>(
            16, // 초기 용량
            0.75f, // 로드 펙터
            true // accessOrder = true (LRU 핵심)
    ) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, CacheEntry<Product>> eldest) {
            boolean shouldRemove = size() > MAX_CACHE_SIZE;

            if(shouldRemove) {
                System.out.println("🗑️ LRU 제거: " + eldest.getKey());
                statistics.recordEviction();
            }

            return shouldRemove;
        }
    };


    private ProductRepository repository = new ProductRepository();


    public static void main(String[] args) throws InterruptedException {
        Day28ProductService service = new Day28ProductService();

        // 1. 초기 조회 (3번 미스)
        service.getProduct("P001");
        service.getProduct("P002");
        service.getProduct("P003");

        // 2. 재조회 (3번 히트)
        service.getProduct("P001");
        service.getProduct("P002");
        service.getProduct("P003");

        // 3. LRU 테스트
        service.getProduct("P004"); // P001 제거
        service.getProduct("P001"); // 미스 (제거됨)

        // 4. 캐시 무효화
        System.out.println();
        service.updateProduct("P002", 50000);
        service.getProduct("P002"); // 미스 (무효화됨)

        // 5. TTL 테스트 (선택)
        System.out.println("\n⏳ 31초 대기 중...\n");
        Thread.sleep(31000);
        service.getProduct("P003"); // 만료

        // 📊 최종 통계
        service.printStatistics();
    }

    public Product getProduct(String productId) {
        CacheEntry<Product> entry = cache.get(productId);

        if(entry != null && !entry.isExpired()) {
            System.out.println("캐시 히트 : " + productId);
            statistics.recordHit();
            return entry.getValue();
        }

        if (entry != null) {
            System.out.println("⏰ 캐시 만료: " + productId);
            statistics.recordExpiration();
        } else {
            System.out.println("🔍 캐시 미스: " + productId);
        }

        statistics.recordMiss();

        Product product = repository.findById(productId);
        CacheEntry<Product> newEntry = new CacheEntry<>(
                product,
                System.currentTimeMillis(),
                DEFAULT_TTL
        );

        cache.put(productId, newEntry);

        return product;
    }

    public void printStatistics() {
        statistics.setCurrentSize(cache.size());
        statistics.printReport();
    }

    public void updateProduct(String productId, int newPrice) {
        Product product = repository.findById(productId);
        product.setPrice(newPrice);
        repository.update(product);

        invalidate(productId);


        System.out.println("✓ 상품 업데이트: " + productId);
        // 캐시 무효화 없음!
    }

    // 특정 항목 캐시 무효화
    public void invalidate(String productId) {
        if(cache.remove(productId) != null) {
            System.out.println("🧹 캐시 무효화: " + productId);
        }
    }

    // 전체 캐시 초기화
    public void invalidateAll() {
        int size = cache.size();
        cache.clear();
        System.out.println("🧹 전체 캐시 초기화: " + size + "개 항목 제거");
    }

}

class CacheEntry<T> {
    private final T value;
    private final long createdAt;
    private final long ttlMillis;

    public CacheEntry(T value, long createdAt, long ttlMillis) {
        this.value = value;
        this.createdAt = createdAt;
        this.ttlMillis = ttlMillis;
    }

    public T getValue() {
        return value;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getTtlMillis() {
        return ttlMillis;
    }

    public boolean isExpired() {
        long currentTime = System.currentTimeMillis();
        return (currentTime - createdAt) > ttlMillis;
    }
}


class Product {
    private String id;
    private String name;
    private int price;

    public Product(String id, String name, int price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    @Override
    public String toString() {
        return "Product{id='" + id + "', name='" + name +
                "', price=" + price + "}";
    }
}

class ProductRepository {
    private Map<String, Product> database = new HashMap<>();

    public ProductRepository() {
        database.put("P001", new Product("P001", "노트북", 150000));
        database.put("P002", new Product("P002", "마우스", 30000));
    }

    public Product findById(String id) {
        return database.get(id);
    }

    public void update(Product product) {
        database.put(product.getId(), product);
    }
}

// 캐시 통계 클래스 생성
class CacheStatistics {
    private long totalRequests = 0;
    private long cacheHits = 0;
    private long cacheMisses = 0;
    private long expirations = 0;
    private long evictions = 0;

    public void recordHit() {
        totalRequests++;
        cacheHits++;
    }


    public void recordMiss() {
        totalRequests++;
        cacheMisses++;
    }

    public void recordExpiration() {
        expirations++;
    }

    public void recordEviction() {
        evictions++;
    }

    public double getHitRate() {
        if(totalRequests == 0) return 0.0;
        return (double) cacheHits / totalRequests * 100;
    }

    public void printReport() {
        System.out.println("\n📊 === 캐시 통계 ===");
        System.out.println("총 요청: " + totalRequests);
        System.out.println("캐시 히트: " + cacheHits);
        System.out.println("캐시 미스: " + cacheMisses);
        System.out.println("만료: " + expirations);
        System.out.println("LRU 제거: " + evictions);
        System.out.printf("히트율: %.2f%%\n", getHitRate());
        System.out.println("현재 캐시 크기: " + getCurrentSize());
    }

    private int currentSize;

    public void setCurrentSize(int size) {
        this.currentSize = size;
    }

    public int getCurrentSize() {
        return currentSize;
    }
}