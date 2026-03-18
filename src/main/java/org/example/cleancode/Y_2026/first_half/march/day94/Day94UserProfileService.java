package org.example.cleancode.Y_2026.first_half.march.day94;


import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *  Day 94 과제 — Caffeine: 고성능 로컬 캐시 적용
 *
 * 1. cache + timestamps 두 맵 관리 -> Caffeine.newBuilder().expireAfterWrite()
 * 2. 캐시 크기 제한 없음 -> .maximumSize()
 * 3. getProfile에 캐시 로직 혼재 -> LoadingCache → get(key) 한 줄로 통합
 */
public class Day94UserProfileService {

    private final UserRepository userRepository;
    private final LoadingCache<String, UserProfile> cache;

    public Day94UserProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.cache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(60, TimeUnit.SECONDS)
                .recordStats()
                .build(userId -> userRepository.findById(userId));

    }

    public UserProfile getProfile(String userId) {
        return cache.get(userId);
    }

    public void updateProfile(String userId, UserProfile updated) {
        userRepository.save(updated);
        cache.invalidate(userId);
    }

    public void deleteProfile(String userId) {
        userRepository.delete(userId);
        cache.invalidate(userId);
    }

    public void printStats() {
        CacheStats stats = cache.stats();
        System.out.println("HIT  : " + stats.hitCount());
        System.out.println("MISS : " + stats.missCount());
        System.out.printf("HIT율 : %.0f%%%n", stats.hitRate() * 100);
    }

    public static void main(String[] args) {
        UserRepository repo = new UserRepository();
        Day94UserProfileService service = new Day94UserProfileService(repo);

        service.getProfile("U001"); // MISS → DB 조회
        service.getProfile("U001"); // HIT → 캐시
        service.getProfile("U002"); // MISS → DB 조회
        service.updateProfile("U001", new UserProfile("U001", "김철수(수정)", "new@example.com"));
        service.getProfile("U001"); // invalidate 후 → MISS

        service.printStats();
    }

}

@Data
@AllArgsConstructor
class UserProfile {
    private String userId;
    private String name;
    private String email;
}

class UserRepository {

    private final Map<String, UserProfile> database = new HashMap<>(Map.of(
            "U001", new UserProfile("U001", "김철수", "kim@example.com"),
            "U002", new UserProfile("U002", "이영희", "lee@example.com"),
            "U003", new UserProfile("U003", "박민준", "park@example.com")
    ));

    public UserProfile findById(String userId) {
        System.out.println("🔍 DB 조회: " + userId);
        return database.get(userId);
    }

    public void save(UserProfile profile) {
        System.out.println("💾 DB 저장: " + profile.getUserId());
        database.put(profile.getUserId(), profile);
    }

    public void delete(String userId) {
        System.out.println("🗑 DB 삭제: " + userId);
        database.remove(userId);
    }
}
