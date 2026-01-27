package org.example.cleancode.Y_2026.day63;

import java.util.ArrayList;
import java.util.List;

/**
 * Day 63 - Strategy 패턴으로 결제 로직 개선하기
 *
 * Strategy 패턴으로 결제 방식 추상화
 * 검증 로직 각 전략으로 이동
 * 새 결제 수단 추가 시 OCP 준수
 * 예외 처리로 문자열 반환 대체
 */
public class Day63OrderService {

    public void processPayment(Order order, PaymentStrategy strategy) throws PaymentException {
        strategy.pay(order);
    }

    public static void main(String[] args) {
        Day63OrderService service = new Day63OrderService();

        System.out.println("=== 결제 시스템 테스트 시작 ===\n");

        // 1. 카드 결제 성공
        System.out.println("[테스트 1] 카드 결제 - 성공");
        try {
            Order order1 = new Order("ORD-001", 50000);
            PaymentStrategy cardStrategy = new CardPaymentStrategy("1234567812345678");
            service.processPayment(order1, cardStrategy);
            System.out.println("✓ 결제 상태: " + order1.getStatus() + "\n");
        } catch (PaymentException e) {
            System.out.println("✗ 에러: " + e.getMessage() + "\n");
        }

        // 2. 카드 결제 실패 (짧은 번호)
        System.out.println("[테스트 2] 카드 결제 - 실패 (14자리)");
        try {
            Order order2 = new Order("ORD-002", 30000);
            PaymentStrategy cardStrategy = new CardPaymentStrategy("12345678901234");
            service.processPayment(order2, cardStrategy);
            System.out.println("✓ 결제 상태: " + order2.getStatus() + "\n");
        } catch (PaymentException e) {
            System.out.println("✗ 에러: " + e.getMessage() + "\n");
        }

        // 3. 계좌이체 성공
        System.out.println("[테스트 3] 계좌이체 - 성공");
        try {
            Order order3 = new Order("ORD-003", 100000);
            PaymentStrategy bankStrategy = new BankTransferStrategy("1234567890");
            service.processPayment(order3, bankStrategy);
            System.out.println("✓ 결제 상태: " + order3.getStatus() + "\n");
        } catch (PaymentException e) {
            System.out.println("✗ 에러: " + e.getMessage() + "\n");
        }

        // 4. 계좌이체 실패 (짧은 계좌)
        System.out.println("[테스트 4] 계좌이체 - 실패 (9자리)");
        try {
            Order order4 = new Order("ORD-004", 70000);
            PaymentStrategy bankStrategy = new BankTransferStrategy("123456789");
            service.processPayment(order4, bankStrategy);
            System.out.println("✓ 결제 상태: " + order4.getStatus() + "\n");
        } catch (PaymentException e) {
            System.out.println("✗ 에러: " + e.getMessage() + "\n");
        }

        // 5. 모바일 결제 성공
        System.out.println("[테스트 5] 모바일 결제 - 성공");
        try {
            Order order5 = new Order("ORD-005", 25000);
            PaymentStrategy mobileStrategy = new MobilePaymentStrategy("01012345678");
            service.processPayment(order5, mobileStrategy);
            System.out.println("✓ 결제 상태: " + order5.getStatus() + "\n");
        } catch (PaymentException e) {
            System.out.println("✗ 에러: " + e.getMessage() + "\n");
        }

        // 6. 모바일 결제 실패 (011로 시작)
        System.out.println("[테스트 6] 모바일 결제 - 실패 (011)");
        try {
            Order order6 = new Order("ORD-006", 15000);
            PaymentStrategy mobileStrategy = new MobilePaymentStrategy("01112345678");
            service.processPayment(order6, mobileStrategy);
            System.out.println("✓ 결제 상태: " + order6.getStatus() + "\n");
        } catch (PaymentException e) {
            System.out.println("✗ 에러: " + e.getMessage() + "\n");
        }

        System.out.println("=== 테스트 완료 ===");
    }
}

// 결제 전략 인터페이스
interface PaymentStrategy {
    // 결제와 검증 동시에
    void pay(Order order) throws PaymentException;
}

// 카드 결제 구현체
class CardPaymentStrategy implements PaymentStrategy {
    private String cardNumber;

    public CardPaymentStrategy(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    @Override
    public void pay(Order order) throws PaymentException {
        // 검증 메서드
        validateCardNumber();
        
        // 결제 처리
        processPayment(order);

        order.setStatus("PAID_BY_CARD");
    }

    private void validateCardNumber() throws PaymentException {
        if (cardNumber == null) {
            throw new PaymentException("카드번호가 없습니다");
        }

        if (cardNumber.length() != 16) {
            throw new PaymentException("카드번호는 16자리여야 합니다");
        }
    }

    private void processPayment(Order order) {
        // 실 결제는 차후 구현
        System.out.println("Processing card payment: " + order.getTotalAmount());
    }
}

// 계좌이체 구현체
class BankTransferStrategy implements PaymentStrategy {
    private String bankAccount;

    public BankTransferStrategy(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    @Override
    public void pay(Order order) throws PaymentException {

        validateBankAccount();

        processPayment(order);

        order.setStatus("PAID_BY_BANK");
    }

    private void validateBankAccount() throws PaymentException {
        if(bankAccount == null) {
            throw new PaymentException("계좌번호가 없습니다");
        }

        if(!bankAccount.matches("\\d{10,12}")) {
            throw new PaymentException("계좌번호는 10~12자리 숫자여야 합니다");
        }
    }

    private void processPayment(Order order) {
        // 결제 차후 구현
        System.out.println("Processing Bank Account : " + order.getTotalAmount());
    }
}

// 모바일 결제 구현체
class MobilePaymentStrategy implements PaymentStrategy {
    private String phoneNumber;

    public MobilePaymentStrategy(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public void pay(Order order) throws PaymentException {

        validatePhoneNumber();

        processPayment(order);

        order.setStatus("PAID_BY_MOBILE");
    }

    private void validatePhoneNumber() throws PaymentException {
        if(phoneNumber == null) {
            throw new PaymentException("핸드폰번호가 없습니다");
        }

        if(!phoneNumber.startsWith("010")) {
            throw new PaymentException("전화번호는 010으로 시작해야 합니다");
        }
    }

    private void processPayment(Order order) {
        // 결제 차후 구현
        System.out.println("Processing Mobile Payment : " + order.getTotalAmount());
    }
}


// 결제 예외 처리
class PaymentException extends Exception {
    public PaymentException(String message) {
        super(message);
    }

    public PaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}


// Order 객체
class Order {
    private String orderId;
    private double totalAmount;
    private String status;
    private List<OrderItem> items;

    public Order(String orderId, double totalAmount) {
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.status = "PENDING";
        this.items = new ArrayList<>();
    }

    // Getters
    public String getOrderId() {
        return orderId;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    // Setters
    public void setStatus(String status) {
        this.status = status;
    }

    public void addItem(OrderItem item) {
        this.items.add(item);
    }
}

class OrderItem {
    private String productName;
    private int quantity;
    private double price;

    public OrderItem(String productName, int quantity, double price) {
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }


    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }
}
