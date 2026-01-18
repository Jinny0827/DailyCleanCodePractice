package org.example.cleancode.Y_2025.day19;


import java.util.*;

/**
 * Day 19: 데이터 접근 계층 분리
 *
 * 문제점:
 * - DB 접근 로직이 서비스에 직접 포함됨
 * - 중복 쿼리 (같은 주문을 여러 번 조회)
 * - 캐싱 로직 부재
 * - 트랜잭션 관리가 명확하지 않음
 * - 테스트하기 어려운 구조
 */
public class Day19OrderQueryService {

    // DB 시뮬레이션용 (실제로는 데이터베이스)
    private static Map<String, OrderEntity> database = new HashMap<>();
    private static Map<String, List<OrderItemEntity>> orderItemsDb = new HashMap<>();

    static {
        // 테스트 데이터
        database.put("ORDER-001",
                new OrderEntity("ORDER-001", "USER-001", "2024-01-15", 150000, "COMPLETED"));
        database.put("ORDER-002",
                new OrderEntity("ORDER-002", "USER-001", "2024-02-20", 50000, "PENDING"));
        database.put("ORDER-003",
                new OrderEntity("ORDER-003", "USER-002", "2024-03-10", 200000, "COMPLETED"));

        orderItemsDb.put("ORDER-001", Arrays.asList(
                new OrderItemEntity("ITEM-001", "노트북", 1, 150000)
        ));
        orderItemsDb.put("ORDER-002", Arrays.asList(
                new OrderItemEntity("ITEM-002", "마우스", 2, 25000)
        ));
    }

    public static void main(String[] args) {
        OrderRepository orderRepository = new DatabaseOrderRepository(database);
        OrderItemRepository orderItemRepository = new DatabaseOrderItemRepository(orderItemsDb);


        CachedOrderRepository cachedOrderRepository = new CachedOrderRepository(orderRepository);
        OrderQueryService service = new OrderQueryService(cachedOrderRepository , orderItemRepository);

        // 테스트
        System.out.println("=== 첫 번째 조회 ===");
        service.getOrderDetails("ORDER-001");

        System.out.println("\n=== 두 번째 조회 (동일 주문) ===");
        service.getOrderDetails("ORDER-001");

        System.out.println("\n=== 사용자별 주문 조회 ===");
        service.getUserOrders("USER-001");
    }
}

interface OrderRepository {
    Optional<OrderEntity> findById(String orderId);
    List<OrderEntity> findByUserId(String userId);
}

interface OrderItemRepository {
    List<OrderItemEntity> findByOrderId(String orderId);
}

// 주문 목록 DB 구현체
class DatabaseOrderRepository implements OrderRepository {
    private final Map<String, OrderEntity> database;

    public DatabaseOrderRepository(Map<String, OrderEntity> database) {
        this.database = database;
    }

    @Override
    public Optional<OrderEntity> findById(String orderId) {
        System.out.println("🔍 DB 쿼리 실행: 주문 조회 - " + orderId);
        return Optional.ofNullable(database.get(orderId));
    }

    @Override
    public List<OrderEntity> findByUserId(String userId) {
        System.out.println("🔍 DB 쿼리 실행: 사용자 주문 목록 조회 - " + userId);

        List<OrderEntity> userOrderList = new ArrayList<>();
        for (OrderEntity order : database.values()) {
            if(order.getUserId().equals(userId)) {
                userOrderList.add(order);
            }
        }

        return userOrderList;
    }
}

// 사용자 주문 목록 DB 구현체

class DatabaseOrderItemRepository implements OrderItemRepository {

    private final Map<String, List<OrderItemEntity>> orderItemsDb;

    public DatabaseOrderItemRepository(Map<String, List<OrderItemEntity>> orderItemsDb) {
        this.orderItemsDb = orderItemsDb;
    }

    @Override
    public List<OrderItemEntity> findByOrderId(String orderId) {
        System.out.println("🔍 DB 쿼리 실행: 주문 항목 조회 - " + orderId);
        List<OrderItemEntity> items = orderItemsDb.get(orderId);
        return items != null ? items : new ArrayList<>();
    }
}

// 서비스 Repository
class OrderQueryService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository itemRepository;

    public OrderQueryService(OrderRepository orderRepository,
                             OrderItemRepository itemRepository) {
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
    }

    public void getOrderDetails(String orderId) {
        Optional<OrderEntity> orderOpt = orderRepository.findById(orderId);

        if (orderOpt.isEmpty()) {
            System.out.println("주문을 찾을 수 없습니다: " + orderId);
            return;
        }

        OrderEntity order = orderOpt.get();
        List<OrderItemEntity> items = itemRepository.findByOrderId(orderId);

        System.out.println("주문번호: " + order.getOrderId());
        System.out.println("주문일자: " + order.getOrderDate());
        System.out.println("총 금액: " + order.getTotalAmount() + "원");
        System.out.println("상태: " + order.getStatus());

        if(!items.isEmpty()) {
            System.out.println("주문 항목 : ");
            for(OrderItemEntity item : items) {
                System.out.println("  - " + item.getProductName() +
                        " x" + item.getQuantity() + " = " + item.getPrice() + "원");
            }
        }
    }

    public void getUserOrders(String userId) {
        List<OrderEntity> userOrders = orderRepository.findByUserId(userId);

        System.out.println(userId + "의 주문 " + userOrders.size() + "건");
        for(OrderEntity order : userOrders) {
            List<OrderItemEntity> items = itemRepository.findByOrderId(order.getOrderId());

            System.out.println("  주문: " + order.getOrderId() +
                    " (" + order.getOrderDate() + ") - " +
                    order.getTotalAmount() + "원");
        }
    }
}


class CachedOrderRepository implements  OrderRepository {
    private final OrderRepository delegate;
    private final Map<String, OrderEntity> cache;

    public CachedOrderRepository(OrderRepository delegate) {
        this.delegate = delegate;
        this.cache = new HashMap<>();
    }

    @Override
    public Optional<OrderEntity> findById(String orderId) {
        
        // 캐시 확인
        if(cache.containsKey(orderId)) {
            System.out.println("💾 캐시에서 조회: " + orderId);
            return Optional.of(cache.get(orderId));
        }

        // 캐시 미스/없음(DB 조회)
        Optional<OrderEntity> order = delegate.findById(orderId);

        order.ifPresent(o-> cache.put(orderId, o));
        
        return order;
    }

    @Override
    public List<OrderEntity> findByUserId(String userId) {
        // userId 기반 조회는 캐싱이 복잡하므로 일단 delegate로 위임
        return delegate.findByUserId(userId);
    }
}




// 엔티티 클래스
class OrderEntity {
    private String orderId;
    private String userId;
    private String orderDate;
    private int totalAmount;
    private String status;

    public OrderEntity(String orderId, String userId, String orderDate,
                       int totalAmount, String status) {
        this.orderId = orderId;
        this.userId = userId;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public String getOrderId() { return orderId; }
    public String getUserId() { return userId; }
    public String getOrderDate() { return orderDate; }
    public int getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
}

class OrderItemEntity {
    private String itemId;
    private String productName;
    private int quantity;
    private int price;

    public OrderItemEntity(String itemId, String productName,
                           int quantity, int price) {
        this.itemId = itemId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    public String getItemId() { return itemId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public int getPrice() { return price; }
}
