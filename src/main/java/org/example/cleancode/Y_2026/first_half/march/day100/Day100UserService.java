package org.example.cleancode.Y_2026.first_half.march.day100;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
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
        // toResponse 테스트
        UserEntity entity = new UserEntity(
                1L, "길동", "홍", "hong@test.com", "010-1234-5678",
                "서울시", Role.ADMIN, LocalDateTime.now(), true
        );
        UserResponse response = UserMapper.INSTANCE.toResponse(entity);
        System.out.println(response);

        // toEntity 테스트
        CreateUserRequest req = new CreateUserRequest("길동 홍", "hong@test.com", "010-1234-5678", "서울시", "ADMIN");
        UserEntity result = UserMapper.INSTANCE.toEntity(req);
        System.out.println(result);

        // toResponseList 테스트
        List<UserEntity> entities = List.of(
                new UserEntity(1L, "길동", "홍", "hong@test.com", "010-1111-1111", "서울", Role.ADMIN, LocalDateTime.now(), true),
                new UserEntity(2L, "영희", "김", "kim@test.com", "010-2222-2222", "부산", Role.USER, LocalDateTime.now(), true)
        );
        List<UserResponse> list = UserMapper.INSTANCE.toResponseList(entities);
        list.forEach(System.out::println);
    }
}

// 매핑 인터페이스
@Mapper
interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    // Entity → Response
    @Mapping(target = "roleName", source = "role")
    @Mapping(target = "fullName", expression = "java(entity.getFirstName() + \" \" + entity.getLastName())")
    UserResponse toResponse(UserEntity entity);

    // List 매핑 (toResponse 재사용)
    List<UserResponse> toResponseList(List<UserEntity> entities);

    // Request → Entity
    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "firstName", ignore = true)
    @Mapping(target = "lastName",  ignore = true)
    @Mapping(target = "role",      ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "active",    ignore = true)
    UserEntity toEntity(CreateUserRequest request);

    @AfterMapping
    default void afterToEntity(CreateUserRequest request, @MappingTarget UserEntity entity) {
        String fullName = request.getFullName();
        if (fullName != null && fullName.contains(" ")) {
            entity.setFirstName(fullName.substring(0, fullName.indexOf(" ")));
            entity.setLastName(fullName.substring(fullName.indexOf(" ") + 1));
        } else {
            entity.setFirstName(fullName);
            entity.setLastName("");
        }
        entity.setRole(Role.valueOf(request.getRoleName()));
        entity.setCreatedAt(LocalDateTime.now());
        entity.setActive(true);
    }
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
    private String fullName;
    private String email;
    private String phone;
    private String createdAt;
    private String roleName;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class CreateUserRequest {
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String roleName;
}

enum Role {
    ADMIN, USER, GUEST
}