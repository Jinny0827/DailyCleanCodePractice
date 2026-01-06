package org.example.cleancode.day48;

/**
 * Day 48 - 레거시 결제 시스템 통합
 *
 * * 학습 목표 *
 * 어댑터 패턴과 전략 패턴 동시 적용
 * 의존성 역전 원칙으로 테스트 가능성 확보
 * 레거시 코드 점진적 개선
 */
public class PaymentProcessor {
    private final PaymentStrategy strategy;

    public static void main(String[] args) {
        // 방법 1: 직접 주입
        PaymentStrategy creditCard = new CreditCardPaymentAdapter(new LegacyCreditCardSystem());
        PaymentProcessor processor1 = new PaymentProcessor(creditCard);
        PaymentResult result1 = processor1.processPayment(100.0, "1234-5678,123,1225");
        System.out.println("신용카드 : " + result1.getMessage());

        // 계좌이체 테스트
        PaymentStrategy bank = new BankTransferPaymentAdapter(new LegacyBankSystem());
        PaymentProcessor processor2 = new PaymentProcessor(bank);
        PaymentResult result2 = processor2.processPayment(200.0, "9876543210,1234");
        System.out.println("계좌이체: " + result2.getMessage());

        // 모바일 결제 테스트
        PaymentStrategy mobile = new MobilePaymentAdapter(new MobilePayAPI());
        PaymentProcessor processor3 = new PaymentProcessor(mobile);
        PaymentResult result3 = processor3.processPayment(300.0, "user@example.com");
        System.out.println("모바일: " + result3.getMessage());



        // 방법 2 : 팩터리 패턴 적용
        PaymentProcessor processor4 = new PaymentProcessor(
                PaymentStrategyFactory.create("BANK_TRANSFER")
        );
        PaymentResult result4 = processor4.processPayment(200.0, "9876543210,1234");
        System.out.println("계좌이체: " + result4.getMessage());
    }

    public PaymentProcessor(PaymentStrategy strategy) {
       this.strategy = strategy;
    }

    public PaymentResult processPayment(double amount, String customerInfo) {
        return strategy.process(amount, customerInfo);
    }

}

// 팩터리 패턴으로 결제 전략 호출
class PaymentStrategyFactory {
    public static PaymentStrategy create(String type) {
        switch (type) {
            case "CREDIT_CARD":
                return new CreditCardPaymentAdapter(new LegacyCreditCardSystem());
            case "BANK_TRANSFER":
                return new BankTransferPaymentAdapter(new LegacyBankSystem());
            case "MOBILE_PAY":
                return new MobilePaymentAdapter(new MobilePayAPI());
            default:
                throw new IllegalArgumentException("Unknown payment type: " + type);
        }
    }
}


// 어댑터 패턴으로 레거시 시스템을 감싸서 다형화
interface PaymentStrategy {
    PaymentResult process(double amount, String customerInfo);
}

// 신용카드 결제의 레거시 시스템을 감싸는 객체
class CreditCardPaymentAdapter implements PaymentStrategy {
    private final LegacyCreditCardSystem legacySystem;

    public CreditCardPaymentAdapter(LegacyCreditCardSystem legacySystem) {
        this.legacySystem = legacySystem;
    }

    @Override
    public PaymentResult process(double amount, String customerInfo) {
        try {
            // info[0]: 카드번호, info[1]: CVV, info[2]: 만료일
            String[] info = customerInfo.split(",");

            int cents = (int)(amount * 100);
            int result = legacySystem.authorize(
                    info[0], info[1], Integer.parseInt(info[2]), cents
            );

            if(result == 1) {
                legacySystem.capture(info[0]);
                return PaymentResult.success();
            }

            return PaymentResult.failure("Credit card authorization failed");
        } catch (Exception e) {
            return PaymentResult.failure("Invalid credit card info: " + e.getMessage());
        }
    }
}

// 계좌이체의 레거시 시스템을 감싸는 객체
class BankTransferPaymentAdapter implements PaymentStrategy {
    private final LegacyBankSystem legacySystem;

    public BankTransferPaymentAdapter(LegacyBankSystem legacySystem) {
        this.legacySystem = legacySystem;
    }

    @Override
    public PaymentResult process(double amount, String customerInfo) {
        try {
            // info[0]: 계좌번호, info[1]: PIN
            String[] info = customerInfo.split(",");

            boolean auth = legacySystem.verifyAccount(info[0], info[1]);

            if(!auth) {
                return PaymentResult.failure("Bank account verification failed");
            }

            String txId = legacySystem.initiateTransfer(info[0], amount);
            int status = legacySystem.confirmTransfer(txId);

            if(status == 200) {
                return PaymentResult.success();
            }

            return PaymentResult.failure("Bank transfer failed with status: " + status);

        } catch (Exception e) {
            return PaymentResult.failure("Invalid bank info: " + e.getMessage());
        }
    }
}

// 모바일 결제의 레거시 시스템 감싸는 객체
class MobilePaymentAdapter implements PaymentStrategy {
    private final MobilePayAPI mobilePayAPI;

    public MobilePaymentAdapter(MobilePayAPI mobilePayAPI) {
        this.mobilePayAPI = mobilePayAPI;
    }

    @Override
    public PaymentResult process(double amount, String customerInfo) {
        try {
            String token = mobilePayAPI.createToken(customerInfo);
            mobilePayAPI.charge(token, amount);

            String status = mobilePayAPI.getStatus(token);
            if ("SUCCESS".equals(status)) {
                return PaymentResult.success();
            }

            return PaymentResult.failure("Mobile payment failed with status: " + status);

        } catch (Exception e) {
            return PaymentResult.failure("Mobile payment error: " + e.getMessage());
        }
    }
}

// 결제 결과를 담는 객체
class PaymentResult {
    private final boolean success;
    private final String message;

    public PaymentResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static PaymentResult success() {
        return new PaymentResult(true, "Payment successful");
    }

    public static PaymentResult failure(String reason) {
        return new PaymentResult(false, reason);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}

// 레거시 시스템들 (변경 불가)
class LegacyCreditCardSystem {
    public int authorize(String card, String cvv, int exp, int cents) {
        return 1; // 1: success, 0: fail
    }
    public void capture(String card) {}
}

class LegacyBankSystem {
    public boolean verifyAccount(String acc, String pin) { return true; }
    public String initiateTransfer(String acc, double amt) { return "TX123"; }
    public int confirmTransfer(String txId) { return 200; }
}

class MobilePayAPI {
    public String createToken(String info) { return "TOKEN"; }
    public void charge(String token, double amt) {}
    public String getStatus(String token) { return "SUCCESS"; }
}
