package org.example.cleancode.Y_2025.day35;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Day 35: 상품 정렬 시스템
 *
 * 문제점:
 * - 정렬 기준이 하드코딩됨 (if-else 분기)
 * - 다중 정렬 조건 처리 불가
 * - 정렬 방향(오름차순/내림차순) 제어 어려움
 * - 새로운 정렬 기준 추가 시 기존 코드 수정 필요
 */

public class Day35ProductSorting {

    public static void main(String[] args) {
        ProductService service = new ProductService();

        System.out.println("=== 평점순 (같으면 가격순) ===");
        Comparator<Product> ratingThenPrice = new SortBuilder()
                .orderBy(ProductComparators.BY_RATING)
                .direction(SortDirection.DESC)
                .thenBy(ProductComparators.BY_PRICE)
                .build();

        // thenBy로 이미 정렬된 값을 다른 조건으로 재정렬(여기선 price로)
        service.sortProducts(ratingThenPrice)
                .forEach(System.out::println);


        System.out.println("\n=== 재고순 (같으면 이름순) ===");
        Comparator<Product> stockThenName = new SortBuilder()
                .orderBy(ProductComparators.BY_STOCK)
                .thenBy(ProductComparators.BY_NAME)
                .build();

        service.sortProducts(stockThenName)
                .forEach(System.out::println);
    }

}

// 정렬 프리셋(자주 사용될 정렬조건들 상수화)
class SortPreset {
    // 인기순 (평점 높고 재고 많은 순)
    public static final Comparator<Product> POPULAR =
            new SortBuilder()
                    .orderBy(ProductComparators.BY_RATING)
                    .direction(SortDirection.DESC)
                    .thenBy(ProductComparators.BY_STOCK)
                    .build();
    
    
    // 가성비 순 (가격 낮고 평점 높은 순)
    public static final Comparator<Product> BEST_VALUE =
            new SortBuilder()
                    .orderBy(ProductComparators.BY_PRICE)
                    .thenBy(ProductComparators.BY_RATING)
                    .build();
    
    // 프리미엄 순 (가격 높고 평점 높은 순)
    public static final Comparator<Product> PREMIUM =
            new SortBuilder()
                    .orderBy(ProductComparators.BY_PRICE)
                    .direction(SortDirection.DESC)
                    .thenBy(ProductComparators.BY_RATING)
                    .build();

}


// 정렬 방향 상수 모음 enum
enum SortDirection {
    ASC, DESC
}

class SortBuilder {
    private Comparator<Product> comparator;

    //기본 오름차순
    private SortDirection direction = SortDirection.ASC;


    public SortBuilder orderBy(Comparator<Product> comparator) {
        if(comparator == null ) {
            throw new IllegalArgumentException("Comparator는 null일 수 없습니다");
        }

        this.comparator = comparator;
        return this;
    }

    public SortBuilder direction(SortDirection direction) {
        this.direction = direction;
        return this;
    }

    public SortBuilder thenBy(Comparator<Product> secondaryComparator) {
        if(comparator == null) {
            throw new IllegalStateException("orderBy()를 먼저 호출해야 합니다");
        }

        if(secondaryComparator == null) {
            throw new IllegalArgumentException("Secondary comparator는 null일 수 없습니다");
        }

        this.comparator = this.comparator.thenComparing(secondaryComparator);
        return this;
    }

    public Comparator<Product> build() {
        if(this.comparator == null) {
            throw new IllegalStateException("정렬 기준이 설정되지 않았습니다");
        }

        if(direction == SortDirection.DESC) {
            return comparator.reversed();
        }

        return comparator;
    }
}


class ProductComparators {
    
    // 가격 기준 오름차순
    public static final Comparator<Product> BY_PRICE =
            Comparator.comparingInt(Product::getPrice);
    //이름 오름차순
    public static final Comparator<Product> BY_NAME =
            Comparator.comparing(Product::getName);

    // 평점 내림차순(높은 평점이 먼저)
    public static final Comparator<Product> BY_RATING =
            Comparator.comparingDouble(Product::getRating);

    // 재고 오름차순
    public static final Comparator<Product> BY_STOCK =
            Comparator.comparingInt(Product::getStock);
}


class Product {
    private String id;
    private String name;
    private int price;
    private int stock;
    private double rating;

    public Product(String id, String name, int price, int stock, double rating) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.rating = rating;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getPrice() { return price; }
    public int getStock() { return stock; }
    public double getRating() { return rating; }

    @Override
    public String toString() {
        return String.format("%s - %s (%d원, 재고:%d, 평점:%.1f)",
                id, name, price, stock, rating);
    }
}

class ProductService {
    private List<Product> products;

    public ProductService() {
        products = new ArrayList<>();
        products.add(new Product("P001", "노트북", 1500000, 5, 4.5));
        products.add(new Product("P002", "마우스", 30000, 15, 4.8));
        products.add(new Product("P003", "키보드", 80000, 8, 4.2));
        products.add(new Product("P004", "모니터", 300000, 3, 4.7));
        products.add(new Product("P005", "헤드셋", 50000, 12, 4.5));
        products.add(new Product("P006", "웹캠", 150000, 7, 4.7));
    }

    // 문제: 정렬 기준이 if-else로 하드코딩됨
    public List<Product> sortProducts(Comparator<Product> comparator) {
      List<Product> result = new ArrayList<>(products);
      result.sort(comparator);
      return result;
    }
}