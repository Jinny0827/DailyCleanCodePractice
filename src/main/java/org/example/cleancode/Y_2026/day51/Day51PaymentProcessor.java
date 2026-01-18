package org.example.cleancode.Y_2026.day51;

/**
 *
 * Day 51 - 결제 정책 리팩터링
 *
 */

public class Day51PaymentProcessor {

    public void processPayment(PaymentType paymentType, double amount, CustomerLevel level) {
        // 전략 선택 (결제 수단에 따른)
        PaymentStrategy strategy = PaymentStrategyFactory.create(paymentType);

        // 결제 실행
        PaymentResult result = strategy.execute(amount, level);

        // 결과 출력
        printPaymentInfo(strategy, result, level);

        // DB 저장
        saveToDatabase(result);

        // 포인트 적립
        earnPoints(result);
        
    }

    // 결과 출력 메서드
    private void printPaymentInfo(PaymentStrategy strategy, PaymentResult result, CustomerLevel level) {

        System.out.printf("%s %s 결제: %.2f원 (수수료: %.2f원)%n",
                level.name(),
                strategy.getMethodName(),
                result.getFinalAmount(),
                result.getFee());
    }
    
    
    // 데이터 베이스 저장
    private void saveToDatabase(PaymentResult result) {
        // 가상
        System.out.println("DB 저장 완료");
    }

    // 포인트 적립
    private void earnPoints(PaymentResult result) {
        if(result.getPointRate() > 0) {
            double points = result.getFinalAmount() * result.getPointRate();
            System.out.printf("포인트 %.0f점 적립%n", points);
        }
    }

}

// 고객 등급 상수값 지정
enum CustomerLevel {

    VIP(0.01, 0.03,    // 신용카드: 수수료 1%, 포인트 3%
            0.0, 0.02),    // 계좌이체: 수수료 0%, 포인트 2%

    REGULAR(0.02, 0.01,    // 신용카드: 수수료 2%, 포인트 1%
            0.005, 0.005); // 계좌이체: 수수료 0.5%, 포인트 0.5%

    private final double creditCardFeeRate;
    private final double creditCardPointRate;
    private final double bankTransferFeeRate;
    private final double bankTransferPointRate;

    CustomerLevel(double ccFee, double ccPoint,
                  double btFee, double btPoint) {
        this.creditCardFeeRate = ccFee;
        this.creditCardPointRate = ccPoint;
        this.bankTransferFeeRate = btFee;
        this.bankTransferPointRate = btPoint;
    }

    public double getCreditCardFeeRate() { return creditCardFeeRate; }
    public double getCreditCardPointRate() { return creditCardPointRate; }
    public double getBankTransferFeeRate() { return bankTransferFeeRate; }
    public double getBankTransferPointRate() { return bankTransferPointRate; }
}

// 결제 수단 enum
enum PaymentType {
    CREDIT_CARD,
    BANK_TRANSFER,
    CRYPTO
}


interface PaymentStrategy {
    PaymentResult execute(double amount, CustomerLevel level);
    String getMethodName();
}

class CreditCardPayment implements PaymentStrategy {

    @Override
    public PaymentResult execute(double amount, CustomerLevel level) {
        double fee = amount * level.getCreditCardFeeRate();
        double pointRate = level.getCreditCardPointRate();

        return new PaymentResult(amount, fee, pointRate);
    }

    @Override
    public String getMethodName() {
        return "신용카드";
    }
}

class BankTransferPayment implements PaymentStrategy {
    @Override
    public PaymentResult execute(double amount, CustomerLevel level) {
        double fee = amount * level.getBankTransferFeeRate();
        double pointRate = level.getBankTransferPointRate();

        return new PaymentResult(amount, fee, pointRate);
    }

    @Override
    public String getMethodName() {
        return "계좌이체";
    }
}

class CryptoPayment  implements PaymentStrategy {
    private static final double FEE_RATE = 0.03;
    private static final double POINT_RATE = 0.0;

    @Override
    public PaymentResult execute(double amount, CustomerLevel level) {
        // 암호화폐는 등급 미상관
        double fee = amount * FEE_RATE;
        return new PaymentResult(amount, fee, POINT_RATE);
    }

    @Override
    public String getMethodName() {
        return "암호화폐";
    }
}

// 팩터리 메서드 정의
class PaymentStrategyFactory {
    public static PaymentStrategy create(PaymentType type) {
        switch (type) {
            case CREDIT_CARD:
                return new CreditCardPayment();
            case BANK_TRANSFER:
                return new BankTransferPayment();
            case CRYPTO:
                return new CryptoPayment();
            default:
                throw new IllegalArgumentException("지원하지 않는 결제 수단: " + type);
        }
    }
}



// 결제 결과 전달 객체
class PaymentResult {
    private final double originalAmount;
    private final double fee;
    private final double finalAmount;
    private final double pointRate;

    public PaymentResult(double originalAmount, double fee, double pointRate) {
        this.originalAmount = originalAmount;
        this.fee = fee;
        this.pointRate = pointRate;
        this.finalAmount = originalAmount + fee;
    }

    public double getOriginalAmount() {
        return originalAmount;
    }

    public double getFee() {
        return fee;
    }

    public double getFinalAmount() {
        return finalAmount;
    }

    public double getPointRate() {
        return pointRate;
    }
}
