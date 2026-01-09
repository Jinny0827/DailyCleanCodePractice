package org.example.cleancode.day50;


import java.util.HashMap;
import java.util.Map;

/**
 * Day 50 - 도메인 주도 설계로 비즈니스 로직 재구성
 *
 * 1. 빈약한 도메인 모델(Anemic Domain Model) 탈피
 * 2. 비즈니스 규칙의 도메인 객체 응집
 * 3. 불변성과 캡슐화 강화
 */
public class Day50OrderService {

    // 주문 처리 메서드
    public void processOrder(Order order, Customer customer, Inventory inventory) {
        if(order.getQuantity().isGreaterThan(inventory.getStock(order.getProductId()))) {
            throw new RuntimeException("재고 부족");
        }

        // 가격 계산
        Money price = order.calculatePrice(customer.getGrade());
        
        // 배송비 추가
        Money finalPrice = order.addShippingFee(price);
        
        // 포인트 적립 - Customer에게 위임
        customer.earnPoints(finalPrice);

        // 재고 차감
        inventory.decrease(order.getProductId(), order.getQuantity());

        // 최종 완료
        order.complete(finalPrice);
    }

}

class Order {
    private String orderId;
    private String productId;
    private Quantity quantity;
    private Money unitPrice;
    private OrderStatus status;
    private Money finalPrice;

    public Order(String orderId, String productId, Quantity quantity, Money unitPrice) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.status = OrderStatus.PENDING;
    }

    // 할인 적용된 가격 계산 처리
    public Money calculatePrice(CustomerGrade grade) {
        // Moeny 타입의 곱셈 메서드를 착실히 이용
        // quantity * unitPrice
        Money totalPrice = unitPrice.multiply(quantity.getValue());

        // 할인 적용: totalPrice * (1 - 할인율)
        double discountRate = grade.getDiscountRate();
        Money discountedPrice = totalPrice.multiply(1 - discountRate);

        return discountedPrice;
    }

    // 배송비 추가 (주문금액 3만원 미만일때)
    public Money addShippingFee(Money price) {
        Money threshold = new Money(30000);
        Money shippingFee = new Money(3000);

        if(price.isLessThen(threshold)) {
            // 3만원 미만일 경우 배송비 포함해서 반환
            return price.add(shippingFee);
        }

        // 3만원 이상일 경우 그대로 반환
        return price;
    }


    // 주문 완료 처리
    public void complete(Money finalPrice) {
        this.finalPrice = finalPrice;
        this.status = OrderStatus.COMPLETED;
    }


    // Getters & Setters

    public String getOrderId() {
        return orderId;
    }

    public String getProductId() {
        return productId;
    }

    public Quantity getQuantity() {
        return quantity;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Money getFinalPrice() {
        return finalPrice;
    }
}

class Customer {
    private String customerId;
    private String name;
    private CustomerGrade grade;
    private int points;

    public Customer(String customerId, String name, CustomerGrade grade, int points) {
        this.customerId = customerId;
        this.name = name;
        this.grade = grade;
        this.points = points;
    }
    
    // 포인트 적립 메서드
    public void earnPoints(Money amount) {
        // 구매가격의 0.01퍼센트 포인트 적립
        int earnedPoints = (int) (amount.getAmount() * 0.01);
        this.points += earnedPoints;
    }
    

    // Getters & Setters
    public String getCustomerId() { return customerId; }
    public String getName() { return name; }
    public CustomerGrade getGrade() { return grade; }
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
}

class Inventory {
    private Map<String, Integer> stocks = new HashMap<>();

    public int getStock(String productId) {
        return stocks.getOrDefault(productId, 0);
    }

    public void setStock(String productId, int stock) {
        stocks.put(productId, stock);
    }

    public void addStock(String productId, int quantity) {
        stocks.put(productId, getStock(productId) + quantity);
    }

    public void decrease(String productId, Quantity quantity) {

        // 현재고 확인 및 재고 차감 처리
        int currentStock = stocks.get(productId);
        setStock(productId, currentStock - quantity.getValue());
    }
}

// 원시타입을 벗어난 Value Object의 Money 객체
class Money {
    private final double amount;

    public Money(double amount) {
        if(amount < 0) {
            throw new IllegalArgumentException("금액은 0 이상이어야 합니다");
        }

        this.amount = amount;
    }

    public static Money of(double amount) {
        return new Money(amount);
    }


    // 곱셈 : Money * 배수 = 새로운 Money
    public Money multiply(double multiplier) {
        return new Money(this.amount * multiplier);
    }

    // 뺄셈 : Money - Money = 새로운 Money
    public Money subtract(Money other) {
        return new Money(this.amount - other.amount);
    }

    // 덧셈 : Money + Money = 새로운 Money
    public Money add(Money other) {
        return new Money(this.amount + other.amount);
    }

    // 비교 : Money < Money ? 참 : 거짓
    public boolean isLessThen(Money other) {
        return this.amount < other.amount;
    }

    // 값 조회
    public double getAmount() {
        return amount;
    }

}

class Quantity {
    private final int value;

    public Quantity(int value) {
        // 수량은 1 이상이어야 한다.
        if(value < 1) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    // 재고와 비교
    public boolean isGreaterThan(int stock) {
        return stock < value;
    }
}

enum CustomerGrade {
    VIP(0.2),
    GOLD(0.1),
    NORMAL(0.0);

    private final double discountRate;

    CustomerGrade(double discountRate) {
        this.discountRate = discountRate;
    }

    public double getDiscountRate() {
        return this.discountRate;
    }
}

enum OrderStatus {
    PENDING, COMPLETED, CANCELLED
}