package org.example.cleancode.Y_2025.day16;

import java.util.Map;

/**
 * Day 16: 복잡한 조건문을 전략 패턴으로 개선
 *
 * 문제점:
 * - 복잡한 if-else 분기
 * - 새로운 결제 수단 추가 시 기존 코드 수정 필요 (OCP 위반)
 * - 결제 로직이 한 곳에 집중되어 있음
 * - 테스트하기 어려움
 */
public class Day16PaymentSystem {
    public static void main(String[] args) {
        PaymentProcessor processor = new PaymentProcessor();

        // 신용카드 결제
        processor.processPayment(PaymentType.CARD, 50000, "1234-5678-9012-3456");
        processor.processPayment(PaymentType.BANK, 30000, "110-123-456789");
        processor.processPayment(PaymentType.POINT, 10000, "user123");
    }
}

enum PaymentType {
    CARD, BANK, POINT
}

class PaymentProcessor {
    private final Map<PaymentType, PaymentStrategy> strategies;
    private final PointRewardService rewardService;

    public PaymentProcessor() {
        this.strategies = Map.of(
                PaymentType.CARD, new CreditCardPayment(),
                PaymentType.BANK, new BankTransferPayment(),
                PaymentType.POINT, new PointPayment()
        );
        this.rewardService = new PointRewardService();
    }

    public void processPayment(PaymentType paymentType, int amount, String credential) {
        PaymentStrategy strategy = strategies.get(paymentType);

        if(strategy == null) {
            throw new IllegalArgumentException("지원하지 않는 결제 수단입니다: " + paymentType);
        }

        strategy.pay(amount, credential);
        rewardService.reward(paymentType, amount);
    }

}

interface PaymentStrategy {
    void pay(int amount, String credential);
}

class CreditCardPayment implements PaymentStrategy {
    private static final int MAX_PAYMENT_AMOUNT = 1_000_000;
    private final CredentialValidator validator = new CardNumberValidator();

    @Override
    public void pay(int amount, String credential) {
        validator.validate(credential);

        System.out.println("카드번호 검증 중: " + credential);

        validateAmount(amount);

        System.out.println("신용카드로 " + amount + "원 결제 완료");
    }

    private void validateAmount(int amount) {
        if (amount > MAX_PAYMENT_AMOUNT) {
            throw new IllegalArgumentException(
                    "카드 결제는 " + MAX_PAYMENT_AMOUNT + "원 이하만 가능합니다"
            );
        }
    }
}

class BankTransferPayment implements PaymentStrategy {
    private final CredentialValidator validator = new AccountNumberValidator();

    @Override
    public void pay(int amount, String credential) {
        validator.validate(credential);

        System.out.println("계좌 잔액 확인 중: " + credential);

        System.out.println("계좌이체로 " + amount + "원 결제 완료");
    }
}

class PointPayment implements PaymentStrategy {
    private static final int MIN_PAYMENT_AMOUNT = 1_000;
    private final CredentialValidator validator = new UserIdValidator();

    @Override
    public void pay(int amount, String credential) {
        validator.validate(credential);

        System.out.println("포인트 잔액 확인 중: " + credential);

        validateAmount(amount);

        System.out.println("포인트로 " + amount + "원 결제 완료");
    }

    private void validateAmount(int amount) {
        if (amount < MIN_PAYMENT_AMOUNT) {
            throw new IllegalArgumentException(
                    "포인트는 " + MIN_PAYMENT_AMOUNT + "원 이상부터 사용 가능합니다"
            );
        }
    }
}

// 포인트 적립
class PointRewardService {
    private static final int CARD_REWARD_RATE = 100;    // 1%
    private static final int BANK_REWARD_RATE = 200;    // 0.5%

    public void reward(PaymentType paymentType, int amount) {
        int earnedPoints = calculatePoints(paymentType, amount);
        if (earnedPoints > 0) {
            System.out.println(earnedPoints + " 포인트 적립");
        }
    }

    public int calculatePoints(PaymentType paymentType, int amount) {
        return switch (paymentType) {
            case CARD -> amount / CARD_REWARD_RATE;
            case BANK -> amount / BANK_REWARD_RATE;
            case POINT -> 0;
        };
    }
}

// 각 결제 수단별 검증
interface CredentialValidator {
    void validate(String credential);
}

class CardNumberValidator implements CredentialValidator {
    private static final int CARD_NUMBER_LENGTH = 19;

    @Override
    public void validate(String credential) {
        if (credential == null || credential.length() != CARD_NUMBER_LENGTH) {
            throw new IllegalArgumentException("올바른 카드번호가 아닙니다");
        }
    }
}

class AccountNumberValidator implements CredentialValidator {
    private static final String ACCOUNT_PATTERN = "\\d{3}-\\d{3}-\\d{6}";

    @Override
    public void validate(String credential) {
        if (credential == null || !credential.matches(ACCOUNT_PATTERN)) {
            throw new IllegalArgumentException("올바른 계좌번호가 아닙니다");
        }
    }
}

class UserIdValidator implements CredentialValidator {
    @Override
    public void validate(String credential) {
        if (credential == null || credential.isEmpty()) {
            throw new IllegalArgumentException("사용자 ID가 필요합니다");
        }
    }
}

