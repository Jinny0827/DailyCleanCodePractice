package org.example.cleancode.Y_2026.day83;

import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;

/**
 *  Day 83 과제 — Vavr Either / Try 를 활용한 함수형 에러 처리
 *
 * null 체크 → Try / Either 로 감싸기
 * try-catch 제거 — 예외를 값으로 표현
 * 각 단계를 독립적인 메서드로 분리 (findUser, findItem, validateStock, createOrder)
 * flatMap 체이닝으로 흐름을 선언적으로 표현
 * Either<String, String> 최종 결과로 호출부에 성공/실패 책임 위임
 */
public class Day83OrderService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;

    public Day83OrderService(UserRepository userRepository, ItemRepository itemRepository, OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.orderRepository = orderRepository;
    }

    public Either<String, String> processOrder(String userId, String itemId, int quantity) {

              return  findUser(userId)
                        .flatMap(user ->
                                findItem(itemId)
                                        .flatMap(item -> validateStock(item, quantity))
                                        .map(item -> {
                                            Order order = new Order(user, item, quantity);
                                            orderRepository.save(order);
                                            return "주문 완료: " + order.getId();
                                        })
                        );

    }

    private Either<String, User> findUser(String userId) {
       return Try.of(() -> userRepository.findById(userId))
                .filter(Objects::nonNull)
                .toEither("사용자를 찾을 수 없습니다: " + userId);
    }

    private Either<String, Item> findItem(String itemId) {
        return Try.of(() -> itemRepository.findById(itemId))
                .filter(Objects::nonNull)
                .toEither("아이템을 찾을 수 없습니다" + itemId);
    }
    private Either<String, Item> validateStock(Item item, int quantity) {
        return item.getStock() >= quantity ?
                Either.right(item) : Either.left("재고 부족: 요청=" + quantity + ", 재고=" + item.getStock());

    }


}

class UserRepository {
    private static final Map<String, User> store = Map.of(
            "u1", new User("u1", "홍길동"),
            "u2", new User("u2", "김철수")
    );

    public User findById(String id) {
        return store.get(id); // 없으면 null 반환
    }
}


class ItemRepository {
    private static final Map<String, Item> store = new HashMap<>(Map.of(
            "i1", new Item("i1", "노트북", 5),
            "i2", new Item("i2", "마우스", 0) // 재고 없음
    ));

    public Item findById(String id) {
        return store.get(id); // 없으면 null 반환
    }
}

class OrderRepository {
    private final List<Order> store = new ArrayList<>();

    public void save(Order order) {
        store.add(order);
    }

    public List<Order> findAll() {
        return Collections.unmodifiableList(store);
    }
}

@Data
@AllArgsConstructor
class User {
    private String id;
    private String name;
}

@Data
@AllArgsConstructor
class Item {
    private String id;
    private String name;
    private int stock;
}

@Data
class Order {
    private static int sequence = 1;

    private String id;
    private User user;
    private Item item;
    private int quantity;

    public Order(User user, Item item, int quantity) {
        this.id = "ORDER-" + sequence++;
        this.user = user;
        this.item = item;
        this.quantity = quantity;
    }
}
