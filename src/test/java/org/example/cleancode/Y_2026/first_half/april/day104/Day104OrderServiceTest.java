package org.example.cleancode.Y_2026.first_half.april.day104;

import org.junit.jupiter.api.Test;

import java.util.List;
//import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Day 104 — AssertJ: 가독성 높은 테스트 단언문
 *
 * 1. assertNotNull + assertEquals 분산 → assertThat().isNotNull().satisfies() 로 응집
 * 2. 리스트 단언 3줄 → .hasSize().contains().doesNotContain() 체이닝 1블록
 * 3. allMatch 스트림 단언 → .allMatch() or .extracting().containsOnly()
 * 4. 예외 메시지 단언 → .isInstanceOf().hasMessageContaining() 한 문장
 */
public class Day104OrderServiceTest {

    @Test
    void 주문_생성_검증() {
        Day104Order order = new Day104Order("ORD-001", "user-A", List.of("item-1", "item-2"), 15000);

//        assertNotNull(order);
//        assertEquals("ORD-001", order.getId());
//        assertEquals("user-A", order.getUserId());
//        assertEquals(2, order.getItems().size());
//        assertTrue(order.getItems().contains("item-1"));
//        assertTrue(order.getItems().contains("item-2"));
//        assertFalse(order.getItems().contains("item-99"));
//        assertEquals(15000, order.getTotalAmount());
//        assertTrue(order.getTotalAmount() > 0);


        //Asssertj
        // 기본 단언문 교체
        assertThat(order).isNotNull();
        assertThat(order.getId()).isEqualTo("ORD-001");
        assertThat(order.getUserId()).isEqualTo("user-A");

        //리스트 단언문 교체
        assertThat(order.getItems())
                .hasSize(2)
                .contains("item-1", "item-2")
                .doesNotContain("item-99");


        // 숫자 범위 단언문 교체
        assertThat(order.getTotalAmount())
                .isEqualTo(15000)
                .isPositive();
    }

    @Test
    void 주문_목록_필터링() {
        List<Day104Order> orders = List.of(
                new Day104Order("ORD-001", "user-A", List.of("item-1"), 5000),
                new Day104Order("ORD-002", "user-B", List.of("item-2"), 12000),
                new Day104Order("ORD-003", "user-A", List.of("item-3"), 8000)
        );

        List<Day104Order> userAOrders = orders.stream()
                .filter(o -> o.getUserId().equals("user-A"))
                .toList();

//        assertEquals(2, userAOrders.size());
//        assertNotNull(userAOrders.get(0));
//        assertEquals("ORD-001", userAOrders.get(0).getId());
//        assertEquals("ORD-003", userAOrders.get(1).getId());
//        assertTrue(userAOrders.stream().allMatch(o -> o.getUserId().equals("user-A")));
//        assertFalse(userAOrders.isEmpty());

        // 컬렉션 순서/조건 단언문 교체
        assertThat(userAOrders)
                .hasSize(2)
                .isNotEmpty()
                .allMatch(o -> o.getUserId().equals("user-A"))
                .extracting(Day104Order::getId)
                .containsExactly("ORD-001", "ORD-003");
    }

    @Test
    void 잘못된_주문_예외_검증() {
//        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
//            new Day104Order("", "user-A", List.of(), -1000);
//        });
//        assertNotNull(exception.getMessage());
//        assertTrue(exception.getMessage().contains("금액"));


        assertThatThrownBy(() -> new Day104Order("", "user-A", List.of(), -1000))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("금액");
    }
}
