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
            }

            return shouldRemove;
        }
    };


    private ProductRepository repository = new ProductRepository();


    public static void main(String[] args) {
        Day28ProductService service = new Day28ProductService();

        System.out.println("=== 캐시에 4개 항목 추가 (최대 3개) ===");
        service.getProduct("P001");
        service.getProduct("P002");
        service.getProduct("P003");
        service.getProduct("P004"); // P001 제거

        System.out.println("\n=== P001 재조회 ===");
        service.getProduct("P001"); // 캐시 미스

        System.out.println("\n=== P002 재조회 ===");
        service.getProduct("P002"); // 캐시 히트
    }

    public Product getProduct(String productId) {
        CacheEntry<Product> entry = cache.get(productId);

        if(entry != null && !entry.isExpired()) {
            System.out.println("캐시 히트 : " + productId);
            return entry.getValue();
        }

        if (entry != null) {
            System.out.println("⏰ 캐시 만료: " + productId);
        } else {
            System.out.println("🔍 캐시 미스: " + productId);
        }

        Product product = repository.findById(productId);
        CacheEntry<Product> newEntry = new CacheEntry<>(
                product,
                System.currentTimeMillis(),
                DEFAULT_TTL
        );

        cache.put(productId, newEntry);

        return product;
    }

    public void updateProduct(String productId, int newPrice) {
        Product product = repository.findById(productId);
        product.setPrice(newPrice);
        repository.update(product);
        System.out.println("✓ 상품 업데이트: " + productId);
        // 캐시 무효화 없음!
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
