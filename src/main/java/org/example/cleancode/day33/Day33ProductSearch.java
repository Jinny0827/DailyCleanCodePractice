package org.example.cleancode.day33;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Day 33: 검색 필터 시스템
 *
 * 문제점:
 * - 필터 조합 로직이 if문으로 하드코딩됨
 * - 새로운 필터 추가 시 메서드 수정 필요
 * - 필터 체인 구성이 유연하지 않음
 * - 필터 적용 순서 제어 불가
 */

public class Day33ProductSearch {

    public static void main(String[] args) {
        ProductSearchService service = new ProductSearchService();

        // 방법 1. 기존 방식
        System.out.println("=== 방법 1: 기존 방식 ===");
        List<Predicate<Product>> filters = Arrays.asList(
                new KeywordFilter("노트북"),
                new PriceRangeFilter(10000, 200000),
                new CategoryFilter("전자기기"),
                new InStockFilter(true)
        );

        List<Product> results = service.search(filters);
        System.out.println("검색 결과: " + results.size() + "건");
        results.forEach(System.out::println);



        // 방법 2: 빌더 방식
        System.out.println("\n=== 방법 2: 빌더 방식 ===");
        List<Product> results2 = service.searchBuilder()
                .keyword("노트북")
                .priceRange(10000, 200000)
                .category("전자기기")
                .onlyInStock()
                .execute();

        System.out.println("검색 결과: " + results2.size() + "건");
        results2.forEach(System.out::println);


        // 방법 3: 다양한 조합 테스트 (빌더를 사용한 이유 -> 일부 파라미터를 사용해서 객체 생성)
        System.out.println("\n=== 방법 3: 일부 조건만 ===");
        List<Product> results3 = service.searchBuilder()
                .category("전자기기")
                .execute();

        System.out.println("검색 결과: " + results3.size() + "건");
        results3.forEach(System.out::println);
    }

}


// Predicate = 입력된 객체가 특정 조건을 만족하는지 여부를 평가
class KeywordFilter implements Predicate<Product> {
    private final String keyword;

    public KeywordFilter(String keyword) {
        this.keyword = keyword;
    }

    @Override
    public boolean test(Product product) {

        if (keyword == null || keyword.isEmpty()) {
           return true;
        }

        return product.getName().toLowerCase()
                .contains(keyword.toLowerCase());
    }
}

class PriceRangeFilter implements Predicate<Product> {
    private final Integer minPrice;
    private final Integer maxPrice;

    public PriceRangeFilter(int minPrice, int maxPrice) {
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }
    @Override
    public boolean test(Product product) {

        if (minPrice != null && product.getPrice() < minPrice) {
            return false;
        }

        if (maxPrice != null && product.getPrice() > maxPrice) {
            return false;
        }

        return true;
    }
}


class CategoryFilter implements Predicate<Product> {
    private final String category;

    public CategoryFilter(String category) {
        this.category = category;
    }

    @Override
    public boolean test(Product product) {

        if (category == null || category.isEmpty()) {
            return true;
        }

        return product.getCategory().equals(category);
    }
}

class InStockFilter implements Predicate<Product> {
    private final boolean requireInStock;

    public InStockFilter(boolean requireInStock) {
        this.requireInStock = requireInStock;
    }

    @Override
    public boolean test(Product product) {

        if(!requireInStock) {
            return true;
        }

        return product.isInStock();
    }
}


// 검색용 빌더 클래스 생성
class SearchBuilder {
    private List<Predicate<Product>> filters = new ArrayList<>();
    private ProductSearchService service;


    public SearchBuilder(ProductSearchService service) {
        this.service = service;
    }


    public SearchBuilder keyword(String keyword) {
        if(keyword != null && !keyword.isEmpty()) {
            filters.add(new KeywordFilter(keyword));
        }

        return this;
    }

    public SearchBuilder priceRange(int minPrice, int maxPrice) {
        filters.add(new PriceRangeFilter(minPrice, maxPrice));

        return this;
    }

    public SearchBuilder category(String category) {
        filters.add(new CategoryFilter(category));

        return this;
    }

    public SearchBuilder onlyInStock() {
        filters.add(new InStockFilter(true));

        return this;
    }

    public List<Product> execute() {
        return service.search(filters);
    }
}



class Product {
    private String id;
    private String name;
    private int price;
    private String category;
    private boolean inStock;

    public Product(String id, String name, int price, String category, boolean inStock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.inStock = inStock;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getPrice() { return price; }
    public String getCategory() { return category; }
    public boolean isInStock() { return inStock; }

    @Override
    public String toString() {
        return String.format("%s - %s (%d원, %s)",
                id, name, price, inStock ? "재고 있음" : "품절");
    }
}


class ProductSearchService {
    private List<Product> database;

    public ProductSearchService() {
        // 테스트 데이터
        database = Arrays.asList(
                new Product("P001", "삼성 노트북", 150000, "전자기기", true),
                new Product("P002", "LG 모니터", 30000, "전자기기", false),
                new Product("P003", "애플 맥북", 250000, "전자기기", true),
                new Product("P004", "책상", 50000, "가구", true)
        );
    }

    // 문제: 모든 필터가 하나의 메서드에 집중
    public List<Product> search(List<Predicate<Product>> filters) {
        
        // 모든 필터를 하나의 predicate로 통합
        Predicate<Product> combinedFilter =
                // 모든 필터를 AND로 결합해서 맞으면 true 반환
                filters.stream()
                        .reduce(Predicate::and)
                        .orElse(product -> true);
        return database.stream()
                .filter(combinedFilter)
                .collect(Collectors.toList());
    }


    public SearchBuilder searchBuilder() {
        return new SearchBuilder(this);
    }
}