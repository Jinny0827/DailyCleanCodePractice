package org.example.cleancode.day36;


import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Day 36: 다국어 메시지 시스템
 *
 * 문제점:
 * - 메시지가 하드코딩됨
 * - 언어별 분기가 복잡함
 * - 메시지 키 오타 위험
 * - 동적 값 치환 불가
 * - 폴백(fallback) 로직 없음
 */

public class Day36MessageSystem {
    public static void main(String[] args) {
        NotificationService service = new NotificationService();

        // 한국어
        service.sendWelcome("ko", "홍길동");
        service.sendOrderConfirm("ko", "ORD-001", 50000);

        // 영어
        service.sendWelcome("en", "John");
        service.sendOrderConfirm("en", "ORD-002", 30000);

        // 미지원 언어
        service.sendWelcome("fr", "Pierre");
    }
}

// 메시지 키 번들 
class MessageBundle {
    private final Map<String, String> messages;
    private final String language;
    private static final String DEFAULT_LANG = "en";

    public MessageBundle(String language) {
        this.language = language;
        MessageRepository repo = new MessageRepository();
        this.messages = repo.getMessages(language);
    }

    String get(Messagekey key, Object... params) {
        String template = messages.get(key.getKey());

        if(template == null) {
            System.out.println("⚠️ 메시지 키 없음: " + key.getKey());
            return key.getKey();
        }

        if(params.length == 0) {
            return template;
        }

        return MessageFormat.format(template, params);
    }
}

// 메시지 키 Enum으로 생성
enum Messagekey {
    WELCOME("welcome"),
    ORDER_CONTENT("order.confirm"),
    ERROR("error");

    private final String key;

    Messagekey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}

// 언어별 메시지 저장소
class MessageRepository {
    private static final Map<String, Map<String, String>> MESSAGES = new HashMap<>();

    // 한영 메시지 분기해서 Map 형태로 저장
    static {

        Map<String, String> ko = new HashMap<>();
        ko.put("welcome", "{0}님, 환영합니다!");
        ko.put("order.confirm", "주문번호 {0}이(가) 완료되었습니다. 결제금액: {1}원");
        ko.put("error", "오류가 발생했습니다: {0}");
        MESSAGES.put("ko", ko);

        Map<String, String> en = new HashMap<>();
        en.put("welcome", "Welcome, {0}!");
        en.put("order.confirm", "Order {0} completed. Amount: ${1}");
        en.put("error", "Error occurred: {0}");
        MESSAGES.put("en", en);

    }

    public Map<String, String> getMessages(String lang) {
        return MESSAGES.getOrDefault(lang, MESSAGES.get("en"));
    }


}


class NotificationService {
    private MessageBundle getBundle(String lang) {
        return new MessageBundle(lang);
    }

    // 환영의 말
    public void sendWelcome(String lang, String username) {
        MessageBundle bundle = getBundle(lang);
        String message = bundle.get(Messagekey.WELCOME, username);
        System.out.println("[" + lang + "] " + message);
    }

    // 주문 확인
    public void sendOrderConfirm(String lang, String orderId, int amount) {
        MessageBundle bundle = getBundle(lang);
        String message = bundle.get(Messagekey.ORDER_CONTENT, orderId, amount);
        System.out.println("[" + lang + "] " + message);
    }
    
    // 에러 전송
    public void sendError(String lang, String errorCode) {
        MessageBundle bundle = getBundle(lang);
        String message = bundle.get(Messagekey.ERROR, errorCode);
        System.out.println("[" + lang + "] " + message);
    }

}