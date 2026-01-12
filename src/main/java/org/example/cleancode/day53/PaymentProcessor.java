package org.example.cleancode.day53;

/**
 * Day 53: 결제 시스템 리팩터링
 *
 */
// 비지니스 로직 객체
public class PaymentProcessor {

    public PaymentResult processPayment(Order order, String paymentType,
                                        String cardNumber, String email, String phoneNumber) {
        if (paymentType.equals("card")) {
            if (cardNumber == null || cardNumber.length() != 16) {
                return new PaymentResult(false, "Invalid card", null);
            }
            double fee = order.getAmount() * 0.029;
            double total = order.getAmount() + fee;
            System.out.println("Processing card payment: $" + total);
            boolean result = Math.random() > 0.1;
            if (result) {
                order.setStatus("paid");
                order.setPaymentMethod("card");
                return new PaymentResult(true, null, "TXN" + System.currentTimeMillis());
            }
            return new PaymentResult(false, "Card declined", null);

        } else if (paymentType.equals("paypal")) {
            if (email == null || !email.contains("@")) {
                return new PaymentResult(false, "Invalid email", null);
            }
            double fee = order.getAmount() * 0.034;
            double total = order.getAmount() + fee;
            System.out.println("Processing PayPal payment: $" + total);
            boolean result = Math.random() > 0.1;
            if (result) {
                order.setStatus("paid");
                order.setPaymentMethod("paypal");
                return new PaymentResult(true, null, "PP" + System.currentTimeMillis());
            }
            return new PaymentResult(false, "PayPal failed", null);

        } else if (paymentType.equals("phone")) {
            if (phoneNumber == null || phoneNumber.length() < 10) {
                return new PaymentResult(false, "Invalid phone", null);
            }
            double fee = order.getAmount() * 0.045;
            double total = order.getAmount() + fee;
            System.out.println("Processing phone payment: $" + total);
            boolean result = Math.random() > 0.1;
            if (result) {
                order.setStatus("paid");
                order.setPaymentMethod("phone");
                return new PaymentResult(true, null, "PH" + System.currentTimeMillis());
            }
            return new PaymentResult(false, "Phone payment failed", null);
        }
        return new PaymentResult(false, "Unknown payment type", null);
    }

}

// 실행 객체
class Main {
    public static void main(String[] args) {
        PaymentProcessor processor = new PaymentProcessor();
        Order order = new Order("ORD001", 100.0);

        PaymentResult result = processor.processPayment(
                order, "card", "1234567890123456", null, null
        );

        System.out.println("Success: " + result.isSuccess());
        System.out.println("Transaction ID: " + result.getTransactionId());
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