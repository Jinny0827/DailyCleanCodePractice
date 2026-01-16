package org.example.cleancode.day53;

/**
 * Day 53: 결제 시스템 리팩터링
 *
 */
// 비지니스 로직 객체
public class Day53PaymentProcessor {
    private PaymentStrategy strategy;

    public Day53PaymentProcessor(PaymentStrategy strategy) {
        this.strategy = strategy;
    }

    public PaymentResult processPayment(Order order) {
        return strategy.process(order);
    }
}

// 실행 객체
class Main {
    public static void main(String[] args) {
        Order order = new Order("ORD001", 100.0);
        
        // Strategy 생성
        PaymentStrategy cardStrategy = new CardPaymentStrategy("1234567890123456");

        // Process에 주입
        Day53PaymentProcessor processor = new Day53PaymentProcessor(cardStrategy);
        
        // 결제처리
        PaymentResult result = processor.processPayment(order);

        System.out.println("Success: " + result.isSuccess());
        System.out.println("Transaction ID: " + result.getTransactionId());
    }
}

// 결제 전략 생성
interface PaymentStrategy {
    // 결제 검증
    boolean validate();
    
    // 수수료율 산정 및 반환
    double getFeeRate();
    
    // 실제 결제 처리
    PaymentResult process(Order order);

    String getPaymentMethodName();
}

// 신용카드 결제
class CardPaymentStrategy implements PaymentStrategy {
    private String cardNumber;

    public CardPaymentStrategy(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    @Override
    public boolean validate() {
        return cardNumber != null && cardNumber.length() == 16;
    }

    @Override
    public double getFeeRate() {

        return 0.029;
    }

    @Override
    public PaymentResult process(Order order) {

        // 검증
        if(!validate()) {
            return new PaymentResult(false, "Invalid card", null);
        }

        // 수수료 계산
        double fee = order.getAmount() * getFeeRate();
        double total = order.getAmount() + fee;
        System.out.println("Processing card payment: $" + total);
        
        // 결제 처리 (시뮬레이션)
        boolean result = Math.random() > 0.1;
        if(result) {
            order.setStatus("paid");
            order.setPaymentMethod(getPaymentMethodName());
            return new PaymentResult(true, null, "TXN" + System.currentTimeMillis());
        }

        return new PaymentResult(false, "Card declined", null);
    }

    @Override
    public String getPaymentMethodName() {
        return "Card";
    }
}

// Paypal 결제
class PayPalPaymentStrategy implements PaymentStrategy {
    private String email;

    public PayPalPaymentStrategy(String email) {
        this.email = email;
    }

    @Override
    public boolean validate() {
        return email != null && email.contains("@");
    }

    @Override
    public double getFeeRate() {
        return 0.034;
    }

    @Override
    public PaymentResult process(Order order) {

        if(!validate()) {
            return new PaymentResult(false, "Invalid email", null);
        }

        // 수수료 계산
        double fee = order.getAmount() * getFeeRate();
        double total = order.getAmount() + fee;
        System.out.println("Processing PayPal payment: $" + total);

        // 결제 처리 (시뮬레이션)
        boolean result = Math.random() > 0.1;
        if (result) {
            order.setStatus("paid");
            order.setPaymentMethod(getPaymentMethodName());
            return new PaymentResult(true, null, "PP" + System.currentTimeMillis());
        }

        return new PaymentResult(false, "PayPal failed", null);
    }

    @Override
    public String getPaymentMethodName() {
        return "PayPal";
    }
}

// 전화 결제
class PhonePaymentStrategy implements PaymentStrategy {
    private String phoneNumber;


    public PhonePaymentStrategy(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public boolean validate() {
        return phoneNumber != null && phoneNumber.length() >= 10;
    }

    @Override
    public double getFeeRate() {
        return 0.045;
    }

    @Override
    public PaymentResult process(Order order) {
        // 검증 실패 시
        if (!validate()) {
            return new PaymentResult(false, "Invalid phone", null);
        }

        // 수수료 계산
        double fee = order.getAmount() * getFeeRate();
        double total = order.getAmount() + fee;  // ✅ 덧셈!
        System.out.println("Processing phone payment: $" + total);

        // 결제 처리 (시뮬레이션)
        boolean result = Math.random() > 0.1;
        if (result) {
            order.setStatus("paid");
            order.setPaymentMethod(getPaymentMethodName());
            // PH 접두사로 트랜잭션 ID 생성
            return new PaymentResult(true, null, "PH" + System.currentTimeMillis());
        }

        return new PaymentResult(false, "Phone payment failed", null);
    }

    @Override
    public String getPaymentMethodName() {
        return "Phone Pay";
    }
}




// 주문 객체
class Order {
    private String id;
    private double amount;
    private String status;
    private String paymentMethod;

    public Order(String id, double amount) {
        this.id = id;
        this.amount = amount;
        this.status = "pending";
    }

    public String getId() { return id; }
    public double getAmount() { return amount; }
    public String getStatus() { return status; }
    public String getPaymentMethod() { return paymentMethod; }

    public void setStatus(String status) { this.status = status; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}

// 결과 전달 객체
class PaymentResult {
    private boolean success;
    private String error;
    private String transactionId;

    public PaymentResult(boolean success, String error, String transactionId) {
        this.success = success;
        this.error = error;
        this.transactionId = transactionId;
    }

    public boolean isSuccess() { return success; }
    public String getError() { return error; }
    public String getTransactionId() { return transactionId; }
}