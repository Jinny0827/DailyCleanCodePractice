package org.example.cleancode.day26;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Day 26: 로깅 시스템 개선
 *
 * 문제점:
 * - 로그 레벨 체크가 매번 하드코딩됨
 * - 로그 출력 대상(콘솔, 파일)을 변경하기 어려움
 * - 로그 포맷이 일관되지 않음
 * - 프로덕션/개발 환경 구분 없음
 */
public class Day26LoggingSystem {

    public static void main(String[] args) {
        
        // 개발 환경 오더
       LogWriter consolelogWriter = new ConsoleLogWriter();
       Logger logger = new Logger(LogLevel.DEBUG, consolelogWriter);
       OrderService service = new OrderService(logger);

        service.processOrder("ORDER-001", 50000);
        service.processOrder("ORDER-002", -100);

        System.out.println("\n=== 프로덕션 환경 ===");
        Logger prodLogger = new Logger(LogLevel.INFO, consolelogWriter);
        OrderService prodService = new OrderService(prodLogger);
        prodService.processOrder("ORDER-003", 30000);

    }

}

enum LogLevel {
    DEBUG(0),
    INFO(1),
    WARN(2),
    ERROR(3);

    private final int priority;

    LogLevel(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    // 이 레벨이 targetLevel보다 중요한지 체크
    public boolean shoudLog(LogLevel targetLevel) {
        return this.priority >= targetLevel.priority;
    }
}

interface LogWriter {
    void write(String message);
}

class ConsoleLogWriter implements LogWriter {
    @Override
    public void write(String message) {
        System.out.println(message);
    }
}

class FileLogWriter implements LogWriter{
    private String fileName;


    public FileLogWriter(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void write(String message) {
        // 실제로는 파일에 적어야 한다.
        System.out.println("[FILE: " + fileName + "] " + message);
    }
}

class Logger {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final LogLevel currentLevel;
    private final LogWriter writer;

    public Logger(LogLevel currentLevel, LogWriter writer) {
        this.currentLevel = currentLevel;
        this.writer = writer;
    }

    public void debug(String message) {
        log(LogLevel.DEBUG, message);
    }

    public void info(String message) {
        log(LogLevel.INFO, message);
    }

    public void warn(String message) {
        log(LogLevel.WARN, message);
    }

    public void error(String message) {
        log(LogLevel.ERROR, message);
    }

    private void log(LogLevel logLevel, String message) {
        if(currentLevel.shoudLog(logLevel)) {
            String timestamp = LocalDateTime.now().format(FORMATTER);
            String formattedMessage = String.format("[%s] %s - %s", logLevel.name(), timestamp, message);

            writer.write(formattedMessage);
        }
    }
}


class OrderService {

    private final Logger logger;

    public OrderService(Logger logger) {
        this.logger = logger;
    }

    public void processOrder(String orderId, int amount) {
        logger.debug("주문 처리 시작: " + orderId);

        if (amount <= 0) {
            logger.error("잘못된 금액: " + amount);
            return;
        }

        logger.info("주문 저장: " + orderId + ", 금액: " + amount);

        try {
            callPaymentApi(orderId, amount);
            logger.debug("결제 API 응답 성공");
        } catch (Exception e) {
            logger.error("결제 실패: " + e.getMessage());
        }

        logger.info("주문 처리 완료: " + orderId);
    }

    private void callPaymentApi(String orderId, int amount) throws Exception {
        // 외부 API 호출 시뮬레이션
        if (Math.random() < 0.1) {
            throw new Exception("Connection timeout");
        }
    }
}