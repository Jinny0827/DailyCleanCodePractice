package org.example.cleancode.day41;


import java.util.*;

/**
 * Day 41: 트랜잭션 관리 시스템
 *
 * 문제점:
 * - 트랜잭션 경계가 불명확
 * - 부분 실패 시 롤백 불가 (데이터 불일치)
 * - 중첩 트랜잭션 미지원
 * - 격리 수준 제어 불가
 * - 트랜잭션 타임아웃 없음
 */
public class Day41TransactionSystem {

    public static void main(String[] args) {
        BankService service = new BankService();

        service.createAccount("ACC-001", 100000);
        service.createAccount("ACC-002", 50000);

        System.out.println("\n=== 초기 잔액 ===");
        System.out.println("ACC-001: " + service.getBalance("ACC-001"));
        System.out.println("ACC-002: " + service.getBalance("ACC-002"));


        // ✨ 타임아웃 테스트 1: 정상 완료 (1초 작업, 3초 제한)
        System.out.println("\n=== 타임아웃 테스트 1: 정상 완료 ===");
        try {
            service.slowTransfer("ACC-001", "ACC-002", 10000, 1000, 3000);
        } catch (Exception e) {
            System.out.println("❌ 실패: " + e.getMessage());
        }


        System.out.println("\n잔액 확인:");
        System.out.println("ACC-001: " + service.getBalance("ACC-001"));
        System.out.println("ACC-002: " + service.getBalance("ACC-002"));

        // ✨ 타임아웃 테스트 2: 타임아웃 발생 (5초 작업, 2초 제한)
        System.out.println("\n=== 타임아웃 테스트 2: 타임아웃 발생 ===");
        try {
            service.slowTransfer("ACC-001", "ACC-002", 10000, 5000, 2000);
            Thread.sleep(3000);  // 타임아웃 후 대기
        } catch (Exception e) {
            System.out.println("❌ 실패: " + e.getMessage());
        }

        System.out.println("\n최종 잔액:");
        System.out.println("ACC-001: " + service.getBalance("ACC-001"));
        System.out.println("ACC-002: " + service.getBalance("ACC-002"));
    }

}

// 트랜잭션 상태 enum
enum TransactionStatus {
    ACTIVE,
    COMMITTED,
    ROLLED_BACK
}


// 트랜잭션 추상화
interface Transaction {
    void commit();
    void rollback();
    boolean isActive();
    TransactionStatus getStatus();
}

// 기본 구현체
class BankTransaction implements Transaction {
    private TransactionStatus status;
    private Map<String, Integer> snapshot;
    private Map<String, Integer> accounts;

    private Timer timeoutTimer;
    private long startTime;

    public BankTransaction(Map<String, Integer> accounts) {
        this(accounts, 0);
    }
    
    // 타임아웃 지원 생성자
    public BankTransaction(Map<String, Integer> accounts, long timeoutMillis) {
        this.status = TransactionStatus.ACTIVE;
        this.accounts = accounts;
        this.snapshot = new HashMap<>(accounts);
        this.startTime = System.currentTimeMillis();

        System.out.println("트랜잭션 스타트");

        if(timeoutMillis > 0) {
            this.timeoutTimer = new Timer(true);
            timeoutTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (isActive()) {
                        System.out.println("⏰ 트랜잭션 타임아웃! (" + timeoutMillis + "ms)");
                        try {
                            rollback();
                        } catch (Exception e) {
                            System.out.println("타임아웃 롤백 실패: " + e.getMessage());
                        }
                    }
                }
            }, timeoutMillis);

            System.out.println("⏱️ 타임아웃 설정: " + timeoutMillis + "ms");
        }
    }

    @Override
    public void commit() {
        if(!isActive()) {
            throw new IllegalStateException("트랜잭션이 활성 상태가 아닙니다");
        }

        if (timeoutTimer != null) {
            timeoutTimer.cancel();
        }


        status = TransactionStatus.COMMITTED;
        snapshot = null;

        long elapsed = System.currentTimeMillis() - startTime;
        System.out.println("트랜잭션 커밋됨 (소요시간: " + elapsed + "ms)");
    }

    @Override
    public void rollback() {
        if (!isActive()) {
            throw new IllegalStateException("트랜잭션이 활성 상태가 아닙니다");
        }

        if (timeoutTimer != null) {
            timeoutTimer.cancel();
        }


        // 스냅샷으로 복원
        accounts.clear();
        accounts.putAll(snapshot);

        status = TransactionStatus.ROLLED_BACK;

        long elapsed = System.currentTimeMillis() - startTime;
        System.out.println("트랜잭션 롤백됨 (소요시간: " + elapsed + "ms)");
    }

    @Override
    public boolean isActive() {
        return status == TransactionStatus.ACTIVE;
    }

    @Override
    public TransactionStatus getStatus() {
        return status;
    }
}

// 트랜잭션 관리 매니저(BankService에서 사용)
class TransactionManager {
    // 다중 트랜잭션을 위한 스택 객체 생성 (옆으로 긴 원통)
    private Stack<Transaction> transactionStack = new Stack<>();
    private Map<String, Integer> accounts;
    private long defaultTimeout = 0;  // 기본 타임아웃


    public TransactionManager(Map<String, Integer> accounts) {
        this.accounts = accounts;
    }

    public void setDefaultTimeout(long defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

    public void beginTransaction() {
        beginTransaction(defaultTimeout);
    }

    public void beginTransaction(long timeoutMillis) {
        Transaction newTransaction = new BankTransaction(accounts, timeoutMillis);
        transactionStack.push(newTransaction);

        System.out.println("📚 트랜잭션 레벨: " + transactionStack.size());
    }

    public void commit() {
        if (transactionStack.isEmpty()) {
            throw new IllegalStateException("활성 트랜잭션이 없습니다");
        }

        Transaction transaction = transactionStack.pop();

        if(!transaction.isActive()) {
            throw new IllegalStateException("트랜잭션이 활성 상태가 아닙니다");
        }

        transaction.commit();
        System.out.println("📚 남은 트랜잭션: " + transactionStack.size());
    }

    public void rollback() {
        if (transactionStack.isEmpty()) {
            throw new IllegalStateException("활성 트랜잭션이 없습니다");
        }

        Transaction transaction = transactionStack.pop();

        if (!transaction.isActive()) {
            throw new IllegalStateException("트랜잭션이 활성 상태가 아닙니다");
        }

        transaction.rollback();
        System.out.println("📚 남은 트랜잭션: " + transactionStack.size());
    }

    public Transaction getCurrentTransaction() {
        if(transactionStack.isEmpty()) {
            return null;
        }

        return transactionStack.peek();
    }

    // 기존 메서드 (타임아웃이 없는)
    public void executeInTransaction(Runnable operation) {
        executeInTransaction(operation, defaultTimeout);
    }
    
    
    // 트랜잭션 내에서 작업 실행
    public void executeInTransaction(Runnable operation, long timeoutMillis) {
        beginTransaction(timeoutMillis);

        try {
            operation.run();
            commit();
        } catch (Exception e) {
            rollback();
            throw e;
        }
    }
}


class BankService {
    private Map<String, Integer> accounts = new HashMap<>();
    private TransactionManager transactionManager;

    public BankService() {
        this.transactionManager = new TransactionManager(accounts);
    }

    public void createAccount(String accountId, int initialBalance) {
        accounts.put(accountId, initialBalance);
    }

    public int getBalance(String accountId) {
        return accounts.getOrDefault(accountId, 0);
    }

    public void transfer(String fromId, String toId, int amount) {
        transactionManager.executeInTransaction(() -> {
            int fromBalance = accounts.get(fromId);
            if(fromBalance < amount) {
                throw new RuntimeException("잔액 부족");
            }
            accounts.put(fromId, fromBalance - amount);

            if (Math.random() < 0.3) {
                throw new RuntimeException("네트워크 오류");
            }

            int toBalance = accounts.get(toId);
            accounts.put(toId, toBalance + amount);

            System.out.println("✓ 이체 완료: " + amount + "원");
        });
    }
    
    // 타임아웃 테스트용 예제
    public void slowTransfer(String fromId, String toId, int amount,
                             long sleepMillis, long timeoutMillis) {
        transactionManager.executeInTransaction(() -> {
            System.out.println("💤 작업 시작 (예상 소요시간: " + sleepMillis + "ms)");

            int fromBalance = accounts.get(fromId);
            if(fromBalance < amount) {
                throw new RuntimeException("잔액 부족");
            }
            accounts.put(fromId, fromBalance - amount);

            // 인위적으로 지연
            try {
                Thread.sleep(sleepMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("작업 중단됨");
            }

            int toBalance = accounts.get(toId);
            accounts.put(toId, toBalance + amount);

            System.out.println("✓ 이체 완료: " + amount + "원");
        }, timeoutMillis);
    }

    

    // 로그 업데이트용
    public void updateLog(String message) {
        transactionManager.executeInTransaction(() -> {
            System.out.println("📝 로그 기록: " + message);

            if(Math.random() < 0.5) {
                throw new RuntimeException("로그 저장 실패");
            }
        });
    }

    // 중첩 트랜잭션 테스트 메서드
    public void transferWithLog(String fromId, String toId, int amount) {
        transactionManager.executeInTransaction(()-> {
            System.out.println("🔵 외부 트랜잭션 시작");

            int formBalance = accounts.get(fromId);
            if(formBalance < amount) {
                throw new RuntimeException("잔액 부족");
            }

            accounts.put(fromId, formBalance - amount);

            int toBalance = accounts.get(toId);
            accounts.put(toId, toBalance + amount);

            System.out.println("✓ 이체 완료: " + amount + "원");


            try {
                System.out.println("🔵 내부 트랜잭션 시도");
                updateLog("이체: " + fromId + " → " + toId + " (" + amount + "원)");
            } catch (Exception e) {
                System.out.println("⚠️ 로그 실패했지만 이체는 유지됨");
            }

            System.out.println("🔵 외부 트랜잭션 완료");
        });
    }
}


