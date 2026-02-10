package org.example.cleancode.Y_2026.day71;


import java.util.HashMap;
import java.util.Map;

/**
 * Day 71: 결제 시스템 리팩터링
 *
 * 전략 패턴 - 각 결제 수단을 독립적인 전략으로
 * 검증 로직 분리 - 공통 검증 추출
 * 책임 분리 - 결제/저장/알림 각각 분리
 * 에러 처리 - 예외를 활용한 명확한 에러 처리
 */
public class Day71PaymentProcessor {
    private final Map<String, PaymentStrategy> strategies = new HashMap<>();
    private final PaymentValidator validator = new PaymentValidator();
    private final PaymentRepository repository;
    private final PaymentNotifier notifier;

    public Day71PaymentProcessor(int customerPoints,
                                 PaymentRepository repository,
                                 PaymentNotifier notifier) {
        strategies.put("CARD", new CardPaymentStrategy());
        strategies.put("BANK", new BankTransferStrategy());
        strategies.put("POINT", new PointPaymentStrategy(customerPoints));

        this.repository = repository;
        this.notifier = notifier;
    }

    public PaymentResult processPayment(String type, double amount, String customerId) {
        try {
            validator.validate(amount, customerId);

            PaymentStrategy strategy = strategies.get(type);
            if(strategy == null) {
                return PaymentResult.failure("Unknown payment type");
            }

            double total = strategy.calculateTotal(amount);

            repository.save(type ,customerId, total, "SUCCESS");
            notifier.notify(customerId, type, total);

            return PaymentResult.success(total);

        } catch (Exception e) {
            return PaymentResult.failure(e.getMessage());
        }
    }
}

// 책임 분리
class PaymentRepository {
    public void save(String type, String customerId, double amount, String status) {
        System.out.println("Saved to DB: " + type + ", " + amount);
    }
}

class PaymentNotifier {
    public void notify(String customerId, String paymentType, double amount) {
        String message = paymentType + " payment of " + amount + " processed";
        System.out.println("Email sent to " + customerId + ": " + message);
    }
}





// 결제 전략 인터페이스 설계
interface PaymentStrategy {
    double calculateTotal(double amount);
    String getPaymentType();
}

// 카드 결제
class CardPaymentStrategy implements PaymentStrategy {
    private static final double FEE_RATE = 0.02;


    @Override
    public double calculateTotal(double amount) {
        return  amount * (1 + FEE_RATE);
    }

    @Override
    public String getPaymentType() {
        return "CARD";
    }
}

class BankTransferStrategy implements PaymentStrategy {
    private static final double FIXED_FEE  = 1000;

    @Override
    public double calculateTotal(double amount) {
        return amount + FIXED_FEE;
    }

    @Override
    public String getPaymentType() {
        return "BANK";
    }
}

class PointPaymentStrategy implements PaymentStrategy {
    private final int availablePoints;

    public PointPaymentStrategy(int availablePoints) {
        this.availablePoints = availablePoints;
    }

    @Override
    public double calculateTotal(double amount) {
        if(availablePoints < amount) {
            throw new IllegalStateException("Insufficient points");
        }

        return amount;
    }

    @Override
    public String getPaymentType() {
        return "POINT";
    }
}



// 결제 결과 객체
class PaymentResult {
    private final boolean success;
    private final String message;
    private final double totalAmount;

    private PaymentResult(boolean success, String message, double totalAmount) {
        this.success = success;
        this.message = message;
        this.totalAmount = totalAmount;
    }

    public static PaymentResult success(double totalAmount) {
        return new PaymentResult(true, "SUCCESS", totalAmount);
    }

    public static PaymentResult failure(String message) {
        return new PaymentResult(false, message, 0);
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

// 유효성 검증
class PaymentValidator {
    public void validate(double amount, String customerId) {
        if(amount < 0) {
            throw new IllegalArgumentException("Invalid amount");
        }

        if(customerId == null || customerId.isEmpty()) {
            throw new IllegalArgumentException("Invalid customer");
        }
    }
}