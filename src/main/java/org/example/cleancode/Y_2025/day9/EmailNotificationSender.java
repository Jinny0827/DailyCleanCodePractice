package org.example.cleancode.Y_2025.day9;

public class EmailNotificationSender extends NotificationSender {

    @Override
    protected boolean validate(NotificationRequest request) {
       String email = request.getRecipient();
       return email != null && email.contains("@");
    }

    @Override
    protected void doSend(NotificationRequest request) {
        System.out.println("[이메일] " + request.getRecipient() + "에게 발송");
        System.out.println("제목: " + request.getTitle());
        System.out.println("내용: " + request.getContent());
    }

    @Override
    protected String getNotificationType() {
        return "EMAIL";
    }
}
