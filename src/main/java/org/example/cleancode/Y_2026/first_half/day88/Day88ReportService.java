package org.example.cleancode.Y_2026.first_half.day88;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.LocalDateTime;
import java.util.List;

/**
 *  Day 88 — Jackson으로 JSON 처리 코드 개선
 *
 * 1. ObjectMapper 매번 생성객체 생성 비용 낭비, 설정 분산
 * 2. null 필드 포함"nickname": null이 그대로 출력
 * 3. LocalDateTime 미등록[2026,3,9,14,30,0,...] 배열로 직렬화
 * 4. 문자열로 JSON 조립" 중복 시 JSON 파싱 오류 발생
 * 5. 필드명 직접 노출 orderId → 클라이언트에 order_id로 보내야 할 때 불가
 */
public class Day88ReportService {

    private static final ObjectMapper mapper = createMappers();

    public String generateReport(String userId) throws Exception {
        User user = findUser(userId);

        String userJson = mapper.writeValueAsString(user);

        List<Order> orders = findOrders(userId);
        String ordersJson = mapper.writeValueAsString(orders);

        ObjectNode root = mapper.createObjectNode();
        root.set("user", mapper.valueToTree(user));
        root.set("orders", mapper.valueToTree(orders));

       return mapper.writeValueAsString(root);
    }

    private User findUser(String userId) {
        return new User(userId, "홍길동", null, LocalDateTime.now(), "ACTIVE");
    }

    private List<Order> findOrders(String userId) {
        return List.of(
                new Order("ORD-001", 15000, "COMPLETED", LocalDateTime.now()),
                new Order("ORD-002", 32000, "PENDING",   LocalDateTime.now())
        );
    }

    public static void main(String[] args) {
        try {
            Day88ReportService service = new Day88ReportService();
            String result = service.generateReport("user-001");
            System.out.println(result);
        } catch(Exception e) {
            e.getMessage();
        }

    }

    private static ObjectMapper createMappers() {
        ObjectMapper mapper = new ObjectMapper();

        // LocalDateTime(java.time)을 지원하는 모듈
        mapper.registerModule(new JavaTimeModule());

        // LocalDateTime을 배열이 아닌 문자열로 직렬화
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return mapper;
    }

}

// null 필드 제외 클래스 레이어 조절
@JsonInclude(JsonInclude.Include.NON_NULL)
class User {
    public String id;
    public String name;
    public String nickname;
    @JsonProperty("joined_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime joinedAt;
    public String status;

    public User(String id, String name, String nickname,
                LocalDateTime joinedAt, String status) {
        this.id = id; this.name = name; this.nickname = nickname;
        this.joinedAt = joinedAt; this.status = status;
    }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
class Order {
    @JsonProperty("order_id")
    public String orderId;

    public int amount;
    public String status;
    @JsonProperty("ordered_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime orderedAt;

    public Order(String orderId, int amount, String status, LocalDateTime orderedAt) {
        this.orderId = orderId; this.amount = amount;
        this.status = status; this.orderedAt = orderedAt;
    }
}
