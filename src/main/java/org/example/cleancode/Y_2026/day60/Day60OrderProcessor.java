package org.example.cleancode.Y_2026.day60;


import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Day 60 - 주문 처리 시스템 리팩터링 (고급)
 *
 * 타입 안정성 (Map 대신 명확한 객체)
 * 단일 책임 원칙 위반
 * 비즈니스 로직 산재
 * 에러 처리 부실
 * 테스트 불가능한 구조
 */
public class Day60OrderProcessor {
    private final OrderService orderService;

    public Day60OrderProcessor(OrderService orderService) {
        this.orderService = orderService;
    }

    public String processOrder(Map<String, Object> orderData) {
       try {
           Integer userId = (Integer) orderData.get("userId");
           String statusStr = (String) orderData.get("status");
           List<Map<String, Object>> itemMaps = (List<Map<String, Object>>) orderData.get("items");

           if(userId == null || statusStr == null || itemMaps == null) {
               return "ERROR: Missing data";
           }

           // String -> Enum 변환
           MembershipStatus status = MembershipStatus.valueOf(statusStr);

           // OrderItem 리스트 생성 (반복문)
           List<OrderItem> items = new ArrayList<>();
           for (Map<String, Object> itemMap : itemMaps) {
               Integer qty = (Integer) itemMap.get("quantity");
               Double price = (Double) itemMap.get("price");
               String typeStr = (String) itemMap.get("type");

               if (qty != null && price != null && typeStr != null) {
                   ItemType type = ItemType.valueOf(typeStr);
                   items.add(new OrderItem(qty, price, type));
               }
           }
            
           // Order 객체 생성
           Order order = new Order(userId, status, items);
           
           // OrderService로 Order 로직 처리
           OrderResult result = orderService.processOrder(order);

            return result.getMessage();
       } catch (Exception e) {
            return "ERROR: " + e.getMessage();
       }
    }

}

// 할인 전략 패턴
interface DiscountPolicy {
    double apply(Order order, double amount);
}

class NoDiscount implements DiscountPolicy {
    @Override
    public double apply(Order order, double amount) {
        return amount;
    }
}

class GoldMembershipDiscount implements DiscountPolicy {
    private static final double THRESHOLD = 1000.0;
    private static final double DISCOUNT_RATE = 0.95;

    @Override
    public double apply(Order order, double amount) {
        if(amount > THRESHOLD) {
            amount *= DISCOUNT_RATE;
        }

        return amount;
    }
}

class VipMembershipDiscount implements DiscountPolicy {
    private static final double DISCOUNT_RATE = 0.90;

    @Override
    public double apply(Order order, double amount) {
        return amount * DISCOUNT_RATE;
    }
}

// 전략 패턴 팩터리 메서드 구현체
class DiscountPolicyFactory {
    public static DiscountPolicy getPolicy(MembershipStatus status) {
        return switch (status) {
            case NORMAL -> new NoDiscount();
            case GOLD -> new GoldMembershipDiscount();
            case VIP -> new VipMembershipDiscount();
        };
    }
}




// 외부 의존성 포함한 인터페이스 소스
interface OrderRepository {
    void save(Order order) throws DatabaseException;
}

interface EmailService {
    void send(int userId, String message) throws EmailException;
}

interface LogService {
    void log(String message);
}


// 외부 리소스에 대한 익셉션처리
class DatabaseException extends Exception {
    public DatabaseException(String message) { super(message); }
}

class EmailException extends Exception {
    public EmailException(String msg) { super(msg); }
}

// Enum 정의
enum ItemType {
    NORMAL(1.0),
    PREMIUM(0.9),
    VIP(0.8);

    private final double discountRate;

    ItemType(double discountRate) {
        this.discountRate = discountRate;
    }

    public double getDiscountRate() {
        return discountRate;
    }
}

// 회원 등급 상태
enum MembershipStatus {
    NORMAL, GOLD, VIP
}


// 주문
class Order {
    private final int userId;
    private final MembershipStatus status;
    private final List<OrderItem> items;


    public Order(int userId, MembershipStatus status, List<OrderItem> items) {
        if (userId <= 0) throw new IllegalArgumentException("유효하지 않은 사용자 ID");
        if (items == null || items.isEmpty()) throw new IllegalArgumentException("주문 항목이 없습니다");

        this.userId = userId;
        this.status = status;
        // 아이템 목록에 대한 방어적 복사
        this.items = new ArrayList<>(items); 
    }
    
    // 모든 항목의 합계 계산
    public double calculateSubtotal() {
        return items.stream()
                .mapToDouble(item -> item.calculateTotal())
                .sum();
    }

    // 멤버십 할인까지 적용된 최종 금액
    public double calculateFinalAmount() {
        return DiscountPolicyFactory.getPolicy(status)
                .apply(this, calculateSubtotal());
    }
    

    public int getUserId() {
        return userId;
    }

    public MembershipStatus getStatus() {
        return status;
    }

    public List<OrderItem> getItems() {
        return new ArrayList<>(items);
    }
}

// 주문 상품
class OrderItem {
    private final int quantity;
    private final double price;
    private final ItemType type;

    public OrderItem(int quantity, double price, ItemType type) {
        if (quantity <= 0) throw new IllegalArgumentException("수량은 양수여야 합니다");
        if (price < 0) throw new IllegalArgumentException("가격은 0 이상이어야 합니다");

        this.quantity = quantity;
        this.price = price;
        this.type = type;
    }

    public double calculateTotal() {
        return quantity * price * type.getDiscountRate();
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public ItemType getType() {
        return type;
    }
}

// 주문 처리 결과
class OrderResult {
    private final boolean success;
    private final double totalAmount;
    private final String message;

    public OrderResult(boolean success, double totalAmount, String message) {
        this.success = success;
        this.totalAmount = totalAmount;
        this.message = message;
    }

    public static OrderResult success(double totalAmount) {
        return new OrderResult(true, totalAmount, "SUCCESS: " + totalAmount);
    }

    public static OrderResult failure(String errorMessage) {
        return new OrderResult(false, 0, "ERROR: " + errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public String getMessage() {
        return message;
    }
}

// 주문 비지니스 로직 작성
class OrderService {
    private final OrderRepository orderRepository;
    private final EmailService emailService;
    private final LogService logService;

    public OrderService(OrderRepository orderRepository, EmailService emailService, LogService logService) {
        this.orderRepository = orderRepository;
        this.emailService = emailService;
        this.logService = logService;
    }

    public OrderResult processOrder(Order order) {
        try {
            double finalAmount = order.calculateFinalAmount();

            // DB 저장
            orderRepository.save(order);

            // 이메일 발송
            emailService.send(order.getUserId(), "Order: " + finalAmount);

            // 로그 적재
            logService.log("Order processed: " + order.getUserId());

            return OrderResult.success(finalAmount);

        } catch (Exception e) {
            return OrderResult.failure(e.getMessage());
        }
    }
}
