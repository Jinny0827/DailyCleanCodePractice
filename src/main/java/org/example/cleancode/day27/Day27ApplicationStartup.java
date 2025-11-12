package org.example.cleancode.day27;


import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Day 27: 서비스 초기화 및 의존성 관리
 *
 * 문제점:
 * - 모든 서비스가 직접 의존성을 생성 (new 키워드 남발)
 * - 싱글톤 인스턴스 관리가 각 클래스에 분산됨
 * - 초기화 순서 문제 발생 가능
 * - 테스트 시 의존성 교체 불가능
 * - 순환 의존성 발생 가능성
 */

public class Day27ApplicationStartup {

    public static void main(String[] args) {
        ServiceContainer serviceContainer = new ServiceContainer();

        // 컨테이너 등록 시 싱글톤 보장
        serviceContainer.register(Logger.class, new Logger());

        Database database = new Database();
        database.connect();
        serviceContainer.register(Database.class, database);

        // 자동 등록 (팩토리 람다 불필요)
        serviceContainer.registerAuto(EmailService.class);
        serviceContainer.registerAuto(UserRepository.class);
        serviceContainer.registerAuto(OrderRepository.class);
        serviceContainer.registerAuto(UserService.class);
        serviceContainer.registerAuto(OrderService.class);

        System.out.println("\n=== 서비스 사용 ===");

        UserService userService = serviceContainer.resolve(UserService.class);
        userService.registerUser("user@test.com", "John Doe");

        System.out.println();

        OrderService orderService = serviceContainer.resolve(OrderService.class);
        orderService.createOrder("user@test.com", 50000);
    }

}


// DI 컨테이너 구현
class ServiceContainer {
    private Map<Class<?>, Object> instances = new HashMap<>();
    private Map<Class<?>, Supplier<?>> factories = new HashMap<>();


    /**
     * 서비스를 컨테이너에 등록(싱글톤)
     * @param serviceType 서비스의 클래스 타입
     * @param instance 등록할 인스턴스
     */
    public <T> void register(Class<T> serviceType, T instance) {
        if(instances == null) {
            throw new IllegalArgumentException("serviceType은 null일 수 없습니다.");
        }

        if (instance == null) {
            throw new IllegalArgumentException("instance는 null일 수 없습니다");
        }

        instances.put(serviceType, instance);
        System.out.println("✓ " + serviceType.getSimpleName() + " 등록됨");
    }

    /**
     * 서비스를 컨테이너에 팩토리로 등록 (지연생성, 싱글톤)
     * */
    public <T> void registerFactory(Class<T> serviceType, Supplier<T> factory) {
        if (serviceType == null) {
            throw new IllegalArgumentException("serviceType은 null일 수 없습니다");
        }
        if (factory == null) {
            throw new IllegalArgumentException("factory는 null일 수 없습니다");
        }

        factories.put(serviceType, factory);
        System.out.println("✓ " + serviceType.getSimpleName() + " 등록됨");
    }


    /**
     * 등록된 서비스를 조회 (필요시 팩토리로 생성)
     * @param serviceType 조회할 서비스의 클래스 타입
     * @return 등록된 인스턴스
     */
    @SuppressWarnings("unchecked")
    public <T> T resolve(Class<T> serviceType) {
        // 이미 생성된 인스턴스가 있으면 반환
        if (instances.containsKey(serviceType)) {
            return (T) instances.get(serviceType);
        }


        if(factories.containsKey(serviceType)) {
            System.out.println("🔧 " + serviceType.getSimpleName() + " 생성 중...");
            T instance = (T) factories.get(serviceType).get();
            instances.put(serviceType, instance);
            return instance;
        }

        throw new IllegalStateException(
                serviceType.getSimpleName() + "이(가) 등록되지 않았습니다"
        );
    }

    /**
     * 생성자를 자동으로 분석하여 등록 (리플렉션)
     */
    public <T> void registerAuto(Class<T> serviceType) {
        if (serviceType == null) {
            throw new IllegalArgumentException("serviceType은 null일 수 없습니다");
        }

        // 람다로 팩토리 생성 - 실제 생성은 resolve 시점에
        registerFactory(serviceType, () -> createInstance(serviceType));
    }

    /**
     * 리플렉션으로 인스턴스 생성
     */
    @SuppressWarnings("unchecked")
    public <T> T createInstance(Class<T> serviceType) {
        try {
            var constructors  = serviceType.getDeclaredConstructors();

            if(constructors .length == 0) {
                throw new IllegalStateException(
                        serviceType.getSimpleName() + "에 생성자가 없습니다"
                );
            }

            // 첫 번째 생성자 사용
            var constructor = constructors[0];
            var parameterTypes = constructor.getParameterTypes();

            // 파라미터 없을 시 생성
            if(parameterTypes.length == 0) {
                return (T) constructor.newInstance();
            }

            // 각 파라미터를 resolve해서 의존성 주입
           Object[] dependencies = new Object[parameterTypes.length];
            for(int i = 0; i < parameterTypes.length; i++) {
                dependencies[i] = resolve(parameterTypes[i]);
            }

            return (T) constructor.newInstance(dependencies);
        } catch (Exception e) {
            throw new RuntimeException(
                    serviceType.getSimpleName() + " 생성 실패: " + e.getMessage(),
                    e
            );
        }
    }

}

class Logger {
    public void log(String message) {
        System.out.println("[LOG] " + message);
    }
}

class Database {
    private boolean connected = false;

    public void connect() {
        connected = true;
        System.out.println("💾 데이터베이스 연결됨");
    }

    public void query(String sql) {
        if (!connected) {
            throw new IllegalStateException("데이터베이스가 연결되지 않았습니다");
        }
        System.out.println("🔍 쿼리 실행: " + sql);
    }
}

class EmailService {
    private Logger logger;

    public EmailService(Logger logger) {
        this.logger = logger;
    }

    public void send(String to, String message) {
        logger.log("이메일 발송: " + to);
        System.out.println("📧 " + to + "에게 발송: " + message);
    }
}

class UserRepository {
    private Database database;
    private Logger logger;

    public UserRepository(Database database, Logger logger) {
        this.database = database;
        this.logger = logger;
    }

    public void save(String email, String name) {
        logger.log("사용자 저장: " + email);
        database.query("INSERT INTO users VALUES ('" + email + "', '" + name + "')");
    }

    public boolean exists(String email) {
        logger.log("사용자 조회: " + email);
        database.query("SELECT * FROM users WHERE email = '" + email + "'");
        return true; // 시뮬레이션
    }
}

class OrderRepository {
    private Database database;
    private Logger logger;

    public OrderRepository(Database database, Logger logger) {
        this.database = database;
        this.logger = logger;
    }

    public void save(String userEmail, int amount) {
        logger.log("주문 저장: " + userEmail + ", " + amount + "원");
        database.query("INSERT INTO orders VALUES ('" + userEmail + "', " + amount + ")");
    }
}

class UserService {
    private UserRepository userRepository;
    private EmailService emailService;
    private Logger logger;

    public UserService(UserRepository userRepository,
                       EmailService emailService,
                       Logger logger) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.logger = logger;
    }

    public void registerUser(String email, String name) {
        logger.log("사용자 등록 시작: " + email);
        userRepository.save(email, name);
        emailService.send(email, "환영합니다!");
    }
}

class OrderService {
    private OrderRepository orderRepository;
    private UserRepository userRepository;
    private EmailService emailService;
    private Logger logger;

    public OrderService(OrderRepository orderRepository,
                        UserRepository userRepository,
                        EmailService emailService,
                        Logger logger) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.logger = logger;
    }

    public void createOrder(String userEmail, int amount) {
        logger.log("주문 생성 시작: " + userEmail);

        if (!userRepository.exists(userEmail)) {
            logger.log("사용자를 찾을 수 없음");
            return;
        }

        orderRepository.save(userEmail, amount);
        emailService.send(userEmail, "주문이 완료되었습니다. 금액: " + amount + "원");
    }
}
