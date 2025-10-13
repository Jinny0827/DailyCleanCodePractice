package org.example.cleancode.day1;

import org.example.Main;

import java.util.ArrayList;
import java.util.List;

public class Day01OrderRefactoring {

    public static void main(String[] args) {
        // 테스트용 주문 생성
        Order order = new Order();
        order.customerEmail = "test@example.com";
        order.couponCode = "WELCOME";
        order.items = new ArrayList<>();
        order.items.add(new Item("노트북", 1200000, 1));
        order.items.add(new Item("마우스", 30000, 2));

        // 주문 처리
        int total = processOrder(order);
        System.out.println("최종 결제 금액: " + total + "원");
    }

    public static int processOrder(Order order) {
        validateOrder(order);
        int total = calculateTotal(order);
        total = applyDiscount(total, order.couponCode);
        total = addShippingFee(total);
        sendOrderEmail(order.customerEmail, total);
        return total;
    }

    // 유효성
    public static void validateOrder(Order order) {
        // 주문 유효성 검사
        if (order.items == null || order.items.isEmpty()) {
            throw new IllegalArgumentException("주문 항목이 없습니다");
        }
        if (order.customerEmail == null || order.customerEmail.isEmpty()) {
            throw new IllegalArgumentException("이메일이 필요합니다");
        }
    }


    public static int calculateTotal(Order order) {
        int total = 0;
        for (Item item : order.items) {  // enhanced for문이 더 깔끔해요
            total += item.price * item.quantity;
        }
        return total;
    }


    public static int applyDiscount(int total, String couponCode) {
        if ("WELCOME".equals(couponCode)) {
            return (int)(total * 0.9);
        } else if ("VIP".equals(couponCode)) {
            return (int)(total * 0.8);
        }
        return total;  // 쿠폰이 없으면 원래 금액
    }

    public static int addShippingFee(int total) {
        if (total < 30000) {
            return total + 3000;
        }
        return total;
    }

    public static void sendOrderEmail(String orderEmail, int total) {

        try {
            System.out.println(orderEmail + "로 주문 확인 메일 발송");
            System.out.println("총 금액: " + total + "원");
        } catch (Exception e) {
            System.out.println("이메일 전송 실패 에러 : {}" + e);
        }

    }

    static class Order {
        String customerEmail;
        String couponCode;
        List<Item> items;
    }

    static class Item {
        String name;
        int price;
        int quantity;

        Item(String name, int price, int quantity) {
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }
    }
}
