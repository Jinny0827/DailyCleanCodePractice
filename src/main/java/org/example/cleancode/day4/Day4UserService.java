package org.example.cleancode.day4;

public class Day4UserService {

    private static final String GRADE_PLATINUM = "PLATINUM";
    private static final String GRADE_GOLD= "GOLD";
    private static final String GRADE_SILVER = "SILVER";
    private static final String GRADE_BRONZE = "BRONZE";

    private static  final int MIN_SCORE = 0;
    private static  final int MAX_SCORE = 100;
    private static final int PLATINUM_SCORE = 90;
    private static final int PREMIUM_GOLD_SCORE = 70;
    private static final int PREMIUM_NONE_LOGIN_GOLD_SCORE = 70;
    private static final int NORMAL_GOLD_SCORE = 90;
    private static final int NORMAL_SILVER_SCORE = 60;
    private static final int PREMIUM_LOGIN_DAYS_THRESHOLD = 30;





    public String getUserGrade(int score, boolean isPremium, int loginDays) {

        // !를 붙일수 없는 조건에선 조건을 정확히 파악하여 조건문을 완성하라
        if(score < MIN_SCORE || score > MAX_SCORE) {
            return "INVALID";
        }

        if(isPremium) {
            return getPremiumUserGrade(score, loginDays);
        } else {
            return getNormalUserGrade(score, loginDays);
        }
    }

    private String getPremiumUserGrade(int score, int loginDays) {
        if(loginDays >= PREMIUM_LOGIN_DAYS_THRESHOLD) {
            return getLongTermPremiumGrade(score);
        } else {
            return getShortTermPremiumGrade(score);
        }
    }

    private String getLongTermPremiumGrade(int score) {
        if(score >= PLATINUM_SCORE) return GRADE_PLATINUM;
        if(score >= PREMIUM_GOLD_SCORE) return GRADE_GOLD;
        return GRADE_SILVER;
    }

    private String getShortTermPremiumGrade(int score) {
        if(score >= PREMIUM_NONE_LOGIN_GOLD_SCORE) return GRADE_GOLD;
        return GRADE_SILVER;
    }

    private String getNormalUserGrade(int score, int loginDays) {
        if(score >= NORMAL_GOLD_SCORE) return GRADE_GOLD;
        if(score >= NORMAL_SILVER_SCORE) return GRADE_SILVER;
        return GRADE_BRONZE;
    }
}
