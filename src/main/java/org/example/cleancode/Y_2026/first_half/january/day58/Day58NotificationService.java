package org.example.cleancode.Y_2026.first_half.january.day58;

import java.util.HashMap;
import java.util.Map;

/**
 * Day 58 - 알림 시스템 리팩터링
 *
 * 전략 패턴: 알림 유형별 전략 분리
 * DIP: 추상화에 의존하도록 구조 개선
 * SRP: 단일 책임 분리
 * OCP: 새 알림 채널 추가 시 기존 코드 수정 불필요
 */
public class Day58NotificationService {
    private final Map<String, NotificationChannel> channels;

    public Day58NotificationService(EmailConfig emailConfig, SmsConfig smsConfig) {
        this.channels = new HashMap<>();
        channels.put("email", new EmailNotificationChannel(emailConfig));
        channels.put("sms", new SmsNotificationChannel(smsConfig));
        // 차후 push 등 확장 시 위 channels 객체에 put 처리
    }

    public void sendNotification(User user, String message, String type) {
        NotificationChannel channel = channels.get(type);

        if(channel != null && channel.canSend(user)) {
            channel.send(user, message);
        }
    }
}

// 알림 채널이 동일한 계약을 따르도록 전략 수립
interface NotificationChannel {
    
    // 유저에 대한 전송 여부 확인
    boolean canSend(User user);

    // 실제 알림 전송
    void send(User user, String message);

    // 채널 타입 반환 (로깅용)
    String getChannelType();
}

// 이메일 알림 채널 구현 객체
class EmailNotificationChannel implements NotificationChannel {
    private final EmailConfig emailConfig;

    public EmailNotificationChannel(EmailConfig emailConfig) {
        this.emailConfig = emailConfig;
    }

    @Override
    public boolean canSend(User user) {
        return user.getEmail() != null && !user.getEmail().isEmpty();
    }

    @Override
    public void send(User user, String message) {
        String subject = "Notification";
        if (message.contains("urgent")) {
            subject = "[URGENT] " + subject;
        }

        EmailSender sender = new EmailSender(
                emailConfig.getHost(),
                emailConfig.getPort(),
                emailConfig.getUsername()
        );

        sender.send(user.getEmail(), subject, message);

        LogManager.log("Email sent to " + user.getEmail());
    }

    @Override
    public String getChannelType() {
        return "email";
    }
}

// SMS 알림 채널 구현 객체
class SmsNotificationChannel implements NotificationChannel {
    private final SmsConfig smsConfig;

    public SmsNotificationChannel(SmsConfig smsConfig) {
        this.smsConfig = smsConfig;
    }

    @Override
    public boolean canSend(User user) {
        return user.getPhone() != null && !user.getPhone().isEmpty();
    }

    @Override
    public void send(User user, String message) {
        String formatted = message.length() > 160 ?
                message.substring(0, 160) : message;

        SmsSender sender = new SmsSender(
                smsConfig.getApiKey()
        );

        sender.send(user.getPhone(), formatted);

        LogManager.log("SMS sent to " + user.getPhone());
    }

    @Override
    public String getChannelType() {
        return "sms";
    }
}





// User 객체
class User {
    private String email;
    private String phone;

    public User(String email, String phone) {
        this.email = email;
        this.phone = phone;
    }

    public String getEmail() { return email; }
    public String getPhone() { return phone; }
}


class EmailConfig {
    private String host, username;
    private int port;

    public EmailConfig(String host, int port, String username) {
        this.host = host;
        this.port = port;
        this.username = username;
    }

    public String getHost() { return host; }
    public int getPort() { return port; }
    public String getUsername() { return username; }
}

class SmsConfig {
    private String apiKey;

    public SmsConfig(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiKey() { return apiKey; }
}

class EmailSender {
    public EmailSender(String host, int port, String username) {}

    public void send(String to, String subject, String body) {
        System.out.println("📧 Email sent to: " + to);
    }
}

class SmsSender {
    public SmsSender(String apiKey) {}

    public void send(String phone, String message) {
        System.out.println("📱 SMS sent to: " + phone);
    }
}

class LogManager {
    public static void log(String message) {
        System.out.println("[LOG] " + message);
    }
}