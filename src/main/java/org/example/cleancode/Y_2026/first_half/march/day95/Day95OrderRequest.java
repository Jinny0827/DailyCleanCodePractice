package org.example.cleancode.Y_2026.first_half.march.day95;


import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.annotation.*;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 95 — Hibernate Validator: Bean Validation으로 검증 로직 제거
 *
 * 1. if-else 검증 난무 -> @NotBlank, @Email, @Size 등 선언적 애노테이션으로 대체
 * 2. 정규식 직접 매칭 -> @Pattern(regexp = ...)
 * 3. 범위 수동 체크 -> @Min, @Max
 * 4. 허용값 수동 체크 -> @Constraint 커스텀 애노테이션 @AllowedValues 구현
 * 5. OrderService에 검증 로직 혼재 -> Validator.validate(req) 한 줄로 분리
 */
public class Day95OrderRequest {

    public static void main(String[] args) {
        OrderService service = new OrderService();

        // 정상 주문
        OrderRequest valid = new OrderRequest(
                "user-001", "홍길동", "hong@example.com",
                "010-1234-5678", 3, 15000, "CARD"
        );
        System.out.println("정상: " + service.placeOrder(valid));

        // 오류 주문
        OrderRequest invalid = new OrderRequest(
                "", "홍", "not-an-email",
                "1234", 0, -500, "BITCOIN"
        );
        System.out.println("오류: " + service.placeOrder(invalid));
    }

    // 왜 AllowedValuesValidator을 구현했는가? -> 어노테이션 자체는 로직을 가질 수 없음(interface) = 오직 데이터를 담는 껍데기일뿐
    // 어노테이션을 사용한 동적 로직 구성은 구현이 필요함
    // ConstraintValidator 사용은 Bean Validation(Jakarta Validation) 프레임워크의 Protocol 이기 때문에 지켜야함
    // <A, T> => A는 어느 어노테이션을 처리, T는 내가 검증할 데이터의 타입은?
    public static class AllowedValuesValidator implements ConstraintValidator<AllowedValue, String> {
        private List<String> allowedValues;


        // 프레임워크가 검증 전 단 한번 호출
        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            // 값이 null 이면 보통 @NotBlank나 @NotNull에서 처리하도록 true를 반환(하는것이 관례)
            if(value == null) {
                return true;
            }

            // 입력값이 허용 리스트에 포함되어있는지 확인
            return allowedValues.contains(value);
        }

        // 실제 검증 일어날때마다 프레임워크가 호출
        @Override
        public void initialize(AllowedValue constraintAnnotation) {
            this.allowedValues = Arrays.asList(constraintAnnotation.values());
        }
    }
}


@Data
@AllArgsConstructor
class OrderRequest {

    @NotBlank(message = "userId는 필수입니다.")
    private String userId;

    @NotBlank(message = "이름은 필수입니다.")
    @Size(min = 2, message = "이름은 2자 이상이어야 합니다.")
    private String customerName;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다.")
    private String phone;

    @Min(value = 1, message = "수량은 1~100 사이여야 합니다")
    @Max(value = 100, message = "수량은 1~100 사이여야 합니다")
    private int quantity;

    @PositiveOrZero(message = "가격은 0 이상이어야 합니다.")
    private int price;

    @AllowedValue(values = {"CARD", "CASH", "POINT"}, message = "결제 수단은 CARD/CASH/POINT 중 하나여야 합니다")
    private String paymentMethod;
}

class OrderService {
    // 검증 엔진 준비(jakarta 엔진 AllowedValue와 찰떡)
    private static final Validator validator;

    // 팩토리 정적 선언
    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    public String placeOrder(OrderRequest req) {
        // 선언된 어노테이션들을 기반으로 자동 검증 진행
        Set<ConstraintViolation<OrderRequest>> violations = validator.validate(req);

        // 검증 실패 시 에러 메시지들을 리스트로 변환시킨다.
        if(!violations.isEmpty()) {
            List<String> errors = violations.stream()
                                    .map(ConstraintViolation::getMessage)
                                    .collect(Collectors.toList());

            return "검증 실패 : " + errors;
        }
        
        // 통과 시 비지니스 로직 수행 (가정)
        return "주문 완료 : " + req.getUserId();
    }
}

// 결론 : 검증용 어노테이션 생성
// 어노테이션 @AllowedValues 정의
// Target = 어디에 붙일 수 있는지 정한다. (여기선 필드 요소)
// Constraint = 어떤 클래스에서 검증을 연결해줄지 접착제 역할
// Retention = 이 객체의 라이프사이클 타이밍 (현재는 런타임때만 가동)
// Documented = Javadoc 같은 문서 생성 시 이 어노테ㅐ이션 정보도 포함시킬때 사용
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = Day95OrderRequest.AllowedValuesValidator.class)
@Documented
@interface AllowedValue {
    
    // 에러 발생 시 출력할 기본 메시지
    String message() default "허용되지 않은 값입니다.";
    
    // Bean Validation의 표준 그룹화 기능 (보통 비워둠)
    Class<?>[] groups() default {};

    // 체크 시 부가적인 정보를 전달할 때 사용 (보통 비워둠)
    Class<? extends Payload>[] payload() default {};

    // 우리가 추가한 속성 : 허용할 문자열 리스트
    String[] values();

}


