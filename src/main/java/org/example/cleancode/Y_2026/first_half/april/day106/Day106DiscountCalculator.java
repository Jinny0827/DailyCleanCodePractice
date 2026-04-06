package org.example.cleancode.Y_2026.first_half.april.day106;

/**
 * Day 106 — JUnit 5 @ParameterizedTest: 반복 테스트 제거
 *
 * @CsvSource — 등급별 정상 케이스를 표 형태로 통합
 * @MethodSource — 복잡한 예외 케이스를 Stream<Arguments>로 공급
 * @ValueSource — 단일 파라미터(가격 경계값) 검증에 활용
 * 테스트 이름 @ParameterizedTest(name = "...") 로 가독성 향상
 *
 */
public class Day106DiscountCalculator {

    public int calculate(String grade, int price) {
        if (price <= 0) throw new IllegalArgumentException("가격은 0보다 커야 합니다.");
        return switch (grade) {
            case "VIP"     -> (int)(price * 0.7);
            case "GOLD"    -> (int)(price * 0.8);
            case "SILVER"  -> (int)(price * 0.9);
            case "BASIC"   -> price;
            default        -> throw new IllegalArgumentException("알 수 없는 등급: " + grade);
        };
    }

}
