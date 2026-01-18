package org.example.cleancode.Y_2025.day9;

/**
 * Day 9: 반복되는 패턴 추상화하기
 *
 * Template Method 패턴 또는 Strategy 패턴 활용
 *
 * 공통 프로세스를 추상화
 * 각 알림 타입별로 다른 부분만 구현
 *
 *
 * 중복 코드 제거
 *
 * 유효성 검사 로직
 * 로그 저장 로직
 * 발송 프로세스
 *
 *
 * 확장성 고려
 *
 * 새로운 알림 타입 추가가 쉬워야 함
 * Slack, Discord 등 추가 시 기존 코드 수정 최소화
 *
 *
 * 인터페이스 설계
 *
 * 각 알림 타입의 공통 인터페이스 정의
 * 다형성을 활용한 코드 작성
 * */


public class Day9NotificationSystem {
    public static void main(String[] args) {
        // 이메일 전송
        NotificationSender emailSender  = new EmailNotificationSender();
        emailSender.send(new NotificationRequest(
                "user@example.com",
                "환영합니다",
                "가입을 축하합니다!"
        ));

        System.out.println();

        // SMS 전송
        NotificationSender smsSender = new SmsNotificationSender();
        smsSender.send(new NotificationRequest(
                "01012345678",
                null,  // SMS는 제목 없음
                "인증번호: 1234"
        ));

        System.out.println();

        // PUSH 알람
        NotificationSender pushSender = new PushNotificationSender();
        pushSender.send(new NotificationRequest(
                "device-abc-123",
                "새 메시지",
                "안읽은 메시지가 있습니다"
        ));

    }
}
