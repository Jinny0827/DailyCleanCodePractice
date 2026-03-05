package org.example.cleancode.Y_2026.day86;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;

/**
 * Day 86 — MapStruct로 DTO 변환 코드 제거
 *
 * 1. 수동 setter 제거 → @Mapper 인터페이스로 대체
 * 2. firstName + lastName 조합은 expression 사용
 * 3. Order → OrderResponse 변환도 별도 매핑 메서드로 분리
 * 4. status.name() 같은 타입 변환은 @Mapping(qualifiedByName=...) 활용
 */
public class Day86UserService {

    private final UserRepository userRepository;

    public Day86UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse getUser(Long id) {
        User user = userRepository.findById(id);

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFullName(user.getFirstName() + " " + user.getLastName());
        response.setEmail(user.getEmail());
        response.setAge(user.getAge());
        response.setPhone(user.getPhone());

        AddressResponse address = new AddressResponse();
        address.setStreet(user.getAddress().getStreet());
        address.setCity(user.getAddress().getCity());
        address.setZipCode(user.getAddress().getZipCode());
        response.setAddress(address);

        List<OrderResponse> orders = new ArrayList<>();
        for (Order order : user.getOrders()) {
            OrderResponse orderResp = new OrderResponse();
            orderResp.setOrderId(order.getId());
            orderResp.setAmount(order.getAmount());
            orderResp.setStatus(order.getStatus().name());
            orders.add(orderResp);
        }
        response.setOrders(orders);

        return response;
    }
}

class Main {
    public static void main(String[] args) {
        UserRepository repository = new UserRepository();
        UserMapper mapper = Mappers.getMapper(UserMapper.class);

        User user = repository.findById(1L);
        UserResponse response = mapper.toResponse(user);

        System.out.println(response);
    }
}

// 수동 setter를 mapper로 대체
@Mapper
interface OrderMapper {
    // Order의 id가 1이면 OrderResponse의 orderId에 1을 매핑해주는 개념
    @Mapping(source = "id", target= "orderId")
    OrderResponse toResponse(Order order);
}

@Mapper
interface AddressMapper {
    // Address와 AddressResponse의 필드명이 같으므로 매핑할 필요 X
    AddressResponse toResponse(Address adress);
}

@Mapper(uses = {AddressMapper.class, OrderMapper.class})
interface UserMapper {
    // User의 firstName과 lastName을 합친 fullName 필드 생성
    // 두 필드 합치기
    @Mapping(target = "fullName",
                expression = "java(user.getFirstName() +  \" \" + user.getLastName())")
    UserResponse toResponse(User user);
}



class UserRepository {

    private static final List<User> USERS = List.of(
            new User(
                    1L, "길동", "홍", "hong@example.com", 30, "010-1234-5678",
                    new Address("테헤란로 123", "서울", "06142"),
                    List.of(
                            new Order(101L, 50000, OrderStatus.PAID),
                            new Order(102L, 30000, OrderStatus.PENDING)
                    )
            ),
            new User(
                    2L, "영희", "김", "kim@example.com", 25, "010-9876-5432",
                    new Address("강남대로 456", "서울", "06100"),
                    List.of(
                            new Order(201L, 120000, OrderStatus.CANCELLED)
                    )
            )
    );

    public User findById(Long id) {
        return USERS.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }
}


// ===== Entity =====

@Data
@AllArgsConstructor
class User {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private int age;
    private String phone;
    private Address address;
    private List<Order> orders;
}

@Data
@AllArgsConstructor
class Address {
    private String street;
    private String city;
    private String zipCode;
}

@Data
@AllArgsConstructor
class Order {
    private Long id;
    private double amount;
    private OrderStatus status;
}

enum OrderStatus {
    PENDING, PAID, CANCELLED
}

// ===== DTO =====

@Data
class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private int age;
    private String phone;
    private AddressResponse address;
    private List<OrderResponse> orders;
}

@Data
class AddressResponse {
    private String street;
    private String city;
    private String zipCode;
}

@Data
class OrderResponse {
    private Long orderId;
    private double amount;
    private String status;
}


