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
    public PaymentResult processPayment(String paymentType, double amount, String cardNumber,
                                 String bankAccount, String walletId, String currency) {
        
        // 금액 검증
        if (amount <= 0) {
            return PaymentResult.failure("ERROR: Invalid amount");
        }

        try {

            // 전략 생성
            PaymentStrategy strategy = PaymentStrategyFactory.createStrategy(paymentType);
            
            // 결제 데이터 생성
            PaymentData paymentData = new PaymentData(cardNumber, bankAccount, walletId, currency);

            // 전략 실행
            PaymentResult result = strategy.process(amount, paymentData);

            if(result.isSuccess()) {
                System.out.println("Processing: " + result.getMessage());
            }

            return result;

        } catch (IllegalArgumentException e) {
            return PaymentResult.failure(e.getMessage());
        }
    }
}


// 결제 결과 객체
class PaymentResult {
       private final boolean success;
       private final String message;
       private final double totalAmount;

    public PaymentResult(boolean success, String message, double totalAmount) {
        this.success = success;
        this.message = message;
        this.totalAmount = totalAmount;
    }

    public static PaymentResult success(String message, double totalAmount) {
        return new PaymentResult(true, message, totalAmount);
    }

    public static PaymentResult failure(String message) {
        return new PaymentResult(false, message, 0.0);
    }


    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public double getTotalAmount() {
        return totalAmount;
    }
}

// 전략 패턴 팩터리
class PaymentStrategyFactory {
    public static PaymentStrategy createStrategy(String paymentType) {
        return switch (paymentType) {
            case "CREDIT_CARD" -> new CreditCardPaymentStrategy();
            case "BANK_TRANSFER" -> new BankTransferPaymentStrategy();
            case "DIGITAL_WALLET" -> new DigitalWalletPaymentStrategy();
            default -> throw new IllegalArgumentException("ERROR: Unsupported payment type");
        };
    }
}

interface PaymentStrategy {
    // 결제 처리
    PaymentResult process(double amount, PaymentData paymentData);
    
    // 결제 방식별 검증 담당 메서드
    boolean isValid(PaymentData paymentData);
    
    // 수수료 계산 메서드
    double calculateFee(double amount);
}

// 신용카드 전략 패턴
class CreditCardPaymentStrategy implements PaymentStrategy {
    private static final double FEE_RATE = 0.03;

    @Override
    public PaymentResult process(double amount, PaymentData paymentData) {
        if (!isValid(paymentData)) {
            return PaymentResult.failure("ERROR: Invalid card number");
        }

        double fee = calculateFee(amount);
        double totalAmount = amount + fee;
        String message = "Credit card payment: $" + totalAmount + " (includes 3% fee)";

        return PaymentResult.success(message, totalAmount);
    }

    @Override
    public boolean isValid(PaymentData paymentData) {
        String cardNumber = paymentData.getCardNumber();
        return cardNumber != null && cardNumber.length() == 16;
    }

    @Override
    public double calculateFee(double amount) {
        return amount * FEE_RATE;
    }
}

// 계좌 이체
class BankTransferPaymentStrategy implements PaymentStrategy {
    private static final double FIXED_FEE = 5.0;

    @Override
    public PaymentResult process(double amount, PaymentData paymentData) {
        if (!isValid(paymentData)) {
            return PaymentResult.failure("ERROR: Invalid bank account");
        }

        double fee = calculateFee(amount);
        double totalAmount = amount + fee;
        String message = "Bank transfer: $" + totalAmount + " (includes $5 fee)";

        return PaymentResult.success(message, totalAmount);
    }

    @Override
    public boolean isValid(PaymentData paymentData) {
        String bankAccount = paymentData.getBankAccount();
        return bankAccount != null && !bankAccount.isEmpty();
    }

    @Override
    public double calculateFee(double amount) {
        return FIXED_FEE;
    }
}

// 디지털 지갑 전략
class DigitalWalletPaymentStrategy implements PaymentStrategy {
    private static final double FEE_RATE = 0.01;

    @Override
    public PaymentResult process(double amount, PaymentData paymentData) {
        if (!isValid(paymentData)) {
            return PaymentResult.failure("ERROR: Invalid wallet ID");
        }

        double fee = calculateFee(amount);
        double totalAmount = amount + fee;
        String message = "Digital wallet payment: $" + totalAmount + " (includes 1% fee)";

        return PaymentResult.success(message, totalAmount);
    }

    @Override
    public boolean isValid(PaymentData paymentData) {
        String walletId = paymentData.getWalletId();
        return walletId != null && !walletId.isEmpty();
    }

    @Override
    public double calculateFee(double amount) {
        return amount * FEE_RATE;
    }
}


class PaymentData {
    private final String cardNumber;
    private final String bankAccount;
    private final String walletId;
    private final String currency;

    public PaymentData(String cardNumber, String bankAccount, String walletId, String currency) {
        this.cardNumber = cardNumber;
        this.bankAccount = bankAccount;
        this.walletId = walletId;
        this.currency = currency;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public String getWalletId() {
        return walletId;
    }

    public String getCurrency() {
        return currency;
    }
}


