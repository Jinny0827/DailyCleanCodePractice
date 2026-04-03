package org.example.cleancode.Y_2026.first_half.april.day105;

/**
 * Day 105 — Mockito: 의존성 목(Mock) 처리
 *
 * 1. 의존성 직접 생성 (new) -> 테스트 시 실제 DB·결제 API 호출
 * 2. 생성자 주입 없음 -> Mock 주입 불가
 * 3. 유저 없음/재고 부족/결제 실패 시나리오 테스트 불가 -> 분기 검증 불가
 * 4. notificationService.sendOrderConfirm() 호출 여부 검증 불가 -> 부수 효과 미검증
 */

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class Day105OrderServiceTest {

    @Mock UserRepository userRepository;
    @Mock InventoryRepository inventoryRepository;
    @Mock PaymentGateway paymentGateway;
    @Mock NotificationService notificationService;

    @InjectMocks Day105OrderService orderService;

    User user = new User("u1", "pay-key", "a@b.com");

    @Test
    void successOrder() {
        // given
        given(userRepository.findById("u1")).willReturn(user);
        given(inventoryRepository.getStock("item-A")).willReturn(10);
        given(paymentGateway.charge("pay-key", 2000)).willReturn(true);

        // when
        OrderResult result = orderService.placeOrder("u1", "item-A", 2);

        // then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).isEqualTo("ORD-u1");
    }

    @Test
    void outOfStock() {
        given(userRepository.findById("u1")).willReturn(user);
        given(inventoryRepository.getStock("item-A")).willReturn(1); // 재고 1개

        OrderResult result = orderService.placeOrder("u1", "item-A", 5); // 5개 요청

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).isEqualTo("재고 부족");
    }

    @Test
    void notUser() {
        given(userRepository.findById("ghost")).willReturn(null);

        OrderResult result = orderService.placeOrder("ghost", "item-A", 2);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).isEqualTo("유저 없음");
    }

    @Test
    void failurePayment() {
        given(userRepository.findById("u1")).willReturn(user);
        given(inventoryRepository.getStock("item-A")).willReturn(10);
        given(paymentGateway.charge("pay-key", 2000)).willReturn(false);
        
        OrderResult result = orderService.placeOrder("u1", "item-A", 2);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).isEqualTo("결제 실패");
    }


    @Test
    void successOrderNotiByOne() {
        given(userRepository.findById("u1")).willReturn(user);
        given(inventoryRepository.getStock("item-A")).willReturn(10);
        given(paymentGateway.charge("pay-key", 2000)).willReturn(true);

        orderService.placeOrder("u1", "item-A", 2);

        then(notificationService).should(times(1)).sendOrderConfirm(user.getEmail());
    }

    @Test
    void notEnoughStockForNotPayment() {
        given(userRepository.findById("u1")).willReturn(user);
        given(inventoryRepository.getStock("item-A")).willReturn(1);

        orderService.placeOrder("u1", "item-A", 5);

        then(paymentGateway).shouldHaveNoInteractions();
    }
}
