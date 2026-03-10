package org.example.cleancode.Y_2026.first_half.day87;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Day 87 과제 — Apache Commons Lang: 보일러플레이트 제거
 *
 * 1. null + 공백 체크 중복StringUtils.isBlank()
 * 2. 이름 조합 분기 장황StringUtils.joinWith() / StringUtils.defaultIfBlank()
 * 3. null 대체값 처리 반복StringUtils.defaultString()
 * 4. 다단계 null-safe 비교CompareToBuilder
 */
public class Day87UserValidator {

    public String formatUserSummary(User user) {
        if (user == null) {
            return "Unknown";
        }

        String firstName = StringUtils.defaultIfBlank(user.getFirstName(), "");
        String lastName = StringUtils.defaultIfBlank(user.getLastName(), "");
        String role = StringUtils.defaultIfBlank(user.getRole(), "GUEST");

        String fullName =
                StringUtils.defaultIfBlank(
                                    Stream.of(firstName,lastName)
                                        .filter(StringUtils::isNotBlank)
                                        .collect(Collectors.joining(" ")),
                        "Unknown");




        String agePart = (user.getAge() != null && user.getAge() > 0)
                ? " (" + user.getAge() + ")"
                : "";

        return String.format("[%s] %s%s", role.toUpperCase(), fullName, agePart);
    }

    public int compareUsers(User a, User b) {
        return new CompareToBuilder()
                .append(a.getLastName(), b.getLastName())
                .append(a.getFirstName(), b.getFirstName())
                .append(a.getAge(), b.getAge())
                .toComparison();
    }
}

class Main {
    public static void main(String[] args) {
        Day87UserValidator validator = new Day87UserValidator();
        UserRepository repo = new UserRepository();

        // formatUserSummary 테스트
        repo.findAll().forEach(user ->
                System.out.println(validator.formatUserSummary(user))
        );

        // compareUsers 테스트
        List<User> users = repo.findAll();
        users.sort(validator::compareUsers);
        System.out.println("\n--- 정렬 결과 ---");
        users.forEach(u -> System.out.println(validator.formatUserSummary(u)));
    }
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class User {
    private Long id;
    private String firstName;
    private String lastName;
    private String role;
    private Integer age;
}

class UserRepository {

    private static final Map<Long, User> store = new HashMap<>();

    static {
        store.put(1L, new User(1L, "Gildong", "Hong", "ADMIN", 30));
        store.put(2L, new User(2L, "Younghee", null,   "USER",  25));
        store.put(3L, new User(3L, null,       "Kim",  null,    -1));
        store.put(4L, new User(4L, null,       null,   null,    null));
        store.put(5L, new User(5L, "  ",       "Lee",  "MANAGER", 40));
    }

    public User findById(Long id) {
        return store.get(id);
    }

    public List<User> findAll() {
        return new ArrayList<>(store.values());
    }
}
