package org.example.cleancode.Y_2026.first_half.april.day105;

/**
 * Day 105 — Mockito: 의존성 목(Mock) 처리
 *
 * 1. 의존성 직접 생성 (new) -> 테스트 시 실제 DB·결제 API 호출
 * 2. 생성자 주입 없음 -> Mock 주입 불가
 * 3. 유저 없음/재고 부족/결제 실패 시나리오 테스트 불가 -> 분기 검증 불가
 * 4. notificationService.sendOrderConfirm() 호출 여부 검증 불가 -> 부수 효과 미검증
 */

public class Day105OrderService {

    private final UserRepository userRepository;
    private final InventoryRepository inventoryRepository;
    private final PaymentGateway paymentGateway;
    private final NotificationService notificationService;

    public Day105OrderService(UserRepository userRepository, InventoryRepository inventoryRepository, PaymentGateway paymentGateway, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.inventoryRepository = inventoryRepository;
        this.paymentGateway = paymentGateway;
        this.notificationService = notificationService;
    }

    public OrderResult placeOrder(String userId, String itemId, int quantity) {
        User user = userRepository.findById(userId);
        if (user == null) {
            return OrderResult.fail("유저 없음");
        }

        int stock = inventoryRepository.getStock(itemId);
        if (stock < quantity) {
            return OrderResult.fail("재고 부족");
        }

        boolean paid = paymentGateway.charge(user.getPaymentKey(), quantity * 1000);
        if (!paid) {
            return OrderResult.fail("결제 실패");
        }

        inventoryRepository.decreaseStock(itemId, quantity);
        notificationService.sendOrderConfirm(user.getEmail());

        return OrderResult.success("ORD-" + userId);
    }
}

class OrderResult {
    private final boolean success;
    private final String message;

    private OrderResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static OrderResult success(String orderId) { return new OrderResult(true, orderId); }
    public static OrderResult fail(String reason)     { return new OrderResult(false, reason); }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}

class User {
    private final String id;
    private final String paymentKey;
    private final String email;

    public User(String id, String paymentKey, String email) {
        this.id = id;
        this.paymentKey = paymentKey;
        this.email = email;
    }

    public String getId()         { return id; }
    public String getPaymentKey() { return paymentKey; }
    public String getEmail()      { return email; }
}


interface UserRepository {
    User findById(String userId);
}

interface InventoryRepository {
    int getStock(String itemId);
    void decreaseStock(String itemId, int quantity);
}

interface PaymentGateway {
    boolean charge(String paymentKey, int amount);
}

interface NotificationService {
    void sendOrderConfirm(String email);
}
