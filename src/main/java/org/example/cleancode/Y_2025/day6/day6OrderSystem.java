package org.example.cleancode.Y_2025.day6;

import java.util.ArrayList;
import java.util.List;

/**
 * 하나의 메서드가 너무 많은 일을 함 (단일 책임 원칙 위반)
 * 데이터가 여러 리스트로 흩어져 있음 (응집도 낮음)
 * 매개변수가 너무 많음 (가독성 저하)
 * 객체지향의 장점을 전혀 활용하지 못함
 * */


public class day6OrderSystem {
    public static void main(String[] args) {
        List<Item> items = List.of(
                new Item("노트북", 1200000, 1),
                new Item("마우스", 30000, 2),
                new Item("키보드", 80000, 1)
        );

        Order order = new Order(items, "wjjung@naver.com", "STUDENT");
        OrderProcessor.processOrder(order);
    }
}

/**
 * 과부화된 메서드를 클래스로 분리
 * Order 클래스 - 주문 정보를 담는 데이터 클래스
 * Item 클래스 - 상품 정보를 담는 데이터 클래스
 * OrderValidator 클래스 - 주문 유효성 검사
 * PriceCalculator 클래스 - 가격 계산 (할인, 배송비 등)
 * EmailService 클래스 - 이메일 발송
 * OrderProcessor 클래스 - 전체 주문 처리 흐름 관리
 * */


class Item {
    private String name;
    private int price;
    private int quantity;

    public Item(String name, int price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getTotalPrice() {
        return price * quantity;
    }
}

class Order {
    private List<Item> items;
    private String customerEmail;
    private String couponCode;

    public Order(List<Item> items, String customerEmail, String couponCode) {
        this.items = items;
        this.customerEmail = customerEmail;
        this.couponCode = couponCode;
    }

    // 빈 주문 생성자 (상품을 나중에 추가할 때)
    public Order(String customerEmail, String couponCode) {
        this.items = new ArrayList<>();
        this.customerEmail = customerEmail;
        this.couponCode = couponCode;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public void addItem(Item item) {
        this.items.add(item);
    }
}

class OrderValidator {

    public static boolean isValid(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            System.out.println("ERROR: 주문 항목이 없습니다");
            return false;
        }

        if(order.getCustomerEmail() == null || order.getCustomerEmail().trim().isEmpty()) {
            System.out.println("ERROR: 이메일이 필요합니다");
            return false;
        }

        // 각 상품을 순회하며 유효성 검사
        for (Item item : order.getItems()) {
            if (item.getName() == null || item.getName().trim().isEmpty()) {
                System.out.println("ERROR: 상품명이 없습니다");
                return false;
            }
            if (item.getPrice() <= 0) {
                System.out.println("ERROR: 가격이 유효하지 않습니다");
                return false;
            }
            if (item.getQuantity() <= 0) {
                System.out.println("ERROR: 수량이 유효하지 않습니다");
                return false;
            }
        }

        return true;
    }

}

class PriceCalculator {

    public static int calculateFinalPrice(Order order) {
        int total = 0;

        for(Item item : order.getItems()) {
            total += item.getPrice() * item.getQuantity();
        }

        double discountRate = 1.0;
        if ("WELCOME".equals(order.getCouponCode())) {
            discountRate = 0.9;
        } else if ("VIP".equals(order.getCouponCode())) {
            discountRate = 0.8;
        } else if ("STUDENT".equals(order.getCouponCode())) {
            discountRate = 0.85;
        }
        total = (int)(total * discountRate);

        if(total < 50000) {
            total += 3000;
        }

        return total;
    }

}

class EmailService {

    public static void sendEmail(Order order, int total) {
        System.out.println("=== 주문 확인 이메일 ===");
        System.out.println("To: " + order.getCustomerEmail());
        System.out.println("주문 내역:");
        for (Item item : order.getItems()) {
            System.out.println("- " + item.getName() +
                    " x" + item.getQuantity() +
                    " = " + item.getTotalPrice() + "원");
        }
        System.out.println("쿠폰: " + (order.getCouponCode() != null ? order.getCouponCode() : "없음"));
        System.out.println("최종 결제 금액: " + total + "원");
        System.out.println("===================");
    }

}

class OrderProcessor {
    public static void processOrder(Order order) {
        if (!OrderValidator.isValid(order)) {
            return;
        }

        int total = PriceCalculator.calculateFinalPrice(order);

        EmailService.sendEmail(order, total);

        System.out.println("주문 처리 완료!");
    }

}
