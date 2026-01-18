package org.example.cleancode.Y_2026.day56;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Day 56: 복잡한 할인 정책 리팩터링
 *
 * 중첩된 조건문 제거
 * 할인 정책을 명시적인 객체로 분리
 * 매직 넘버 상수화
 * 전략 패턴 또는 체인 적용
 */
public class Day56OrderCalculator {
    private List<DiscountPolicy> itemDiscountPolicies;
    private List<OrderDiscountPolicy> orderDiscountPolicies;

    public static void main(String[] args) {

    }

    public Day56OrderCalculator() {
        this.itemDiscountPolicies = Arrays.asList(
                new VipElectronicsDiscount(),
                new VipClothingDiscount(),
                new VipOtherDiscount(),
                new RegularLoyaltyDiscount()
        );

        this.orderDiscountPolicies = Arrays.asList(
                new BulkDiscountLarge(),
                new BulkDiscountMedium()
        );
    }

    public double calculateTotal(Order order, String customerType,
                                 boolean hasLoyaltyCard, int itemCount) {
        double total = 0;

        for (Item item : order.getItems()) {
            double price = item.getPrice();
            
            // 정책 적용
            for(DiscountPolicy policy : itemDiscountPolicies) {
                if (policy.isApplicable(item, customerType, hasLoyaltyCard)) {
                    price = policy.applyDiscount(item);
                    break;
                }
            }

            total += price * item.getQuantity();
        }
        
        // 수량별 할인
        for (OrderDiscountPolicy policy : orderDiscountPolicies) {
            if(policy.isApplicable(itemCount)) {
                total = policy.applyDiscount(total);
                break;
            }
        }

        return total;
    }

}

// 할인 정책(전략) 인터페이스
interface DiscountPolicy {
    double applyDiscount(Item item);
    boolean isApplicable(Item item, String customerType, boolean hasLoyaltyCard);
}

// VIP 전자제품 전략 구현체
class VipElectronicsDiscount implements DiscountPolicy {
    @Override
    public double applyDiscount(Item item) {
        return item.getPrice() * (1 - DiscountRate.VIP_ELECTRONICS);
    }

    @Override
    public boolean isApplicable(Item item, String customerType, boolean hasLoyaltyCard) {
        return customerType.equals("VIP") &&
                item.getCategory().equals("ELECTRONICS");
    }
}

// VIP 의류 할인 정책 구현체
class VipClothingDiscount implements DiscountPolicy {
    @Override
    public double applyDiscount(Item item) {
        return item.getPrice() * (1 - DiscountRate.VIP_CLOTHING);
    }

    @Override
    public boolean isApplicable(Item item, String customerType, boolean hasLoyaltyCard) {
        return customerType.equals("VIP") &&
                item.getCategory().equals("CLOTHING");
    }
}

// VIP 그외 할인 정책 구현체
class VipOtherDiscount implements DiscountPolicy {

    @Override
    public double applyDiscount(Item item) {
        return item.getPrice() * (1 - DiscountRate.VIP_OTHER);
    }

    @Override
    public boolean isApplicable(Item item, String customerType, boolean hasLoyaltyCard) {
        return customerType.equals("VIP") &&
                !item.getCategory().equals("ELECTRONICS") &&
                !item.getCategory().equals("CLOTHING");

    }
}

// 레귤러 할인 정책 구현체
class RegularLoyaltyDiscount implements DiscountPolicy {
    @Override
    public double applyDiscount(Item item) {
        return item.getPrice() * (1 - DiscountRate.REGULAR_LOYALTY);
    }

    @Override
    public boolean isApplicable(Item item, String customerType, boolean hasLoyaltyCard) {
        return customerType.equals("REGULAR") && hasLoyaltyCard;
    }
}


// 주문 전체 할인 정책
interface OrderDiscountPolicy {
    double applyDiscount(double totalAmount);
    boolean isApplicable(int itemCount);
}

// 대량 구매 할인에 대한 정책 구현
class BulkDiscountLarge implements OrderDiscountPolicy {
    @Override
    public double applyDiscount(double totalAmount) {
        return totalAmount * (1 - DiscountRate.BULK_DISCOUNT_LARGE);
    }

    @Override
    public boolean isApplicable(int itemCount) {
        return itemCount > 10;
    }
}

class BulkDiscountMedium implements OrderDiscountPolicy {
    @Override
    public double applyDiscount(double totalAmount) {
        return totalAmount * (1 - DiscountRate.BULK_DISCOUNT_MEDIUM);
    }

    @Override
    public boolean isApplicable(int itemCount) {
        return itemCount > 5;
    }
}




// 상품 객체
class Item {
    private String name;
    private double price;
    private int quantity;
    private String category;  // "ELECTRONICS", "CLOTHING", "FOOD" 등

    public Item(String name, double price, int quantity, String category) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
    }

    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public String getCategory() { return category; }
}

// 주문
class Order {
    private List<Item> items;

    public Order() {
        this.items = new ArrayList<>();
    }

    public void addItem(Item item) {
        items.add(item);
    }

    public List<Item> getItems() {
        return items;
    }

    public int getTotalItemCount() {
        return items.stream()
                .mapToInt(Item::getQuantity)
                .sum();
    }
}

// 매직 넘버 클래스
class DiscountRate {
    
    // VIP 고객 할인율
    public static final double VIP_ELECTRONICS = 0.15;
    public static final double VIP_CLOTHING = 0.20;
    public static final double VIP_OTHER = 0.10;
    
    // REGULAR 고객 할인율
    public static final double REGULAR_LOYALTY = 0.05;

    // 수량별 할인율 (10개 초과, 5개 초과)
    public static final double BULK_DISCOUNT_LARGE = 0.05;
    public static final double BULK_DISCOUNT_MEDIUM = 0.02;
}







