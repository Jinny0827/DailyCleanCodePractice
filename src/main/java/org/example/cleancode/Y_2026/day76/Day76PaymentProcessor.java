package org.example.cleancode.Y_2026.day76;

/**
 * Day 76 - 결제 시스템 리팩터링
 *
 * SRP 위반: 하나의 메서드가 너무 많은 책임
 * OCP 위반: 새 결제 방식 추가 시 기존 코드 수정 필요
 * 복잡한 조건문: if-else 체인으로 가독성 저하
 * 중복 코드: 수수료 계산과 검증 로직 반복
 * 하드코딩: 수수료율이 코드에 직접 embedded
 */
public class Day76PaymentProcessor {
    public String processPayment(String paymentType, double amount, String cardNumber,
                                 String bankAccount, String walletId, String currency) {

        if (amount <= 0) {
            return "ERROR: Invalid amount";
        }

        String result = "";

        if (paymentType.equals("CREDIT_CARD")) {
            if (cardNumber == null || cardNumber.length() != 16) {
                return "ERROR: Invalid card number";
            }
            double fee = amount * 0.03;
            double total = amount + fee;
            result = "Credit card payment: $" + total + " (includes 3% fee)";

        } else if (paymentType.equals("BANK_TRANSFER")) {
            if (bankAccount == null || bankAccount.isEmpty()) {
                return "ERROR: Invalid bank account";
            }
            double fee = 5.0;
            double total = amount + fee;
            result = "Bank transfer: $" + total + " (includes $5 fee)";

        } else if (paymentType.equals("DIGITAL_WALLET")) {
            if (walletId == null || walletId.isEmpty()) {
                return "ERROR: Invalid wallet ID";
            }
            double fee = amount * 0.01;
            double total = amount + fee;
            result = "Digital wallet payment: $" + total + " (includes 1% fee)";

        } else {
            return "ERROR: Unsupported payment type";
        }

        // 결제 로그
        System.out.println("Processing: " + result);

        return "SUCCESS: " + result;
    }
}
