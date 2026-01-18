package org.example.cleancode.Y_2025.day12;

import java.util.*;

public class Day12ProductService {


    /**
     * Day 12 - Optional을 활용한 Null 안전성
     *
     * 문제점:
     * 1. null 체크가 여러 곳에 산재
     * 2. NullPointerException 발생 가능성
     * 3. null을 반환하는 메서드들
     * 4. 중첩된 null 체크
     *
     * 개선 목표:
     * - Optional을 사용한 명시적인 null 처리
     * - 함수형 스타일의 안전한 체이닝
     * - 기본값 처리 개선
     */

        public static void main(String[] args) {
            ProductRepository repository = new ProductRepository();
            ProductService service = new ProductService(repository);

            // 테스트 케이스
            System.out.println("=== 상품 조회 테스트 ===");
            service.printProductInfo("LAPTOP-001");
            service.printProductInfo("PHONE-999"); // null 데이터

            System.out.println("\n=== 할인가 계산 테스트 ===");
            service.printDiscountedPrice("LAPTOP-001");
            service.printDiscountedPrice("TABLET-003");

            System.out.println("\n=== 재고 부족 상품 조회 ===");
            service.printLowStockProducts();
        }
    }

    class ProductService {
        private ProductRepository repository;

        public ProductService(ProductRepository repository) {
            this.repository = repository;
        }

        // 문제 1: null을 반환하고, null 체크가 호출하는 쪽에서 필요
        public void printProductInfo(String productId) {
            repository.findById(productId)
                   .ifPresentOrElse(
                           product-> {
                               String categoryInfo = product.getCategory()
                                       .orElse("카테고리 없음");
                               System.out.println(product.getName() + " (" + categoryInfo + ") - " + product.getPrice() + "원");
                           },
                           () -> System.out.println("상품을 찾을 수 없습니다.")
                   );
        }

        // 문제 2: 중첩된 null 체크
        public void printDiscountedPrice(String productId) {
                repository.findById(productId)
                        .ifPresentOrElse(
                                product-> {
                                    product.getDiscount()
                                            .ifPresentOrElse(
                                                    discount -> {
                                                        int discountedPrice = product.getPrice() - discount.getAmount();
                                                        System.out.println(product.getName() + " 할인가: " + discountedPrice + "원");
                                                    },
                                                    () -> System.out.println(product.getName() + " 할인 없음")
                                            );
                                },
                                () -> System.out.println("상품을 찾을 수 없습니다")
                        );
        }

        // 문제 3: 리스트를 반환하지만 null일 수 있음
        public void printLowStockProducts() {
            List<Product> products = repository.findLowStockProducts();
            if(products.isEmpty()) {
                System.out.println("재고 부족 상품이 없습니다");
                return;
            }

            System.out.println("재고 부족 상품:");
            products.forEach(product ->
                    System.out.println("- " + product.getName() + " (재고: " + product.getStock() + ")")
            );
        }
    }

    class ProductRepository {
        private Map<String, Product> products = new HashMap<>();

        public ProductRepository() {
            // 테스트 데이터
            products.put("LAPTOP-001", new Product("LAPTOP-001", "노트북", 1500000, "전자기기", 5, new Discount(100000)));
            products.put("MOUSE-002", new Product("MOUSE-002", "마우스", 30000, "전자기기", 15, null));
            products.put("TABLET-003", new Product("TABLET-003", "태블릿", 800000, null, 3, new Discount(50000)));
        }

        // 문제: null을 반환
        public Optional<Product> findById(String id) {
            // null 허용 처리
            return Optional.ofNullable(products.get(id));
        }

        // 문제: null을 반환할 수 있음
        public List<Product> findLowStockProducts() {
            List<Product> result = new ArrayList<>();
            for (Product product : products.values()) {
                if (product.getStock() < 10) {
                    result.add(product);
                }
            }
            return result;
        }
    }

    class Product {
        private String id;
        private String name;
        private int price;
        private String category;  // null 가능
        private int stock;
        private Discount discount;  // null 가능

        public Product(String id, String name, int price, String category, int stock, Discount discount) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.category = category;
            this.stock = stock;
            this.discount = discount;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public int getPrice() { return price; }
        public Optional<String> getCategory() { return Optional.ofNullable(category); }
        public int getStock() { return stock; }
        public Optional<Discount> getDiscount() { return Optional.ofNullable(discount); }
    }

    class Discount {
        private int amount;

        public Discount(int amount) {
            this.amount = amount;
        }

        public int getAmount() { return amount; }
}
