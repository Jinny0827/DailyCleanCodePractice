package org.example.cleancode.day2;

import java.util.Set;

public class Day2UserValidator {

    // 사용자명 관련 상수
    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MAX_USERNAME_LENGTH = 20;

    // 이메일 관련 상수
    private static final int MAX_EMAIL_LENGTH = 50;
    private static final String EMAIL_REQUIRED_CHAR = "@";

    // 나이 관련 상수
    private static final int MIN_AGE = 14;
    private static final int MAX_AGE = 120;

    private static final Set<String> FORBIDDEN_USERNAMES = Set.of(
            "admin","root","system"
    );

    // 유저 레벨 관련 enum
    public enum UserLevel {
        GOLD(1000),
        SILVER(500),
        BRONZE(100),
        NORMAL(0);

        private final int threshold;

        UserLevel(int threshold) {
            this.threshold = threshold;
        }


        public int getThreshold() {
            return threshold;
        }

        public static UserLevel fromScore(int score) {
            if(score >= GOLD.getThreshold()) return GOLD;
            if(score >= SILVER.getThreshold()) return SILVER;
            if(score >= BRONZE.getThreshold()) return BRONZE;

            return NORMAL;
        }
    }


    public static boolean isValidUser(String username, String email, int age) {
        // 사용자 유효성 검사
        if (username == null || username.length() < MIN_USERNAME_LENGTH || username.length() > MAX_USERNAME_LENGTH) {
            return false;
        }

        if (email == null || !email.contains(EMAIL_REQUIRED_CHAR) || email.length() > MAX_EMAIL_LENGTH) {
            return false;
        }

        if (age < MIN_AGE || age > MAX_AGE) {
            return false;
        }

        // 금지된 사용자명 체크
        if (FORBIDDEN_USERNAMES.contains(username)) {
            return false;
        }

        return true;
    }

    public static UserLevel getUserLevel(int score) {
      return UserLevel.fromScore(score);
    }

    public static void main(String[] args) {
        System.out.println(isValidUser("john", "john@email.com", 25));
        System.out.println(getUserLevel(750));
    }
}
