package org.example.cleancode.Y_2026.day66;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Day 66 — Decorator Pattern: 횡단 관심사 분리
 *
 * 로깅·검증·캐싱이 비즈니스 로직에 혼재된 코드를 Decorator로 분리
 * Decorator = 각 기능을 독립 클래스로 만들되, 자유롭게 조합할 수 있게 한다.
 */
public class Day66OrderService {
    private final OrderRepository repository;
    private final Map<String, Order> cache = new HashMap<>();

    public Day66OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    public static void main(String[] args) {
        // Repository 준비 (저장소)
        InMemoryOrderRepository repository = new InMemoryOrderRepository();
        repository.save(new Order("order-1", "홍길동", 50000));
        repository.save(new Order("order-2", "김철수", 30000));

        // Decorator 조합
        OrderService service = new LoggingDecorator(
                new CachingDecorator(
                        new ValidatingDecorator(
                                new CoreOrderService(repository)
                        )
                )
        );

        // 테스트
        System.out.println("=== 첫 번째 호출 (캐시 미스) ===");
        Order order1 = service.getOrder("order-1");
        System.out.println("결과: " + order1.getCustomerName());

        System.out.println("\n=== 두 번째 호출 (캐시 히트) ===");
        Order order2 = service.getOrder("order-1");
        System.out.println("결과: " + order2.getCustomerName());

        try {
            service.getOrder(null);
        } catch (IllegalArgumentException e) {
            System.out.println("예외 발생: " + e.getMessage());
        }
    }

    
    // 문제 코드 (원본)
    public Order getOrder(String orderId) {
        System.out.println("[LOG] getOrder 호출: " + orderId);

        if (orderId == null || orderId.isBlank()) {
            System.out.println("[LOG] 유효성 검사 실패");
            throw new IllegalArgumentException("orderId는 비어있지 않아야 합니다");
        }

        if (cache.containsKey(orderId)) {
            System.out.println("[LOG] 캐시 히트: " + orderId);
            return cache.get(orderId);
        }

        long start = System.currentTimeMillis();
        Order order = repository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        System.out.println("[LOG] 조회 시간: " + (System.currentTimeMillis() - start) + "ms");

        cache.put(orderId, order);
        return order;
    }
}


// 공통 계약 정의 인터페이스
interface OrderService {
    Order getOrder(String orderId);
}

// 비지니스 로직을 추출한 공통 계약 정의 구현체
class CoreOrderService implements OrderService {
    private final OrderRepository orderRepository;

    public CoreOrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Order getOrder(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }
}

// 공통 계약 추상 참조 클래스
abstract class AbstractOrderDecorator implements OrderService {
    private final OrderService orderService;

    public AbstractOrderDecorator(OrderService orderService) {
        this.orderService = orderService;
    }
    @Override
    public Order getOrder(String orderId) {
        return orderService.getOrder(orderId);
    }
}

// 주문 유효성 데코레이터
class ValidatingDecorator extends AbstractOrderDecorator {

    public ValidatingDecorator(OrderService wrapped) {
        super(wrapped);
    }

    @Override
    public Order getOrder(String orderId) {

        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId는 비어있지 않아야 합니다");
        }

        return super.getOrder(orderId);
    }
}

// 캐시 관리 데코레이터
class CachingDecorator extends AbstractOrderDecorator {
    private Map<String, Order> cachingDeco = new HashMap<>();

    public CachingDecorator(OrderService orderService) {
        super(orderService);
    }

    @Override
    public Order getOrder(String orderId) {
        
        if(cachingDeco.containsKey(orderId)) {
            return cachingDeco.get(orderId); // 캐시히트
        }
        
        // 캐시 미스 -> 아래로 위임
        Order order = super.getOrder(orderId);
        cachingDeco.put(orderId, order);

        return order;
    }
}

// 로그 관리 데코레이터
class LoggingDecorator extends AbstractOrderDecorator {

    public LoggingDecorator(OrderService wrapped) {
        super(wrapped);
    }

    @Override
    public Order getOrder(String orderId) {
        System.out.println("[LOG] getOrder 호출: " + orderId);

        return super.getOrder(orderId);
    }
}




// 주문 저장 인터페이스 (저장소)
interface OrderRepository {
    Optional<Order> findById(String orderId);
}

// 저장소 구현체
class InMemoryOrderRepository implements OrderRepository {
    private final Map<String, Order> store = new HashMap<>();

    public void save(Order order) {
        store.put(order.getOrderId(), order);
    }

    @Override
    public Optional<Order> findById(String orderId) {
        return Optional.ofNullable(store.get(orderId));
    }
}





//----- 도움 객체 -------
// 커스텀 예외 구현
class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String orderId) {
        super("주문을 찾을 수 없습니다: " + orderId);
    }
}

// 주문 객체
class Order {
    private final String orderId;
    private final String customerName;
    private final int totalAmount;

    public Order(String orderId, String customerName, int totalAmount) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.totalAmount = totalAmount;
    }

    public String getOrderId()        { return orderId; }
    public String getCustomerName()   { return customerName; }
    public int getTotalAmount()       { return totalAmount; }
}
