package org.example.cleancode.Y_2026.first_half.april.day109;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingInheritanceStrategy;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

/**
 * Day 109 — MapStruct: DTO ↔ Entity 변환 자동화
 *
 * 필드명 불일치 (emailAddress → email) -> @Mapping(source=, target=)
 * fullName 조합 -> @Mapping(expression = "java(...)")
 * 나이 계산 (birthDate → age) -> default 메서드 or @Named + @Mapping(qualifiedByName=)
 * role 코드 → 한글 -> @Named 변환 메서드
 * 목록 변환 -> 시그니처만 선언하면 자동 생성
 */
public class Day109UserMapperManual {

    public static void main(String[] args) {
        UserMapper mapper = Mappers.getMapper(UserMapper.class);
        UserEntity entity = new UserEntity(1L, "길동", "홍", "hong@email.com",
                LocalDate.of(1995, 3, 10), "ADMIN");

        UserResponse res = mapper.toResponse(entity);
        System.out.println(res);
    }
}

@Mapper
interface UserMapper {

    @Mapping(
            target = "fullName",
            expression = "java(entity.getFirstName() + \" \" + entity.getLastName())"
    )
    @Mapping(source = "roleCode", target = "role", qualifiedByName = "toRoleLabel")
    @Mapping(source = "birthDate", target = "age",   qualifiedByName = "toAge")
    @Mapping(source = "emailAddress", target = "email")
    UserResponse toResponse(UserEntity entity);

    List<UserResponse> toResponseList(List<UserEntity> entities);

    @Named("toAge")
    default int toAge(LocalDate birthDate) {
        if (birthDate != null) {
            return Period.between(birthDate, LocalDate.now()).getYears();
        }

        return 0;
    }

    @Named("toRoleLabel")
    default String toRoleLabel(String roleCode) {
        return switch (roleCode) {
            case "ADMIN" -> "관리자";
            case "USER"  -> "일반사용자";
            default      -> "게스트";
        };
    }
}


@Data
@AllArgsConstructor
class UserEntity {
    private Long id;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private LocalDate birthDate;
    private String roleCode;      // "ADMIN", "USER", "GUEST"
}

@Data
class UserResponse {
    private Long id;
    private String fullName;       // firstName + " " + lastName
    private String email;          // emailAddress 필드명 다름
    private int age;               // birthDate → 나이 계산
    private String role;           // "ADMIN" → "관리자" 변환
}