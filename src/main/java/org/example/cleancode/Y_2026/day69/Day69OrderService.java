package org.example.cleancode.Y_2026.day69;


import java.util.List;

/**
 * Day 69 - 전략 패턴으로 할인 정책 리팩터링
 *
 * 복잡한 조건문 중첩
 * 고객 타입 추가 시 메서드 수정 필요 (OCP 위반)
 * 할인 정책 로직이 서비스에 강결합
 * 테스트하기 어려운 구조
 */
public class Day69OrderService {

    public double calculateDiscount(Order order) {
        DiscountStrategy strategy = selectStrategy(order);

        double discount = strategy.calculate(order);

        return Math.min(discount, order.getTotalAmount() * 0.30);
    }


    // 할인 전략 셀렉트
    private DiscountStrategy selectStrategy(Order order) {
        String customerType = order.getCustomer().getType();

        switch (customerType) {
            case "VIP":
                return new VipDiscountStrategy();
            case "GOLD" :
                return new GoldDiscountStrategy();
            case "SILVER":
                return new SilverDiscountStrategy();
            default:
                return new NormalDiscountStrategy();

        }

    }

}


// 할인 전략 인터페이스
interface DiscountStrategy {
    double calculate(Order order);
}

class VipDiscountStrategy implements DiscountStrategy {

    @Override
    public double calculate(Order order) {
        double totalAmount = order.getTotalAmount();

        if (totalAmount > 100000) {
            return totalAmount * 0.20;
        } else {
            return totalAmount * 0.15;
        }
    }
}

class GoldDiscountStrategy implements DiscountStrategy {

    @Override
    public double calculate(Order order) {
        double totalAmount = order.getTotalAmount();
        double discount = totalAmount * 0.10;
        int itemCount = order.getItems().size();

        if (itemCount >= 5) {
            discount += 5000;
        }

        return discount;
    }
}

class SilverDiscountStrategy implements DiscountStrategy {

    @Override
    public double calculate(Order order) {
        double totalAmount = order.getTotalAmount();
        double discount = 0.0;
        int itemCount = order.getItems().size();

        if (itemCount >= 10) {
            discount = totalAmount * 0.05;
        }

        return discount;
    }
}

class NormalDiscountStrategy implements DiscountStrategy {

    @Override
    public double calculate(Order order) {
        double totalAmount = order.getTotalAmount();
        double discount = 0.0;

        if (totalAmount > 50000) {
            discount = 1000;
        }

        return discount;
    }
}


// 매개  객체
class Order {
    private Customer customer;
    private List<OrderItem> items;
    private double totalAmount;

    public Customer getCustomer() { return customer; }
    public List<OrderItem> getItems() { return items; }
    public double getTotalAmount() { return totalAmount; }
}

class Customer {
    private String type; // "VIP", "GOLD", "SILVER", "NORMAL"

    public String getType() { return type; }
}

class OrderItem {
    private String productId;
    private int quantity;
    private double price;

    public OrderItem(String productId, int quantity, double price) {
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

    public double getPrice() {
        return price;
    }
}