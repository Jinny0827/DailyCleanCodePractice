package org.example.cleancode.day8;


/**
 * Day8: 알림 발송 시스템
 *
 * 문제점:
 * - NotificationService가 구체적인 발송 방식에 강하게 결합됨
 * - 새로운 알림 채널 추가 시 기존 코드 수정 필요
 * - 테스트하기 어려운 구조
 * - 알림 방식별 로직이 한 곳에 집중됨
 */

import java.util.HashMap;
import java.util.Map;

/**
 * 인터페이스 분리
 *
 * NotificationChannel 인터페이스 정의
 * 각 발송 방식을 별도 클래스로 구현
 *
 *
 * 의존성 주입
 *
 * NotificationService가 구체 클래스가 아닌 인터페이스에 의존
 * 생성자 또는 메서드를 통한 전략 주입
 *
 *
 * Strategy 패턴
 *
 * 알림 발송 알고리즘을 캡슐화
 * 런타임에 전략 교체 가능
 *
 *
 * 확장성
 *
 * 새로운 채널 추가 시 기존 코드 수정 없이 확장
 * OCP(Open-Closed Principle) 준수
 * */

public class Day8NotificationService {

    // Map 타입으로 채널들을 관리
    private final Map<String,NotificationChannel> channels;

    public Day8NotificationService() {
        channels = new HashMap<>();
        channels.put("email", new EmailChannel());
        channels.put("sms", new SmsChannel());
        channels.put("push", new PushChannel());
        channels.put("slack", new SlackChannel());
    }


    public void sendNotification(String type, String message, String recipient) {
        
        // if-else로 type 구분하던 로직에서 인터페이스의 get을 통한 다형성 추구
        NotificationChannel channel = channels.get(type);

        if (channel == null) {
            throw new IllegalArgumentException("지원하지 않는 알림 타입: " + type);
        }

        channel.send(message, recipient);
    }

    // 여러 채널로 동시 발송
    public void sendToMultipleChannels(String[] types, String message, String recipient) {
        for (String type : types) {
            sendNotification(type, message, recipient);
        }
    }



    public static void main(String[] args) {
        Day8NotificationService service = new Day8NotificationService();

        // 단일 채널 발송
        service.sendNotification("email", "주문이 완료되었습니다", "user@example.com");
        System.out.println();

        service.sendNotification("sms", "배송이 시작되었습니다", "010-1234-5678");
        System.out.println();

        // 다중 채널 발송
        String[] channels = {"email", "push", "slack"};
        service.sendToMultipleChannels(channels, "긴급 공지사항", "user@example.com");
    }
}

    // 전송방식에 대한 분리를 위해 인터페이스 작성
    interface NotificationChannel {
        void send(String message, String recipient);

    }

    class EmailChannel implements NotificationChannel {
        @Override
        public void send(String message, String recipient) {
            System.out.println("=== 이메일 발송 ===");
            System.out.println("수신자: " + recipient);
            System.out.println("제목: 알림");
            System.out.println("내용: " + message);
            System.out.println("SMTP 서버 연결...");
            System.out.println("이메일 전송 완료");
        }
    }

    class SmsChannel implements NotificationChannel {
        @Override
        public void send(String message, String recipient) {
            System.out.println("=== SMS 발송 ===");
            System.out.println("수신자: " + recipient);
            System.out.println("내용: " + message);
            System.out.println("통신사 API 호출...");
            System.out.println("SMS 전송 완료");
        }
    }

    class PushChannel implements NotificationChannel {
        @Override
        public void send(String message, String recipient) {
            System.out.println("=== 푸시 알림 ===");
            System.out.println("디바이스: " + recipient);
            System.out.println("메시지: " + message);
            System.out.println("FCM 서버 연결...");
            System.out.println("푸시 알림 전송 완료");
        }
    }

    class SlackChannel implements NotificationChannel {
        @Override
        public void send(String message, String recipient) {
            System.out.println("=== Slack 메시지 ===");
            System.out.println("채널: " + recipient);
            System.out.println("메시지: " + message);
            System.out.println("Slack API 호출...");
            System.out.println("메시지 전송 완료");
        }
    }

