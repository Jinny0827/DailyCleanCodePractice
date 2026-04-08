package org.example.cleancode.Y_2026.first_half.april.day108;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

/**
 *
 * Day 108 — Jackson: 커스텀 직렬화/역직렬화
 *
 * calDateTime 오류 -> JavaTimeModule + @JsonFormat
 * 금액 단위 없음 -> JsonSerializer<Integer> 커스텀
 * enum 숫자 노출 -> JsonSerializer<OrderStatus> 한글 변환
 * null 필드 노출 -> NON_NULL 설정
 */
public class Day108OrderController {

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    public String toJson(Order order) throws Exception {
        return mapper.writeValueAsString(order);
    }

    public Order fromJson(String json) throws Exception {
        return mapper.readValue(json, Order.class);
    }

    public static void main(String[] args) throws Exception {
        Day108OrderController controller = new Day108OrderController();

        Order order = new Order("ORD-001", 15000, OrderStatus.PAID,
                LocalDateTime.now(), null);

        String json = controller.toJson(order);
        System.out.println("직렬화: " + json);

        Order restored = controller.fromJson(json);
        System.out.println("역직렬화 amount: " + restored.getAmount());
    }
}

// Money 커스텀 직렬화
class MoneySerializer extends JsonSerializer<Integer> {
    @Override
    public void serialize(Integer value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(value + "원");
    }
}

// Money 커스텀 역직렬화
class MoneyDeserializer extends JsonDeserializer<Integer> {
    @Override
    public Integer deserialize(JsonParser p,
                               DeserializationContext c) throws IOException {
        return Integer.parseInt(p.getText().replace("원", ""));
    }
}

class OrderStatusSerializer extends JsonSerializer<OrderStatus> {
    private static final Map<OrderStatus, String> LABELS = Map.of(
            OrderStatus.PENDING,   "결제대기",
            OrderStatus.PAID,      "결제완료",
            OrderStatus.CANCELLED, "취소됨"
    );

    @Override
    public void serialize(OrderStatus value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(LABELS.get(value));
    }
}

class OrderStatusDeserializer extends  JsonDeserializer<OrderStatus> {
    private static final Map<String, OrderStatus> REVERSE  = Map.of(
            "결제대기", OrderStatus.PENDING,
            "결제완료", OrderStatus.PAID,
            "취소됨",  OrderStatus.CANCELLED
    );

    @Override
    public OrderStatus deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        return REVERSE.get(p.getText());
    }
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Order {
    private String id;

    @JsonSerialize(using = MoneySerializer.class)
    @JsonDeserialize(using = MoneyDeserializer.class)
    private int amount;

    @JsonSerialize(using = OrderStatusSerializer.class)
    @JsonDeserialize(using = OrderStatusDeserializer.class)
    private OrderStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String memo;
}

enum OrderStatus {
    PENDING, PAID, CANCELLED
}
