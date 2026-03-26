package org.example.cleancode.Y_2026.first_half.march.day100;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Day 100 — MapStruct: DTO ↔ Entity 매핑 자동화
 *
 * 1. @Mapper 인터페이스 선언만으로 구현체 자동 생성
 * 2. @Mapping으로 필드명/타입 불일치 명시적 처리
 * 3. expression / qualifiedByName으로 커스텀 변환 로직 분리
 * 4. @AfterMapping으로 매핑 후처리 (active=true 기본값 등)
 * 5. List 매핑 메서드 중복 제거
 */
public class Day100UserService {

    public static void main(String[] args) {
        UserEntity entity = new UserEntity(
                1L, "길동", "홍", "hong@test.com", "010-1234-5678",
                "서울시", Role.ADMIN, LocalDateTime.now(), true
        );

        UserResponse response = UserMapper.INSTANCE.toResponse(entity);
        System.out.println(response);

    }


    public UserResponse toResponse(UserEntity entity) {
        UserResponse response = new UserResponse();
        response.setId(entity.getId());
        response.setFullName(entity.getFirstName() + " " + entity.getLastName()); // 조합 필요
        response.setEmail(entity.getEmail());
        response.setPhone(entity.getPhone());
        response.setCreatedAt(entity.getCreatedAt().toString()); // 타입 변환 직접
        response.setRoleName(entity.getRole().name());           // enum → String 직접
        return response;
    }

    public UserEntity toEntity(CreateUserRequest request) {
        UserEntity entity = new UserEntity();
        entity.setFirstName(extractFirst(request.getFullName()));
        entity.setLastName(extractLast(request.getFullName()));
        entity.setEmail(request.getEmail());
        entity.setPhone(request.getPhone());
        entity.setRole(Role.valueOf(request.getRoleName()));
        entity.setCreatedAt(LocalDateTime.now());
        entity.setActive(true);
        entity.setAddress(request.getAddress());
        return entity;
    }

    public List<UserResponse> toResponseList(List<UserEntity> entities) {
        List<UserResponse> result = new ArrayList<>();
        for (UserEntity entity : entities) {   // stream 미사용, toResponse() 중복 호출
            result.add(toResponse(entity));
        }
        return result;
    }

    private String extractFirst(String fullName) {
        if (fullName == null || !fullName.contains(" ")) return fullName;
        return fullName.substring(0, fullName.indexOf(" "));
    }

    private String extractLast(String fullName) {
        if (fullName == null || !fullName.contains(" ")) return "";
        return fullName.substring(fullName.indexOf(" ") + 1);
    }

}

// 매핑 인터페이스
@Mapper
interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    // 반환 객체에 대한 자동 매핑
    @Mapping(target="roleName", source = "role")
    @Mapping(target="fullName", expression = "java(entity.getFirstName() + \" \" + entity.getLastName())")
    UserResponse toResponse(UserEntity entity);

    // 요청 객체에 대한 자동 매핑
    @
    UserEntity toEntity(CreateUserRequest request);

}


@Data
@AllArgsConstructor
@NoArgsConstructor
class UserEntity {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private Role role;
    private LocalDateTime createdAt;
    private boolean active;
}

@Data
class UserResponse {
    private Long id;
    private String fullName;      // firstName + lastName 조합
    private String email;
    private String phone;
    private String createdAt;     // LocalDateTime → String 변환
    private String roleName;      // Role enum → String 변환
}

@Data
class CreateUserRequest {
    private String fullName;      // → firstName / lastName 분리
    private String email;
    private String phone;
    private String address;
    private String roleName;      // → Role enum 변환
}

enum Role {
    ADMIN, USER, GUEST
}