package org.example.cleancode.Y_2025.day24;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Day 24: API 응답 처리 및 DTO 패턴
 *
 * 문제점:
 * - 데이터베이스 엔티티를 직접 API 응답으로 반환
 * - 민감한 정보(비밀번호)가 그대로 노출될 위험
 * - API 응답 구조와 내부 데이터 구조가 강하게 결합
 * - 프론트엔드가 필요하지 않은 정보까지 전송
 */
public class Day24UserApiController {


    private UserRepository userRepository = new UserRepository();

    public static void main(String[] args) {
        Day24UserApiController controller = new Day24UserApiController();

        System.out.println("=== 사용자 조회 ===");
        UserDetailResponse detail = controller.getUserById("USER-001");
        System.out.println(detail);

        System.out.println("\n=== 전체 사용자 ===");
        List<UserListResponse> users = controller.getAllUsers();
        users.forEach(System.out::println);

        System.out.println("\n=== 프로필 조회 ===");
        UserProfileResponse profile = controller.getUserProfile("USER-002");
        System.out.println(profile);
    }

    public UserDetailResponse getUserById(String userId) {
        User user = userRepository.findById(userId);
        return UserMapper.toDetailResponse(user);
    }

    public List<UserListResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserListResponse> listResponse = new ArrayList<>();

        for(User user : users) {
            listResponse.add(UserMapper.toListResponse(user));
        }

        return listResponse;
    }

    public UserProfileResponse getUserProfile(String userId) {
        User user = userRepository.findById(userId);
        return UserMapper.toProfileResponse(user);
    }
}

// DB 엔티티 (모든 필드 포함)
class User {
    private String id;
    private String username;
    private String email;
    private String password;  // 민감 정보!
    private String phone;
    private String address;
    private String role;
    private boolean active;
    private String createdAt;
    private String lastLoginAt;

    public User(String id, String username, String email, String password,
                String phone, String address, String role, boolean active,
                String createdAt, String lastLoginAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.address = address;
        this.role = role;
        this.active = active;
        this.createdAt = createdAt;
        this.lastLoginAt = lastLoginAt;
    }

    // Getters
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getRole() { return role; }
    public boolean isActive() { return active; }
    public String getCreatedAt() { return createdAt; }
    public String getLastLoginAt() { return lastLoginAt; }

    @Override
    public String toString() {
        return "User{id='" + id + "', username='" + username +
                "', email='" + email + "'," +
                "', phone='" + phone + "', role='" + role + "'}";
    }
}

class UserRepository {
    private Map<String, User> database = new HashMap<>();

    public UserRepository() {
        database.put("USER-001", new User(
                "USER-001", "john_doe", "john@test.com", "secret123!",
                "010-1234-5678", "서울시 강남구", "USER", true,
                "2024-01-15", "2024-11-20"
        ));
        database.put("USER-002", new User(
                "USER-002", "jane_smith", "jane@test.com", "pass456!",
                "010-9876-5432", "서울시 서초구", "ADMIN", true,
                "2024-02-20", "2024-11-22"
        ));
    }

    public User findById(String id) {
        return database.get(id);
    }

    public List<User> findAll() {
        return new ArrayList<>(database.values());
    }
}

class UserListResponse {
    private String id;
    private String username;
    private String email;

    public UserListResponse(String id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "UserListResponse{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}

class UserDetailResponse {
    private String id;
    private String username;
    private String email;
    private String phone;
    private String address;
    private String role;
    private boolean active;
    private String createdAt;


    public UserDetailResponse(String id, String username, String email, String phone, String address, String role, boolean active, String createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.role = role;
        this.active = active;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "UserDetailResponse{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", role='" + role + '\'' +
                ", active=" + active +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}

class UserProfileResponse {
    private String username;
    private String email;
    private String role;

    public UserProfileResponse(String username, String email, String role) {
        this.username = username;
        this.email = email;
        this.role = role;
    }


    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    @Override
    public String toString() {
        return "UserProfileResponse{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}

class UserMapper {
    public static UserListResponse toListResponse(User user) {
        // User -> UserListResponse 변환
        return new UserListResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }

    public static UserDetailResponse toDetailResponse(User user) {
        // User -> UserDetailResponse 변환
        return new UserDetailResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt()
        );
    }

    public static UserProfileResponse toProfileResponse(User user) {
        // User -> UserProfileResponse 변환
        return new UserProfileResponse(
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }
}


