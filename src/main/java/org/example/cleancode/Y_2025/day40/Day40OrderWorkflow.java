package org.example.cleancode.Y_2025.day40;


/**
 * Day 40: 주문 상태 관리 시스템
 *
 * 문제점:
 * - 상태 전이 로직이 if-else로 흩어져 있음
 * - 잘못된 상태 전이 방지 불가
 * - 상태별 행동이 명확하지 않음
 * - 새로운 상태 추가 시 여러 곳 수정 필요
 */

public class Day40OrderWorkflow {

    public static void main(String[] args) {
        OrderContext order = new OrderContext("ORD-001", 50000);

        // 주문 처리
        order.confirm();
        order.pay();
        order.ship();
        order.deliver();

        // 잘못된 상태 전이 시도
        System.out.println("\n=== 잘못된 상태 전이 시도 ===");
        OrderContext order2 = new OrderContext("ORD-002", 30000);
        try {
            // 예외 발생 상태
            order2.ship();
        } catch (IllegalStateException e) {
            System.out.println("❌ " + e.getMessage());
        }
    }

}

// 주문 상태 인터페이스
interface OrderState {
    // 상태별 실행할 액션
    void confirm(OrderContext context);
    void pay(OrderContext context);
    void ship(OrderContext context);
    void deliver(OrderContext context);
    void cancel(OrderContext context);

    String getStateName();
}

class OrderContext {
    private String orderId;
    private int amount;
    private OrderState currentState;

    public OrderContext(String orderId, int amount) {
        this.orderId = orderId;
        this.amount = amount;
        // 초기 상태 고정
        this.currentState = new PendingState();
    }

    public void setState(OrderState newState) {
        System.out.println("상태 전이: " + currentState.getStateName()
                + " → " + newState.getStateName());
        this.currentState = newState;
    }

    // 외부 노출 액션들
    public void confirm() {
        currentState.confirm(this);
    }

    public void pay() {
        currentState.pay(this);
    }

    public void ship() {
        currentState.ship(this);
    }

    public void deliver() {
        currentState.deliver(this);
    }

    public void cancel() {
        currentState.cancel(this);
    }

    public int getAmount() {
        return amount;
    }
}

// 기본 상태 추상 클래스 (잘못된 전이에 대한 기본 처리)
abstract class AbstractOrderState implements OrderState {

    @Override
    public void confirm(OrderContext context) {
        throw new IllegalStateException(
                getStateName() + " 상태에서는 confirm을 할 수 없습니다");
    }

    @Override
    public void pay(OrderContext context) {
        throw new IllegalStateException(
                getStateName() + " 상태에서는 pay를 할 수 없습니다");
    }

    @Override
    public void ship(OrderContext context) {
        throw new IllegalStateException(
                getStateName() + " 상태에서는 ship을 할 수 없습니다");
    }

    @Override
    public void deliver(OrderContext context) {
        throw new IllegalStateException(
                getStateName() + " 상태에서는 deliver를 할 수 없습니다");
    }

    @Override
    public void cancel(OrderContext context) {
        throw new IllegalStateException(
                getStateName() + " 상태에서는 cancel을 할 수 없습니다");
    }
    
}

// 대기 상태
class PendingState extends AbstractOrderState {

    @Override
    public String getStateName() {
        return "PENDING";
    }

    @Override
    public void confirm(OrderContext context) {
        System.out.println("✓ 주문 확인됨");
        context.setState(new ConfirmedState());
    }

    @Override
    public void cancel(OrderContext context) {
        System.out.println("✓ 주문 취소됨");
        context.setState(new CancelledState());
    }
}

// 확인된 상태
class ConfirmedState extends AbstractOrderState {

    @Override
    public String getStateName() {
        return "CONFIRMED";
    }

    @Override
    public void pay(OrderContext context) {
        System.out.println("💳 결제 처리 중...");
        System.out.println("✓ 결제 완료");
        context.setState(new PaidState());
    }

    @Override
    public void cancel(OrderContext context) {
        System.out.println("✓ 주문 취소됨");
        context.setState(new CancelledState());
    }
}

// 결제 완료 상태
class PaidState extends AbstractOrderState {

    @Override
    public String getStateName() {
        return "PAID";
    }

    @Override
    public void ship(OrderContext context) {
        System.out.println("📦 배송 업체 통보");
        System.out.println("✓ 배송 시작");
        context.setState(new ShippedState());
    }

    @Override
    public void cancel(OrderContext context) {
        System.out.println("💰 환불 처리");
        System.out.println("✓ 주문 취소됨");
        context.setState(new CancelledState());
    }
}

// 배송 중 상태
class ShippedState extends  AbstractOrderState {
    @Override
    public String getStateName() {
        return "SHIPPED";
    }

    @Override
    public void deliver(OrderContext context) {
        System.out.println("✓ 배송 완료");
        context.setState(new DeliveredState());
    }

    @Override
    public void cancel(OrderContext context) {
        System.out.println("💰 환불 처리");
        System.out.println("✓ 주문 취소됨 (배송 중단)");
        context.setState(new CancelledState());
    }
}



// 취소 상태
class CancelledState extends AbstractOrderState {

    @Override
    public String getStateName() {
        return "CANCELLED";
    }
}

//배송 완료 상태
class DeliveredState extends AbstractOrderState {

    @Override
    public String getStateName() {
        return "DELIVERED";
    }
    
}
