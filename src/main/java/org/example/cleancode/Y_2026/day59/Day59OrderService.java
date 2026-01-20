package org.example.cleancode.Y_2026.day59;


import java.util.ArrayList;
import java.util.List;

/**
 * Day 59: 의존성 주입과 테스트 가능성
 *
 * 생성자에서 구체 클래스를 직접 생성 (테스트 불가능)
 * 의존성이 하드코딩되어 있음
 * 비즈니스 로직과 인프라 코드 혼재
 * 에러 처리가 콘솔 출력으로만 되어 있음
 */
public class Day59OrderService {
    private EmailSender emailSender;
    private PaymentGateway paymentGateway;
    private OrderRepository orderRepository;

    public Day59OrderService(EmailSender emailSender,
                             PaymentGateway paymentGateway,
                             OrderRepository orderRepository) {
        this.emailSender = emailSender;
        this.paymentGateway = paymentGateway;
        this.orderRepository = orderRepository;
    }

    public OrderResult processOrder(Order order) {
        if (order.getItems().isEmpty()) {
            order.setStatus(OrderStatus.FAILED);
            return OrderResult.failure("Order has no Items");
        }

        double total = calculateTotal(order);

        boolean paymentSuccess = paymentGateway.charge(order.getCustomerId(), total);
        if (!paymentSuccess) {
            order.setStatus(OrderStatus.FAILED);
            return OrderResult.failure("Payment failed");
        }
        // 결제 완료
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);
        
        //주문 완료
        order.setStatus(OrderStatus.COMPLETED);
        emailSender.send(order.getCustomerEmail(), "주문 완료", "주문이 처리되었습니다.");

        return OrderResult.success();
    }


    private double calculateTotal(Order order) {
        double total = 0;

        for(OrderItem item : order.getItems()) {
            total += item.getPrice() * item.getQuantity();
        }

        return total;
    }
}

class Order {
    private String orderId;
    private String customerId;
    private String customerEmail;
    private List<OrderItem> items;
    private OrderStatus status;

    public Order(String orderId, String customerId, String customerEmail) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.customerEmail = customerEmail;
        this.items = new ArrayList<>();
        this.status = OrderStatus.PENDING;
    }

    // Getters & Setters
    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public String getCustomerEmail() { return customerEmail; }
    public List<OrderItem> getItems() { return items; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public void addItem(OrderItem item) {
        this.items.add(item);
    }
}

class OrderItem {
    private String productId;
    private String productName;
    private double price;
    private int quantity;

    public OrderItem(String productId, String productName, double price, int quantity) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
    }

    // Getters
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
}

enum OrderStatus {
    PENDING,
    PAID,
    COMPLETED,
    FAILED
}

interface EmailSender {
    void send(String to, String subject, String body);
}

interface PaymentGateway {
    boolean charge(String customerId, double amount);
}

interface OrderRepository {
    void save(Order order);
}

class OrderResult {
    private boolean success;
    private String message;

    public OrderResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static OrderResult success() {
        return new OrderResult(true, "Order processed successfully");
    }

    public static OrderResult failure(String message) {
        return new OrderResult(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}


class SmtpEmailSender implements EmailSender {
    @Override
    public void send(String to, String subject, String body) {
        System.out.println("Sending email to: " + to);
    }
}

class StripePaymentGateway implements PaymentGateway {
    @Override
    public boolean charge(String customerId, double amount) {
        return Math.random() > 0.1; // 90% 성공
    }
}

class MySqlOrderRepository implements  OrderRepository {
    @Override
    public void save(Order order) {
        System.out.println("Saving order: " + order.getOrderId());
    }
}