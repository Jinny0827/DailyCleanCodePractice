package org.example.cleancode.day9;

public class SmsNotificationSender extends NotificationSender{


    @Override
    protected boolean validate(NotificationRequest request) {
        // 핸드폰 번호에 대한 정규식 입혀도 됨
        String phone = request.getRecipient();
        return phone != null && phone.length() >= 10;
    }

    @Override
    protected void doSend(NotificationRequest request) {
        System.out.println("[SMS] " + request.getRecipient() + "로 발송");
        System.out.println("내용: " + request.getContent());
    }

    // 전송 타입에 대한 상수 반환
    @Override
    protected String getNotificationType() {
        return "SMS";
    }
}
