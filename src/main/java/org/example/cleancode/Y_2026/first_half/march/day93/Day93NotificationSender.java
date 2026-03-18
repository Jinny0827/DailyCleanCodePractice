package org.example.cleancode.Y_2026.first_half.march.day93;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Day 93 — Jackson: 다형성 JSON 직렬화/역직렬화
 *
 * 1. 문자열 JSON 조립ObjectMapper.writeValueAsString() + POJO
 * 2. 수동 파싱ObjectMapper.readValue()
 * 3. instanceof + 문자열 조립@JsonTypeInfo + @JsonSubTypes
 * 4. kind 파라미터로 타입 분기다형성 역직렬화로 분기 제거
 */
public class Day93NotificationSender {

    private final static ObjectMapper mapper = new ObjectMapper();

    // 문제 1: 문자열로 JSON 직접 조립 → 이스케이프 누락 시 파싱 오류
    public String buildPayload(String type, String userId, String message, boolean urgent) throws Exception {
        return mapper.writeValueAsString(new NotificationPayload(type, userId, message, urgent));
    }

    // 문제 2: 수동 파싱 → 필드 순서나 공백 변경 시 즉시 버그
    public String extractField(String json, String field) throws Exception {
        return mapper.readTree(json).path(field).asText(null);
    }

    // 문제 3: 다형성 이벤트를 if-else + 문자열 조립으로 처리
    public String serializeEvent(NotificationEvent event) throws Exception {
       return mapper.writeValueAsString(event);
    }

    // 문제 4: 역직렬화 시 타입 정보 없어서 Object로만 받음 → 캐스팅 필요
    public NotificationEvent deserializeEvent(String json) throws Exception {
        return mapper.readValue(json, NotificationEvent.class);
    }

    public static void main(String[] args) throws Exception {
        Day93NotificationSender sender = new Day93NotificationSender();

        // 직렬화
        PushEvent push = new PushEvent();
        push.deviceId = "device-001";
        push.title = "새 알림";

        String json = sender.serializeEvent(push);
        System.out.println("직렬화: " + json);

        // 역직렬화
        NotificationEvent event = sender.deserializeEvent(json);
        System.out.println("타입 확인: " + (event instanceof PushEvent)); // true
        System.out.println("deviceId: " + ((PushEvent) event).deviceId);
    }
}

// 다형성 처리 클래스
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind")
@JsonSubTypes({
        @JsonSubTypes.Type(value = PushEvent.class, name = "PUSH"),
        @JsonSubTypes.Type(value = EmailEvent.class, name = "EMAIL"),
        @JsonSubTypes.Type(value = SmsEvent.class, name = "SMS")
})
abstract class NotificationEvent {
    // 공통 필드가 있으면 여기에 (예: timestamp, userId 등)
    // 없으면 비워도 OK
}


record NotificationPayload(String type, String userId,
                           String message, boolean urgent, long timestamp) {
    NotificationPayload(String type, String userId, String message, boolean urgent) {
        this(type, userId, message, urgent, System.currentTimeMillis());
    }
}


class PushEvent extends NotificationEvent { public String deviceId; public String title; }
class EmailEvent extends NotificationEvent { public String to; public String subject; }
class SmsEvent extends NotificationEvent { public String phone; public String body; }
