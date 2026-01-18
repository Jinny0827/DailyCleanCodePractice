package org.example.cleancode.Y_2025.day47;

import java.util.ArrayList;
import java.util.List;

/**
 * Day 47: 주문 처리 시스템 (분산 트랜잭션)
 *
 * 소스코드 관점의 수정 필요 ----
 * 분산 시스템에서 여러 서비스에 걸친 트랜잭션을 안전하게 관리하기
 *
 * Orchestration 방식 Saga 구현
 * 보상 트랜잭션 (Compensation) 패턴
 * 장애 시 자동 롤백
 *
 * 서비스적 관점의 문제
 * 문제점:
 * - 여러 서비스 호출이 하나의 트랜잭션처럼 묶여야 함
 * - 중간에 실패 시 이전 단계 롤백 불가
 * - 보상 로직이 비즈니스 로직과 섞여있음
 * - 재시도 전략 없음
 * - 상태 추적 불가
 */
public class Day47OrderSaga {

    public static void main(String[] args) {
        OrderService service = new OrderService();


        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("📌 기존 방식 (중첩 try-catch)");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        // 정상 케이스
        service.processOrder("ORD-001", "USER-001", 50000);

        // 실패 케이스 (결제 실패)
        service.processOrder("ORD-002", "USER-NO-CARD", 30000);


        System.out.println("\n\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("🚀 Saga 패턴 방식");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        // 정상 케이스
        service.processOrderWithSaga("ORD-003", "USER-001", 50000);

        // 실패 케이스 (결제 실패)
        service.processOrderWithSaga("ORD-004", "USER-NO-CARD", 30000);
    }

}


// Saga의 각 단계를 나타내는 인터페이스
interface SagaStep {
    // 실행
    void execute() throws Exception;

    //롤백
    void compensate();

    // 로깅용(단계 확인)
    String getStepName();
}

// 주문 관리 스텝
class CreateOrderStep implements SagaStep {
    private final OrderRepository orderRepo;
    private final String orderId;
    private final String userId;
    private final int amount;


    public CreateOrderStep(OrderRepository orderRepo, String orderId, String userId, int amount) {
        this.orderRepo = orderRepo;
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
    }

    @Override
    public void execute() throws Exception {
        orderRepo.createOrder(orderId, userId, amount);

        System.out.println("✓ 1단계: 주문 생성");
    }

    @Override
    public void compensate() {
        // 롤백(상태 값 취소로 변경)
        orderRepo.updateStatus(orderId, "CANCELLED");

        System.out.println("  ← 1단계 보상: 주문 취소");
    }

    @Override
    public String getStepName() {
        return "주문 생성";
    }
}

// 결제 처리 스텝
class ChargePaymentStep implements SagaStep {
    private final PaymentService paymentService;
    private final String userId;
    private final int amount;

    public ChargePaymentStep(PaymentService paymentService, String userId, int amount) {
        this.paymentService = paymentService;
        this.userId = userId;
        this.amount = amount;
    }

    @Override
    public void execute() throws Exception {

        paymentService.charge(userId, amount);

        System.out.println("✓ 2단계: 결제 완료");
    }

    @Override
    public void compensate() {
        // 환불 보상 실패 X -> 트랜잭션에 Excpetion이 없는 이유
        paymentService.refund(userId, amount);

        System.out.println("  ← 2단계 보상: 환불 처리");
    }

    @Override
    public String getStepName() {
        return "결제 처리";
    }
}

// 재고 관리 스텝
class ReserveInventoryStep implements SagaStep {
    private final InventoryService inventoryService;
    private final String orderId;
    private final int quantity;

    public ReserveInventoryStep(InventoryService inventoryService, String orderId, int quantity) {
        this.inventoryService = inventoryService;
        this.orderId = orderId;
        this.quantity = quantity;
    }


    @Override
    public void execute() throws Exception {
        inventoryService.reserve(orderId, quantity);

        System.out.println("✓ 3단계: 재고 차감");
    }

    @Override
    public void compensate() {
        // 재고 차감 복원
        inventoryService.cancelReserve(orderId);

        System.out.println("  ← 3단계 보상: 재고 복원");
    }

    @Override
    public String getStepName() {
        return "재고 차감";
    }
}


class ScheduleDeliveryStep implements SagaStep {
    private final DeliveryService deliveryService;
    private final String orderId;

    public ScheduleDeliveryStep(DeliveryService deliveryService, String orderId) {
        this.deliveryService = deliveryService;
        this.orderId = orderId;
    }

    @Override
    public void execute() throws Exception {

        deliveryService.schedule(orderId);

        System.out.println("✓ 4단계: 배송 등록");
    }

    @Override
    public void compensate() {
        // 배송 취소 처리
        try {
            // 배송사 API 취소 시도
            deliveryService.cancel(orderId);
            System.out.println("  ← 4단계 보상: 배송 취소 완료");

        } catch (Exception e) {
            System.out.println("  ⚠️ 4단계 보상 실패: " + e.getMessage());
            System.out.println("     → 수동 처리 필요: " + orderId);
        }
        
    }

    @Override
    public String getStepName() {
        return "배송 등록";
    }
}

// 오케스트레이터가 관리 (실행할 Step 리스트, 실행 결과)
class SagaOrchestrator {
    // Step들 순서대로 저장
    List<SagaStep> steps = new ArrayList<>();
    
    // Step 추가
    public void addStep(SagaStep step) {
        steps.add(step);
        System.out.println("📌 Step 등록: " + step.getStepName());
    }

    public SagaExecutionResult execute() {
        System.out.println("\n🚀 Saga 실행 시작 (총 " + steps.size() + "단계)\n");

        List<SagaStep> executedSteps = new ArrayList<>();

        for (int i = 0; i < steps.size(); i++) {
            SagaStep step = steps.get(i);

            try {

                step.execute();
                executedSteps.add(step);

            } catch (Exception e) {
                System.out.println("❌ " + (i + 1) + "단계 실패: "
                        + e.getMessage());

                compensateAll(executedSteps);

                return SagaExecutionResult.failure(
                        i + 1,
                        steps.size(),
                        step.getStepName(),
                        e.getMessage()
                );
            }
        }

        System.out.println("\n🎉 Saga 완료! (" + steps.size() + "/"
                + steps.size() + " 단계 성공)\n");

        return SagaExecutionResult.success(steps.size());
    }

    private void compensateAll(List<SagaStep> executedSteps) {
        System.out.println("\n🔄 보상 트랜잭션 시작");

        for (int i = executedSteps.size() - 1; i >= 0; i--) {
            SagaStep step = executedSteps.get(i);

            try {

                step.compensate();

            } catch (Exception e) {
                System.out.println("  ⚠️ 보상 중 오류: " + step.getStepName()
                        + " - " + e.getMessage());
            }
        }
    }
}

// 결과 담는 객체
class SagaExecutionResult {
    private final boolean success;
    private final int completedSteps;
    private final int totalSteps;

    
    private final int failedStep;
    private final String failedStepName;
    private final String failureReason;


    // private 생성자
    // 외부에서 직접 생성하지 못하도록(팩터리 메서드만 사용)
    private SagaExecutionResult(boolean success,
                                int completedSteps,
                                int totalSteps,
                                int failedStep,
                                String failedStepName,
                                String failureReason) {
        this.success = success;
        this.completedSteps = completedSteps;
        this.totalSteps = totalSteps;
        this.failedStep = failedStep;
        this.failedStepName = failedStepName;
        this.failureReason = failureReason;
    }

    // 성공
    public static SagaExecutionResult success(int totalSteps) {
        return new SagaExecutionResult(
                true,
                totalSteps,
                totalSteps,
                0,
                null,
                null
        );
    }
    
    
    // 실패
    public static SagaExecutionResult failure(int failedStep,
                                              int totalSteps,
                                              String stepName,
                                              String reason) {
        return new SagaExecutionResult(
                false,
                failedStep - 1,
                totalSteps,
                failedStep,
                stepName,
                reason
        );
    }

    public boolean isSuccess() {
        return success;
    }

    public int getCompletedSteps() {
        return completedSteps;
    }

    public int getTotalSteps() {
        return totalSteps;
    }

    public int getFailedStep() {
        return failedStep;
    }

    public String getFailedStepName() {
        return failedStepName;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void printSummary() {
        if (success) {
            System.out.println("✅ Saga 성공: " + completedSteps + "/"
                    + totalSteps + " 단계 완료");
        } else {
            System.out.println("❌ Saga 실패");
            System.out.println("   완료: " + completedSteps + "/" + totalSteps);
            System.out.println("   실패 단계: " + failedStep + ". " + failedStepName);
            System.out.println("   실패 사유: " + failureReason);
        }
    }
}


class OrderService {
    private OrderRepository orderRepo = new OrderRepository();
    private PaymentService paymentService = new PaymentService();
    private InventoryService inventoryService = new InventoryService();
    private DeliveryService deliveryService = new DeliveryService();

    // 문제: 단계별 실패 시 롤백 로직이 복잡함
    public void processOrder(String orderId, String userId, int amount) {
        System.out.println("=== 주문 처리 시작: " + orderId + " ===");

        // 1단계: 주문 생성
        orderRepo.createOrder(orderId, userId, amount);
        System.out.println("✓ 주문 생성");

        try {
            // 2단계: 결제
            paymentService.charge(userId, amount);
            System.out.println("✓ 결제 완료");

            try {
                // 3단계: 재고 차감
                inventoryService.reserve(orderId, 2);
                System.out.println("✓ 재고 차감");

                try {
                    // 4단계: 배송 등록
                    deliveryService.schedule(orderId);
                    System.out.println("✓ 배송 등록");

                    orderRepo.updateStatus(orderId, "COMPLETED");
                    System.out.println("🎉 주문 완료!\n");

                } catch (Exception e) {
                    // 배송 실패 → 재고 복원
                    System.out.println("❌ 배송 실패: " + e.getMessage());
                    inventoryService.cancelReserve(orderId);
                    paymentService.refund(userId, amount);
                    orderRepo.updateStatus(orderId, "FAILED");
                }

            } catch (Exception e) {
                // 재고 실패 → 결제 취소
                System.out.println("❌ 재고 실패: " + e.getMessage());
                paymentService.refund(userId, amount);
                orderRepo.updateStatus(orderId, "FAILED");
            }

        } catch (Exception e) {
            // 결제 실패 → 주문 취소
            System.out.println("❌ 결제 실패: " + e.getMessage());
            orderRepo.updateStatus(orderId, "FAILED");
        }
    }

    // 누적 try 문 제거 주문 생성부터 취소까지
    public void processOrderWithSaga(String orderId, String userId, int amount) {
        System.out.println("=== Saga 방식 주문 처리: " + orderId + " ===");

        SagaOrchestrator saga = new SagaOrchestrator();
        
        // 1단계 주문 생성
        saga.addStep(new CreateOrderStep(
                orderRepo,      // OrderRepository
                orderId,        // 주문 ID
                userId,         // 사용자 ID
                amount          // 금액
        ));

        
        // 2단계 결제 처리
        saga.addStep(new ChargePaymentStep(
                paymentService, // PaymentService
                userId,         // 사용자 ID
                amount          // 결제 금액
        ));

        // 3단계 재고 차감
        saga.addStep(new ReserveInventoryStep(
                inventoryService,   // InventoryService
                orderId,            // 주문 ID
                2                   // 수량 (기존 코드에서 하드코딩된 값)
        ));
        
        // 4단계 배송 등록
        saga.addStep(new ScheduleDeliveryStep(
                deliveryService,    // DeliveryService
                orderId             // 주문 ID
        ));

        // Saga 실행
        SagaExecutionResult result = saga.execute();

        if(result.isSuccess()) {
            orderRepo.updateStatus(orderId, "COMPLETED");
        } else {
            orderRepo.updateStatus(orderId, "FAILED");
        }

        // 결과 요약 출력
        result.printSummary();
    }
}

// 서비스들 (시뮬레이션)
class OrderRepository {
    public void createOrder(String orderId, String userId, int amount) {
        // DB 저장
    }

    public void updateStatus(String orderId, String status) {
        System.out.println("주문 상태 변경: " + status);
    }
}

class PaymentService {
    public void charge(String userId, int amount) throws Exception {
        if (userId.contains("NO-CARD")) {
            throw new Exception("카드 없음");
        }
        // 결제 처리
    }

    public void refund(String userId, int amount) {
        System.out.println("💰 환불: " + amount + "원");
    }
}

class InventoryService {
    public void reserve(String orderId, int quantity) throws Exception {
        if (Math.random() < 0.2) {
            throw new Exception("재고 부족");
        }
    }

    public void cancelReserve(String orderId) {
        System.out.println("📦 재고 복원");
    }
}

class DeliveryService {
    public void schedule(String orderId) throws Exception {
        if (Math.random() < 0.1) {
            throw new Exception("배송사 오류");
        }
    }
    
    // 취소 요청 (추가)
    public void cancel(String orderId) throws Exception {
        if(Math.random() < 0.1) {
            throw new Exception("배송 이미 출발 - 취소 불가");
        }
    }
}