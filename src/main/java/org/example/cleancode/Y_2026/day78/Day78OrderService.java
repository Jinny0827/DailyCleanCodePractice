package org.example.cleancode.Y_2026.day78;

import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Day 78 — 함수형 에러 처리 (Vavr)
 *
 *  Try 로 예외 발생 지점을 값으로 변환
 * Either<String, T> 로 에러 메시지(Left) vs 성공값(Right) 분리
 * flatMap 체이닝으로 중첩 null 체크 / 조건 검사 제거
 * 각 검증 로직을 독립 메서드로 분리
 * 최종 결과를 fold()로 단일 반환 처리
 *
 */
public class Day78OrderService {
    private static ItemRepository itemRepository;
    private static UserRepository userRepository;
    private static OrderRepository orderRepository;


    public String processOrder(String userId, String itemId, int quantity) {

        Either<String, Void> result = validateUser(userId)
                .flatMap(user -> validateItem(itemId, quantity)
                    .flatMap(item -> saveOrder(user, item, quantity))
                );

        return result.fold(
                error -> error,
                success -> "SUCCESS: 주문완료"
        );
    }

    private Either<String, User> validateUser(String userId) {
        Try<User> tryUser = Try.of(()-> userRepository.findById(userId));
        Either<String, User> result = tryUser.toEither().mapLeft(Throwable::getMessage);
        Either<String, User> validatedUser = result
                .filterOrElse(User::isActive, u -> "ERROR: 비활성 유저");

        return validatedUser;
    }

    private Either<String, Item> validateItem(String itemId, int quantity) {
        Try<Item> tryItem = Try.of(() -> itemRepository.findById(itemId));
        Either<String, Item> result2 = tryItem
                .toEither()
                .mapLeft(Throwable::getMessage)
                .flatMap(item -> item == null
                        ? Either.left("ERROR: 상품 없음")
                        : Either.right(item));
        Either<String, Item> validatedItem = result2.filterOrElse(item -> item.getStock() >= quantity, i -> "재고 부족");

        return validatedItem;
    }

    private Either<String, Void> saveOrder(User user, Item item, int quantity) {
        Try<Void> trySave = Try.run(() ->
                orderRepository.save(new Order(user, item, quantity)));

        Either<String, Void> result3 = trySave.toEither().mapLeft(Throwable::getMessage);

        return result3;
    }

}

interface UserRepository {
    User findById(String userId);
}

interface ItemRepository {
    Item findById(String itemId);
}

interface OrderRepository {
    void save(Order order);
}

class FakeUserRepository implements UserRepository {
    private final Map<String, User> store = new HashMap<>();

    public void add(User user) {
        store.put(user.getId(), user);
    }

    @Override
    public User findById(String userId) {
        return store.get(userId);
    }
}

class FakeItemRepository implements ItemRepository {
    private final Map<String, Item> store = new HashMap<>();

    public void add(Item item) {
        store.put(item.getId(), item);
    }

    @Override
    public Item findById(String itemId) {
        return store.get(itemId);
    }
}

class FakeOrderRepository implements OrderRepository {
    private final List<Order> orders = new ArrayList<>();

    @Override
    public void save(Order order) {
        orders.add(order);
    }

    public List<Order> getOrders() {
        return orders;
    }
}

@Getter
class User {
    private final String id;
    private final String name;
    private final boolean active;

    public User(String id, String name, boolean active) {
        this.id = id;
        this.name = name;
        this.active = active;
    }
}

@Getter
class Item {
    private final String id;
    private final String name;
    private final int stock;

    public Item(String id, String name, int stock) {
        this.id = id;
        this.name = name;
        this.stock = stock;
    }
}

@Getter
class Order {
    private final User user;
    private final Item item;
    private final int quantity;

    public Order(User user, Item item, int quantity) {
        this.user = user;
        this.item = item;
        this.quantity = quantity;
    }
}
