package org.example.cleancode.day30;


import javax.xml.crypto.Data;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;

/**
 * Day 30: 데이터 동기화 시스템
 *
 * 문제점:
 * - 동시 수정 감지 불가 (Lost Update)
 * - 충돌 해결 전략 없음
 * - 동기화 실패 시 롤백 불가
 * - 데이터 일관성 보장 어려움
 */

public class Day30SyncSystem {

    public static void main(String[] args) {
        // 시뮬레이션
        SyncManager manager = new SyncManager();

        // 1. 초기 동기화 (API v0 → DB v1)
        manager.syncFromApiToDb("USER-001");

        System.out.println("\n--- 충돌 시나리오 ---");

        // 2. API 수정 (v1 → v2)
        manager.updateInApi("USER-001", "john_api");

        Thread.sleep(100);  // 타임스탬프 차이 만들기

        // 3. DB에서 이전 버전(v1)으로 덮어쓰기 시도 → 충돌!
        VersionedUserData oldData = manager.db.get("USER-001");
        oldData.setUsername("john_db");
        manager.db.save(oldData);  // 👈 버전 충돌 발생!

        System.out.println("\n📊 최종 결과:");
        System.out.println("API: " + manager.api.get("USER-001").getUsername()
                + " (v" + manager.api.get("USER-001").getVersion() + ")");
        System.out.println("DB: " + manager.db.get("USER-001").getUsername()
                + " (v" + manager.db.get("USER-001").getVersion() + ")");
    }

}

// 충돌 해결 전략을 위한 인터페이스 생성
interface ConflictResolver {
    VersionedUserData resolve(
            VersionedUserData source,
            VersionedUserData target
    );
}

/** 충돌 해결 전략을 위한 구현체 */
// 최신 우선 구현체
class LastWriteWinsResolver implements ConflictResolver {
    @Override
    public VersionedUserData resolve(VersionedUserData source, VersionedUserData target) {
        return source.getLastModified() > target.getLastModified() ? source : target;
    }
}

//소스 우선
class SourceWinsResolver implements ConflictResolver {
    @Override
    public VersionedUserData resolve(VersionedUserData source, VersionedUserData target) {
        return source;
    }
}

// 타겟 우선
class targetWinsResolver implements ConflictResolver {
    @Override
    public VersionedUserData resolve(VersionedUserData source, VersionedUserData target) {
        return target;
    }
}




// 버전 관리 추가
class VersionedUserData {
    private String id;
    private String username;
    private String email;
    private int version;
    private long lastModified;

    public VersionedUserData(String id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.version = 0;
        this.lastModified = System.currentTimeMillis();
    }

    public void setUsername(String username) {
        this.username = username;
        this.lastModified = System.currentTimeMillis();
    }

    public void incrementVersion() {
        this.version++;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public int getVersion() {
        return version;
    }

    public long getLastModified() {
        return lastModified;
    }
}

// 인터페이스로 DataSource 다형화
interface DataSource {
    VersionedUserData get(String id);
    void save(VersionedUserData data);
}

class DatabaseDataSource implements DataSource {
    private Map<String, VersionedUserData> storage = new HashMap<>();
    private ConflictResolver resolver;

    public DatabaseDataSource(ConflictResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public VersionedUserData get(String id) {
        return storage.get(id);
    }

    @Override
    public void save(VersionedUserData data) {
        VersionedUserData existing = storage.get(data.getId());

        // 버전 충돌 체크
        if(existing != null && existing.getVersion() != data.getVersion()) {
            System.out.println("⚠️ 충돌 감지! Resolver 실행...");

            // 버전 증가로 충돌 해결
            VersionedUserData resolved = resolver.resolve(data, existing);
            resolved.incrementVersion();
            storage.put(resolved.getId(), resolved);
            
            System.out.println("✅ 충돌 해결: " + resolved.getUsername()
                    + " (v" + resolved.getVersion() + ")");
            return;
        }

        data.incrementVersion();
        storage.put(data.getId(), data);
        System.out.println("💾 DB 저장: " + data.getUsername() + " (v" + data.getVersion() + ")");
    }
}

class ApiDataSource implements DataSource {
    private Map<String, VersionedUserData> storage = new HashMap<>();
    private ConflictResolver resolver;


    public ApiDataSource(ConflictResolver resolver) {
        this.resolver = resolver;
        storage.put("USER-001", new VersionedUserData("USER-001", "john", "john@api.com"));
    }

    @Override
    public VersionedUserData get(String id) {
        return storage.get(id);
    }

    @Override
    public void save(VersionedUserData data) {
        VersionedUserData existing = storage.get(data.getId());

        if (existing != null && existing.getVersion() != data.getVersion()) {
            System.out.println("⚠️ API 충돌 감지! Resolver 실행...");

            VersionedUserData resolved = resolver.resolve(data, existing);
            resolved.incrementVersion();
            storage.put(resolved.getId(), resolved);

            System.out.println("✅ API 충돌 해결: " + resolved.getUsername()
                    + " (v" + resolved.getVersion() + ")");
            return;
        }

        data.incrementVersion();
        storage.put(data.getId(), data);
        System.out.println("🌐 API 저장: " + data.getUsername() + " (v" + data.getVersion() + ")");
    }
}
class CacheDataSource implements DataSource {
    private Map<String, VersionedUserData> storage = new HashMap<>();
    private ConflictResolver resolver;

    public CacheDataSource(ConflictResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public VersionedUserData get(String id) {
        return storage.get(id);
    }

    @Override
    public void save(VersionedUserData data) {
        VersionedUserData existing = storage.get(data.getId());

        if (existing != null && existing.getVersion() != data.getVersion()) {
            System.out.println("⚠️ Cache 충돌 감지! Resolver 실행...");

            VersionedUserData resolved = resolver.resolve(data, existing);
            resolved.incrementVersion();
            storage.put(resolved.getId(), resolved);

            System.out.println("✅ Cache 충돌 해결: " + resolved.getUsername()
                    + " (v" + resolved.getVersion() + ")");
            return;
        }

        data.incrementVersion();
        storage.put(data.getId(), data);
        System.out.println("🌐 API 저장: " + data.getUsername() + " (v" + data.getVersion() + ")");
    }
}

class SyncManager {
    public ApiDataSource api;
    public DatabaseDataSource db;
    public CacheDataSource cache;

    public SyncManager() {
        ConflictResolver resolver = new LastWriteWinsResolver();

        this.api = new ApiDataSource(resolver);
        this.db = new DatabaseDataSource(resolver);
        this.cache = new CacheDataSource(resolver);
    }


    // 문제 1: 버전 관리 없음 - 동시 수정 감지 불가
    public void syncFromApiToDb(String userId) {
        VersionedUserData apiData = api.get(userId);
        if (apiData != null) {
            db.save(apiData);
            System.out.println("✓ API → DB 동기화");
        }
    }

    // 문제 2: 충돌 감지 및 해결 전략 없음
    public void syncApiToDb(String userId) {
        VersionedUserData apiData = api.get(userId);
        VersionedUserData dbData = db.get(userId);

        if (apiData != null && dbData != null) {
            // 어느 것이 최신인지 모름!
            if (apiData.getLastModified() > dbData.getLastModified()) {
                db.save(apiData);
                System.out.println("✓ API가 최신 - DB 업데이트");
            } else {
                api.save(apiData);
                System.out.println("✓ DB가 최신 - API 업데이트");
            }
        }
    }

    public void updateInApi(String userId, String newUsername) {
        VersionedUserData data = api.get(userId);
        data.setUsername(newUsername);
        api.save(data);
        System.out.println("API 업데이트: " + newUsername);
    }

    public void updateInDb(String userId, String newUsername) {
        VersionedUserData data = db.get(userId);
        data.setUsername(newUsername);
        db.save(data);
        System.out.println("DB 업데이트: " + newUsername);
    }
}