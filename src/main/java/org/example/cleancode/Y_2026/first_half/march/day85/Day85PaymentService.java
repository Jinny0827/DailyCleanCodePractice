package org.example.cleancode.Y_2026.first_half.march.day85;


import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

/**
 * Day 85 — Vavr Try / Either를 활용한 함수형 에러 처리
 *
 *  try-catch 남발, 예외 정보 손실Try로 체이닝
 *  null 체크 + 런타임 예외 혼용Either<String, T>로 실패 이유 명시
 *  검증 로직이 비즈니스 로직에 혼재각 단계를 독립 함수로 분리
 *  알림 실패 처리 의도 불명확Try + onFailure 로 명시적 처리
 */
public class Day85PaymentService {

    private final UserRepository userRepository;
    private final PaymentGateway paymentGateway;
    private final NotificationService notificationService;

    public Day85PaymentService(UserRepository userRepository, PaymentGateway paymentGateway, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.paymentGateway = paymentGateway;
        this.notificationService = notificationService;
    }

    public Either<String, PaymentResult> processPayment(String userId, double amount) {
        return findUser(userId)
                .flatMap(user -> validateAmount(amount)
                        .flatMap(validateAmount -> charge(user, validateAmount))
                        .peek(result -> Try.run(
                                () -> notificationService.send(user.getEmail(), "결제 완료: " + amount))
                                .onFailure(e -> System.err.println("[알림 실패] " + e.getMessage()))
                        ));

    }

    // 1. 유저 조회 (Try → Either 변환 포함)
    private Either<String, User> findUser(String userId) {
        return Try.of(() -> userRepository.findById(userId))
                .toEither()
                .mapLeft(Throwable::getMessage)
                .flatMap(u -> u == null
                        ? Either.left("유저 없음")
                        : Either.right(u));
    }

    // 2. 금액 검증
    private Either<String, Double> validateAmount(double amount) {
        return amount <= 0
                ? Either.left("금액 오류")
                : Either.right(amount);
    }

    // 3. 결제 실행 (Try → Either 변환 포함)
    private Either<String, PaymentResult> charge(User user, double amount) {
        return Try.of(() -> paymentGateway.charge(user.getCardNumber(), amount))
                .toEither()
                .mapLeft(Throwable::getMessage)
                .flatMap(result -> result.isSuccess()
                        ? Either.right(result)
                        : Either.left(result.getFailureReason()));
    }

}

// 실행점
class Main {
    public static void main(String[] args) {
        Day85PaymentService service = new Day85PaymentService(
                new UserRepository(),
                new PaymentGateway(),
                new NotificationService()
        );

        // 정상 케이스
        System.out.println(service.processPayment("user1", 5000));

        // 유저 없음
        System.out.println(service.processPayment("ghost", 5000));

        // 금액 오류
        System.out.println(service.processPayment("user1", -1));

        // 한도 초과
        System.out.println(service.processPayment("user1", 9_999_999));
    }
}



@Data
@Builder
class User {
    private String id;
    private String email;
    private String cardNumber;
    private String name;
}

@Data
@Builder
class PaymentResult {
    private boolean success;
    private String transactionId;
    private String failureReason;
}


class UserRepository {
    private static final Map<String, User> DB = Map.of(
            "user1", User.builder()
                    .id("user1").name("김철수")
                    .email("kim@example.com").cardNumber("1234-5678")
                    .build()
    );

    public User findById(String userId) throws Exception {
        if (userId == null || userId.isBlank())
            throw new Exception("userId is blank");
        return DB.get(userId); // 없으면 null
    }
}

class PaymentGateway {

    public PaymentResult charge(String cardNumber, double amount) throws Exception {
        if (cardNumber.startsWith("0000"))
            throw new Exception("카드 정보 오류");

        boolean approved = amount < 1_000_000;
        return PaymentResult.builder()
                .success(approved)
                .transactionId(approved ? UUID.randomUUID().toString() : null)
                .failureReason(approved ? null : "한도 초과")
                .build();
    }
}

class NotificationService {

    public void send(String email, String message) throws Exception {
        if (email == null || !email.contains("@"))
            throw new Exception("잘못된 이메일: " + email);
        System.out.println("[알림] " + email + " → " + message);
    }
}