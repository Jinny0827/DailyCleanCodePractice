package org.example.cleancode.Y_2026.day82;

import io.vavr.collection.Seq;
import io.vavr.control.Either;
import io.vavr.control.Try;
import io.vavr.control.Validation;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Day 82 — Vavr를 활용한 함수형 에러 처리
 *
 * try-catch로 흐름 제어 → Either<PaymentError, PaymentResult> 반환으로 교체
 * 검증 로직 분산 → Vavr Validation 조합으로 한번에 처리
 * 문자열 반환("SUCCESS" / "FAILED") → 타입 안전한 결과 타입으로
 * 예외 → 에러 값으로 다루기 (Try.of(...))
 * 메서드 책임 분리 (검증 / 실행 / 알림)
 */
public class Day82PaymentService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;
    private final PaymentGateway paymentGateway;

    public Day82PaymentService(UserRepository userRepository,
                               OrderRepository orderRepository,
                               PaymentGateway paymentGateway,
                               NotificationService notificationService) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.paymentGateway = paymentGateway;
        this.notificationService = notificationService;
    }

    public Either<PaymentError, PaymentResult> processPayment(String userId, double amount, String cardNumber) {

        return validate(userId, amount, cardNumber)
                .toEither().mapLeft(errors -> errors.head())
                .flatMap(this::executePayment);

    }

    private Either<PaymentError, PaymentResult> executePayment(ValidatedInput input) {
        return findUser(input.getUserId())
                .flatMap(user -> charge(input.getCardNumber(), input.getAmount())
                        .flatMap(charged -> {
                            orderRepository.save(new Order(input.getUserId(), input.getAmount()));
                            notificationService.sendReceipt(user.getEmail(), input.getAmount());
                            return Either.right(new PaymentResult(input.getUserId(), input.getAmount()));
                        }));
    }


    private Either<PaymentError, User> findUser(String userId) {
        return userRepository.findById(userId)
                .map(Either::<PaymentError, User>right)
                .orElse(Either.left(PaymentError.USER_NOT_FOUND));
    }

    private Either<PaymentError, Boolean> charge(String cardNumber, double amount) {
        return Try.of(() -> paymentGateway.charge(cardNumber, amount))
                .toEither()
                .mapLeft(e -> PaymentError.PAYMENT_GATEWAY_FAILED)
                .flatMap(success -> success ? Either.right(true) : Either.left(PaymentError.PAYMENT_GATEWAY_FAILED));
    }


    private Validation<PaymentError, String> validateUserId(String userId) {
        return (userId == null || userId.isEmpty()) ?
                Validation.invalid(PaymentError.INVALID_USER_ID) :
                Validation.valid(userId);
    }

    private Validation<PaymentError, Double> validateAmount(double amount) {
        return (amount <= 0) ?
                Validation.invalid(PaymentError.INVALID_AMOUNT) :
                Validation.valid(amount);
    }

    private Validation<PaymentError, String> validateCard(String cardNumber) {
        return (cardNumber == null || cardNumber.length() != 16) ?
                Validation.invalid(PaymentError.INVALID_CARD) :
                Validation.valid(cardNumber);
    }
    
    private Validation<Seq<PaymentError>, ValidatedInput> validate(String userId, double amount, String cardNumber) {
        return Validation.combine(
                validateUserId(userId),
                validateAmount(amount),
                validateCard(cardNumber)
        ).ap(ValidatedInput::new);
    }

    public static void main(String[] args) {
        Day82PaymentService service = new Day82PaymentService(
                new InMemoryUserRepository(),
                new InMemoryOrderRepository(),
                new StubPaymentGateway(),
                new ConsoleNotificationService()
        );

        run(service, "user1",  100.0, "1234567890123456");  // ✅ 성공
        run(service, "",       100.0, "1234567890123456");  // ❌ INVALID_USER_ID
        run(service, "user1",  -1.0, "1234567890123456");  // ❌ INVALID_AMOUNT
        run(service, "user1",  100.0, "123");               // ❌ INVALID_CARD
        run(service, "user99", 100.0, "1234567890123456");  // ❌ USER_NOT_FOUND
        run(service, "user1",  100.0, "1111111111111111");  // ❌ PAYMENT_GATEWAY_FAILED
    }

    private static void run(Day82PaymentService service, String userId, double amount, String cardNumber) {
        Either<PaymentError, PaymentResult> result = service.processPayment(userId, amount, cardNumber);
        result.peekLeft(error -> System.out.println("❌ FAILED: " + error))
                .peek(success -> System.out.println("✅ SUCCESS: " + success));
    }
}

@Value
class User {
    String id;
    String email;
    String name;
}

@Value
class Order {
    String userId;
    double amount;
    LocalDateTime createdAt;

    public Order(String userId, double amount) {
        this.userId = userId;
        this.amount = amount;
        this.createdAt = LocalDateTime.now();
    }
}

@Value
class PaymentResult {
    String userId;
    double amount;
}

@Value
class ValidatedInput {
    String userId;
    double amount;
    String cardNumber;

}


enum PaymentError {
    INVALID_USER_ID,
    INVALID_AMOUNT,
    INVALID_CARD,
    USER_NOT_FOUND,
    PAYMENT_GATEWAY_FAILED,
    ORDER_SAVE_FAILED
}




interface UserRepository {
    Optional<User> findById(String userId);
}

interface OrderRepository {
    void save(Order order);
}

interface PaymentGateway {
    boolean charge(String cardNumber, double amount);
}

interface NotificationService {
    void sendReceipt(String email, double amount);
}

class InMemoryUserRepository implements UserRepository {
    private final Map<String, User> store = Map.of(
            "user1", new User("user1", "user1@test.com", "Alice"),
            "user2", new User("user2", "user2@test.com", "Bob")
    );

    @Override
    public Optional<User> findById(String userId) {
        return Optional.ofNullable(store.get(userId));
    }
}

class InMemoryOrderRepository implements OrderRepository {
    private final List<Order> orders = new ArrayList<>();

    @Override
    public void save(Order order) {
        orders.add(order);
        System.out.println("Order saved: " + order);
    }
}


class StubPaymentGateway implements PaymentGateway {
    @Override
    public boolean charge(String cardNumber, double amount) {
        // 1111...으로 시작하면 실패 시뮬레이션
        return !cardNumber.startsWith("1111");
    }
}

class ConsoleNotificationService implements NotificationService {
    @Override
    public void sendReceipt(String email, double amount) {
        System.out.printf("Receipt sent to %s | amount: %.2f%n", email, amount);
    }
}