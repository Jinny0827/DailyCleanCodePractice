package org.example.cleancode.Y_2025.day32;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Day 32: 파일 업로드 시스템
 *
 * 문제점:
 * - 파일 검증 로직이 중복됨
 * - 확장자/크기 체크가 하드코딩됨
 * - 업로드 실패 시 부분 정리 불가
 * - 진행 상황 추적 없음
 * - 다양한 스토리지(로컬/클라우드) 대응 어려움
 */
public class Day32FileUploader {

    public static void main(String[] args) {
        FileUploader localUploader = new FileUploader(
                new LocalStorageUpload("/uploads"),
                new ConsoleProgressListener()
        );

//        FileUploader s3Uploader = new FileUploader(
//                new S3StorageUpload("my-bucket")
//        );

        List<UploadFile> files = Arrays.asList(
                new UploadFile("document.pdf", 2_000_000, "application/pdf"),
                new UploadFile("image.jpg", 8_000_000, "image/jpeg"),
                new UploadFile("video.mp4", 50_000_000, "video/mp4"),
                new UploadFile("script.exe", 1_000_000, "application/exe")
        );

        for (UploadFile file : files) {
            try {
                localUploader.upload(file);
            } catch (Exception e) {
                System.out.println("❌ 업로드 실패: " + file.getName() + " - " + e.getMessage());
            }
        }
    }

}

//------------------------------------------------------------------------------------
// 파일 검증 관련 로직


// 검증 결과 리턴 클래스
class ValidationResult {
    private boolean valid;
    private String errorMessage;

    public ValidationResult(boolean valid, String errorMessage) {
        this.valid = valid;
        this.errorMessage = errorMessage;
    }

    public static ValidationResult success() {
        return new ValidationResult(true, null);
    }

    public static ValidationResult failure(String message) {
        if(message == null || message.isEmpty()) {
            throw new IllegalArgumentException("에러 메시지는 필수입니다");
        }

        return new ValidationResult(false, message);
    }

    public boolean isValid() {
        return valid;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        return valid ? "✓ 검증 통과" : "✗ 검증 실패: " + errorMessage;
    }

}


// 파일 검증 인터페이스
interface FileValidator {
    ValidationResult validate(UploadFile file);
}

// 파일 확장자 검증(금지된 확장자 목록(블랙리스트)과 비교)
class ExtensionValidator implements FileValidator {
    private final Set<String> blockedExtensions;

    public ExtensionValidator(Set<String> blockedExtensions) {
        if(blockedExtensions == null || blockedExtensions.isEmpty()) {
            throw new IllegalArgumentException("금지 확장자 목록은 필수입니다");
        }

        this.blockedExtensions = blockedExtensions;
    }

    @Override
    public ValidationResult validate(UploadFile file) {
        String extension = file.getExtension();


        if(extension.isEmpty()) {
            return ValidationResult.failure("파일 확장자가 없습니다");
        }

        if(blockedExtensions.contains(extension)) {
            return ValidationResult.failure(
                    extension + "는 금지된 파일 형식입니다"
            );
        }

        return ValidationResult.success();
    }
}

// 파일 크기 검증(최대 허용 크기 초과 확인)
class FileSizeValidator implements FileValidator {
    private final long maxSizeBytes;

    public FileSizeValidator(long maxSizeBytes) {
        if(maxSizeBytes < 0) {
            throw new IllegalArgumentException("최대 크기는 0보다 커야 합니다");
        }

        this.maxSizeBytes = maxSizeBytes;
    }

    @Override
    public ValidationResult validate(UploadFile file) {
        long fileSize = file.size();

        if(fileSize > maxSizeBytes) {
            return ValidationResult.failure(
                    String.format("파일 크기 초과 (최대 %dMB, 현재 %dMB)",
                            maxSizeBytes / 1_000_000,
                            fileSize / 1_000_000)
            );
        }

        return ValidationResult.success();
    }
}

// MIME 타입 검증(화이트리스트 MIME 타입과 비교)
class MimeTypeValidator implements FileValidator {
    private final Set<String> allowedPrefixes;

    public MimeTypeValidator(Set<String> allowedPrefixes) {
        if(allowedPrefixes == null || allowedPrefixes.isEmpty()) {
            throw new IllegalArgumentException("허용 타입 목록은 필수입니다");
        }

        this.allowedPrefixes = allowedPrefixes;
    }

    @Override
    public ValidationResult validate(UploadFile file) {
        String mimeType = file.getMimeType();

        if(mimeType == null || mimeType.isEmpty()) {
            return ValidationResult.failure("MIME 타입이 없습니다");
        }

        for(String prefix : allowedPrefixes) {
            if(mimeType.startsWith(prefix)) {
                return ValidationResult.success();
            }
        }

        return ValidationResult.failure(
                "지원하지 않는 파일 타입: " + mimeType
        );
    }
}

//------------------------------------------------------------------------------------
// 파일 업로드 전략 (추상화)
interface UploadStrategy {
    void upload(UploadFile file, UploadProgressListener listener) throws Exception;
}

class LocalStorageUpload implements UploadStrategy {
    private String basePath;

    public LocalStorageUpload(String basePath) {
        this.basePath = basePath;
    }

    @Override
    public void upload(UploadFile file, UploadProgressListener listener) throws Exception {
        System.out.println("💾 로컬 저장: " + basePath + "/" + file.getName());

        for (int i = 25; i <= 100; i += 25) {
            Thread.sleep(50);
            listener.onProgress(file.getName(), i);
        }

        listener.onComplete(file.getName());
    }
}

class S3StorageUpload implements UploadStrategy {
    private String bucketName;

    public S3StorageUpload(String bucketName) {
        this.bucketName = bucketName;
    }

    @Override
    public void upload(UploadFile file, UploadProgressListener listener) throws Exception {
        System.out.println("☁️ S3 업로드: " + bucketName + "/" + file.getName());
        Thread.sleep(150);
    }
}


//------------------------------------------------------------------------------------
// 업로드 진행 상황 추적

interface UploadProgressListener {
    void onProgress(String fileName, int percent);
    void onComplete(String fileName);
}


class ConsoleProgressListener implements UploadProgressListener {
    @Override
    public void onProgress(String fileName, int percent) {
        System.out.println("💾 업로드 중: " + percent + "%");
    }

    @Override
    public void onComplete(String fileName) {
        System.out.println("✅ 완료: " + fileName);
    }
}

//------------------------------------------------------------------------------------




class UploadFile {
    private String name;
    private long size;
    private String mimeType;

    public UploadFile(String name, long size, String mimeType) {
        this.name = name;
        this.size = size;
        this.mimeType = mimeType;
    }

    public String getName() { return name; }
    public long size() { return size; }
    public String getMimeType() { return mimeType; }

    public String getExtension() {
        int lastDot = name.lastIndexOf('.');
        return lastDot > 0 ? name.substring(lastDot + 1).toLowerCase() : "";
    }
}

class FileUploader {
    private List<FileValidator> validators;
    private UploadStrategy uploadStrategy;
    private UploadProgressListener progressListener;

    public FileUploader(UploadStrategy uploadStrategy, UploadProgressListener listener) {
        this.uploadStrategy = uploadStrategy;
        this.progressListener = listener;

        validators = new ArrayList<>();
        validators.add(new ExtensionValidator(Set.of("exe", "bat", "sh", "dll")));
        validators.add(new FileSizeValidator(10_000_000));
        validators.add(new MimeTypeValidator(Set.of("image/", "application/pdf")));
    }

    public void upload(UploadFile file) throws Exception {
        System.out.println("📤 업로드 시작: " + file.getName());

        for (FileValidator validator : validators) {
            ValidationResult result = validator.validate(file);

            if(!result.isValid()) {
                throw new Exception(result.getErrorMessage());
            }

            System.out.println("✓ " + validator.getClass().getSimpleName() + " 통과");
        }


        uploadStrategy.upload(file, progressListener);
    }
}