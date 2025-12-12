package org.example.cleancode.day39;


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

    public void addTask(Task<?> task, int priority) {
        tasks.add(new QueuedTask<>(task, priority));
    }


    public void processAll() {
        for(QueuedTask<?> queuedTask : tasks) {
            try {
                queuedTask.setStatus(TaskStatus.RUNNING);
                Object result = queuedTask.getTask().execute();
                queuedTask.setStatus(TaskStatus.COMPLETED);
                System.out.println("완료: " + result);
            } catch (Exception e){
                queuedTask.setStatus(TaskStatus.FAILED);
                System.out.println("실패: " + e.getMessage());
            }
        }
    }
}

