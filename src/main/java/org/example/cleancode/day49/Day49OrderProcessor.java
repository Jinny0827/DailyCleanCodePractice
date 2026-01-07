package org.example.cleancode.day49;

import java.util.List;

/**
 * Day 49: 복잡한 비즈니스 로직 분리
 *
 * 복잡하게 얽힌 도메인 로직을 책임별로 분리하고, 전략 패턴과 DI를 활용한 확장 가능한 구조로 개선
 */
public class Day49OrderProcessor {

    public static void main(String[] args) {


    }

    public double processOrder(Order order, CustomerType customerType, PaymentMethod paymentMethod) {
        double total = calculateSubtotal(order);
        total = applyDiscount(total, customerType);
        total = applyPayment(total, paymentMethod);
        total = addShippingFee(total, customerType);
        notifyIfNeeded(customerType, total);
        return total;
    }

    private void sendEmail(String message) {
        System.out.println("Email sent: " + message);
    }

    private double calculateSubtotal(Order order) {
        double total = 0.0;

        for (Item item : order.getItems()) {
            total += item.getPrice() * item.getQuantity();
        }

        return total;
    }

    private double applyDiscount(double total, CustomerType customerType) {
        if (customerType == CustomerType.VIP) {
            total *= PriceConstants.VIP_DISCOUNT_RATE;
        } else if (customerType == CustomerType.REGULAR) {
            total *= PriceConstants.REGULAR_DISCOUNT_RATE;
        } else if (customerType == CustomerType.NEW) {
            total *= PriceConstants.NEW_DISCOUNT_RATE;
        }

        return total;
    }

    private double applyPayment(double total, PaymentMethod paymentMethod) {
        if (paymentMethod == PaymentMethod.CARD) {
            total *= PriceConstants.CARD_FEE_RATE;
        } else if (paymentMethod == PaymentMethod.CASH) {
            total *= PriceConstants.CASH_DISCOUNT_RATE;
        } else if (paymentMethod == PaymentMethod.POINT) {
            if (total > PriceConstants.POINT_FEE_THRESHOLD) {
                total *= PriceConstants.POINT_FEE_RATE;
            }
        }

        return total;
    }


    private double addShippingFee(double total, CustomerType customerType) {
        if (total < PriceConstants.BASIC_SHIPPING_THRESHOLD) {
            total += PriceConstants.HIGH_SHIPPING_FEE;
        } else if (total < PriceConstants.FREE_SHIPPING_THRESHOLD && customerType != CustomerType.VIP) {
            total += PriceConstants.LOW_SHIPPING_FEE;
        }

        return total;
    }

    private void notifyIfNeeded(CustomerType customerType, double total) {
        System.out.println("Order processed: " + total);
        if (customerType == CustomerType.VIP) {
            sendEmail("VIP order completed");
        }
    }
}

// 할인 전략 인터페이스
interface DiscountPolicy {
    double apply(double amount);
}

class VipDiscountPolicy implements DiscountPolicy {
    @Override
    public double apply(double amount) {
        return amount * PriceConstants.VIP_DISCOUNT_RATE;
    }
}

// 전략 선택 팩터리 메서드
class DiscountPolicyFactory {
    static DiscountPolicy getPolicy(CustomerType type) {
        return switch (type) {
            case VIP -> new VipDiscountPolicy();
            case REGULAR -> new RegularDiscountPolicy();
            case NEW -> new NewDiscountPolicy();
        };
    }
}



// 매직넘버 상수화 (Enum)
// 고객 타입
enum CustomerType {
    VIP, REGULAR, NEW
}

// 결제 방법
enum PaymentMethod {
    CARD, CASH, POINT
}

// 비즈니스 상수
class PriceConstants {
    // 할인율
    static final double VIP_DISCOUNT_RATE = 0.8;
    static final double REGULAR_DISCOUNT_RATE  = 0.95;
    static final double NEW_DISCOUNT_RATE  = 0.98;

    // 결제 수수료율
    static final double CARD_FEE_RATE = 1.03;
    static final double CASH_DISCOUNT_RATE = 0.98;
    static final double POINT_FEE_RATE = 1.05;
    // 포인트 수수료 적용 기준
    static final double POINT_FEE_THRESHOLD = 100_000;
    
    // 배송비
    static final double FREE_SHIPPING_THRESHOLD = 50_000;
    static final double BASIC_SHIPPING_THRESHOLD = 30_000;
    static final double HIGH_SHIPPING_FEE = 3_000;
    static final double LOW_SHIPPING_FEE = 2_000;
    
}



// 주문 클래스
class Order {
    private List<Item> items;

    public Order(List<Item> items) {
        this.items = items;
    }

    public List<Item> getItems() {
        return items;
    }
}

// 상품 클래스
class Item {
    private String name;
    private double price;
    private int quantity;

    public Item(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }
}
