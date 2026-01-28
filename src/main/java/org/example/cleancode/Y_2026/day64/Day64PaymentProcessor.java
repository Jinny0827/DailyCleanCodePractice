package org.example.cleancode.Y_2026.day64;

import org.example.cleancode.Y_2025.day48.PaymentProcessor;

import java.util.HashMap;
import java.util.Map;

/**
 * Day 64: 결제 처리 시스템 리팩터링
 *
 * 전략 패턴 - 결제 방식별 클래스 분리
 * OCP 위반 - 새 결제 수단 추가 시 기존 코드 수정 필요
 * SRP 위반 - 검증, 수수료 계산, 처리가 한 곳에
 * 매직 넘버 - 수수료율 하드코딩
 */

public class Day64PaymentProcessor {
    private final Map<String, PaymentMethod> paymentMethods;

    public Day64PaymentProcessor() {
        paymentMethods = new HashMap<>();
        paymentMethods.put("CARD", new CardPayment());
        paymentMethods.put("BANK", new BankPayment());
        paymentMethods.put("CRYPTO", new CryptoPayment());
        paymentMethods.put("PAYPAL", new PayPalPayment());
    }

    public PaymentResult processPayment(String type, PaymentInfo info) {
        PaymentMethod paymentMethod = paymentMethods.get(type);

        if(paymentMethod == null) {
            return PaymentResult.failure("Error: Unknown payment type");
        }

        return paymentMethod.process(info);
    }

    public static void main(String[] args) {
        

        Day64PaymentProcessor paymentProcessor = new Day64PaymentProcessor();

        // 카드 결제 테스트
        CardPaymentInfo cardInfo = new CardPaymentInfo(100, "1234567890123456");
        PaymentResult cardResult = paymentProcessor.processPayment("CARD", cardInfo);

        // 페이팔 결제 테스트
        PayPalPaymentInfo payPalPaymentInfo = new PayPalPaymentInfo(100, "aasdf@naver.com");
        PaymentResult paypalResult = paymentProcessor.processPayment("PAYPAL", payPalPaymentInfo);
        
    }
}



// 결제 전략 패턴
interface PaymentMethod {
    // 결제수단 검증
    boolean validate(PaymentInfo paymentInfo);

    // 수수료 계산
    double calculate(double amount);

    // 실제 결제 처리
    PaymentResult process(PaymentInfo paymentInfo);
}

// 카드 결제 구현체
class CardPayment implements PaymentMethod {
    // 수수료 상수화
    private static final double FEE_RATE = 0.03;

    @Override
    public boolean validate(PaymentInfo paymentInfo) {
        // 같은 카드 타입인지 확인
        if(!(paymentInfo instanceof CardPaymentInfo)) {
            return false;
        }

        CardPaymentInfo cardPaymentInfo = (CardPaymentInfo) paymentInfo;
        String cardNumber = cardPaymentInfo.getCardNumber();

        if (cardNumber == null || cardNumber.length() != 16) {
            return false;
        }

        return true;
    }

    @Override
    public double calculate(double amount) {
        return amount * FEE_RATE;
    }

    @Override
    public PaymentResult process(PaymentInfo paymentInfo) {

        // 검증
        if (!validate(paymentInfo)) {
            return PaymentResult.failure("Error: Invalid card");
        }
    
        // 수수료 계산
        double amount = paymentInfo.getAmount();
        double fee = calculate(amount);
        double total = amount + fee;

        System.out.println("Card payment: " + total);
        return PaymentResult.success(total, "Card payment successful: " + total);
    }
}

class BankPayment implements PaymentMethod {
    private static final double FEE_RATE = 0.01;

    @Override
    public boolean validate(PaymentInfo paymentInfo) {
        if(!(paymentInfo instanceof BankPaymentInfo)) {
            return false;
        }

        BankPaymentInfo bankPaymentInfo = (BankPaymentInfo) paymentInfo;
        String accountNumber = bankPaymentInfo.getAccountNumber();

        if (accountNumber == null || accountNumber.length() != 12) {
            return false;
        }

        return true;
    }

    @Override
    public double calculate(double amount) {
        return amount * FEE_RATE;
    }

    @Override
    public PaymentResult process(PaymentInfo paymentInfo) {
        if (!validate(paymentInfo)) {
            return PaymentResult.failure("Error: Invalid bank");
        }

        // 수수료 계산
        double amount = paymentInfo.getAmount();
        double fee = calculate(amount);
        double total = amount + fee;

        System.out.println("Bank payment: " + total);
        return PaymentResult.success(total, "Bank payment successful: " + total);
    }
}

class CryptoPayment implements PaymentMethod {
    private static final double FEE_RATE = 0.05;

    @Override
    public boolean validate(PaymentInfo paymentInfo) {
        if(!(paymentInfo instanceof CryptoPaymentInfo)) {
            return false;
        }

        CryptoPaymentInfo cryptoPaymentInfo = (CryptoPaymentInfo) paymentInfo;
        String cryptoAddress = cryptoPaymentInfo.getCryptoAddress();

        if (cryptoAddress == null || !cryptoAddress.startsWith("0x")) {
            return false;
        }

        return true;
    }

    @Override
    public double calculate(double amount) {
        return amount * FEE_RATE;
    }

    @Override
    public PaymentResult process(PaymentInfo paymentInfo) {
        if (!validate(paymentInfo)) {
            return PaymentResult.failure("Error: Invalid Crypto");
        }

        // 수수료 계산
        double amount = paymentInfo.getAmount();
        double fee = calculate(amount);
        double total = amount + fee;

        System.out.println("Crypto payment: " + total);
        return PaymentResult.success(total, "Crypto payment successful: " + total);
    }
}

// Paypal 결제 테스트 위한 메서드 구현
class PayPalPayment implements PaymentMethod {
    private static final double FEE_RATE = 0.02;

    @Override
    public boolean validate(PaymentInfo paymentInfo) {
        if(!(paymentInfo instanceof PayPalPaymentInfo)) {
            return false;
        }

        PayPalPaymentInfo payPalPaymentInfo = (PayPalPaymentInfo) paymentInfo;
        String paypalEmail = payPalPaymentInfo.getEmail();

        if(paypalEmail == null || !paypalEmail.contains("@")) {
            return false;
        }

        return true;
    }

    @Override
    public double calculate(double amount) {
        return amount * FEE_RATE;
    }

    @Override
    public PaymentResult process(PaymentInfo paymentInfo) {
        if (!validate(paymentInfo)) {
            return PaymentResult.failure("Error: Invalid Paypal");
        }

        double amount = paymentInfo.getAmount();
        double fee = calculate(amount);
        double total = amount + fee;

        System.out.println("Paypal payment: " + total);
        return PaymentResult.success(total, "Paypal payment successful: " + total);
    }
}



// 결제 요청 부분 구현체
abstract class PaymentInfo {
    private final double amount;

    public PaymentInfo(double amount) {
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }
}

// 카드 결제 구현체
class CardPaymentInfo extends PaymentInfo {
    private final String cardNumber;

    public CardPaymentInfo(double amount, String cardNumber) {
        super(amount);
        this.cardNumber = cardNumber;
    }

    public String getCardNumber() {
        return cardNumber;
    }
}

// 계좌 이체 구현체
class BankPaymentInfo extends PaymentInfo {
    private final String accountNumber;

    public BankPaymentInfo(double amount, String accountNumber) {
        super(amount);
        this.accountNumber = accountNumber;
    }

    public String getAccountNumber() {
        return accountNumber;
    }
}

// 암호화 결제
class CryptoPaymentInfo extends PaymentInfo {
    private final String cryptoAddress;

    public CryptoPaymentInfo(double amount, String cryptoAddress) {
        super(amount);
        this.cryptoAddress = cryptoAddress;
    }

    public String getCryptoAddress() {
        return cryptoAddress;
    }
}


// Paypal 결제 테스트를 위한 클래스
class PayPalPaymentInfo extends PaymentInfo {
    private final String email;

    public PayPalPaymentInfo(double amount, String email) {
        super(amount);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}



// 결제 결과 객체
class PaymentResult {
    private final boolean success;
    private final double amount;
    private final String message;

    public PaymentResult(boolean success, double amount, String message) {
        this.success = success;
        this.amount = amount;
        this.message = message;
    }

    public static PaymentResult success(double amount, String message) {
        return new PaymentResult(true, amount, message);
    }

    public static PaymentResult failure(String message) {
        return new PaymentResult(false, 0, message);
    }


    public boolean isSuccess() {
        return success;
    }

    public double getAmount() {
        return amount;
    }

    public String getMessage() {
        return message;
    }
}


