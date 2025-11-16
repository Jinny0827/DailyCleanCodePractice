package org.example.cleancode.day29;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Day 29: 배치 처리 시스템
 *
 * 문제점:
 * - 동기 처리로 느림 (순차 실행)
 * - 한 건 실패 시 전체 중단
 * - 진행 상황 추적 불가
 * - 재시도 로직 없음
 * - 부분 성공 결과 손실
 */
public class Day29DataProcessor {

    public static void main(String[] args) {
        DataProcessor processor = new DataProcessor();
        RetryHandler retryHandler = new RetryHandler();
        ParallelBatchProcessor batchProcessor =
                new ParallelBatchProcessor(processor, retryHandler);

       List<DataRecord> records = Arrays.asList(
                new DataRecord("R001", "valid-data"),
                new DataRecord("R002", "ERROR"),      // 실패 예정
                new DataRecord("R003", "valid-data"),
                new DataRecord("R004", "valid-data"),
                new DataRecord("R005", "ERROR")       // 실패 예정
        );

       BatchResult result = batchProcessor.processBatch(records);

       result.printRecords();

       batchProcessor.shutdown();
    }

}

// 실행 결과 객체
class ProcessResult {
    private final String recordId;
    private final boolean success;
    private final String errorMessage;
    private final int retryCount;

    public ProcessResult(String recordId, boolean success, String errorMessage, int retryCount) {
        this.recordId = recordId;
        this.success = success;
        this.errorMessage = errorMessage;
        this.retryCount = retryCount;
    }

    public String getRecordId() {
        return recordId;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getRetryCount() {
        return retryCount;
    }
}

// 배치 결과 객체
class BatchResult {
    private final int total;
    private final int success;
    private final int failed;
    private final List<ProcessResult> failedRecords;

    public BatchResult(int total, int success, int failed, List<ProcessResult> failedRecords) {
        this.total = total;
        this.success = success;
        this.failed = failed;
        this.failedRecords = failedRecords;
    }

    public int getTotal() {
        return total;
    }

    public int getSuccess() {
        return success;
    }

    public int getFailed() {
        return failed;
    }

    public double getSuccessRate() {
        if (total == 0) {
            return 0.0;
        }
        return (double) success / total * 100;  // (성공 / 전체) × 100
    }

    public List<ProcessResult> getFailedRecords() {
        return failedRecords;
    }

    public void printRecords() {
        System.out.println("\n📊 === 배치 처리 결과 ===");
        System.out.println("전체: " + total + "건");
        System.out.println("✅ 성공: " + success + "건");
        System.out.println("❌ 실패: " + failed + "건");
        System.out.printf("📈 성공률: %.2f%%\n", getSuccessRate());

        if (!failedRecords.isEmpty()) {
            System.out.println("\n❌ 실패 목록:");
            for (ProcessResult r : failedRecords) {
                System.out.println("  - " + r.getRecordId() +
                        ": " + r.getErrorMessage() +
                        " (재시도: " + r.getRetryCount() + "회)");
            }
        }
    }
}

// 재시도 로직
class RetryHandler {
    private static final int MAX_RETRIES = 3;
    private static final int BASE_DELAY_MS = 100;

    public ProcessResult processWithRetry(DataRecord record, DataProcessor processor) {
        int retryCount = 0;
        String lastError = null;

        for(int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                processor.processRecord(record);

                return new ProcessResult(
                        record.getId(),
                        true,
                        null,
                        retryCount
                );
            } catch(Exception ex) {
                retryCount++;
                lastError = ex.getMessage();

                System.out.println("⚠️ 재시도 " + retryCount + "/" + MAX_RETRIES
                        + ": " + record.getId());

                // 마지막 시도가 아니면 대기
                // (반복을 선으로 실행하고 아니면 반복문 빠져나가기때문에 4-1이라 생각)
                if(attempt < MAX_RETRIES - 1) {
                    waitWithExponentialBackoff(attempt);
                }
            }
        }

        return new ProcessResult(
                record.getId(),
                false,
                lastError,
                retryCount
        );
    }

    // 지수 백오프 로직 : 100ms → 200ms → 400ms
    private void waitWithExponentialBackoff(int attempt) {
        try {
           long delay = BASE_DELAY_MS * (long) Math.pow(2, attempt);
           Thread.sleep(delay);
        }catch(Exception e) {
            Thread.currentThread().interrupt();
        }
    }
}

// 병렬 처리 로직
class ParallelBatchProcessor {
    private static final int THREAD_POOL_SIZE = 4;

    private final DataProcessor dataProcessor;

    private final RetryHandler retryHandler;
    private final ExecutorService executorService;

    public ParallelBatchProcessor(DataProcessor dataProcessor,
                                  RetryHandler retryHandler) {
        this.dataProcessor = dataProcessor;
        this.retryHandler = retryHandler;
        this.executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    public BatchResult processBatch(List<DataRecord> records) {
        System.out.println("🚀 병렬 배치 시작: " + records.size() + "건");

        // Future 리스트 생성
        List<Future<ProcessResult>> futures = new ArrayList<>();


        // 각 레코드를 스레드 풀에 제출
        for (DataRecord record : records) {
            Future<ProcessResult> future = executorService.submit( () -> {
                return retryHandler.processWithRetry(record, dataProcessor);
            });

            futures.add(future);
        }

        // 모든 결과 수집
        List<ProcessResult> results = new ArrayList<>();
        for (Future<ProcessResult> future : futures) {
            try {
                results.add(future.get());
            } catch(Exception e) {
                System.out.println("⚠️ 작업 실패: " + e.getMessage());
            }
        }

        // BatchResult 생성
        int successCount = 0;
        int failedCount = 0;
        List<ProcessResult> failedRecords = new ArrayList<>();

        for (ProcessResult result : results) {
            if(result.isSuccess()) {
                successCount++;
            } else {
                failedCount++;
                failedRecords.add(result);
            }
        }


        // int success, int failed, List<ProcessResult> failedRecords)
        return new BatchResult(
                records.size(),
                successCount,
                failedCount,
                failedRecords
        );
    }

    public void shutdown() {
        executorService.shutdown();
    }
}

class DataRecord {
    private String id;
    private String data;

    public DataRecord(String id, String data) {
        this.id = id;
        this.data = data;
    }

    public String getId() { return id; }
    public String getData() { return data; }
}

class DataProcessor {

    // 실행간 발생할 익셉션에 대한 외부의 예외처리에 대한 로직 추가(외부에서 호출 후 외부에서 예외 처리)
     void processBatch(List<DataRecord> records) throws Exception {
        System.out.println("배치 시작: " + records.size() + "건");

        int processed = 0;

        for (DataRecord record : records) {
            // 동기 처리 - 느림
            processRecord(record);
            processed++;

            // 진행 상황 표시 없음
        }

        System.out.println("완료: " + processed + "건");
        // 실패 건수, 성공률 정보 없음
    }

    void processRecord(DataRecord record) throws Exception {
        System.out.println("처리 중: " + record.getId());

        // 검증
        if (record.getData().equals("ERROR")) {
            throw new RuntimeException("처리 실패: " + record.getId());
            // 전체 배치 중단!
        }

        // 실제 처리 시뮬레이션
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("완료: " + record.getId());
    }
}