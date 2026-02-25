package org.example.cleancode.Y_2026.day80;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Day 80 🔥 — CompletableFuture 리팩터링
 *
 * | **순차 블로킹** | 독립적인 API 호출이 직렬 실행 → 총 2000ms |
 * | **부분 실패 처리 없음** | 하나 실패 시 전체 실패 |
 * | **타임아웃 없음** | 특정 클라이언트 hang 시 무한 대기 |
 * | **스레드 낭비** | 요청 스레드가 I/O 동안 계속 점유 |
 */

@Slf4j
@RequiredArgsConstructor
public class Day80ProductDetailService {
    private final InventoryClient inventoryClient;
    private final ReviewClient reviewClient;
    private final RecommendClient recommendClient;
    private final PriceClient priceClient;

    // 메서드 내에서 실행시 4 * 100 처리 됨
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    public ProductDetailResponse getDetail(Long productId) {

        CompletableFuture<InventoryInfo> inventoryFuture =
                CompletableFuture.supplyAsync(() -> inventoryClient.getInventory(productId), executor)
                        .orTimeout(1, TimeUnit.SECONDS)
                        .exceptionally(e -> {
                            log.warn("inventory 조회 실패: {}", e.getMessage());
                            return InventoryInfo.builder().productId(productId).stockCount(0).available(false).build();
                        });

        CompletableFuture<ReviewSummary> reviewSummaryFuture =
                CompletableFuture.supplyAsync(() -> reviewClient.getReviews(productId), executor)
                        .orTimeout(1, TimeUnit.SECONDS)
                        .exceptionally(e -> {
                            log.warn("review 조회 실패: {}", e.getMessage());
                            return ReviewSummary.builder().productId(productId).averageScore(0.0).reviewCount(0).build();
                        });

        CompletableFuture<List<Product>> recommendsFuture =
                CompletableFuture.supplyAsync(() -> recommendClient.getRecommends(productId), executor)
                        .orTimeout(1, TimeUnit.SECONDS)
                        .exceptionally(e -> {
                            log.warn("recommend 조회 실패: {}", e.getMessage());
                            return List.of();
                        });

        CompletableFuture<PriceInfo> priceFuture =
                CompletableFuture.supplyAsync(() -> priceClient.getPrice(productId), executor)
                        .orTimeout(1, TimeUnit.SECONDS)
                        .exceptionally(e -> {
                            log.warn("price 조회 실패: {}", e.getMessage());
                            return PriceInfo.builder()
                                    .productId(productId).originalPrice(0)
                                    .discountPrice(0).discountRate(0).build();
                        });

        CompletableFuture.allOf(inventoryFuture, reviewSummaryFuture, recommendsFuture, priceFuture).join();


        return ProductDetailResponse.builder()
                .inventory(inventoryFuture.join())
                .reviews(reviewSummaryFuture.join())
                .recommends(recommendsFuture.join())
                .price(priceFuture.join())
                .build();
    }


}

@Data
@Builder
class InventoryInfo {
    private Long productId;
    private int stockCount;
    private boolean available;
}

@Data @Builder
class ReviewSummary {
    private Long productId;
    private double averageScore;
    private int reviewCount;
}

@Data @Builder
class Product {
    private Long productId;
    private String name;
    private int price;
}

@Data @Builder
class PriceInfo {
    private Long productId;
    private int originalPrice;
    private int discountPrice;
    private int discountRate;
}

@Data @Builder
class ProductDetailResponse {
    private InventoryInfo inventory;
    private ReviewSummary reviews;
    private List<Product> recommends;
    private PriceInfo price;
}

interface InventoryClient {
    InventoryInfo getInventory(Long productId);  // 500ms
}

interface ReviewClient {
    ReviewSummary getReviews(Long productId);    // 600ms
}

interface RecommendClient {
    List<Product> getRecommends(Long productId); // 400ms
}

interface PriceClient {
    PriceInfo getPrice(Long productId);          // 500ms
}

class StubInventoryClient implements InventoryClient {
    public InventoryInfo getInventory(Long productId) {
        sleep(500);
        return InventoryInfo.builder()
                .productId(productId).stockCount(10).available(true).build();
    }

    // 공통 유틸
    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}

class StubReviewClient implements ReviewClient {
    public ReviewSummary getReviews(Long productId) {
        sleep(600);
        return ReviewSummary.builder()
                .productId(productId).averageScore(4.5).reviewCount(120).build();
    }

    // 공통 유틸
    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}

class StubRecommendClient implements RecommendClient {
    public List<Product> getRecommends(Long productId) {
        sleep(400);
        return List.of(
                Product.builder().productId(2L).name("연관상품A").price(15000).build(),
                Product.builder().productId(3L).name("연관상품B").price(23000).build()
        );
    }

    // 공통 유틸
    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}

class StubPriceClient implements PriceClient {
    public PriceInfo getPrice(Long productId) {
        sleep(500);
        return PriceInfo.builder()
                .productId(productId).originalPrice(30000)
                .discountPrice(24000).discountRate(20).build();
    }

    // 공통 유틸
    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}




