package org.example.cleancode.Y_2026.day79;

import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.w3c.dom.ls.LSOutput;

import java.util.HashMap;
import java.util.Map;

/**
 * Day 79 — 함수형 에러 처리 with Vavr
 *
 * 문자열 기반 에러 처리 → Either<OrderError, Order> 로 타입 안전하게
 * null 체크 → Option<T> 또는 Try 로 대체
 * 긴 메서드 → 검증 / 결제 / 저장 단계로 함수 분리
 * 사이드 이펙트 혼재 → 순수 검증 로직과 분리
 * 알림 실패 처리 → Try.run() 으로 명시적 무시
 */
public class Day79OrderService {

    private final OrderRepository repo;
    private final PaymentClient paymentClient;
    private final NotificationClient notifier;

    public Day79OrderService(OrderRepository repo, PaymentClient paymentClient, NotificationClient notifier) {
        this.repo = repo;
        this.paymentClient = paymentClient;
        this.notifier = notifier;
    }

    public static void main(String[] args) {
        Day79OrderService service = new Day79OrderService(
                new MockOrderRepository(),
                new MockPaymentClient(),
                (userId, itemId) -> {}
        );

        Either<OrderError, Integer> result = service.processOrder(1L, 10L, 2);

        String output = result.fold(
                error -> "실패: " + error.getMessage(),
                totalPrice -> "성공: " + totalPrice + "원"
        );

        System.out.println(output);
    }


    public Either<OrderError, Integer> processOrder(Long userId, Long itemId, int qty) {
        return validateUser(userId)
                .flatMap(user -> validateItem(itemId, qty)
                        .flatMap(item -> validateBalance(user, item, qty)
                                .flatMap(totalPrice -> processPayment(userId, totalPrice)
                                        .peek(t -> saveAndNotify(user, item, itemId, qty, totalPrice)))));
    }

    private Option<User> getUser(Long userId) {
        return Option.of(repo.findUser(userId));
    }

    private Option<Item> getItem(Long itemId) {
        return Option.of(repo.findItem(itemId));
    }

    // 유저 존재 여부
    private Either<OrderError, User> validateUser(Long userId) {
        return getUser(userId)
                .toEither(OrderError.USER_NOT_FOUND);
    }

    // 상품 존재, 재고 확인
    private Either<OrderError, Item> validateItem(Long itemId, int qty) {
        return getItem(itemId)
                .toEither(OrderError.ITEM_NOT_FOUND)
                .flatMap(item -> item.getStock() >= qty ? Either.right(item) : Either.left(OrderError.OUT_OF_STOCK));
    }

    // 잔액 확인
    private Either<OrderError, Integer> validateBalance(User user, Item item, int qty) {
        //totalPrice 반환
        int totalPrice = item.getPrice() * qty;
        return user.getBalance() >= totalPrice ?
                Either.right(totalPrice) : Either.left(OrderError.INSUFFICIENT_BALANCE);
    }

    // 결제 및 저장에 따른 상태 변화 적용
    private void saveAndNotify(User user, Item item, Long itemId, int qty, int totalPrice) {
        
        // 1. 상태 변경(결제 진행 과정)
        item.setStock(item.getStock() - qty);
        user.setBalance(user.getBalance() - totalPrice);

        // 2. 저장
        repo.saveItem(item);
        repo.saveUser(user);
        
        // 3. 알림(실패해도 무시, 중요하지 않은 작업)
        Try.run(() -> notifier.sendSuccess(user.getId(), itemId))
                .onFailure(e -> System.out.println("알림 실패 (무시) : " + e.getMessage()));
        
    }

    private Either<OrderError, Integer> processPayment(Long userId, int totalPrice) {
        return Try.of(() -> paymentClient.pay(userId, totalPrice))
                .toEither()
                .mapLeft(e -> OrderError.PAYMENT_FAILED)
                .flatMap(paid -> paid ? Either.right(totalPrice) : Either.left(OrderError.PAYMENT_FAILED));
    }
}

@Data
@AllArgsConstructor
class User {
    private Long id;
    private String name;
    private int balance;
}

@Data
@AllArgsConstructor
class Item {
    private Long id;
    private String name;
    private int price;
    private int stock;
}

@Getter
@AllArgsConstructor
enum OrderError {
    USER_NOT_FOUND("유저 없음"),
    ITEM_NOT_FOUND("상품 없음"),
    OUT_OF_STOCK("재고 부족"),
    INSUFFICIENT_BALANCE("잔액 부족"),
    PAYMENT_FAILED("결제 실패");

    private final String message;
}

interface OrderRepository {
    User findUser(Long userId);       // null 반환 가능
    Item findItem(Long itemId);       // null 반환 가능
    void saveUser(User user);
    void saveItem(Item item);
}

interface PaymentClient {
    boolean pay(Long userId, int amount); // 실패 시 false 또는 예외
}

interface NotificationClient {
    void sendSuccess(Long userId, Long itemId); // 실패 시 예외
}

class MockOrderRepository implements OrderRepository {
    private final Map<Long, User> users = new HashMap<>(Map.of(
            1L, new User(1L, "홍길동", 50_000)
    ));
    private final Map<Long, Item> items = new HashMap<>(Map.of(
            10L, new Item(10L, "노트북", 30_000, 5)
    ));

    @Override public User findUser(Long id) { return users.get(id); }
    @Override public Item findItem(Long id) { return items.get(id); }
    @Override public void saveUser(User u) { users.put(u.getId(), u); }
    @Override public void saveItem(Item i) { items.put(i.getId(), i); }
}

class MockPaymentClient implements PaymentClient {
    @Override
    public boolean pay(Long userId, int amount) { return true; } // 항상 성공
}