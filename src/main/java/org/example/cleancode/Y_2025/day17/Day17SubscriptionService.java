package org.example.cleancode.Y_2025.day17;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Day 17: 주석 제거 및 자기 설명적 코드 만들기
 *
 * 문제점:
 * - 불필요하고 중복된 주석
 * - 코드로 설명 가능한 내용을 주석으로 작성
 * - 오래된 주석이 실제 로직과 불일치
 * - 매직 넘버에 주석으로만 설명
 */
public class Day17SubscriptionService {

    public static void main(String[] args) {
        SubscriptionService service = new SubscriptionService();

        service.checkSubscription("USER001");
        service.checkSubscription("USER002");
        service.checkSubscription("USER003");
    }

}

// 구독 플랜 enum 클래스
enum SubscriptionPlan {
    BASIC(7),
    PREMIUM(30);

    private final int defaultDurationDays;

    SubscriptionPlan(int defaultDurationDays) {
        this.defaultDurationDays = defaultDurationDays;
    }

    public int getDefaultDurationDays() {
        return defaultDurationDays;
    }
}

// 구독 상태 enum 클래스

enum SubscriptionStatus {
    ACTIVE("구독 활성"),
    EXPIRING_TODAY("구독 오늘 만료"),
    RENEWABLE("구독 만료 (갱신 가능)"),
    EXPIRED("구독 만료");

    private final String message;

    SubscriptionStatus(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}



class SubscriptionService {
    private final List<Subscription> subscriptions = new ArrayList<>();

    public SubscriptionService() {
        subscriptions.add(new Subscription("USER001", SubscriptionPlan.BASIC, LocalDate.now().minusDays(10)));
        subscriptions.add(new Subscription("USER002", SubscriptionPlan.PREMIUM, LocalDate.now().minusDays(40)));
        subscriptions.add(new Subscription("USER003", SubscriptionPlan.BASIC, LocalDate.now().minusDays(5)));
    }

    public void checkSubscription(String userId) {
        Subscription subscription = findSubscription(userId);


        if (subscription == null) {
            printNoSubscription(userId);
            return;
        }

        printSubscriptionStatus(userId, subscription);
    }

    private Subscription findSubscription(String userId) {
        for(Subscription subscription : subscriptions) {
            if(subscription.getUserId().equals(userId)) {
                return subscription;
            }
        }
        return null;
    }

    private void printNoSubscription(String userId) {
        System.out.println(userId + "구독 없음");
    }

    private void printSubscriptionStatus(String userId, Subscription subscription) {
        SubscriptionStatus status = subscription.getStatus();
        String planName = subscription.getPlan().name();
        long remainingDays = subscription.getRemainingDays();

        switch(status) {
            case ACTIVE:
                System.out.println(userId + ": " + planName + " " +
                        status.getMessage() + " (남은 기간: " + remainingDays + "일)");
                break;
            default:
                System.out.println(userId + ": " + planName + " " + status.getMessage());
                break;
        }
    }
}

class Subscription {
    // 캡슐화
    private static final int RENEWAL_GRACE_PERIOD_DAYS = 3;
    private final String userId;      // 사용자 ID
    private final SubscriptionPlan plan;        // 플랜 (BASIC, PREMIUM)
    private final LocalDate startDate; // 시작일
    private final int durationDays;       // 기간 (일)

    public Subscription(String userId, SubscriptionPlan plan, LocalDate startDate) {
        this.userId = userId;
        this.plan = plan;
        this.startDate = startDate;
        this.durationDays = plan.getDefaultDurationDays();
    }

    public long getRemainingDays() {
        long elapsedDays = ChronoUnit.DAYS.between(startDate, LocalDate.now());
        return durationDays - elapsedDays;
    }

    public SubscriptionStatus getStatus() {
        long remaining = getRemainingDays();

        if(remaining > 0) {
            return SubscriptionStatus.ACTIVE;
        }

        if(remaining == 0) {
            return SubscriptionStatus.EXPIRING_TODAY;
        }

        if(isWithinRenewalPeriod(remaining)) {
            return SubscriptionStatus.RENEWABLE;
        }

        return SubscriptionStatus.EXPIRED;
    }

    private boolean isWithinRenewalPeriod(long remainingDays) {
        return Math.abs(remainingDays) <= RENEWAL_GRACE_PERIOD_DAYS;
    }


    public String getUserId() {
        return userId;
    }

    public SubscriptionPlan getPlan() {
        return plan;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public int getDurationDays() {
        return durationDays;
    }
}