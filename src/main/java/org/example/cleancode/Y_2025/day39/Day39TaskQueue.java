package org.example.cleancode.Y_2025.day39;


import java.util.ArrayList;
import java.util.List;

/**
 * Day 39: 비동기 작업 큐
 *
 * 문제점:
 * - Promise 체이닝이 복잡함
 * - 동시 실행 수 제어 불가
 * - 작업 실패 시 재시도 로직 없음
 * - 우선순위 처리 미지원
 * - 진행 상황 추적 어려움
 */
public class Day39TaskQueue {

    public static void main(String[] args) {
        TaskQueue queue = new TaskQueue();
        
        // 무조건 실패
        queue.addTask(new AbstractTask<String>("TASK-FAIL", "실패 작업") {
            @Override
            public String execute() throws Exception {
                System.out.println("💥 작업 실행 중...");
                throw new Exception("의도적 실패");
            }
        }, 100);
        

        // 작업 추가
        queue.addTask(new AbstractTask<String>("TASK-001", "작업 1") {
            @Override
            public String execute() throws Exception {
                System.out.println("작업 1 실행");
                Thread.sleep(1000);
                return "결과 1";
            }
        }, 5);

        queue.addTask(new AbstractTask<String>("TASK-002", "작업 2") {
            @Override
            public String execute() throws Exception {
                System.out.println("작업 2 실행");
                Thread.sleep(500);
                if (Math.random() < 0.5) {
                    throw new Exception("작업 2 실패");
                }
                return "결과 2";
            }
        }, 10);

        // 모든 작업 실행
        queue.processAll();
    }

}

enum TaskStatus {
    PENDING,    // 대기 중
    RUNNING,    // 실행 중
    COMPLETED,  // 완료
    FAILED,     // 실패
    RETRYING    // 재시도 중
}

// 재시도 처리 인터페이스
interface RetryPolicy {
    // 최대 재시도 횟수
    int getMaxRetries();

    // 재시도 대기 시간 계산 (밀리초)
    long getDelayMillis(int attemptNumber);

    // 재시도 가능 여부 판단
    boolean shouldRetry(Exception exception, int currentRetryCount);
}

// 지수 백오프(시간 간격 up) 재시도 처리
class ExponentialBackoffRetryPolicy implements RetryPolicy {
    private final int maxRetries;
    private final long baseDelayMs;

    public ExponentialBackoffRetryPolicy(int maxRetries, long baseDelayMs) {
        this.maxRetries = maxRetries;
        this.baseDelayMs = baseDelayMs;  // 예: 100ms
    }

    @Override
    public int getMaxRetries() {
        return maxRetries;
    }

    @Override
    public long getDelayMillis(int attemptNumber) {
        // 지수 백오프: 100ms → 200ms → 400ms → 800ms
        return baseDelayMs * (long) Math.pow(2, attemptNumber);
    }

    @Override
    public boolean shouldRetry(Exception exception, int currentRetryCount) {

        // 재시도 횟수가 맥스치 넘기면
        if(currentRetryCount > maxRetries) {
            return false;
        }

        // 특정 예외는 재시도 X (매개변수 에러)
        if(exception instanceof IllegalArgumentException) {
            System.out.println("🚫 재시도 불가 예외: " + exception.getClass().getSimpleName());
            return false;
        }

        return true;
    }
}


class TaskExecutor {
    private final RetryPolicy retryPolicy;

    public TaskExecutor(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    public <T> TaskResult<T> execute(QueuedTask<T> queuedTask) {
        int attemptCount = 0;
        Exception lastException = null;

        while (attemptCount <= retryPolicy.getMaxRetries()) {
            try {
                queuedTask.setStatus(TaskStatus.RUNNING);

                // 재시도일 경우 대기
                if (attemptCount > 0) {
                    queuedTask.setStatus(TaskStatus.RETRYING);
                    long delay = retryPolicy.getDelayMillis(attemptCount - 1);
                    Thread.sleep(delay);

                    System.out.println("⚠️ 재시도 " + attemptCount + "/"
                            + retryPolicy.getMaxRetries() + ": " + queuedTask.getId());
                }

                // 실제 작업 실행
                T result = queuedTask.getTask().execute();

                queuedTask.setStatus(TaskStatus.COMPLETED);
                return TaskResult.success(queuedTask.getId(), result);

            } catch (Exception e) {
                lastException = e;
                queuedTask.incrementRetry();
                attemptCount++;

                // 재시도 가능 여부 확인
                if (!retryPolicy.shouldRetry(e, attemptCount)) {
                    break;
                }
            }
        }

        // 모든 재시도 실패
        queuedTask.setStatus(TaskStatus.FAILED);
        return TaskResult.failure(queuedTask.getId(), lastException);
    }
}

// 수행 결과 반환 메서드
class TaskResult<T> {
    private final String taskId;
    private final boolean success;
    private final T result;
    private final Exception exception;

    private TaskResult(String taskId, boolean success, T result, Exception exception) {
        this.taskId = taskId;
        this.success = success;
        this.result = result;
        this.exception = exception;
    }

    public static <T> TaskResult<T> success(String taskId, T result) {
        return new TaskResult<>(taskId, true, result, null);
    }

    public static <T> TaskResult<T> failure(String taskId, Exception exception) {
        return new TaskResult<>(taskId, false, null, exception);
    }

    // Getters
    public boolean isSuccess() { return success; }
    public T getResult() { return result; }
    public Exception getException() { return exception; }
    public String getTaskId() { return taskId; }
}




interface Task<T> {
    // 작업 고유 ID
    String getId();
    // 작업 설명 (로깅용)
    String getDescription();
    T execute() throws Exception;
}

class QueuedTask<T> implements Comparable<QueuedTask<T>> {
    private final String id;
    private final Task<T> task;
    private final int priority;
    private TaskStatus status;
    private int retryCount;
    private final long createdAt;

    public QueuedTask(Task<T> task, int priority) {
        this.id = task.getId();
        this.task = task;
        this.priority = priority;
        this.status = TaskStatus.PENDING;
        this.retryCount = 0;
        this.createdAt = System.currentTimeMillis();
    }

    @Override
    public int compareTo(QueuedTask<T> other) {
        // 우선순위가 높은 것이 먼저 (내림 차순)
        int priorityCompare = Integer.compare(other.priority, this.priority);

        // 우선순위 같으면 먼저 생성된 것이 먼저 진행 (FIFO)
        if(priorityCompare == 0) {
            return Long.compare(this.createdAt, other.createdAt);
        }

        return priorityCompare;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public void incrementRetry() {
        this.retryCount++;
    }

    public Task<T> getTask() {
        return task;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public String getId() {
        return id;
    }

    public int getPriority() {
        return priority;
    }
}

abstract class AbstractTask<T> implements Task<T> {
    private final String id;
    private final String description;

    public AbstractTask(String id, String description) {
        this.id = id;
        this.description = description;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDescription() {
        return description;
    }
}


class TaskQueue {
    private List<QueuedTask<?>> tasks = new ArrayList<>();
    private final TaskExecutor executor;

    public TaskQueue() {

        // 기본 재시도 정책 : 최대 3회, 100ms 시작
        RetryPolicy retryPolicy = new ExponentialBackoffRetryPolicy(3, 100);
        this.executor = new TaskExecutor(retryPolicy);
    }

    public void addTask(Task<?> task, int priority) {
        tasks.add(new QueuedTask<>(task, priority));
    }


    public void processAll() {
        // 우선순위 정렬 -> Comparable 기본 정렬 사용
        tasks.sort(null);

        System.out.println("🚀 " + tasks.size() + "개 작업 시작\n");

        for(QueuedTask<?> queuedTask : tasks) {
            System.out.println("📌 [" + queuedTask.getId()
                    + "] 우선순위: " + queuedTask.getPriority());

          TaskResult<?> result = executor.execute(queuedTask);

            if (result.isSuccess()) {
                System.out.println("✅ 완료: " + result.getResult());
            } else {
                System.out.println("❌ 실패: " + result.getException().getMessage());
            }
            
            // 가독성 위한 줄띄기
            System.out.println();
        }
    }
}

