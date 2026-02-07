package org.example.cleancode.Y_2026.day68;

import java.util.*;

/**
 *  Day 68 - 주문 할인 정책 리팩터링
 *
 * 중첩 조건문 - 깊이 3단계 if문 제거
 * 전략 패턴 - 할인 정책을 독립적인 객체로 분리
 * OCP 원칙 - 새 할인 정책 추가 시 기존 코드 수정 불필요
 * 책임 분리 - 각 할인 정책의 독립적 테스트 가능
 */
public class Day68OrderService {
    private final DiscountCalculator discountCalculator;

    public Day68OrderService(DiscountCalculator discountCalculator) {
        this.discountCalculator = discountCalculator;
    }

    public double calculateDiscount(Order order, Customer customer) {
        return discountCalculator.createTotalDiscount(order, customer);
    }
}

// 팩터리 사용 메서드
class DiscountCalculator {
    private final List<DiscountPolicy> policies;

    public DiscountCalculator() {
        this.policies = DiscountPolicyFactory.createAllPolicy();
    }

    public double createTotalDiscount(Order order, Customer customer) {
        double totalDiscount = 0.0;

        for(DiscountPolicy policy : policies) {
            totalDiscount += policy.calculate(order, customer);
        }

        return Math.min(totalDiscount, order.getTotalAmount());
    }
}



// 할인 정책 팩터리 메서드
class DiscountPolicyFactory {
    public static List<DiscountPolicy> createAllPolicy() {
        List<DiscountPolicy> policies = new ArrayList<>();
        policies.add(createGradePolicy());
        policies.add(createCouponPolicy());
        policies.add(createFirstPurchasePolicy());

        return policies;
    }

    public static DiscountPolicy createGradePolicy() {
        return new GradeDiscountPolicy();
    }

    public static DiscountPolicy createCouponPolicy() {
        return new CouponDiscountPolicy();
    }

    public static DiscountPolicy createFirstPurchasePolicy() {
        return new FirstPurchaseDiscountPolicy();
    }
}


// 할인 정책 인터페이스
interface DiscountPolicy {
    
    // 할인 금액 계산
    double calculate(Order order, Customer customer);

    // 할인 정책 적용 가능 여부
    boolean isApplicable(Order order, Customer customer);
}

// 등급별 할인 정책 구현체
class GradeDiscountPolicy implements DiscountPolicy {
    private final Map<String, Map<Double, Double>> gradeDiscountRates;

    public GradeDiscountPolicy() {
        gradeDiscountRates = new HashMap<>();
        initializeDiscountRates();
    }

    private void initializeDiscountRates() {
        // VIP: 10만원 이상 15%, 5만원 이상 10%, 그 외 5%
        Map<Double, Double> vipRates = new LinkedHashMap<>();
        vipRates.put(100000.0, 0.15);
        vipRates.put(50000.0, 0.10);
        vipRates.put(0.0, 0.05);
        gradeDiscountRates.put("VIP", vipRates);

        // Gold
        Map<Double, Double> goldRates = new LinkedHashMap<>();
        goldRates.put(100000.0, 0.10);
        goldRates.put(50000.0, 0.07);
        gradeDiscountRates.put("GOLD", goldRates);

        // Silver
        Map<Double, Double> silverRates = new LinkedHashMap<>();
        silverRates.put(50000.0, 0.05);
        gradeDiscountRates.put("SILVER", silverRates);
    }

    @Override
    public double calculate(Order order, Customer customer) {
        if (!isApplicable(order, customer)) {
            return 0;
        }

        Map<Double, Double> rates = gradeDiscountRates.get(customer.getGrade());
        double amount = order.getTotalAmount();

        for(Map.Entry<Double, Double> entry : rates.entrySet()) {
            if(amount >= entry.getKey()) {
                return amount * entry.getValue();
            }
        }

        return 0;
    }

    @Override
    public boolean isApplicable(Order order, Customer customer) {
        return gradeDiscountRates.containsKey(customer.getGrade());
    }
}



// 쿠폰 할인 정책
class CouponDiscountPolicy implements DiscountPolicy{

    @Override
    public double calculate(Order order, Customer customer) {
        if (!isApplicable(order, customer)) {
            return 0;
        }

        String couponType = customer.getCouponType();
        double amount = order.getTotalAmount();

        if (couponType.equals("FIXED")) {
            return customer.getCouponAmount();
        } else if (couponType.equals("PERCENTAGE")) {
            return amount * (customer.getCouponRate() / 100);
        }

        return 0;
    }

    @Override
    public boolean isApplicable(Order order, Customer customer) {
        return customer.hasCoupon();
    }
}

// 첫 구매 할인 정책
class FirstPurchaseDiscountPolicy implements DiscountPolicy {
    private static final double MINIMUM_AMOUNT = 30000.0;
    private static final double DISCOUNT_AMOUNT = 5000.0;

    @Override
    public double calculate(Order order, Customer customer) {
        if(!isApplicable(order, customer)) {
            return 0;
        }

        return DISCOUNT_AMOUNT;
    }

    @Override
    public boolean isApplicable(Order order, Customer customer) {
        return customer.isFirstPurchase() && order.getTotalAmount() >= MINIMUM_AMOUNT;
    }
}




// 데이터 객체

class Customer {
    private String customerId;
    private String grade; // VIP, GOLD, SILVER, NORMAL
    private boolean firstPurchase;
    private boolean hasCoupon;
    private String couponType; // FIXED, PERCENTAGE
    private double couponAmount; // 고정 할인액
    private double couponRate; // 퍼센트 할인율

    public Customer(String customerId, String grade, boolean firstPurchase) {
        this.customerId = customerId;
        this.grade = grade;
        this.firstPurchase = firstPurchase;
        this.hasCoupon = false;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getGrade() {
        return grade;
    }

    public boolean isFirstPurchase() {
        return firstPurchase;
    }

    public boolean hasCoupon() {
        return hasCoupon;
    }

    public String getCouponType() {
        return couponType;
    }

    public double getCouponAmount() {
        return couponAmount;
    }

    public double getCouponRate() {
        return couponRate;
    }

    public void setCoupon(String type, double value) {
        this.hasCoupon = true;
        this.couponType = type;
        if (type.equals("FIXED")) {
            this.couponAmount = value;
        } else {
            this.couponRate = value;
        }
    }
}

class Order {
    private String orderId;
    private double totalAmount;
    private List<OrderItem> items;

    public Order(String orderId, double totalAmount) {
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.items = new ArrayList<>();
    }

    public String getOrderId() {
        return orderId;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public List<OrderItem> getItems() {
        return items;
    }
}


class OrderItem {
    private String productId;
    private String productName;
    private int quantity;
    private double price;

    public OrderItem(String productId, String productName, int quantity, double price) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }
}
