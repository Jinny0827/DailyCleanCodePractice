package org.example.cleancode.Y_2025.day46;


import java.util.*;

/**
 * Day 46: 상품 관리 시스템
 * Command Query Responsibility Segregation (CQRS)
 * 읽기/쓰기 모델을 분리하여 각각 최적화하기
 *
 * 문제점:
 * - 읽기/쓰기가 같은 모델 사용 (Product 엔티티 직접 노출)
 * - 복잡한 조회 쿼리 성능 저하
 * - 통계 조회 시 매번 계산 (비효율)
 * - 읽기 전용 데이터에 쓰기 락 발생
 * - 확장성 제한 (읽기/쓰기 독립 스케일링 불가)
 */
public class Day46ProductManagement {

    public static void main(String[] args) {
        ProductService service = new ProductService();

        // 상품 등록
        service.createProduct("P001", "노트북", 1500000, "전자기기", 10);
        service.createProduct("P002", "마우스", 30000, "전자기기", 50);
        service.createProduct("P003", "책상", 200000, "가구", 5);

        // 재고 변경
        service.updateStock("P001", 8);
        service.updateStock("P002", 45);

        // 조회 (문제: 매번 계산, 비효율적)
        service.printProductList();
        service.printCategoryStats();
        service.printLowStockAlert();

    }

}

// 도메인 이벤트 생성
interface DomainEvent {
    String getEventId();
    String getAggregateId();
    long getTimestamp();
}

// 상품 생성됨 이벤트
class ProductCreatedEvent implements DomainEvent {
    private final String eventId;
    private final String productId;
    private final String name;
    private final int price;
    private final String category;
    private final int stock;
    private final long timestamp;

    public ProductCreatedEvent(String productId, String name, int price, String category, int stock) {
        this.eventId = UUID.randomUUID().toString();
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.category = category;
        this.stock = stock;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public String getEventId() {
        return eventId;
    }

    public String getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }

    public int getStock() {
        return stock;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String getAggregateId() {
        return productId;
    }
}


class StockUpdatedEvent implements DomainEvent {
    private final String eventId;
    private final String productId;
    private final int oldStock;
    private final int newStock;
    private final long timestamp;

    public StockUpdatedEvent(String productId, int oldStock, int newStock) {
        this.eventId = UUID.randomUUID().toString();
        this.productId = productId;
        this.newStock = newStock;
        this.oldStock = oldStock;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public String getEventId() {
        return eventId;
    }

    public String getProductId() {
        return productId;
    }

    public int getNewStock() {
        return newStock;
    }

    @Override
    public String getAggregateId() {
        return productId;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public int getOldStock() {
        return oldStock;
    }
}



class Product {
    private String id;
    private String name;
    private int price;
    private String category;
    private int stock;
    private long createdAt;
    private long updatedAt;

    public Product(String id, String name, int price, String category, int stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.stock = stock;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters/Setters...
    public void updateStock(int newStock) {
        this.stock = newStock;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }

    public int getStock() {
        return stock;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }
}

// 상품 목록 뷰 (DTO)
class ProductListView {
    private final Map<String, ProductSummary> products;

    public ProductListView() {
        this.products = new HashMap<>();
    }

    // 스톡 생성
    public void on(ProductCreatedEvent event) {
        ProductSummary summary = new ProductSummary(
                event.getProductId(),
                event.getName(),
                event.getStock()
        );

        products.put(event.getProductId(), summary);
        System.out.println("📊 ProductListView 업데이트");
    }

    // 스톡업데이트
    public void on(StockUpdatedEvent event) {
        ProductSummary summary = products.get(event.getProductId());
        if(summary != null) {
            summary.updateStock(event.getNewStock());
            System.out.println("📊 ProductListView 업데이트");
        }
    }

    // 조회
    public List<ProductSummary> getAll() {
        return new ArrayList<>(products.values());
    }
}


// 간단한 DTO
class ProductSummary {
    private final String id;
    private final String name;
    private int stock;

    public ProductSummary(String id, String name, int stock) {
        this.id = id;
        this.name = name;
        this.stock = stock;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getStock() {
        return stock;
    }

    public void updateStock(int newStock) {
        this.stock = newStock;
    }
}

// 카테고리 통계 (DTO)
class CategoryStats {
    // 상품 수
    private int productCount;
    
    // 재고 가치 (price * stock)
    private long totalValue;

    public CategoryStats() {
        this.productCount = 0;
        this.totalValue = 0;
    }

    // 상품 추가 시 호출
    public void addProduct(int price, int stock) {
        this.productCount++;
        this.totalValue += (long) price * stock;
    }
    
    
    // 재고 업데이트 시 호출 (차이만 적용)
    // totalValue -= (price × oldStock), totalValue += (price × newStock)
    public void updateStock(int price, int oldStock, int newStock) {
        long oldValue = (long) price * oldStock;
        long newValue = (long) price * newStock;
        this.totalValue = this.totalValue - oldValue + newValue;
    }

    public int getProductCount() {
        return productCount;
    }

    public long getTotalValue() {
        return totalValue;
    }
}


// 카테고리 통계 뷰
class CategoryStatsView {
    private final Map<String, CategoryStats> stats;
    // 상품 정보 캐시 (price를 알기 위한)
    private final Map<String, ProductInfo> productCache;

    public CategoryStatsView() {
        this.stats = new HashMap<>();
        this.productCache = new HashMap<>();
    }

    // 상품 생성 이벤트
    public void on(ProductCreatedEvent event) {
        // 캐시에 저장 (나중에 StockUpdatedEvent 에서 사용)
        ProductInfo info = new ProductInfo(
                event.getProductId(),
                event.getPrice(),
                event.getCategory()
        );
        productCache.put(event.getProductId(), info);

        // 통계 업데이트
        CategoryStats categoryStats = stats.computeIfAbsent(
                event.getCategory(),
                k -> new CategoryStats()
        );
        categoryStats.addProduct(event.getPrice(), event.getStock());

        System.out.println("📊 CategoryStatsView 업데이트");
    }

    // 재고 업데이트
    public void on(StockUpdatedEvent event) {
        // 캐시에서 상품 정보 가져오기
        ProductInfo info = productCache.get(event.getProductId());
        if(info == null) return;


        // 통계 업데이트
        CategoryStats categoryStats = stats.get(info.getCategory());
        if (categoryStats != null) {
            categoryStats.updateStock(
                    info.getPrice(),
                    event.getOldStock(),  // ← 이거 getter 추가 필요!
                    event.getNewStock()
            );
            System.out.println("📊 CategoryStatsView 업데이트");
        }
    }

    // 조회
    public CategoryStats getStats(String category) {
        return stats.get(category);
    }

    public Map<String, CategoryStats> getAllStats() {
        return new HashMap<>(stats);
    }
}

// 상품 정보 캐시용 클래스
class ProductInfo {
    private final String productId;
    private final int price;
    private final String category;

    public ProductInfo(String productId, int price, String category) {
        this.productId = productId;
        this.price = price;
        this.category = category;
    }

    public int getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }
}

// 재고 부족 상품 자동 필터링
// 목표: 재고 10개 미만 상품을 자동으로 필터링!
class LowStockView {
    private static final int LOW_STOCK_THRESHOLD = 10;

    // 재고 부족 상품만 저장 (자동 필터링)
    private final Map<String, ProductSummary> lowStockProducts;
    
    // 상품 정보 캐시 추가
    private final Map<String, String> productNames;

    public LowStockView() {
        this.lowStockProducts = new HashMap<>();
        this.productNames = new HashMap<>();
    }

    // 재고 생성 이벤트
    public void on(ProductCreatedEvent event) {
        // 상품명 캐시 저장
        productNames.put(event.getProductId(), event.getName());

        if(event.getStock() < LOW_STOCK_THRESHOLD) {
            ProductSummary summary = new ProductSummary(
                event.getProductId(),
                    event.getName(),
                    event.getStock()
            );
            lowStockProducts.put(event.getProductId(), summary);
            System.out.println("📊 LowStockView 업데이트 (추가: " + event.getName() + ")");
        }
    }
    
    // 재고 업데이트 이벤트
    public void on(StockUpdatedEvent event) {
        String productId = event.getProductId();

        if (event.getNewStock() < LOW_STOCK_THRESHOLD) {
            String name = productNames.get(event.getProductId());
            if(name != null) {
                ProductSummary summary = new ProductSummary(
                    event.getProductId(),
                        name,
                        event.getNewStock()
                );

                lowStockProducts.put(event.getProductId(), summary);
                System.out.println("📊 LowStockView 업데이트 (추가)");
            }


        } else {
            // 재고가 충분했으면 제거
            if(lowStockProducts.remove(productId) != null ) {
                System.out.println("📊 LowStockView 업데이트 (제거: " + productId + ")");
            }

        }
    }

    public List<ProductSummary> getLowStockProducts() {
        return new ArrayList<>(lowStockProducts.values());
    }

}



// 목표: ProductService가 Command를 받아서 → 이벤트 발행 → 뷰들 자동 업데이트
class ProductService {
    // 문제: 읽기/쓰기가 같은 저장소
    private Map<String, Product> products = new HashMap<>();

    private ProductListView productListView = new ProductListView();
    private CategoryStatsView categoryStatsView = new CategoryStatsView();
    private LowStockView lowStockView = new LowStockView();

    // Command: 쓰기 작업
    public void createProduct(String id, String name, int price,
                              String category, int stock) {

        Product product = new Product(id, name, price, category, stock);
        products.put(id, product);

        // 이벤트 발행 추가
        ProductCreatedEvent event = new ProductCreatedEvent(
            id, name, price, category, stock
        );

        // 이벤트 발행 후 뷰 추가
        productListView.on(event);
        categoryStatsView.on(event);
        lowStockView.on(event);
        
        System.out.println("✓ 상품 등록: " + name);
    }

    public void updateStock(String id, int newStock) {
        Product product = products.get(id);
        if (product != null) {
            int oldStock = product.getStock();
            product.updateStock(newStock);

            // 이벤트 발행
            StockUpdatedEvent event = new StockUpdatedEvent(
                id, oldStock, newStock
            );

            // 이벤트 발행 후 뷰 추가
            productListView.on(event);
            categoryStatsView.on(event);
            lowStockView.on(event);


            System.out.println("✓ 재고 업데이트: " + id);
        }
    }

    // Query: 읽기 작업 (문제: 매번 계산, 느림)
    public void printProductList() {
        System.out.println("\n=== 상품 목록 ===");
        for (ProductSummary p : productListView.getAll()) {
            System.out.println(p.getId() + " - " + p.getName() +
                    " (재고: " + p.getStock() + ")");
        }
    }

    // 문제: 매번 전체 순회하며 계산
    public void printCategoryStats() {
        System.out.println("\n=== 카테고리별 통계 ===");
        Map<String, CategoryStats> allStats = categoryStatsView.getAllStats();

        for (Map.Entry<String, CategoryStats> entry : allStats.entrySet()) {
            CategoryStats stats = entry.getValue();
            System.out.println(entry.getKey() + ": " +
                    stats.getProductCount() + "개, " +
                    "재고 가치 " + stats.getTotalValue() + "원");
        }
    }

    // 문제: 매번 필터링
    public void printLowStockAlert() {
        System.out.println("\n=== 재고 부족 알림 ===");
        for (ProductSummary p : lowStockView.getLowStockProducts()) {
            System.out.println("⚠️ " + p.getName() + " 재고 부족 (" +
                    p.getStock() + "개)");
        }
    }
}