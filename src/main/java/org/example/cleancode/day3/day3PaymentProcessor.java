package org.example.cleancode.day3;


/**
 * Day3 중첩된 조건문 평탄화 하기
 * Early Return (Guard Clause) 패턴 활용
 * 복잡한 조건을 의미 있는 이름으로 추출하기
 */
public class day3PaymentProcessor {

    public PaymentResult processPayment(User user, int amount, String paymentMethod) {
        if (user != null) {
            return fail("사용자 정보 없음");
        }

        if (!user.isActive()) {
            return fail("비활성화된 계정");
        }

        if (amount <= 0) {
            return fail("유효하지 않은 금액");
        }

        if (amount > user.getBalance()) {
            return fail("잔액 부족");
        }

        if (paymentMethod.equals("card") || paymentMethod.equals("bank")) {
            PaymentResult payResult = paymentProcess(user, amount, paymentMethod);
            return payResult;
        } else {
            return fail("유효하지 않은 결제 수단");
        }
    }

    // 결제 처리 메서드
    private PaymentResult paymentProcess(User user, int amount, String paymentMethod) {
        if (paymentMethod.equals("card")) {
            if (user.isCardVerified()) {
                user.setBalance(user.getBalance() - amount);
                return new PaymentResult(true, "카드 결제 완료");
            } else {
                return new PaymentResult(false, "카드 인증 필요");
            }
        } else {
            user.setBalance(user.getBalance() - amount);
            return new PaymentResult(true, "계좌이체 완료");

        }
    }


    // return 메시지에 대한 헬퍼 메서드 (실패/성공)
    private PaymentResult fail(String message) {
        return new PaymentResult(false, message);
    }

    private PaymentResult success(String message) {
        return new PaymentResult(true, message);
    }


    // 간단한 User 클래스
    static class User {
        private boolean active;
        private int balance;
        private boolean cardVerified;

        public User(boolean active, int balance, boolean cardVerified) {
            this.active = active;
            this.balance = balance;
            this.cardVerified = cardVerified;
        }

        public boolean isActive() {
            return active;
        }

        public int getBalance() {
            return balance;
        }

        public void setBalance(int balance) {
            this.balance = balance;
        }

        public boolean isCardVerified() {
            return cardVerified;
        }
    }

    // 결제 결과 클래스
    static class PaymentResult {
        private boolean success;
        private String message;

        public PaymentResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return "PaymentResult{success=" + success + ", message='" + message + "'}";
        }
    }

    // 테스트용 메인
    public static void main(String[] args) {
        day3PaymentProcessor processor = new day3PaymentProcessor();
        User user = new User(true, 100000, true);

        PaymentResult result = processor.processPayment(user, 50000, "card");
        System.out.println(result);
    }
}
