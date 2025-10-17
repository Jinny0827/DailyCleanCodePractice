package org.example.cleancode.day5;

public class Day5UserGradeSystem {

    /**
     * 사용자 등급 시스템
     * 사용자의 포인트에따라 따라 배달 수수료, 할인율 처리
     *
     *
     * # 개선 포인트
     * 마법 숫자를 상수로 추출: 500, 1000, 2000 같은 숫자가 무엇을 의미하는지 명확하게
     * 반복되는 등급 기준 통일: 여러 메서드에서 같은 포인트 기준이 반복됨
     * 의미 있는 상수명 사용: BRONZE_THRESHOLD, SILVER_MIN_POINTS 등
     * (선택) Enum 활용: 등급을 Enum으로 관리하면 더 체계적
     * */

    public enum UserGrade {
        BRONZE(0, 499, 3000, 0.05),
        SILVER(500, 999, 2000, 0.1),
        GOLD(1000, 1999, 1000, 0.15),
        PLATINUM(2000, Integer.MAX_VALUE, 0, 0.2);

        private final int minPoints;
        private final int maxPoints;
        private final int deliveryFee;
        private  final double discountRate;

        UserGrade(int minPoints, int maxPoints, int deliveryFee, double discountRate) {
            this.minPoints = minPoints;
            this.maxPoints = maxPoints;
            this.deliveryFee = deliveryFee;
            this.discountRate = discountRate;
        }

        // 배달 수수료
        public int getDeliveryFee() {
            return deliveryFee;
        }

        // 할인율
        public double getDiscountRate() {
            return discountRate;
        }
        
        // 포인트로 등급찾기
        public static UserGrade fromPoitns(int points) {
            for (UserGrade userGrade : UserGrade.values()) {
                if(points >= userGrade.minPoints && points <= userGrade.maxPoints) {
                    return userGrade;
                }
            }
            throw new IllegalArgumentException("유효하지 않은 포인트: " + points);
        }

    }
    



    public static void main(String[] args) {
        System.out.println(getUserGrade(150));
        System.out.println(getUserGrade(550));
        System.out.println(getUserGrade(1200));
        System.out.println(getDeliveryFee(150));
        System.out.println(getDeliveryFee(550));
        System.out.println(getDiscountRate(1200));
    }

    // 포인트에 따른 유저 등급 추출
    public static UserGrade getUserGrade(int points) {
        return UserGrade.fromPoitns(points);
    }

    // 포인트에 따른 유저 등급에 따른 배달 수수료 추출
    public static int getDeliveryFee(int points) {
        return UserGrade.fromPoitns(points).getDeliveryFee();
    }

    // 포인트에 따른 유저 등급에 따른 할인율 추출
    public static double getDiscountRate(int points) {
        return UserGrade.fromPoitns(points).getDiscountRate();
    }

//    public static String getUserGrade(int points) {
//        if (points < 500) {
//            return "브론즈";
//        } else if (points < 1000) {
//            return "실버";
//        } else if (points < 2000) {
//            return "골드";
//        } else {
//            return "플래티넘";
//        }
//    }
//
//    public static int getDeliveryFee(int points) {
//        if (points < 500) {
//            return 3000;
//        } else if (points < 1000) {
//            return 2000;
//        } else {
//            return 0;
//        }
//    }
//
//    public static double getDiscountRate(int points) {
//        if (points < 500) {
//            return 0.05;
//        } else if (points < 1000) {
//            return 0.1;
//        } else if (points < 2000) {
//            return 0.15;
//        } else {
//            return 0.2;
//        }
//    }
}
