package org.example.cleancode.Y_2026.first_half.april.day110;


/**
 *  Day 110 — Java Record + Sealed Class: 불변 도메인 모델링
 *
 * 보일러플레이트 -> record OrderItem(...) — 생성자/getter/equals/hashCode/toString 자동
 * 상태별 필드 혼재 -> sealed interface OrderStatus + record 구현체로 상태마다 필요한 데이터만 보유
 * String 분기 -> switch (status) + 패턴 매칭으로 컴파일 타임 안전성 확보
 */
public class Day110OrderModel {
    static record OrderItem(String productId, int quantity, int unitPrice) {
        OrderItem {
            if (productId == null || productId.isBlank())
                throw new IllegalArgumentException("productId는 필수입니다.");
            if (quantity < 1)
                throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
            if (unitPrice <= 0)
                throw new IllegalArgumentException("단가는 0보다 커야 합니다.");
        }

        public int totalPrice() {
            return quantity * unitPrice;
        }
    }


    static record Order(String orderId, OrderItem item, OrderStatus status) {

        public String describe() {
            if (status instanceof Pending) {
                return orderId + " 결제 대기 중";
            } else if (status instanceof Paid) {
                return orderId + " 결제 완료";
            } else if (status instanceof Cancelled c) {
                return orderId + " 취소됨: " + c.reason();
            } else if (status instanceof Refunded r) {
                return orderId + " 환불됨: " + r.refundAmount() + "원";
            }
            return "알 수 없는 상태";
        }
    }



    static sealed interface OrderStatus permits Pending, Paid, Cancelled, Refunded { }
    record Pending() implements OrderStatus {}
    record Paid() implements OrderStatus {}
    record Cancelled(String reason)     implements OrderStatus {}
    record Refunded(int refundAmount)   implements OrderStatus {}


    public static void main(String[] args) {
        OrderItem item = new OrderItem("P001", 2, 15000);
        Order o1 = new Order("ORD-001", item, new Paid());
        Order o2 = new Order("ORD-002", item, new Cancelled("단순 변심"));
        Order o3 = new Order("ORD-003", item, new Refunded(30000));

        System.out.println(o1.describe());
        System.out.println(o2.describe());
        System.out.println(o3.describe());
    }
}




