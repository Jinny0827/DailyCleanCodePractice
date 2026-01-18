package org.example.cleancode.Y_2025.day44;

import java.util.ArrayList;
import java.util.List;

/**
 * Day 44: 헬스 체크 및 모니터링 시스템
 *
 * 문제점:
 * - 헬스 체크 로직이 하드코딩됨
 * - 의존성별 가중치 고려 안함
 * - 복구 전략 없음
 * - 메트릭 수집이 비효율적
 * - 알림 로직이 분산됨
 */
public class Day44HealthCheck {
    public static void main(String[] args) {
        HealthOrchestrator orchestrator = new HealthOrchestrator();

        orchestrator.register(new DatabaseHealthCheck());
        orchestrator.register(new RedisHealthCheck());
        orchestrator.register(new ExternalApiHealthCheck());

        System.out.println("🚀 새로운 헬스 체크 시스템 시작");
        SystemHealthReporter report = orchestrator.checkSystem();

        report.printSummary();

        if (report.getOverallStatus() != HealthStatus.HEALTHY) {
            System.out.println("\n🚨 알림: 시스템에 문제가 있습니다!");
        }
    }
}

// 헬스 체크 값들을 통한 전체 상태 판단 로직
interface HealthEvaluator {
    HealthStatus evaluate(List<HealthResult> results);
}

// 가중치 기반 체크 상태 구현체
class WeightedHealthEvaluator  implements HealthEvaluator {

    @Override
    public HealthStatus evaluate(List<HealthResult> results) {
        if(results.isEmpty()) {
            return HealthStatus.DOWN;
        }

        // 필수 컴포넌트 체크
        for (HealthResult result : results) {
            HealthCheckConfig config = getConfigFor(result.getComponentName());
            if(config != null && config.isCritical() &&
                result.getStatus() == ComponentStatus.DOWN ){

                System.out.println("🚨 필수 컴포넌트 장애: " + result.getComponentName());
                return HealthStatus.DOWN;

            }
        }
        
        // 가중치 기반 점수 계산
        int totalWeight = 0;
        int healthyWeight = 0;

        for (HealthResult result : results) {
            HealthCheckConfig config = getConfigFor(result.getComponentName());
            if(config != null) {
                totalWeight += config.getWeight();
                if(result.getStatus() == ComponentStatus.UP) {
                    healthyWeight += config.getWeight();
                }
            }
        }

        // 점수 기반 상세 스탯 판단
        if (totalWeight == 0) return HealthStatus.DOWN;

        double healthScore = (double) healthyWeight / totalWeight;
        System.out.println("📊 헬스 스코어: " + String.format("%.1f%%", healthScore * 100));


        if(healthScore >= 0.8) {
            return HealthStatus.HEALTHY;
        } else if (healthScore >= 0.5) {
            return HealthStatus.DEGRADED;
        } else {
            return HealthStatus.DOWN;
        }
    }

    private HealthCheckConfig getConfigFor(String componentName) {
        // 임시로 스위치처리
        switch (componentName) {
            case "database": return new HealthCheckConfig("database", 10, true, 3, 5000);
            case "redis": return new HealthCheckConfig("redis", 5, false, 2, 3000);
            case "external-api": return new HealthCheckConfig("external-api", 3, false, 1, 2000);
            default: return null;
        }
    }
}


// 헬스 체크 상태 enum
enum HealthStatus {
    HEALTHY("모든 시스템 정상"),
    DEGRADED("일부 시스템 장애"),
    DOWN("주요 시스템 장애");

    private final String description;

    HealthStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

// 오케스트레이터 객체
class HealthOrchestrator {
    private final List<HealthCheck> healthChecks = new ArrayList<>();
    private final HealthEvaluator evaluator = new WeightedHealthEvaluator();
    
    // 헬스체크 등록
    public void register(HealthCheck healthCheck) {
        healthChecks.add(healthCheck);
    }

    // 전체 헬스체크 실행
    public SystemHealthReporter checkSystem() {
        long startTime = System.currentTimeMillis();

        List<HealthResult> results = new ArrayList<>();
        for (HealthCheck check : healthChecks) {
            results.add(check.check());
        }

        HealthStatus overallStatus = evaluator.evaluate(results);
        long totalTime = System.currentTimeMillis() - startTime;

        return new SystemHealthReporter(overallStatus,results, totalTime);
    }

}


// 전체 상태를 보고서 형태로 출력하는 객체
class SystemHealthReporter {
    private final HealthStatus overallStatus;
    private final List<HealthResult> componentResults;
    private final long totalCheckTimeMs;
    private final long timestamp;

    public SystemHealthReporter(HealthStatus overallStatus,
                                List<HealthResult> componentResults,
                                long totalCheckTimeMs) {
        this.overallStatus = overallStatus;
        this.componentResults = new ArrayList<>(componentResults);
        this.totalCheckTimeMs = totalCheckTimeMs;
        this.timestamp = System.currentTimeMillis();
    }

    public HealthStatus getOverallStatus() {
        return overallStatus;
    }

    public List<HealthResult> getComponentResults() {
        return componentResults;
    }

    public long getTotalCheckTimeMS() {
        return totalCheckTimeMs;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // 요약 출력
    public void printSummary() {
        System.out.println("\n=== 시스템 헬스 체크 결과 ===");
        System.out.println("전체 상태: " + overallStatus.getDescription());
        System.out.println("총 소요시간: " + totalCheckTimeMs + "ms");
        System.out.println("컴포넌트 상태:");

        for (HealthResult result : componentResults) {
            String status = result.getStatus() == ComponentStatus.UP ? "✅" : "❌";
            System.out.println("  " + status + " " + result.getComponentName() +
                    " (" + result.getResponseTimeMs() + "ms)");
        }
    }
}


// 컴포넌트 상태 enum
enum ComponentStatus {
    UP, DOWN, UNKNOWN
}

// Config 클래스
class HealthCheckConfig {
    private final String componentName;
    private final int weight;
    private final boolean critical;
    private final int retryCount;
    private final long timeoutMs;


    public HealthCheckConfig(String componentName,
                             int weight,
                             boolean critical,
                             int retryCount,
                             long timeoutMs) {
        this.componentName = componentName;
        this.weight = weight;
        this.critical = critical;
        this.retryCount = retryCount;
        this.timeoutMs = timeoutMs;
    }

    public String getComponentName() {
        return componentName;
    }

    public int getWeight() {
        return weight;
    }

    public boolean isCritical() {
        return critical;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }
}

// 결과 클래스
class HealthResult {
    private final String componentName;
    private final ComponentStatus status;
    private final String message;
    private final long responseTimeMs;
    private final long timestamp;

    public HealthResult(String componentName,
                        ComponentStatus status,
                        String message,
                        long responseTimeMs) {
        this.componentName = componentName;
        this.status = status;
        this.message = message;
        this.responseTimeMs = responseTimeMs;
        this.timestamp = System.currentTimeMillis();
    }

    public String getComponentName() {
        return componentName;
    }

    public ComponentStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public long getResponseTimeMs() {
        return responseTimeMs;
    }

    public long getTimestamp() {
        return timestamp;
    }
}

// 헬스 체크 인터페이스(메인 인터페이스)
interface HealthCheck {
    HealthResult check();
    HealthCheckConfig getConfig();
}

// 메인 인터페이스의 기본 구현체
abstract class AbstractHealthCheck implements HealthCheck {
    protected final HealthCheckConfig config;

    public AbstractHealthCheck(HealthCheckConfig config) {
        this.config = config;
    }

    // 재시도 로직 포함
    @Override
    public HealthResult check() {
        long startTime = System.currentTimeMillis();

        for(int attempt = 1; attempt <= config.getRetryCount(); attempt++) {
            try {
                if(doHealthCheck()) {
                    long responseTime = System.currentTimeMillis() - startTime;
                    return new HealthResult(
                            config.getComponentName(),
                            ComponentStatus.UP,
                            "정상",
                            responseTime
                    );
                }
            } catch (Exception e) {
                if (attempt == config.getRetryCount()) {
                    // 마지막 시도도 실패
                    long responseTime = System.currentTimeMillis() - startTime;
                    return new HealthResult(
                            config.getComponentName(),
                            ComponentStatus.DOWN,
                            "체크 실패: " + e.getMessage(),
                            responseTime
                    );
                }
                
                // 재시도 전 잠깐 대기
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {

                }
            }
        }

        // 최후 응답
        long responseTime = System.currentTimeMillis() - startTime;
        return new HealthResult(
                config.getComponentName(),
                ComponentStatus.DOWN,
                "모든 재시도 실패",
                responseTime
        );
    }

    protected abstract boolean doHealthCheck() throws Exception;

    @Override
    public HealthCheckConfig getConfig() {
        return config;
    }
}

// 데이터베이스 헬스체크
class DatabaseHealthCheck extends AbstractHealthCheck {
    public DatabaseHealthCheck() {
        super(new HealthCheckConfig(
                "database",
                10,
                true,
                3,
                5000
        ));
    }

    @Override
    protected boolean doHealthCheck() throws Exception {
        System.out.println("DB 연결 확인 중...");
        // DB 연결 시뮬레이션
        Thread.sleep(100);

        // 30% 실패율
        if (Math.random() < 0.3) {
            throw new Exception("DB 연결 실패");
        }

        System.out.println("✓ DB 정상");
        return true;
    }
}

// Redis 헬스체크
class RedisHealthCheck extends AbstractHealthCheck {

    public RedisHealthCheck() {
        super(new HealthCheckConfig(
                "redis",
                5,         // 중간 가중치
                false,     // 선택적 컴포넌트 (없어도 동작)
                2,         // 2회 재시도
                3000       // 3초 타임아웃
        ));
    }

    @Override
    protected boolean doHealthCheck() throws Exception {
        System.out.println("Redis 연결 확인 중...");
        Thread.sleep(50);

        // 20% 실패율
        if (Math.random() < 0.2) {
            throw new Exception("Redis 연결 실패");
        }

        System.out.println("✓ Redis 정상");
        return true;
    }
}

// 외부 API 헬스체크
class ExternalApiHealthCheck extends AbstractHealthCheck {

    public ExternalApiHealthCheck() {
        super(new HealthCheckConfig(
                "external-api",
                3,         // 낮은 가중치 (외부 시스템)
                false,     // 선택적
                1,         // 1회만 시도 (외부라 빠르게 포기)
                2000       // 2초 타임아웃
        ));
    }

    @Override
    protected boolean doHealthCheck() throws Exception {
        System.out.println("외부 API 확인 중...");
        Thread.sleep(200);

        // 40% 실패율 (외부 시스템이라 불안정)
        if (Math.random() < 0.4) {
            throw new Exception("외부 API 응답 없음");
        }

        System.out.println("✓ 외부 API 정상");
        return true;
    }
}






