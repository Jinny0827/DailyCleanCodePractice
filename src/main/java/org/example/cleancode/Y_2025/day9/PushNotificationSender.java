package org.example.cleancode.Y_2025.day9;

public class PushNotificationSender extends NotificationSender {
    @Override
    protected boolean validate(NotificationRequest request) {
        String deviceId = request.getRecipient();
        return deviceId != null && !deviceId.isEmpty();
    }

    @Override
    protected void doSend(NotificationRequest request) {
        System.out.println("[푸시] " + request.getRecipient() + "로 발송");
        System.out.println("제목: " + request.getTitle());
        System.out.println("내용: " + request.getContent());
    }

    @Override
    protected String getNotificationType() {
        return "PUSH";
    }
}
