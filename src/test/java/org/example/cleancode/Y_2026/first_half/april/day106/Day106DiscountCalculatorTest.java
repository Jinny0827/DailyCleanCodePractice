package org.example.cleancode.Y_2026.first_half.april.day106;



import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

/**
 * Day 106 — JUnit 5 @ParameterizedTest: 반복 테스트 제거
 *
 * @CsvSource — 등급별 정상 케이스를 표 형태로 통합
 * @MethodSource — 복잡한 예외 케이스를 Stream<Arguments>로 공급
 * @ValueSource — 단일 파라미터(가격 경계값) 검증에 활용
 * 테스트 이름 @ParameterizedTest(name = "...") 로 가독성 향상
 *
 */
public class Day106DiscountCalculatorTest {

    Day106DiscountCalculator calc = new Day106DiscountCalculator();

    // 객체 정적 삽입 처리 (값만 다른 같은 종류의 매개변수에 대한 함수 통합화)
    @ParameterizedTest(name = "{0} 등급, {1}원 -> {2}원")
    @CsvSource({
            "VIP,   10000, 7000",
            "GOLD,  10000, 8000",
            "SILVER,10000, 9000",
            "BASIC, 10000, 10000"
    })
    void gradeApplyDiscount(String grade, int price, int expected) {
        assertThat(calc.calculate(grade, price)).isEqualTo(expected);
    }
    
    // 예외 케이스 통합
    @ParameterizedTest(name = "grade={0}, price={1}")
    @MethodSource("invalid_Exception_Supplier")
    void invalidExceptionInput(String grade, int price) {
        assertThatThrownBy(() -> calc.calculate(grade, price))
                .isInstanceOf(IllegalArgumentException.class);
    }

    static Stream<Arguments> invalid_Exception_Supplier() {
        return Stream.of(
                of("BRONZE", 10000),
                of("", 10000)
        );
    }

    @ParameterizedTest(name="price={0} 은 0이하라 예외")
    @ValueSource(ints = {0, -1, -1000, Integer.MIN_VALUE})
    void priceThresholdException(int price) {
        assertThatThrownBy(() -> calc.calculate("VIP", price))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
