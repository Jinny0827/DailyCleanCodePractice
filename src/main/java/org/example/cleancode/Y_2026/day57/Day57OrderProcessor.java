package org.example.cleancode.Y_2026.day57;


/**
 * Day 57 - 복잡한 주문 처리 로직 리팩터링
 *
 *
 * 단일 책임 원칙 위반: 할인, 배송, 처리 로직이 한 메서드에 집중
 * 개방-폐쇄 원칙 위반: 새로운 고객 타입이나 주문 타입 추가시 기존 코드 수정 필요
 * 복잡한 조건문: 중첩된 if-else로 가독성 저하
 * 하드코딩: 할인율, 배송일, 창고 정보가 코드에 직접 작성됨
 */
public class Day57OrderProcessor {

    public void processOrder(String orderType, double amount, String customerType, boolean isUrgent) {
        double finalAmount = amount;
        String processingMethod = "";
        int deliveryDays = 0;

        // 고객 타입별 할인 적용
        if (customerType.equals("PREMIUM")) {
            finalAmount = amount * 0.85;  // 15% 할인
        } else if (customerType.equals("VIP")) {
            finalAmount = amount * 0.75;  // 25% 할인
        } else if (customerType.equals("REGULAR")) {
            finalAmount = amount * 0.95;  // 5% 할인
        }

        // 주문 타입별 처리 방식 결정
        if (orderType.equals("ELECTRONICS")) {
            processingMethod = "WAREHOUSE_A";
            deliveryDays = isUrgent ? 1 : 3;
            if (finalAmount > 1000) {
                finalAmount -= 50; // 추가 할인
            }
        } else if (orderType.equals("CLOTHING")) {
            processingMethod = "WAREHOUSE_B";
            deliveryDays = isUrgent ? 2 : 5;
            if (finalAmount > 500) {
                finalAmount -= 25; // 추가 할인
            }
        } else if (orderType.equals("BOOKS")) {
            processingMethod = "WAREHOUSE_C";
            deliveryDays = isUrgent ? 1 : 7;
            // 도서는 추가 할인 없음
        }

        // 긴급 주문 추가 비용
        if (isUrgent) {
            finalAmount += 20;
        }

        System.out.println("Processing: " + processingMethod);
        System.out.println("Final amount: $" + finalAmount);
        System.out.println("Delivery in: " + deliveryDays + " days");

        // 실제 주문 처리 로직...
        sendToWarehouse(processingMethod, finalAmount);
        updateInventory(orderType, 1);
        notifyCustomer(customerType, deliveryDays);
    }

    private void sendToWarehouse(String warehouse, double amount) { /* ... */ }
    private void updateInventory(String type, int quantity) { /* ... */ }
    private void notifyCustomer(String customerType, int days) { /* ... */ }

}

// 주문 객체
class Order {
    private final String orderType;
    private final double amount;
    private final String customerType;
    private final boolean isUrgent;

    public Order(String orderType, double amount, String customerType, boolean isUrgent) {
        this.orderType = orderType;
        this.amount = amount;
        this.customerType = customerType;
        this.isUrgent = isUrgent;
    }


    public String getOrderType() {
        return orderType;
    }

    public double getAmount() {
        return amount;
    }

    public String getCustomerType() {
        return customerType;
    }

    public boolean isUrgent() {
        return isUrgent;
    }
}


// 주문 결과 객체
class OrderResult {
    private final double finalAmount;
    private final String processingMethod;
    private final int deliveryDays;

    public OrderResult(double finalAmount, String processingMethod, int deliveryDays) {
        this.finalAmount = finalAmount;
        this.processingMethod = processingMethod;
        this.deliveryDays = deliveryDays;
    }

    public double getFinalAmount() {
        return finalAmount;
    }

    public String getProcessingMethod() {
        return processingMethod;
    }

    public int getDeliveryDays() {
        return deliveryDays;
    }
}