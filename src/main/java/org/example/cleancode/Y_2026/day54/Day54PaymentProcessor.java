package org.example.cleancode.Y_2026.day54;

/**
 * 레거시 결제 시스템 리팩터링
 *
 *  실무에서 자주 마주치는 레거시 코드 - 여러 문제가 복합된 결제 처리 로직을 단계적으로 개선하기
 */
public class Day54PaymentProcessor {
    private final NotificationService notificationService;
    private final PaymentGateway paymentGateway;

    public Day54PaymentProcessor(NotificationService notificationService,
                                 PaymentGateway paymentGateway) {
        this.notificationService = notificationService;
        this.paymentGateway = paymentGateway;
    }

    public String processPayment(PaymentRequest request) {
        try {
            // 검증
            if (request.getAmount() <= 0 || request.getUserId() == null || request.getUserId().isEmpty()) {
                return "ERROR: Invalid input";
            }

            // 전략 패턴 사용
            FeeCalculator calculator = request.getMethod().getCalculator();
            calculator.validate(request);  // 결제 수단별 검증
            double finalAmount = calculator.calculate(request.getAmount(), request.isPremium());


            // 외부 API 호출 시뮬레이션
            paymentGateway.process(finalAmount);

            // 이메일 발송
            notificationService.sendPaymentConfirmation(request.getEmail(), finalAmount);

            return "SUCCESS: " + finalAmount;
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
}

// 수수료 전략 인터페이스
interface FeeCalculator {
    double calculate(double amount, boolean isPremium);
    // 결제 수단별 검증
    void validate(PaymentRequest request);
}

// CARD 전략
class CardFeeCalculator implements FeeCalculator {

    @Override
    public double calculate(double amount, boolean isPremium) {
        return isPremium ? amount * 0.98 : amount * 1.03;
    }

    @Override
    public void validate(PaymentRequest request) {
        if (request.getCardNumber() == null || request.getCardNumber().length() != 16) {
            throw new IllegalArgumentException("Invalid card");
        }
    }
}

// BANK 전략
class BankFeeCalculator implements FeeCalculator {
    @Override
    public double calculate(double amount, boolean isPremium) {
        return isPremium ? amount * 0.99 : amount * 1.01;
    }

    @Override
    public void validate(PaymentRequest request) {
        // BANK는 추가 검증 없음
    }
}

// CRYPTO 전략
class CryptoFeeCalculator implements FeeCalculator {
    @Override
    public double calculate(double amount, boolean isPremium) {
        return amount * 0.95; // 항상 5% 할인
    }

    @Override
    public void validate(PaymentRequest request) {
        if (!request.isPremium()) {
            throw new IllegalArgumentException("Crypto only for premium");
        }
    }
}


// 알림 서비스 인터페이스
interface NotificationService {
    void sendPaymentConfirmation(String email, double amount);
}

// 이메일 알림 구현체
class EmailNotificationService implements NotificationService {
    @Override
    public void sendPaymentConfirmation(String email, double amount) {
        if(email != null && email.contains("@")) {
            System.out.println("Email sent to: " + email + ", Amount: " + amount);
        }
    }
}


// 외부 결제 API 서비스
interface PaymentGateway {
    void process(double amount);
}

class ExternalPaymentGateway implements PaymentGateway {
    @Override
    public void process(double amount) {
        try {
            // 외부 API 호출 시나리오
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}



// 타입 안정성과 검증을 위한 enum 클래스
enum PaymentMethod {
    CARD(new CardFeeCalculator()),
    BANK(new BankFeeCalculator()),
    CRYPTO(new CryptoFeeCalculator());

    private final FeeCalculator calculator;

    PaymentMethod(FeeCalculator calculator) {
        this.calculator = calculator;
    }

    public FeeCalculator getCalculator() {
        return calculator;
    }

    public static PaymentMethod fromString(String type) {
        try {
            return PaymentMethod.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown Payment Method " + e);
        }
    }
}

// 파라미터를 객체로 묶어 반환
class PaymentRequest {
    private final PaymentMethod method;
    private final double amount;
    private final String userId;
    private final String cardNumber;
    private final String email;
    private final boolean isPremium;

    public PaymentRequest(PaymentMethod method, double amount, String userId,
                          String cardNumber, String email, boolean isPremium) {
        this.method = method;
        this.amount = amount;
        this.userId = userId;
        this.cardNumber = cardNumber;
        this.email = email;
        this.isPremium = isPremium;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public double getAmount() {
        return amount;
    }

    public String getUserId() {
        return userId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getEmail() {
        return email;
    }

    public boolean isPremium() {
        return isPremium;
    }
}
