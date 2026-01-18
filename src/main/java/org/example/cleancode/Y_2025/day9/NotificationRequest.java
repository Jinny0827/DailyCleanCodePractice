package org.example.cleancode.Y_2025.day9;

public class NotificationRequest {
    private String recipient;
    private String title;
    private String content;

    public NotificationRequest(String recipient, String title, String content) {
        this.recipient = recipient;
        this.title = title;
        this.content = content;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}
