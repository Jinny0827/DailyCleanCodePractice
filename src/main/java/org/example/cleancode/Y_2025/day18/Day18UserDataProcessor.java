package org.example.cleancode.Y_2025.day18;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Day 18: 데이터 변환 파이프라인
 *
 * 문제점:
 * - 모든 변환 로직이 한 메서드에 집중
 * - 단계별 처리 과정이 명확하지 않음
 * - 중간 결과를 추적하기 어려움
 * - 특정 단계만 재사용하기 어려움
 */
public class Day18UserDataProcessor {

    public static void main(String[] args) {
        List<RawUserData> rawData = Arrays.asList(
                new RawUserData("john_doe", "JOHN@EXAMPLE.COM", "25", "true"),
                new RawUserData("", "invalid-email", "abc", "false"),
                new RawUserData("jane_smith", "JANE@TEST.COM", "17", "true"),
                new RawUserData("bob_wilson", "bob@company.com", "30", "false")
        );

        List<ProcessedUser> result = processUsers(rawData);

        System.out.println("=== 처리된 사용자 ===");
        result.forEach(System.out::println);
    }

    // 문제: 모든 처리가 하나의 메서드에 몰려있음
    public static List<ProcessedUser> processUsers(List<RawUserData> rawDataList) {

        return rawDataList.stream()
                .peek(raw -> System.out.println("처리 시작 : " + raw.getUsername()))
                .filter(RawUserData::isValid)
                .filter(Day18UserDataProcessor::isAdult)
                .peek(raw -> System.out.println("검증 통과 : " + raw.getUsername()))
                .map(Day18UserDataProcessor::normalize)
                .map(Day18UserDataProcessor::toProcessedUser)
                .peek(user -> System.out.println("변환 완료 : " + user))
                .collect(Collectors.toList());
    }

    // String으로 들어오는 나이를 옵셔널 타입으로 변환
    private static Optional<Integer> parseAge(String ageStr) {
        try {
            return Optional.of(Integer.parseInt(ageStr));
        } catch(NumberFormatException e) {
            return Optional.empty();
        }
    }

    // 성인 필터 메서드
    private static boolean isAdult(RawUserData raw) {
        Optional<Integer> age = parseAge(raw.getAge());
        return age.isPresent() && age.get() >= 18;
    }

    private static NormalizedUserData normalize(RawUserData raw) {
        String username = raw.getUsername().toLowerCase().trim();
        String email = raw.getEmail().toLowerCase().trim();
        int age = parseAge(raw.getAge()).orElseThrow();
        boolean isPremium = Boolean.parseBoolean(raw.getIsPremium());

        return new NormalizedUserData(username, email, age, isPremium);
    }

    // 회원 등급 변환 메서드
    private static String calculateGrade(int age, boolean isPremium) {
        if (isPremium && age >= 25) {
                return "GOLD";
            } else if (isPremium) {
                return "SILVER";
            } else if (age >= 25) {
                return "BRONZE";
            } else {
                return "BASIC";
            }
    }

    // NormalizedUserData -> ProcessedUser 로 변환해주는 헬퍼 메서드
    private static ProcessedUser toProcessedUser(NormalizedUserData normalized) {
        String grade = calculateGrade(normalized.getAge(), normalized.isPremium());

        return new ProcessedUser(
                normalized.getUsername(),
                normalized.getEmail(),
                normalized.getAge(),
                normalized.isPremium(),
                grade
        );
    }
}

//중간 DTO 클래스
class NormalizedUserData {
    private final String username;
    private final String email;
    private final int age;
    private final boolean isPremium;

    public NormalizedUserData(String username, String email, int age, boolean isPremium) {
        this.username = username;
        this.email = email;
        this.age = age;
        this.isPremium = isPremium;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public int getAge() {
        return age;
    }

    public boolean isPremium() {
        return isPremium;
    }
}

class RawUserData {
    private final String username;
    private final String email;
    private final String age;
    private final String isPremium;

    public RawUserData(String username, String email, String age, String isPremium) {
        this.username = username;
        this.email = email;
        this.age = age;
        this.isPremium = isPremium;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getAge() {
        return age;
    }

    public String getIsPremium() {
        return isPremium;
    }

    // 기본 유효성 검사
    public boolean isValid() {
        return username != null && !username.isEmpty()
                && email != null && email.contains("@");
    }

}

class ProcessedUser {
    private final String username;
    private final String email;
    private final int age;
    private final boolean isPremium;
    private final String grade;

    public ProcessedUser(String username, String email, int age,
                         boolean isPremium, String grade) {
        this.username = username;
        this.email = email;
        this.age = age;
        this.isPremium = isPremium;
        this.grade = grade;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - Age: %d, Grade: %s",
                username, email, age, grade);
    }
}
