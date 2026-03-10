package org.example.cleancode.Y_2026.first_half.day67;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Day 67 - 주문 처리 시스템 리팩터링
 *
 * 🎯 SRP 위반 (재고/할인/결제/알림 모두 처리)
 * 🔗 강한 결합 (Database, EmailService 등 직접 호출)
 * 🧪 테스트 불가능 (외부 의존성)
 * 🔄 중복 로직 (반복문, 조건문)
 * 💉 SQL Injection 취약점
 */
public class Day67OrderProcessor {
    private final StockValidator stockValidator;
    private final StockService stockService;
    private final DiscountCalculator discountCalculator;
    private final NotificationService notificationService;



    public Day67OrderProcessor(StockValidator stockValidator,
                               DiscountCalculator discountCalculator,
                               StockService stockService,
                               NotificationService notificationService) {
        this.stockValidator = stockValidator;
        this.discountCalculator = discountCalculator;
        this.stockService = stockService;
        this.notificationService = notificationService;
    }

    public boolean processOrder(Order order, User user) {
        // 재고 확인
        if(!stockValidator.validateStock(order.getItems())) {
            notificationService.sendOrderFailure(user, "재고 부족");
            return false;
        }

        // 할인 계산
        double finalAmount = discountCalculator.calculate(order, user);

        // 재고 차감
        try
        {
            stockService.decreaseStock(order.getItems());
        } catch(IllegalStateException e) {
            notificationService.sendOrderFailure(user, e.getMessage());
            return false;
        }

        // 결제 및 알림
        if (!PaymentGateway.charge(user.getCardNumber(), finalAmount)) {
            notificationService.sendOrderFailure(user, "결제 실패");
            return false;
        }

        // 주문 저장 및 성공 알림
        Database.execute("INSERT INTO orders VALUES (" + order.getId() + ", " + finalAmount + ")");
        notificationService.sendOrderSuccess(user, finalAmount);

        return true;
    }

}


// 재고 저장소 (의존성 주입)
interface StockRepository {
    int getStock(Long productId);
    void decreaseStock(Long productId, int quantity);
}

class StockRepositoryImpl implements StockRepository {

    @Override
    public int getStock(Long productId) {
        String sql = "SELECT stock FROM products WHERE id=" + productId;
        return Database.query(sql);
    }

    @Override
    public void decreaseStock(Long productId, int quantity) {
        String sql = "UPDATE products SET stock = stock - " + quantity
                + " WHERE id = " + productId;
        Database.execute(sql);
    }
}

// 재고 차감 (비지니스 로직)
class StockService {
    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    // 모든 아이템의 재고를 차감
    public void decreaseStock(List<OrderItem> items) {

        // 빈 리스트 체크
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("주문 항목이 비어있습니다.");
        }


        // 1단계: 모든 재고 확인
        for (OrderItem item : items) {
            int stock = stockRepository.getStock(item.getProductId());
            if (stock < item.getQuantity()) {
                throw new IllegalStateException(
                        "재고 부족: " + item.getName() + " (현재: " + stock + ")"
                );
            }
        }

        // 2단계: 모두 OK면 차감
        for (OrderItem item : items) {
            stockRepository.decreaseStock(item.getProductId(), item.getQuantity());
        }

    }
}


// 재고 검증만 담당하는 클래스
class StockValidator {
    private final StockRepository stockRepository;

    public StockValidator(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public boolean validateStock(List<OrderItem> items) {
        for (OrderItem item : items) {
            if(!hasEnoughStock(item)) {
                return false;
            }
        }

        return true;
    }

    public boolean hasEnoughStock(OrderItem item) {
        int currentStock = stockRepository.getStock(item.getProductId());
        return currentStock >= item.getQuantity();
    }

}

// 할인 계산 담당 클래스
class DiscountCalculator {

    public double calculate(Order order, User user) {
        double total = calculateTotal(order.getItems());
        return applyDiscount(total, user.getType());
    }

    private double calculateTotal(List<OrderItem> items) {
        double total = 0;

        for (OrderItem item : items) {
            total += item.getPrice() * item.getQuantity();
        }

        return total;
    }
    private double applyDiscount(double total, UserType type) {

        if (type == UserType.VIP) {
            return total * 0.8;
        } else if (type == UserType.GOLD) {
            return total * 0.9;
        } else if (type == UserType.SILVER) {
            return total * 0.95;
        }

        return total;  // NORMAL은 할인 없음
    }

}


// 알림 서비스 인터페이스
interface NotificationService {
    void sendOrderSuccess(User user, double amount);
    void sendOrderFailure(User user, String reason);
}

class NotificationServiceImpl implements NotificationService {

    @Override
    public void sendOrderSuccess(User user, double amount) {
        EmailService.send(user.getEmail(), "주문 완료", "금액: " + amount);
        SMSService.send(user.getPhone(), "주문이 완료되었습니다.");
    }

    @Override
    public void sendOrderFailure(User user, String reason) {
        // 실패 알림
        EmailService.send(user.getEmail(), "주문 실패", reason);
    }
}








// ------------------------------------------ 기반 클래스 ------------------------------------------
class Database {
    private static Map<Long, Integer> stockMap = new HashMap<>();

    static {
        stockMap.put(1L, 100);
        stockMap.put(2L, 50);
    }

    public static int query(String sql) {
        // 실제로는 DB 조회, 여기서는 간단히 시뮬레이션
        return stockMap.getOrDefault(1L, 0);
    }

    public static void execute(String sql) {
        System.out.println("SQL 실행: " + sql);
    }
}

class EmailService {
    public static void send(String email, String subject, String body) {
        System.out.println("[이메일 발송] TO: " + email + ", 제목: " + subject);
    }
}

class SMSService {
    public static void send(String phone, String message) {
        System.out.println("[SMS 발송] TO: " + phone + ", 내용: " + message);
    }
}

class PaymentGateway {
    public static boolean charge(String cardNumber, double amount) {
        System.out.println("[결제 처리] 카드: " + cardNumber + ", 금액: " + amount);
        return true; // 성공으로 가정
    }
}


// DTO/VO 객체
class Order {
    private Long id;
    private List<OrderItem> items;
    private OrderStatus status;

    public Order(Long id, List<OrderItem> items) {
        this.id = id;
        this.items = items;
        this.status = OrderStatus.PENDING;
    }

    public Long getId() { return id; }
    public List<OrderItem> getItems() { return items; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
}

class OrderItem {
    private Long productId;
    private String name;
    private int quantity;
    private double price;

    public OrderItem(Long productId, String name, int quantity, double price) {
        this.productId = productId;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    public Long getProductId() { return productId; }
    public String getName() { return name; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
}

class User {
    private Long id;
    private String email;
    private String phone;
    private String cardNumber;
    private UserType type;

    public User(Long id, String email, String phone, String cardNumber, UserType type) {
        this.id = id;
        this.email = email;
        this.phone = phone;
        this.cardNumber = cardNumber;
        this.type = type;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getCardNumber() { return cardNumber; }
    public UserType getType() { return type; }
}

enum UserType {
    VIP, GOLD, SILVER, NORMAL
}

enum OrderStatus {
    PENDING, COMPLETED, FAILED, CANCELLED
}