package org.example.cleancode.Y_2026.day72;

import java.math.BigDecimal;
import java.util.*;

/**
 * Day 72: 복합 패턴 리팩터링 (전략 + 캐싱 + DI)
 *
 *
 * Strategy 패턴: 고객 타입별 가격 계산 전략 분리
 * 캐시 추상화: 캐싱 로직을 독립적인 책임으로 분리
 * Magic Number: 할인율/금액을 상수나 설정으로 관리
 * 캐시 키 생성: 더 안전하고 명확한 방식 필요
 * 테스트 가능성: 의존성 주입으로 각 컴포넌트 독립 테스트
 */
public class Day72PriceCalculator {
    private final CacheManager<PriceCacheKey, BigDecimal> priceCache;
    private final Map<CustomerType, PricingStrategy> strategies;
    private final PricingStrategy defaultStrategy;

    public Day72PriceCalculator(
            CacheManager<PriceCacheKey, BigDecimal> priceCache,
            Map<CustomerType, PricingStrategy> strategies,
            PricingStrategy defaultStrategy
    ) {
        this.priceCache = priceCache;
        this.strategies = strategies;
        this.defaultStrategy = defaultStrategy;
    }

    public BigDecimal calculatePrice(Order order) {
        CustomerType type = CustomerType.fromString(order.getCustomerType());
        PriceCacheKey cacheKey = new PriceCacheKey(type, order.getTotalAmount());

        if (priceCache.containsKey(cacheKey)) {
            System.out.println("Cache hit");
            return priceCache.get(cacheKey);
        }

        PricingStrategy strategy = strategies.getOrDefault(type, defaultStrategy);
        BigDecimal finalPrice = strategy.calculate(order.getTotalAmount());

        priceCache.put(cacheKey, finalPrice);

        return finalPrice;
    }

//    public static void main(String[] args) {
//        Order order = new Order(
//
//        );
//
//        Day72PriceCalculator calculator = new Day72PriceCalculator();
//        calculator.calculatePrice();
//
//    }
}

// 가격 계산 팩토리
class PriceCalculatorFactory  {
    public static Day72PriceCalculator create() {
        Map<CustomerType, PricingStrategy> strategies = new HashMap<>();
        strategies.put(CustomerType.VIP, new VipPricingStrategy());
        strategies.put(CustomerType.REGULAR, new RegularPricingStrategy());
        strategies.put(CustomerType.NEW, new NewCustomerPricingStrategy());

        return new Day72PriceCalculator(
                new InMemoryCache<>(),
                strategies,
                new DefaultPricingStrategy()
        );
    }
}


// 전략 인터페이스 구성
interface PricingStrategy {
    BigDecimal calculate(BigDecimal totalAmount);
}

// 전략 구현
class VipPricingStrategy implements PricingStrategy {
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.8");
    private static final BigDecimal BONUS_THRESHOLD = new BigDecimal("10000");
    private static final BigDecimal BONUS_DISCOUNT = new BigDecimal("500");

    @Override
    public BigDecimal calculate(BigDecimal totalAmount) {
        // 20퍼센트 할인
        BigDecimal discounted = totalAmount.multiply(DISCOUNT_RATE);

        // 10000원 초과시 추가 500원 할인
        if(totalAmount.compareTo(BONUS_THRESHOLD) > 0) {
            discounted = discounted.subtract(BONUS_DISCOUNT);
        }

        return discounted;
    }
}

class RegularPricingStrategy implements PricingStrategy {
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.95");

    @Override
    public BigDecimal calculate(BigDecimal totalAmount) {
        return totalAmount.multiply(DISCOUNT_RATE);
    }
}

class NewCustomerPricingStrategy implements PricingStrategy {
    private static final BigDecimal THRESHOLD = new BigDecimal("5000");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.98");

    @Override
    public BigDecimal calculate(BigDecimal totalAmount) {
        if (totalAmount.compareTo(THRESHOLD) > 0) {
            return totalAmount.multiply(DISCOUNT_RATE);
        }
        return totalAmount;
    }
}

class DefaultPricingStrategy implements PricingStrategy {
    @Override
    public BigDecimal calculate(BigDecimal totalAmount) {
        return totalAmount;
    }
}


// 캐시 인터페이스
interface CacheManager<K, V> {
    V get(K key);
    void put(K key, V value);
    boolean containsKey(K key);
}

// 캐시 구현체
class InMemoryCache<K, V> implements CacheManager<K, V> {
    private final Map<K, V> cache = new HashMap<>();

    @Override
    public V get(K key) {
        return cache.get(key);
    }

    @Override
    public void put(K key, V value) {
        cache.put(key, value);
    }

    @Override
    public boolean containsKey(K key) {
        return cache.containsKey(key);
    }
}

// 문자열 상수화
enum CustomerType {
    VIP, REGULAR, NEW, GUEST;

    public static CustomerType fromString(String type) {
        try {
            return CustomerType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return GUEST;  // 기본값
        }
    }
}

// 캐시 키 객체 (불변)
class PriceCacheKey {
    private final CustomerType customerType;
    private final BigDecimal amount;

    public PriceCacheKey(CustomerType customerType, BigDecimal amount) {
        this.customerType = customerType;
        this.amount = amount;
    }

    @Override
    public boolean equals(Object obj) {
        // 같은 값
        if(this == obj) return true;
        
        // 같은 타입
        if(!(obj instanceof PriceCacheKey)) return false;

        // 강제 캐스팅
        // BigDecimal은 compareTo로 비교
        PriceCacheKey that = (PriceCacheKey) obj;
        return customerType == that.customerType &&
                amount.compareTo(that.amount) == 0;
    }

    @Override
    public int hashCode() {
        // stripTrailingZeros : 10.50과 10.5를 같게 처리한다.
        return Objects.hash(customerType, amount.stripTrailingZeros());
    }
}




class Order {
    private String orderId;
    private String customerType;
    private BigDecimal totalAmount;
    private List<OrderItem> items;

    public Order(String orderId, String customerType, BigDecimal totalAmount) {
        this.orderId = orderId;
        this.customerType = customerType;
        this.totalAmount = totalAmount;
        this.items = new ArrayList<>();
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerType() {
        return customerType;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void addItem(OrderItem item) {
        this.items.add(item);
    }
}

class OrderItem {
    private String productId;
    private int quantity;
    private BigDecimal price;

    public OrderItem(String productId, int quantity, BigDecimal price) {
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }


    public String getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }
}