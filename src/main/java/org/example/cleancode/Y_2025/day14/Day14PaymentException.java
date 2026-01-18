package org.example.cleancode.Y_2025.day14;

/**
 * Day 14: 결제 시스템 예외 처리
 *
 * 문제점:
 * - 일반적인 Exception만 사용 (구체성 부족)
 * - 에러 메시지가 하드코딩되어 있음
 * - 예외 처리 책임이 분산됨
 * - 에러 원인을 파악하기 어려움
 * - 복구 가능한 예외와 불가능한 예외 구분 없음
 */
public class Day14PaymentException {

    public static void main(String[] args) {
        PaymentService service = new PaymentService();

        try {
            // 정상 결제
            service.processPayment("USER001", 50000, "CREDIT_CARD");
            System.out.println("결제 성공!");

        } catch (PaymentValidationException e) {
            System.out.println("❌ 입력 오류 [" + e.getErrorCodeString() + "]: " + e.getMessage());
            System.out.println("👉 입력값을 확인해주세요.");

        } catch (PaymentBusinessException e) {
            System.out.println("❌ 비즈니스 오류 [" + e.getErrorCodeString() + "]: " + e.getMessage());
            System.out.println("👉 정책을 확인하거나 충전 후 다시 시도해주세요.");

        } catch (PaymentSystemException e) {
            System.out.println("❌ 시스템 오류 [" + e.getErrorCodeString() + "]: " + e.getMessage());
            System.out.println("👉 잠시 후 다시 시도해주세요.");
        }

        try {
            // 잔액 부족
            service.processPayment("USER002", 1000000, "CREDIT_CARD");

        } catch (PaymentValidationException e) {
            System.out.println("❌ 입력 오류 [" + e.getErrorCodeString() + "]: " + e.getMessage());
            System.out.println("👉 입력값을 확인해주세요.");

        } catch (PaymentBusinessException e) {
            System.out.println("❌ 비즈니스 오류 [" + e.getErrorCodeString() + "]: " + e.getMessage());
            System.out.println("👉 정책을 확인하거나 충전 후 다시 시도해주세요.");

        } catch (PaymentSystemException e) {
            System.out.println("❌ 시스템 오류 [" + e.getErrorCodeString() + "]: " + e.getMessage());
            System.out.println("👉 잠시 후 다시 시도해주세요.");
        }

        try {
            // 잘못된 결제 수단
            service.processPayment("USER003", 30000, "INVALID_METHOD");

        } catch (PaymentValidationException e) {  // 👈 여기로 들어옴!
            System.out.println("❌ 입력 오류 [" + e.getErrorCodeString() + "]: " + e.getMessage());
            System.out.println("👉 입력값을 확인해주세요.");

        } catch (PaymentBusinessException e) {
            System.out.println("❌ 비즈니스 오류 [" + e.getErrorCodeString() + "]: " + e.getMessage());
            System.out.println("👉 정책을 확인하거나 충전 후 다시 시도해주세요.");

        } catch (PaymentSystemException e) {
            System.out.println("❌ 시스템 오류 [" + e.getErrorCodeString() + "]: " + e.getMessage());
            System.out.println("👉 정책을 확인하거나 충전 후 다시 시도해주세요.");
        }
    }
}

enum PaymentErrorCode {
    // 입력 검증 오류
    INVALID_USER_ID("E4001", "사용자 ID가 없습니다"),
    INVALID_AMOUNT("E4002", "잘못된 금액입니다"),
    INVALID_PAYMENT_METHOD("E4003", "지원하지 않는 결제 수단입니다"),

    // 비지니스 룰 위반
    PAYMENT_LIMIT_EXCEEDED("E4201", "1회 결제 한도를 초과했습니다"),
    INSUFFICIENT_BALANCE("E4202", "잔액이 부족합니다"),

    // 시스템 오류
    PAYMENT_PROCESSING_ERROR("E5001", "결제 처리 중 오류 발생"),
    EXTERNAL_SYSTEM_ERROR("E5002", "외부 시스템 오류");


    private final String code;
    private final String message;

    PaymentErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }

}


class PaymentService {

    public void processPayment(String userId, int amount, String paymentMethod) {
        // 사용자 검증
        if (userId == null || userId.isEmpty()) {
            throw new PaymentValidationException(
                    PaymentErrorCode.INVALID_USER_ID,
                    PaymentErrorCode.INVALID_USER_ID.getMessage()
            );
        }

        // 금액 검증
        if (amount <= 0) {
            throw new PaymentValidationException(
                    PaymentErrorCode.INVALID_AMOUNT,
                    PaymentErrorCode.INVALID_AMOUNT.getMessage()
            );
        }

        // 금액 검증2
        if (amount > 1000000) {
            throw new PaymentBusinessException(
                    PaymentErrorCode.PAYMENT_LIMIT_EXCEEDED,
                    PaymentErrorCode.PAYMENT_LIMIT_EXCEEDED.getMessage()
            );
        }

        // 결제 수단 검증
        if (!isValidPaymentMethod(paymentMethod)) {
            throw new PaymentValidationException(
                    PaymentErrorCode.INVALID_PAYMENT_METHOD,
                    PaymentErrorCode.INVALID_PAYMENT_METHOD.getMessage()
            );
        }

        // 잔액 확인
        int balance = getUserBalance(userId);
        if (balance < amount) {
            throw new PaymentBusinessException(
                    PaymentErrorCode.INSUFFICIENT_BALANCE,
                    PaymentErrorCode.INSUFFICIENT_BALANCE.getMessage()
            );
        }

        // 결제 처리
        try {
            executePayment(userId, amount, paymentMethod);
        } catch (Exception e) {
            throw new PaymentSystemException(
                    PaymentErrorCode.PAYMENT_PROCESSING_ERROR,
                    PaymentErrorCode.PAYMENT_PROCESSING_ERROR.getMessage()
            );
        }
    }

    private boolean isValidPaymentMethod(String method) {
        return method.equals("CREDIT_CARD") ||
                method.equals("BANK_TRANSFER") ||
                method.equals("MOBILE_PAY");
    }

    private int getUserBalance(String userId) {
        // 실제로는 DB 조회
        if (userId.equals("USER001")) return 100000;
        if (userId.equals("USER002")) return 500;
        return 50000;
    }

    private void executePayment(String userId, int amount, String method) throws Exception {
        // 실제 결제 API 호출
        if (Math.random() < 0.1) {
            throw new Exception("Connection timeout");
        }
        System.out.println(userId + " - " + amount + "원 결제 완료 (" + method + ")");
    }
}

abstract class PaymentException extends RuntimeException {
    private final PaymentErrorCode errorCode;

    public PaymentException(PaymentErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public PaymentErrorCode getErrorCode() {
        return errorCode;
    }

    public String getErrorCodeString() {
        return errorCode.getCode();
    }

    public String getErrorMessage() {
        return errorCode.getMessage();
    }
}

 class PaymentValidationException extends PaymentException {
    public PaymentValidationException(PaymentErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}

 class PaymentBusinessException extends PaymentException {
    public PaymentBusinessException(PaymentErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}

 class PaymentSystemException extends PaymentException {
    public PaymentSystemException(PaymentErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}