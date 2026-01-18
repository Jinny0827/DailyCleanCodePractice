package org.example.cleancode.Y_2025.day9;

public abstract class NotificationSender {

    public final void send(NotificationRequest request) {
        if(!validate(request)) {
            logError(getNotificationType(), "유효성 검사 실패");
            return;
        }

        doSend(request);
        saveLog(request);
    }


    protected abstract boolean validate(NotificationRequest request);
    protected abstract void doSend(NotificationRequest request);
    protected abstract String getNotificationType();


    private void saveLog(NotificationRequest request) {
        System.out.println("로그 저장: " + getNotificationType()
                + " - " + request.getRecipient());
    }

    private void logError(String type, String message) {
        System.out.println("[오류] " + type + ": " + message);
    }

}
