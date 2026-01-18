package org.example.cleancode.Y_2025.day43;


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

    public static void main(String[] args) throws InterruptedException {
       AuditLogger logger = new AuditLogger();

        logger.log(AuditEventType.USER_LOGIN, "user123", "Login from 192.168.1.1");
        Thread.sleep(100);

        logger.log(AuditEventType.ORDER_CREATE, "user123", "Order: ORD-001, Amount: 50000");
        Thread.sleep(100);

        long midTime = System.currentTimeMillis();
        Thread.sleep(100);

        logger.log(AuditEventType.ORDER_CANCEL, "user456", "Order: ORD-002");
        Thread.sleep(100);

        logger.log(AuditEventType.USER_LOGOUT, "user123", "Logout");


        // === 테스트 1: 시간 범위 조회 ===
        System.out.println("\n=== midTime 이후 로그 ===");
        List<AuditLog> recentLogs = logger.getLogsBetween(midTime, System.currentTimeMillis());
        recentLogs.forEach(System.out::println);

        // === 테스트 2: 쿼리 빌더 - 단일 조건 ===
        System.out.println("\n=== user123 로그 (쿼리 빌더) ===");
        List<AuditLog> user123Logs = logger.query()
                .byUser("user123")
                .execute();
        user123Logs.forEach(System.out::println);


        // === 테스트 3: 쿼리 빌더 - 복합 조건 ===
        System.out.println("\n=== user123의 주문 생성 로그 ===");
        List<AuditLog> user123Orders = logger.query()
                .byUser("user123")
                .byEventType(AuditEventType.ORDER_CREATE)
                .execute();
        user123Orders.forEach(System.out::println);


        // === 테스트 4: 개수만 확인 ===
        System.out.println("\n=== user456 로그 개수 ===");
        int count = logger.query()
                .byUser("user456")
                .count();
        System.out.println("총 " + count + "건");
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

class AuditQuery {
    private final List<AuditLog> allLogs;
    private List<AuditLog> filteredLogs;

    public AuditQuery(List<AuditLog> logs) {
        this.allLogs = new ArrayList<>(logs);
        this.filteredLogs = new ArrayList<>(logs);
    }

    // 사용자 필터
    public AuditQuery byUser(String userId) {
        List<AuditLog> result = new ArrayList<>();
        for (AuditLog log : filteredLogs) {
            if(log.getUserId().equals(userId)) {
                result.add(log);
            }
        }

        filteredLogs = result;
        // 메서드 체이닝을 위한 본인 반환
        return this;
    }

    // 이벤트 타입 필터
    public AuditQuery byEventType(AuditEventType eventType) {
        List<AuditLog> result = new ArrayList<>();
        for (AuditLog log : filteredLogs) {
            if(log.getEventType() == eventType) {
                result.add(log);
            }
        }

        filteredLogs = result;
        return this;
    }

    // 시간 범위 필터
    public AuditQuery between(long startTime, long endTime) {
        List<AuditLog> result = new ArrayList<>();
        for (AuditLog log : filteredLogs) {
            long timestamp = log.getTimestamp();
            if (timestamp >= startTime && timestamp <= endTime) {
                result.add(log);
            }
        }
        filteredLogs = result;
        return this;
    }

    // 최종 실행
    public List<AuditLog> execute() {
        return new ArrayList<>(filteredLogs);
    }

    // 갯수 반환
    public int count() {
        return filteredLogs.size();
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

    // 시간 범위 조회
    public List<AuditLog> getLogsBetween(long startTime, long endTime) {
        List<AuditLog> result = new ArrayList<>();
        for (AuditLog log : logs) {
            long timestamp = log.getTimestamp();
            if (timestamp >= startTime && timestamp <= endTime) {
                result.add(log);
            }
        }

        return result;
    }
    
    // 쿼리빌더 시작점
    public AuditQuery query() {
        return new AuditQuery(logs);
    }


    // 전체 출력
    public void printAll() {
        System.out.println("\n=== 전체 로그 (" + logs.size() + "건) ===");
        for (AuditLog log : logs) {
            System.out.println(log);
        }
    }



}
