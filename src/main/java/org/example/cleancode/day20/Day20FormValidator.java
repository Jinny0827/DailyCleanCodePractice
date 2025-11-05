package org.example.cleancode.day20;


import java.util.*;

/**
 * Day 20: 이벤트 핸들러 통합 및 리스너 관리
 *
 * 문제점:
 * - 각 필드마다 개별 이벤트 리스너 등록
 * - 중복되는 검증 로직
 * - 리스너 해제 로직 부재
 * - 검증 규칙이 하드코딩됨
 */

public class Day20FormValidator {

    public static void main(String[] args) {
        FormValidator validator = new FormValidator();

        // 검증 규칙 생성
        validator.registerRules("username",
                new MinLengthRule(3, "사용자명")
        );
        
        validator.registerRules("email",
                new EmailFormatRule()
        );

        validator.registerRules("password",
                new MinLengthRule(8, "비밀번호"),
                new ContainsDigitRule("비밀번호")
        );

        validator.registerRules("age",
                new MinValueRule(18, "나이")
        );

        // 폼 초기화
        validator.addField("username", "john_doe");
        validator.addField("email", "john@test");  // 잘못된 이메일
        validator.addField("password", "123");      // 너무 짧음
        validator.addField("age", "25");

        // 검증 실행
        validator.validateAll();
    }
}

interface ValidationRule {
    ValidationResult validate(String value);
}

// 최소길이 검증
class MinLengthRule implements ValidationRule {
    private final int minLength;
    private final String fieldName;

    public MinLengthRule(int minLength, String fieldName) {
        this.minLength = minLength;
        this.fieldName = fieldName;
    }

    @Override
    public ValidationResult validate(String value) {
        if(value == null || value.length() < minLength) {
            return ValidationResult.failure(  fieldName + "은(는) " + minLength + "자 이상이어야 합니다");
        }

        return ValidationResult.success();
    }
}

// 이메일 검증
class EmailFormatRule implements ValidationRule {

    private static final String EMAIL_PATTERN =
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    @Override
    public ValidationResult validate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return ValidationResult.failure("이메일은 필수입니다");
        }

        if(!value.matches(EMAIL_PATTERN)) {
            return ValidationResult.failure("올바른 이메일 형식이 아닙니다.");
        }

        return ValidationResult.success();
    }
}

// 숫자 포함 검증
class ContainsDigitRule implements ValidationRule {

    private final String fieldName;

    public ContainsDigitRule(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public ValidationResult validate(String value) {
        if (value == null || !value.matches(".*[0-9].*")) {
            return ValidationResult.failure(
                    fieldName + "에 숫자가 포함되어야 합니다"
            );
        }
        return ValidationResult.success();
    }
}

// 최소값 검증
class MinValueRule implements ValidationRule {
    private final int minValue;
    private final String fieldName;

    public MinValueRule(int minValue, String fieldName) {
        this.minValue = minValue;
        this.fieldName = fieldName;
    }

    @Override
    public ValidationResult validate(String value) {
        try {
            int intValue = Integer.parseInt(value);
            if (intValue < minValue) {
                return ValidationResult.failure(
                        fieldName + "은(는) " + minValue + " 이상이어야 합니다"
                );
            }
            return ValidationResult.success();
        } catch (NumberFormatException e) {
            return ValidationResult.failure(
                    "올바른 " + fieldName + "을(를) 입력해주세요"
            );
        }
    }
}






class ValidationResult {
    private final boolean valid;
    private final String errorMessage;

    private ValidationResult(boolean valid, String errorMessage) {
        this.valid = valid;
        this.errorMessage = errorMessage;
    }

    // 성공
    public static ValidationResult success() {
        return new ValidationResult(true, null);
    }

    // 실패
    public static ValidationResult failure(String errorMessage) {
        if(errorMessage == null || errorMessage.isEmpty()) {
            throw new IllegalArgumentException("에러 메시지는 필수 입니다.");
        }

        return new ValidationResult(false, errorMessage);
    }

    // Optional 반환 (에러 메시지가 없을 수 있음)
    public Optional<String> getErrorMessageOptional() {
        return Optional.ofNullable(errorMessage);
    }

    public boolean isValid() {
        return valid;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}



class FormValidator {
    private final Map<String, String> fields = new HashMap<>();
    private final Map<String, List<ValidationRule>> rules = new HashMap<>();
    private final Map<String, String> errors = new HashMap<>();
    
    // 규칙 등록 메서드
    public void registerRule(String fieldName, ValidationRule rule) {
        rules.computeIfAbsent(fieldName, k -> new ArrayList<>()).add(rule);
    }

    // 여러 규칙 한번에 등록
    public void registerRules(String fieldName, ValidationRule... ruleArray) {
        for(ValidationRule rule : ruleArray) {
            registerRule(fieldName, rule);
        }
    }


    public void addField(String name, String value) {
        fields.put(name, value);
        validateField(name, value);
    }

    public void validateField(String fieldName, String value) {
        System.out.println("처리 시작: " + fieldName + " - " + value);

        List<ValidationRule> fieldRules = rules.get(fieldName);
        if(fieldRules == null || fieldRules.isEmpty()) {
            // 규칙이 없는 경우
            return;
        }

        for (ValidationRule rule : fieldRules) {
            ValidationResult result = rule.validate(value);

            if (!result.isValid()) {
                errors.put(fieldName, result.getErrorMessage());
                System.out.println("❌ " + result.getErrorMessage());
                return;
            }
        }

        errors.remove(fieldName);
        System.out.println("✓ " + fieldName + " 검증 성공");
    }

    public void validateAll() {
        System.out.println("\n=== 전체 검증 결과 ===");
        if (errors.isEmpty()) {
            System.out.println("✓ 모든 필드가 유효합니다");
        } else {
            System.out.println("❌ 오류가 " + errors.size() + "개 있습니다");
            for (Map.Entry<String, String> error : errors.entrySet()) {
                System.out.println("  - " + error.getKey() + ": " + error.getValue());
            }
        }
    }
}