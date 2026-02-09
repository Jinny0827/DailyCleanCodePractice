package org.example.cleancode.Y_2026.day70;

import java.util.Map;

/**
 * Day 70 - 결제 처리 시스템 리팩터링
 *
 * 전략 패턴: 결제 방식별 로직 분리
 * DIP 적용: 구체 클래스 의존 → 인터페이스 의존
 * 팩토리 패턴: 결제 전략 생성 로직 분리
 * 책임 분리: 검증/실행/에러처리 각각 분리
 * 타입 안정성: Map → 강타입 DTO
 */
public class Day70PaymentProcessor {
    private PaymentStrategyFactory factory;

    public Day70PaymentProcessor(PaymentStrategyFactory factory) {
        this.factory = factory;
    }

    public PaymentResult process(PaymentRequest request) {
        PaymentStrategy strategy  = factory.getStrategy(request.getType());
        return strategy.execute(request);
    }

    public static void main(String[] args) {
        // Mock 객체 생성
        ExternalCardAPI cardAPI = new ExternalCardAPI();
        BankTransferService bankService = new BankTransferService();
        KakaoPaySDK kakaoSDK = new KakaoPaySDK();

        // 팩토리 & 프로세서 생성
        PaymentStrategyFactory factory = new PaymentStrategyFactory(
                cardAPI, bankService, kakaoSDK
        );
        Day70PaymentProcessor  processor = new Day70PaymentProcessor (factory);

        // 테스트 1: 카드 결제
        PaymentRequest cardReq = new PaymentRequest(
                PaymentType.CARD,
                10000.0,
                Map.of("cardNumber", "1234567890123456", "cvv", "123")
        );
        System.out.println(processor.process(cardReq));

        // 테스트 2: 계좌이체
        PaymentRequest bankReq = new PaymentRequest(
                PaymentType.BANK,
                50000.0,
                Map.of("account", "1234567890")
        );
        System.out.println(processor.process(bankReq));
    }
}

// 결제 전략 인터페이스
interface PaymentStrategy {
    PaymentResult execute(PaymentRequest request);
}

// 결제 전략 구현체
class CardPaymentStrategy implements PaymentStrategy {
    private final ExternalCardAPI cardAPI;

    public CardPaymentStrategy(ExternalCardAPI cardAPI) {
        this.cardAPI = cardAPI;
    }

    @Override
    public PaymentResult execute(PaymentRequest request) {
        String cardNumber = request.getCredential("cardNumber");
        String cvv = request.getCredential("cvv");
        double amount = request.getAmount();

        if (cardNumber == null || cvv == null) {
            return PaymentResult.failure("Missing card credentials");
        }

        try {
            cardAPI.authorize(cardNumber, cvv, amount);
            cardAPI.capture(amount);
            return PaymentResult.success();
        } catch (Exception e) {
            System.out.println("Card payment failed: " + e.getMessage());
            return PaymentResult.failure(e.getMessage());
        }
    }
}

class BankTransferStrategy implements PaymentStrategy {
    private final BankTransferService bankService;

    public BankTransferStrategy(BankTransferService bankService) {
        this.bankService = bankService;
    }

    @Override
    public PaymentResult execute(PaymentRequest request) {
        String account = request.getCredential("account");
        double amount = request.getAmount();
        if (account == null) {
            return PaymentResult.failure("Account verification failed");
        }

        boolean verified = bankService.verifyAccount(account);
        if (!verified) {
            return PaymentResult.failure("Transfer failed");
        }

        boolean transferred = bankService.transfer(account, amount);
        if (!transferred) {
            return PaymentResult.failure("Transfer failed");
        }

        return PaymentResult.success();
    }
}

class KakaoPayStrategy implements PaymentStrategy {
    private final KakaoPaySDK kakaoSDK;

    public KakaoPayStrategy(KakaoPaySDK kakaoSDK) {
        this.kakaoSDK = kakaoSDK;
    }

    @Override
    public PaymentResult execute(PaymentRequest request) {
        String userId = request.getCredential("userId");
        double amount = request.getAmount();

        if(userId == null) {
            return PaymentResult.failure("Missing user userId");
        }

        try {
            kakaoSDK.init(userId);

            boolean paid  = kakaoSDK.pay(amount);
            if (!paid) {
                return PaymentResult.failure("KakaoPay payment failed");
            }

            return PaymentResult.success();
        } catch (Exception e) {
            return PaymentResult.failure(e.getMessage());
        }

    }
}

// 결제 전략에 대한 팩터리 메서드 구현
class PaymentStrategyFactory {
    private final Map<PaymentType, PaymentStrategy> strategies;

    public PaymentStrategyFactory(
            ExternalCardAPI cardAPI,
            BankTransferService bankService,
            KakaoPaySDK kakaoSDK
    ) {
        this.strategies = Map.of(
                PaymentType.CARD, new CardPaymentStrategy(cardAPI),
                PaymentType.BANK, new BankTransferStrategy(bankService),
                PaymentType.KAKAO, new KakaoPayStrategy(kakaoSDK)
        );
    }

    public PaymentStrategy getStrategy(PaymentType type) {
        PaymentStrategy strategy = strategies.get(type);

        if(strategy == null) {
            throw new IllegalArgumentException("Unsupported payment type: " + type);
        }

        return strategy;
    }
}




// 외부 라이브러리 객체
class ExternalCardAPI {
    public void authorize(String cardNumber, String cvv, double amount) throws Exception {
        // 시뮬레이션: 카드번호 유효성 체크
        if (cardNumber.length() != 16) {
            throw new Exception("Invalid card number");
        }
        if (cvv.length() != 3) {
            throw new Exception("Invalid CVV");
        }
        if (amount <= 0) {
            throw new Exception("Invalid amount");
        }
        System.out.println("Card authorized: " + amount);
    }

    public void capture(double amount) throws Exception {
        if (amount <= 0) {
            throw new Exception("Capture failed");
        }
        System.out.println("Payment captured: " + amount);
    }
}

class BankTransferService {
    public boolean verifyAccount(String accountNumber) {
        // 시뮬레이션: 계좌번호 10자리 체크
        if (accountNumber == null || accountNumber.length() != 10) {
            return false;
        }
        System.out.println("Account verified: " + accountNumber);
        return true;
    }

    public boolean transfer(String accountNumber, double amount) {
        if (amount <= 0 || amount > 1000000) {
            System.out.println("Transfer failed: invalid amount");
            return false;
        }
        System.out.println("Transfer completed: " + amount + " to " + accountNumber);
        return true;
    }
}

class KakaoPaySDK {
    private String currentUserId;

    public void init(String userId) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("User ID required");
        }
        this.currentUserId = userId;
        System.out.println("KakaoPay initialized for user: " + userId);
    }

    public boolean pay(double amount) {
        if (currentUserId == null) {
            System.out.println("KakaoPay not initialized");
            return false;
        }
        if (amount <= 0) {
            System.out.println("Invalid amount");
            return false;
        }
        System.out.println("KakaoPay payment: " + amount + " (user: " + currentUserId + ")");
        return true;
    }
}

// 결제 요청 객체
class PaymentRequest {
    private final PaymentType type;
    private final double amount;
    private final Map<String, String> credentials;

    public PaymentRequest(PaymentType type, double amount, Map<String, String> credentials) {
        this.type = type;
        this.amount = amount;
        this.credentials = credentials;
    }

    public String getCredential(String key) {
        return credentials.get(key);
    }


    public PaymentType getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public Map<String, String> getCredentials() {
        return credentials;
    }
}


// 결제 결과 객체
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

    // 이것만 추가!
    @Override
    public String toString() {
        return success ? "✅ " + message : "❌ " + message;
    }
}

// 결제 타입 enum
enum PaymentType {
   CARD, BANK, KAKAO
}