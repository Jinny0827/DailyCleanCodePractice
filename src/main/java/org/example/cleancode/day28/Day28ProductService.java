package org.example.cleancode.day28;


import java.util.HashMap;
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

    private Map<String, Product> cache = new HashMap<>();
    private ProductRepository repository = new ProductRepository();

    public static void main(String[] args) {
        Day28ProductService service = new Day28ProductService();

        // 캐시 미스
        service.getProduct("P001");
        service.getProduct("P002");

        // 캐시 히트
        service.getProduct("P001");
        service.getProduct("P001");

        // 상품 업데이트 (캐시 무효화 필요)
        service.updateProduct("P001", 200000);
        service.getProduct("P001");  // 오래된 데이터 반환
    }

    public Product getProduct(String productId) {
        if (cache.containsKey(productId)) {
            System.out.println("💾 캐시에서 조회: " + productId);
            return cache.get(productId);
        }

        System.out.println("🔍 DB에서 조회: " + productId);
        Product product = repository.findById(productId);
        cache.put(productId, product);
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
