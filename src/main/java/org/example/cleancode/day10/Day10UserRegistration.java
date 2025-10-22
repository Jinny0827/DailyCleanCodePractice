package org.example.cleancode.day10;


/**
 *  예외 처리(Exception Handling)를 체계적으로 개선하는 연습
 *
 *  1. 커스텀 예외 클래스(Exception 대신 구체적인 예외 타입 생성)
 *  2. 예외 계층 구조(비즈니스 로직 예외와 시스템 예외 구분)
 *  3. 예외 처리 분리(검증 로직과 예외 처리 로직 분리)
 *  4. 구체적인 예외 정보(어떤 필드에서 문제가 발생했는지 명확히)
 *
 *
 *  */

public class Day10UserRegistration {

    public static void main(String[] args) {
        registerUser("john", "short", "john@email", 15);
        registerUser("admin", "password123", "admin@company.com", 25);
        registerUser("user1", "validpass123", "user@email.com", 30);
    }

    public static void registerUser(String username, String password,
                                    String email, int age) {
        try {

            UserValidator.validateUsername(username);
            UserValidator.validatePassword(password);
            UserValidator.validateEmail(email);
            UserValidator.validateAge(age);

            System.out.println("사용자 등록 성공");

        } catch (ValidationException  e) {
            System.out.println("등록실패 [" + e.getField() + "]: " + e.getMessage());
        }
    }
}

// 예외 처리 분리를 위한 통합 클래스 생성
class ValidationException extends RuntimeException  {
    private final String field;

    public ValidationException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}

// 1. 유저명에 대한 예외처리 클래스(통합 예외처리를 상속받아 사용)
class InvalidUsernameException extends ValidationException {
    public InvalidUsernameException(String message) {
        super("username", message);
    }
}

// 2. 비밀번호에 대한 예외처리 클래스
class InvalidPasswordException  extends ValidationException {
    public InvalidPasswordException(String message) {
        super("password", message);
    }
}

// 3. 이메일 관련 예외
class InvalidEmailException extends ValidationException {
    public InvalidEmailException(String message) {
        super("email", message);
    }
}

// 4. 나이 관련 예외
class InvalidAgeException extends ValidationException {
    public InvalidAgeException(String message) {
        super("age", message);
    }
}

// 5. 중복 사용자 예외 (추가)
class DuplicateUserException extends ValidationException {
    public DuplicateUserException(String username) {
        super("username", "이미 사용 중인 사용자명입니다: " + username);
    }
}

// 검증 클래스
class UserValidator {
    public static void validateUsername(String username) {
        if(username == null || username.length() < 5) {
            throw new InvalidUsernameException("사용자명은 5자 이상이어야 합니다.");
        }
        if (username.equals("admin") || username.equals("root")) {
            throw new DuplicateUserException(username);
        }
    }

    public static void validatePassword(String password) {

        if (password == null || password.length() < 8) {
           throw new InvalidPasswordException("비밀번호는 8자 이상이어야 합니다.");
        }
        if (!password.matches(".*[0-9].*")) {
            throw new InvalidPasswordException("비밀번호에 숫자가 포함되어야 합니다");
        }

    }

    public static void validateEmail(String email) {
        // 이메일 검증
        if (email == null || !email.contains("@")) {
            throw new InvalidEmailException("유효하지 않은 이메일입니다");
        }
    }

    public static void validateAge(int age) {
        if (age < 18) {
            throw new InvalidAgeException("만 18세 이상만 가입 가능합니다");
        }
    }
}