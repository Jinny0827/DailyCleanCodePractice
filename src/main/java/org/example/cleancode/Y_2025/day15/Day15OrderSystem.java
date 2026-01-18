package org.example.cleancode.Y_2025.day15;


import java.util.Objects;

/**
 * Day 15: 원시 타입 집착(Primitive Obsession) 개선
 *
 * 문제점:
 * - int, String 같은 원시 타입으로 도메인 개념 표현
 * - 검증 로직이 여러 곳에 중복
 * - 비즈니스 로직이 분산됨
 * - 타입 안정성 부족 (금액과 수량을 혼동 가능)
 */
public class Day15OrderSystem {

    public static void main(String[] args) {
        // 주문 생성
        Order order = new Order(
                "ORD-001",
                new Email("user@example.com"),
                new Money(50000),
                new Address("서울시 강남구 테헤란로 123"),
                new PhoneNumber("010-1234-5678")
        );

        // 배송비 추가
        order.addShippingFee(new Money(3000));

        // 할인 적용
        order.applyDiscount(new Money(5000));

        // 주문 정보 출력
        System.out.println("주문번호: " + order.getOrderId());
        System.out.println("이메일: " + order.getEmail());
        System.out.println("주문금액: " + order.getAmount());
        System.out.println("배송지: " + order.getAddress());
        System.out.println("연락처: " + order.getPhone());
    }

}

class Order {
    private String orderId;
    private final Email email;
//    private int amount;  // 원시 타입으로 금액 표현
//    private String address;  // 원시 타입으로 주소 표현
//    private String phone;  // 원시 타입으로 전화번호 표현
    private Money amount;
    private final Address address;
    private final PhoneNumber phone;

    public Order(String orderId, Email email, Money amount,
                 Address address, PhoneNumber phone) {
        if(orderId == null || orderId.isEmpty()) {
            throw new IllegalArgumentException("주문번호는 필수입니다.");
        }

        this.orderId = orderId;
        this.email = email;
        this.amount = amount;
        this.address = address;
        this.phone = phone;
    }

    public void addShippingFee(Money  fee) {
        this.amount = this.amount.add(fee);
    }

    public void applyDiscount(Money discount) {
        this.amount = this.amount.subtract(discount);
    }

    // Getters
    public String getOrderId() { return orderId; }
    public Email getEmail() { return email; }
    public Money getAmount() { return amount; }
    public Address getAddress() { return address; }
    public PhoneNumber getPhone() { return phone; }
}

class Money {
    private final int amount;

    public Money(int amount) {

        if (amount < 0) {
            throw new IllegalArgumentException("금액은 0 이상이어야 합니다");
        }

        this.amount = amount;
    }

    public Money add(Money other) {
        return new Money(this.amount + other.amount);
    }

    public Money subtract(Money other) {
        if(this.amount < other.amount) {
            throw new IllegalArgumentException("할인금액이 주문금액보다 큽니다.");
        }
        return new Money(this.amount - other.amount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return amount == money.amount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount);
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "Money{" +
                "amount=" + amount +
                '}';
    }
}

class Email {
    private final String value;

    public Email(String value) {
        if(value == null || !value.contains("@")) {
            throw new IllegalArgumentException("올바른 이메일이 아닙니다.");
        }

        if(!value.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다.");
        }

        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return Objects.equals(value, email.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "Email{" +
                "value='" + value + '\'' +
                '}';
    }
}

class Address {
    private final String value;

    public Address(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("주소는 필수입니다");
        }

        if (value.length() < 10) {
            throw new IllegalArgumentException("주소가 너무 짧습니다");
        }

        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(value, address.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "Address{" +
                "value='" + value + '\'' +
                '}';
    }
}


class PhoneNumber {
    private final String value;

    public PhoneNumber(String value) {
        if (value == null) {
            throw new IllegalArgumentException("전화번호는 필수입니다");
        }


        // 유효성 검증 위해 하이픈 제거
        String cleaned = value.replaceAll("-", "");

        if (cleaned.length() < 10 || cleaned.length() > 11) {
            throw new IllegalArgumentException("올바른 전화번호가 아닙니다");
        }

        if (!cleaned.matches("^[0-9]+$")) {
            throw new IllegalArgumentException("전화번호는 숫자만 입력 가능합니다");
        }

        this.value = cleaned;
    }


    public String getValue() {
        return value;
    }

    public String getFormatted() {
        if (value.length() == 10) {
            return value.substring(0, 3) + "-" +
                    value.substring(3, 6) + "-" +
                    value.substring(6);
        } else if (value.length() == 11) {
            return value.substring(0, 3) + "-" +
                    value.substring(3, 7) + "-" +
                    value.substring(7);
        }
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhoneNumber that = (PhoneNumber) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "PhoneNumber{" +
                "value='" + value + '\'' +
                '}';
    }
}
