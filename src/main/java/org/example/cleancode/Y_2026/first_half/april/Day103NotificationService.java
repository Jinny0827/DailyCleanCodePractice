package org.example.cleancode.Y_2026.first_half.april;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


/**
 *  Day 103 — Awaitility: 비동기 테스트 개선
 *
 * 1. Thread.sleep(1000) 고정 대기 -> await().atMost(2, SECONDS).until(...)
 * 2. while 폴링 + 수동 타임아웃 -> await().pollInterval(100, MILLISECONDS).until(...)
 * 3. 실패 시 메시지 불명확 -> await().alias("SMS 전송 확인").until(...)
 * 4. 이벤트 로그 조건 검색 -> until(() -> svc.eventLog.stream().anyMatch(...))
 */
public class Day103NotificationService {
    final ExecutorService executor = Executors.newFixedThreadPool(3);
    final AtomicBoolean emailSent = new AtomicBoolean(false);
    final AtomicBoolean smsSent = new AtomicBoolean(false);
    final AtomicInteger retryCount = new AtomicInteger(0);
    final BlockingQueue<String> eventLog = new LinkedBlockingQueue<>();

    public void sendEmail(String to) {
        executor.submit(() -> {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            emailSent.set(true);
            eventLog.add("EMAIL_SENT:" + to);
        });
    }

    public void sendSms(String to) {
        executor.submit(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            smsSent.set(true);
            eventLog.add("SMS_SENT:" + to);
        });
    }

    public void sendWithRetry(String to) {
        executor.submit(() -> {
            for (int i = 0; i < 3; i++) {
                retryCount.incrementAndGet();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                if (i == 2) eventLog.add("RETRY_SUCCESS:" + to);
            }
        });
    }
}
