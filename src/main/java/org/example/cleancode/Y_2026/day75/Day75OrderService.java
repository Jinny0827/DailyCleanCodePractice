package org.example.cleancode.Y_2026.day75;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Day 75: 값 객체와 도메인 불변성
 *
 * Primitive Obsession: 원시 타입만 사용
 * 도메인 규칙 분산: 검증 로직이 여기저기 흩어짐
 * 불변성 부재: Map으로 데이터 전달
 * 캡슐화 부족: 비즈니스 규칙이 노출됨
 */
public class Day75OrderService {
    private static final Money MINIMUM_ORDER_AMOUNT= new Money(10000);

    public void processOrder(String customerId, List<OrderItem> items) {
        Money total = new Money(0.0);
        for (OrderItem item : items) {
            total = total.plus(item.calculateTotal());
        }

        if(total.isLessThan(MINIMUM_ORDER_AMOUNT)) {
            throw new IllegalArgumentException("Minimum order amount is 10000");
        }

        System.out.println("Order processed: " + total.getAmount());
    }

}

// 값 객체 생성
// 상속 방지(final): 누군가 Money를 상속해서 동작을 변경하는 것을 막음
final class Money {
    private final double amount;

    public Money(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        this.amount = amount;
    }


    public Money plus(Money other) {
        return new Money(this.amount + other.amount);
    }

    public Money multiply(double multiplier) {
        return new Money(this.amount * multiplier);
    }

    public boolean isLessThan(Money other) {
        return this.amount < other.amount;
    }

    public double getAmount() {
        return amount;
    }
}

final class Quantity {
    private final int value;

    public Quantity(int value) {
        if(value <= 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

        this.value = value;
    }

    public boolean isEligibleForDiscount() {
        return value >= 10;
    }

    public Money multiply(Money unitPrice) {
        return unitPrice.multiply(value);
    }

    public int getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Quantity quantity = (Quantity) o;
        return value == quantity.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}

final class OrderItem {
    private final Quantity quantity;
    private final Money unitPrice;

    public OrderItem(Quantity quantity, Money unitPrice) {
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }


    // 총액 계산
    public Money calculateTotal() {
        Money total = quantity.multiply(unitPrice);

        if(quantity.isEligibleForDiscount()) {
            // 10% discount
            return total.multiply(0.9);
        }

        return total;
    }
}