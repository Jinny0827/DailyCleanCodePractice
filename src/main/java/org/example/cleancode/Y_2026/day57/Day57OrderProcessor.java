package org.example.cleancode.Y_2026.day57;


/**
 * Day 57 - 복잡한 주문 처리 로직 리팩터링
 *
 *
 * 단일 책임 원칙 위반: 할인, 배송, 처리 로직이 한 메서드에 집중
 * 개방-폐쇄 원칙 위반: 새로운 고객 타입이나 주문 타입 추가시 기존 코드 수정 필요
 * 복잡한 조건문: 중첩된 if-else로 가독성 저하
 * 하드코딩: 할인율, 배송일, 창고 정보가 코드에 직접 작성됨
 */
public class Day57OrderProcessor {
    private final WarehouseService warehouseService;
    private final InventoryService inventoryService;
    private final NotificationService notificationService;

    public Day57OrderProcessor(WarehouseService warehouseService,
                               InventoryService inventoryService,
                               NotificationService notificationService) {
        this.warehouseService = warehouseService;
        this.inventoryService = inventoryService;
        this.notificationService = notificationService;
    }

    public OrderResult processOrder(String orderType, double amount, String customerType, boolean isUrgent) {
        Order order = new Order(orderType, amount, customerType, isUrgent);
        // 계산 로직 (반환 필요)
        OrderResult result = calculateOrder(order);

        System.out.println("Processing: " + result.getProcessingMethod());
        System.out.println("Final amount: $" + result.getFinalAmount());
        System.out.println("Delivery in: " + result.getDeliveryDays() + " days");


        // 실제 주문 처리 로직 (반환 미필요)
        executeOrder(result, order);

        return result;
    }

    private OrderResult calculateOrder(Order order) {
        // 고객 타입별 할인 적용
        DiscountStrategy discountStrategy = DiscountStrategyFactory.getStrategy(order.getCustomerType());
        double finalAmount = discountStrategy.applyDiscount(order.getAmount());

        // 주문 타입별 처리 방식 결정
        OrderProcessingStrategy orderProcessingStrategy = OrderProcessingStrategyFactory.getStrategy(order.getOrderType());
        OrderProcessingInfo processingInfo = orderProcessingStrategy.process(finalAmount, order.isUrgent());

        // 추가 할인 적용
        finalAmount -= processingInfo.getAdditionalDiscount();

        // 긴급 주문 추가 비용
        if (order.isUrgent()) {
            finalAmount += 20;
        }

        return new OrderResult(finalAmount, processingInfo.getWarehouse(), processingInfo.getDeliveryDays());
    }

    private void executeOrder(OrderResult result, Order order) {
        warehouseService.sendToWarehouse(result.getProcessingMethod(), result.getFinalAmount());
        inventoryService.updateInventory(order.getOrderType(), 1);
        notificationService.notifyCustomer(order.getCustomerType(), result.getDeliveryDays());

    }

}

//----------------- 할인 전략 시작 -----------------


// 할인 전략
interface DiscountStrategy {
    double applyDiscount(double amount);
}

// 프리미엄 할인 전략
class PremiumDiscountStrategy implements DiscountStrategy {
    @Override
    public double applyDiscount(double amount) {
        return amount * 0.85;
    }
}

class VipDiscountStrategy implements DiscountStrategy {
    @Override
    public double applyDiscount(double amount) {
        return amount * 0.75;
    }
}

class RegularDiscountStrategy implements DiscountStrategy {
    @Override
    public double applyDiscount(double amount) {
        return amount * 0.95;
    }
}

// 할인 전략 팩터리 메서드 구현
class DiscountStrategyFactory {
    public static DiscountStrategy getStrategy(String customerType) {
        switch (customerType) {
            case "PREMIUM":
                return new PremiumDiscountStrategy();
            case "VIP":
                return new VipDiscountStrategy();
            case "REGULAR":
                return new RegularDiscountStrategy();
            default:
                return new RegularDiscountStrategy();
        }
    }
}

//----------------- 할인 전략 끝 -----------------


//----------------- 주문 처리 전략 시작 -----------------

// 주문 처리 전략 인터페이스
interface OrderProcessingStrategy {
    OrderProcessingInfo process(double amount, boolean isUrgent);
}

// 전자 제품 주문 처리 전략 구현체
class ElectronicsProcessingStrategy implements OrderProcessingStrategy {
    @Override
    public OrderProcessingInfo process(double amount, boolean isUrgent) {
        String warehouse = "WAREHOUSE_A";
        int deliveryDays = isUrgent ? 1 : 3;
        double additionalDiscount = amount > 1000 ? 50 : 0;

        return new OrderProcessingInfo(warehouse, deliveryDays, additionalDiscount);
    }
}

// 의류 처리 전략
class ClothingProcessingStrategy implements OrderProcessingStrategy {
    @Override
    public OrderProcessingInfo process(double amount, boolean isUrgent) {
        String warehouse = "WAREHOUSE_B";
        int deliveryDays = isUrgent ? 2 : 5;
        double additionalDiscount = amount > 500 ? 25 : 0;
        return new OrderProcessingInfo(warehouse, deliveryDays, additionalDiscount);
    }
}

// 도서 처리 전략
class BooksProcessingStrategy implements OrderProcessingStrategy {
    @Override
    public OrderProcessingInfo process(double amount, boolean isUrgent) {
        String warehouse = "WAREHOUSE_C";
        int deliveryDays = isUrgent ? 1 : 7;
        double additionalDiscount = 0; // 도서는 추가 할인 없음

        return new OrderProcessingInfo(warehouse, deliveryDays, additionalDiscount);
    }
}


// 주문 처리 전략 팩터리 메서드
class OrderProcessingStrategyFactory {
    public static OrderProcessingStrategy getStrategy(String orderType) {
        switch (orderType) {
            case "ELECTRONICS":
                return new ElectronicsProcessingStrategy();
            case "CLOTHING":
                return new ClothingProcessingStrategy();
            case "BOOKS":
                return new BooksProcessingStrategy();
            default:
                throw new IllegalArgumentException("Unknown order type: " + orderType);
        }
    }
}


//----------------- 주문 처리 전략 끝 -----------------


// 서비스 인터페이스 생성 및 의존성 주입
interface WarehouseService {
    void sendToWarehouse(String warehouse, double amount);
}

interface InventoryService {
    void updateInventory(String orderType, int quantity);
}

interface NotificationService {
    void notifyCustomer(String customerType, int deliveryDays);
}





// 주문 처리 정보 값 객체
class OrderProcessingInfo {
    private final String warehouse;
    private final int deliveryDays;
    private final double additionalDiscount;

    public OrderProcessingInfo(String warehouse, int deliveryDays, double additionalDiscount) {
        this.warehouse = warehouse;
        this.deliveryDays = deliveryDays;
        this.additionalDiscount = additionalDiscount;
    }

    public String getWarehouse() {
        return warehouse;
    }

    public int getDeliveryDays() {
        return deliveryDays;
    }

    public double getAdditionalDiscount() {
        return additionalDiscount;
    }
}



// 주문 객체
class Order {
    private final String orderType;
    private final double amount;
    private final String customerType;
    private final boolean isUrgent;

    public Order(String orderType, double amount, String customerType, boolean isUrgent) {
        this.orderType = orderType;
        this.amount = amount;
        this.customerType = customerType;
        this.isUrgent = isUrgent;
    }


    public String getOrderType() {
        return orderType;
    }

    public double getAmount() {
        return amount;
    }

    public String getCustomerType() {
        return customerType;
    }

    public boolean isUrgent() {
        return isUrgent;
    }
}


// 주문 결과 객체
class OrderResult {
    private final double finalAmount;
    private final String processingMethod;
    private final int deliveryDays;

    public OrderResult(double finalAmount, String processingMethod, int deliveryDays) {
        this.finalAmount = finalAmount;
        this.processingMethod = processingMethod;
        this.deliveryDays = deliveryDays;
    }

    public double getFinalAmount() {
        return finalAmount;
    }

    public String getProcessingMethod() {
        return processingMethod;
    }

    public int getDeliveryDays() {
        return deliveryDays;
    }
}