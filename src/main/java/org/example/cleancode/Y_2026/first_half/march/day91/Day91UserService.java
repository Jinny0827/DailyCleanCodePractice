package org.example.cleancode.Y_2026.first_half.march.day91;

import lombok.Data;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Day 91 — MapStruct: DTO ↔ Entity 변환 자동화
 *
 * 수동 setter 제거 — toResponse(), toEntity() 본문을 Mapper 인터페이스로 완전 대체
 * 필드명 불일치 처리 — @Mapping(source, target) 명시
 * 커스텀 변환 로직 — expression vs @Named 각각의 적절한 사용처 구분
 * 리스트 변환 — 메서드 선언 한 줄로 자동 위임
 */
public class Day91UserService {

    // Entity
    @Data
    public static class UserEntity {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private LocalDate birthDate;
        private String roleCode;
        private boolean active;
    }

    // DTO (응답용)
    @Data
    public static class Day91UserResponse {
        private Long id;
        private String fullName;
        private String email;
        private int age;
        private String role;
        private String status;
    }

    // DTO (요청용)
    @Data
    public static class Day91UserCreateRequest {
        private String firstName;
        private String lastName;
        private String email;
        private LocalDate birthDate;
        private String roleCode;
    }

    @Mapper(imports = LocalDate.class)
    public interface Day91UserMapper {

        Day91UserMapper INSTANCE = Mappers.getMapper(Day91UserMapper.class);

        @Mapping(source = "roleCode", target = "role")
        @Mapping(target = "fullName", expression = "java(entity.getFirstName() + \" \" + entity.getLastName())")
        @Mapping(target = "age",      expression = "java(LocalDate.now().getYear() - entity.getBirthDate().getYear())")
        @Mapping(target = "status",   source = "active", qualifiedByName = "activeToStatus")
        Day91UserService.Day91UserResponse toResponse(Day91UserService.UserEntity entity);

        @Named("activeToStatus")
        default String activeToStatus(boolean active) {
            return active ? "활성" : "비활성";
        }

        @Mapping(target = "active", constant = "true")
        @Mapping(target = "id", ignore = true)
        UserEntity toEntity(Day91UserCreateRequest request);

        List<Day91UserResponse> toResponseList(List<UserEntity> entities);
    }
}
