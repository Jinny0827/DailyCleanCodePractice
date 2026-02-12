package org.example.cleancode.Y_2026.day73;


import java.util.Map;

/**
 *  Day 73 - 할인 정책 계산기 리팩터링
 *
 * Strategy 패턴: 고객 유형별 할인 계산 분리
 * 단일 책임 원칙: 각 할인 정책의 독립성
 * 개방-폐쇄 원칙: 새로운 할인 정책 추가 용이
 * 매직 넘버 제거: 할인율과 기준 금액 상수화
 * 조건문 중첩 제거: 다형성 활용
 */
public class Day73OrderService {
    private static final double MAX_DISCOUNT_RATE = 0.30;

    public double calculateDiscount(CustomerType customerType, double orderAmount,
                                    boolean hasLoyaltyCard, int purchaseCount) {
        DiscountPolicy discountPolicy = customerType.getPolicy();

        // 파라미터를 객체화
        DiscountRequest request = new DiscountRequest(orderAmount,hasLoyaltyCard,purchaseCount);

        // 정책별 할인율 책정
        double discount = discountPolicy.calculate(request);

        // 최대할인율을 적용하여 반환
        return Math.min(discount, orderAmount * MAX_DISCOUNT_RATE); // 최대 30% 할인
    }
}



// 할인 정책 인터페이스
interface DiscountPolicy {
    double calculate(DiscountRequest request);
}

// 할인 정책 구현체
class VipDiscountPolicy implements DiscountPolicy {
    private static final double LARGE_ORDER_THRESHOLD = 100000;
    private static final double MEDIUM_ORDER_THRESHOLD = 50000;
    private static final double LARGE_ORDER_RATE = 0.20;
    private static final double MEDIUM_ORDER_RATE = 0.15;
    private static final double BASIC_RATE = 0.10;
    private static final double LOYALTY_BONUS = 0.05;

    @Override
    public double calculate(DiscountRequest request) {
        double orderAmount = request.getOrderAmount();
        boolean hasLoyaltyCard = request.hasLoyaltyCard();
        double discount = 0.0;

        if (orderAmount > LARGE_ORDER_THRESHOLD) {
            discount = orderAmount * LARGE_ORDER_RATE;
        } else if (orderAmount > MEDIUM_ORDER_THRESHOLD) {
            discount = orderAmount * MEDIUM_ORDER_RATE;
        } else {
            discount = orderAmount * BASIC_RATE;
        }
        if (hasLoyaltyCard) {
            discount += orderAmount * LOYALTY_BONUS;
        }

        return discount;
    }
}

class RegularDiscountPolicy implements DiscountPolicy {
    private static final int HIGH_PURCHASE_THRESHOLD = 10;
    private static final int MEDIUM_PURCHASE_THRESHOLD = 5;
    private static final double HIGH_PURCHASE_RATE = 0.10;
    private static final double MEDIUM_PURCHASE_RATE = 0.05;
    private static final double LOYALTY_BONUS = 0.03;


    @Override
    public double calculate(DiscountRequest request) {
        double orderAmount = request.getOrderAmount();
        int purchaseCount = request.getPurchaseCount();
        boolean hasLoyaltyCard = request.hasLoyaltyCard();
        double discount = 0.0;

        if (purchaseCount > HIGH_PURCHASE_THRESHOLD) {
            discount = orderAmount * HIGH_PURCHASE_RATE;
        } else if (purchaseCount > MEDIUM_PURCHASE_THRESHOLD) {
            discount = orderAmount * MEDIUM_PURCHASE_RATE;
        }
        if (hasLoyaltyCard) {
            discount += orderAmount * LOYALTY_BONUS;
        }

        return discount;
    }
}

class NewCustomerDiscountPolicy implements DiscountPolicy {
    private static final double MINIMUM_ORDER_THRESHOLD = 30000;
    private static final double DISCOUNT_RATE = 0.05;

    @Override
    public double calculate(DiscountRequest request) {
        double orderAmount = request.getOrderAmount();
        double discount = 0.0;

        if (orderAmount > MINIMUM_ORDER_THRESHOLD) {
            discount = orderAmount * DISCOUNT_RATE;
        }

        return discount;
    }
}



// 할인 요청 객체
class DiscountRequest {
    private final double orderAmount;
    private final boolean hasLoyaltyCard;
    private final int purchaseCount;

    public DiscountRequest(double orderAmount, boolean hasLoyaltyCard, int purchaseCount) {
        this.orderAmount = orderAmount;
        this.hasLoyaltyCard = hasLoyaltyCard;
        this.purchaseCount = purchaseCount;
    }

    public double getOrderAmount() {
        return orderAmount;
    }

    public boolean hasLoyaltyCard() {
        return hasLoyaltyCard;
    }

    public int getPurchaseCount() {
        return purchaseCount;
    }
}

// 고객 관리 enum
enum CustomerType {
    VIP(new VipDiscountPolicy()),
    REGULAR(new RegularDiscountPolicy()),
    NEW(new NewCustomerDiscountPolicy());

    private final DiscountPolicy policy;

    CustomerType(DiscountPolicy policy) {
        this.policy = policy;
    }

    public DiscountPolicy getPolicy() {
        return policy;
    }
}