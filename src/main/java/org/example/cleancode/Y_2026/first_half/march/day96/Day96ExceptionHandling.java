package org.example.cleancode.Y_2026.first_half.march.day96;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Day 96 — Global Exception Handler: 비즈니스 예외와 공통 응답 구조화
 *
 * 1	리턴 타입 혼선 (String/Error)	ApiResponse<T> 객체로 응답 통일
 * 2	비즈니스 로직에 try-catch 혼재	예외 던지기(throw)로 책임 분리
 * 3	에러 메시지 하드코딩	ErrorCode 열거형(Enum) 도입
 */
public class Day96ExceptionHandling {

    public static void main(String[] args) {
        OrderService service = new OrderService();
        
        // 테스트 케이스들
        Object[] inputs = {"001", 10, null, 100};

        for (int i = 0; i < inputs.length; i += 2) {
            String id = (String) inputs[i];
            int qty = (int) inputs[i+1];

            try {
                String data = service.processOrder(id, qty);
                System.out.println("결과: " + ApiResponse.success(data));
            } catch (Exception e) {
                System.out.println("결과: " + GlobalExceptionHandler.handle(e));
            }
        }
    }

}


class OrderService {
    public String processOrder(String itemId, int quantity) {
        // 예외 발생 시 호출자에게 예외처리를 넘긴다.
        if(itemId == null) {
            throw new ItemNotFoundException();
        }

        if(quantity > 50) {
            throw new OutOfStockException();
        }

        return "ITEM-" + itemId + " 주문 성공";
    }
}

// Global Exception Handler 역할
class GlobalExceptionHandler {
    public static ApiResponse<?> handle(Exception e) {
        // 커스텀한 비지니스 예외가 존재하는 경우
        if(e instanceof BusinessException) {
            BusinessException be = (BusinessException) e;
            return ApiResponse.error(be.getErrorCode());
        }

        // 그외 시스템 에러
        return ApiResponse.error(ErrorCode.SERVER_ERROR);
    }
}



@Getter
@AllArgsConstructor
enum ErrorCode {
    INVALID_INPUT("E001", "입력값이 올바르지 않습니다."),
    ITEM_NOT_FOUND("E002", "아이템을 찾을 수 없습니다."),
    OUT_OF_STOCK("E003", "재고가 부족합니다."),
    SERVER_ERROR("E999", "서버 내부 오류가 발생했습니다.");

    private final String code;
    private final String message;
}

// 모든 응답의 표준 규격(제네릭 사용)
@Getter
class ApiResponse<T> {
    private final boolean success;
    private final T data;
    private final ErrorResponse error;
    private final LocalDateTime timestamp = LocalDateTime.now();

    public ApiResponse(boolean success, T data, ErrorResponse error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    // 성공 응답 정적 팩터리 메서드
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }
    
    // 실패 응답 정적 팩터리 메서드
    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return new ApiResponse<>(false, null, new ErrorResponse(errorCode.getCode(), errorCode.getMessage()));
    }

    // 내부 에러 상세 객체
    @Getter
    @AllArgsConstructor
    static class ErrorResponse {
        private String code;
        private String message;
        private List<String> details;

        // 일반적인 에러용 생성자
        public ErrorResponse(String code, String message) {
            this(code, message, null);
        }
    }
}

// 커스텀 최상위 비지니스 예외
@Getter
class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}

// 구체적인 재고 부족 예외
class OutOfStockException extends BusinessException{
    public OutOfStockException() {
        super(ErrorCode.OUT_OF_STOCK);
    }
}

// 아이템 없음 예외
class ItemNotFoundException extends BusinessException {
    public ItemNotFoundException() {
        super(ErrorCode.ITEM_NOT_FOUND);
    }
}