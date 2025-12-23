package org.example.cleancode.day43;


import java.util.*;

/**
 * Day 43: 감사 로그(Audit Trail) 시스템
 *
 * 문제점:
 * - 로그 데이터가 Map<String, String>으로 비정형
 * - 이벤트 타입 검증 없음 (오타 위험)
 * - 필터링/검색 기능 부재
 * - 불변성 보장 안됨 (로그 변조 가능)
 * - 시간 범위 조회 비효율적
 */
public class Day43AuditLog {

    public static void main(String[] args) {
       AuditLogger logger = new AuditLogger();

        logger.log(AuditEventType.USER_LOGIN, "user123", "Login from 192.168.1.1");
        logger.log(AuditEventType.ORDER_CREATE, "user123", "Order: ORD-001, Amount: 50000");
        logger.log(AuditEventType.ORDER_CANCEL, "user456", "Order: ORD-002");
        logger.log(AuditEventType.USER_LOGOUT, "user123", "Logout");

        logger.printAll();


        // 사용자별 조회
        System.out.println("\n=== user123 로그 ===");
        List<AuditLog> user123Logs = logger.getLogsByUser("user123");
        user123Logs.forEach(System.out::println);

        // 이벤트 타입별 조회
        System.out.println("\n=== 주문 생성 로그 ===");
        List<AuditLog> orderLogs = logger.getLogsByEventType(AuditEventType.ORDER_CREATE);
        orderLogs.forEach(System.out::println);
    }

}

enum AuditEventType {
    USER_LOGIN("사용자 로그인"),
    USER_LOGOUT("사용자 로그아웃"),
    ORDER_CREATE("주문 생성"),
    ORDER_CANCEL("주문 취소"),
    PAYMENT_PROCESS("결제 처리");

    private final String description;

    AuditEventType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

class AuditLog {
    private final String id;
    private final AuditEventType eventType;
    private final String userId;
    private final String details;
    private final long timestamp;

    public AuditLog(AuditEventType eventType, String userId, String details) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.eventType = eventType;
        this.userId = userId;
        this.details = details;
        this.timestamp = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public AuditEventType getEventType() {
        return eventType;
    }

    public String getUserId() {
        return userId;
    }

    public String getDetails() {
        return details;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "AuditLog{" +
                "id='" + id + '\'' +
                ", eventType=" + eventType +
                ", userId='" + userId + '\'' +
                ", details='" + details + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}


class AuditLogger {
    // 정형화된 데이터 구조(AuditLog 객체에 맞게)
    private final List<AuditLog> logs = new ArrayList<>();

    // AuditLog 객체 사용
    public void log(AuditEventType eventType, String userId, String details) {
        AuditLog auditLog = new AuditLog(eventType, userId, details);
        logs.add(auditLog);
        System.out.println("📝 로그 기록: " + eventType.getDescription());
    }
    
    // 불변 리스트 반환
    public List<AuditLog> getAllLogs() {
        return new ArrayList<>(logs);
    }

    // 사용자별 조회

    public List<AuditLog> getLogsByUser(String userId) {
        List<AuditLog> result = new ArrayList<>();
        for (AuditLog log : logs) {
            if(log.getUserId().equals(userId)) {
                result.add(log);
            }
        }

        return result;
    }

    // 이벤트 타입별 조회
    public List<AuditLog> getLogsByEventType(AuditEventType eventType) {
        List<AuditLog> result = new ArrayList<>();
        for (AuditLog log : logs) {
            if(log.getEventType() == eventType) {
                result.add(log);
            }
        }

        return result;
    }
    
    // 전체 출력
    public void printAll() {
        System.out.println("\n=== 전체 로그 (" + logs.size() + "건) ===");
        for (AuditLog log : logs) {
            System.out.println(log);
        }
    }



}
