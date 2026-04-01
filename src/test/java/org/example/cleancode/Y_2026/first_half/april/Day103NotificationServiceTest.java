package org.example.cleancode.Y_2026.first_half.april;

import org.awaitility.core.ConditionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static java.util.concurrent.TimeUnit.*;

import static org.awaitility.Awaitility.*;

public class Day103NotificationServiceTest {

    Day103NotificationService svc = new Day103NotificationService();
    
    // 공통적인 설정에 대한 추출 및 공통화
    private final ConditionFactory defaultAwait = await().atMost(2, SECONDS);

    @AfterEach
    void tearDown() {
        svc.executor.shutdown();
    }

    @Test
    void waitEmail() {
        svc.sendEmail("user@test.com");

        defaultAwait.until(() -> svc.emailSent.get());
    }

    @Test
    void waitSMS() {
        svc.sendSms("010-1234-5678");
        
        // PollInterval = 조건 확인 주기 (100ms마다 체크), alias = 타임 아웃 시 ConditionTimeoutException: SMS 전송 확인 출력
        defaultAwait.pollInterval(100, MILLISECONDS).until(() -> svc.smsSent.get());
    }

    @Test
    void waitRetry() {
        svc.sendWithRetry("user@test.com");

        defaultAwait.until(() -> svc.eventLog.stream().anyMatch(log -> log.startsWith("RETRY_SUCCESS")));

    }
}
