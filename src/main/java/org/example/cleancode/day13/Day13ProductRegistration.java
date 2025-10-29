package org.example.cleancode.day13;


/**
 * Day 13: 상품 등록 시스템
 *
 * 문제점:
 * - 생성자 파라미터가 너무 많음 (텔레스코핑 생성자 문제)
 * - 필수/선택 파라미터 구분이 불명확
 * - 객체 생성 시 파라미터 순서 혼동 가능
 * - 유효성 검증이 분산되어 있음
 */
public class Day13ProductRegistration {

        public static void main(String[] args) {
            // 생성자가 너무 많은 파라미터를 가짐 -> 빌더 패턴으로 변경
            Product product1 = new Product.Builder("P001", "노트북")
                    .description("삼성 갤럭시북")
                    .price(1500000)
                    .stock(10)
                    .category("전자제품")
                    .brand("삼성")
                    .featured(true)
                    .discountRate(0.1)
                    .imageUrl("laptop.jpg")
                    .build();

            // 일부 필드만 설정하고 싶을 때 null을 전달해야 함
            Product product2 = new Product.Builder("P002", "마우스")
                    .price(30000)
                    .stock(50)
                    .category("액세서리")
                    .build();


            System.out.println(product1);
            System.out.println(product2);
        }
}

class Product {
    private final String id;
    private final String name;
    private final String description;
    private final int price;
    private final int stock;
    private final String category;
    private final String brand;
    private final boolean featured;
    private final double discountRate;
    private final String imageUrl;

    // 모든 필드를 받는 거대한 생성자
    private Product(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;

        this.description = builder.description;
        this.price = builder.price;
        this.stock = builder.stock;
        this.category = builder.category;
        this.brand = builder.brand;
        this.featured = builder.featured;
        this.discountRate = builder.discountRate;
        this.imageUrl = builder.imageUrl;
    }

    public static class Builder {
        
        // 필수 필드
        private final String id;
        private final String name;

        // 선택 필드 (기본값 설정)
        private String description = "";
        private int price = 0;
        private int stock = 0;
        private String category = "";
        private String brand = "";
        private boolean featured = false;
        private double discountRate = 0.0;
        private String imageUrl = "";

        public Builder(String id, String name){
            this.id = id;
            this.name = name;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder price(int price) {
            this.price = price;
            return this;
        }

        public Builder brand(String brand) {
            this.brand = brand;
            return this;
        }

        public Builder featured(boolean featured) {
            this.featured = featured;
            return this;
        }


        public Builder discountRate(double discountRate) {
            this.discountRate = discountRate;
            return this;
        }

        public Builder imageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public Builder stock(int stock) {
            this.stock = stock;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Product build() {

            if (id == null || id.trim().isEmpty()) {
                throw new IllegalArgumentException("상품 ID는 필수입니다");
            }
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("상품명은 필수입니다");
            }
            if (price < 0) {
                throw new IllegalArgumentException("가격은 0 이상이어야 합니다");
            }
            if (stock < 0) {
                throw new IllegalArgumentException("재고는 0 이상이어야 합니다");
            }
            if (discountRate < 0 || discountRate > 1) {
                throw new IllegalArgumentException("할인율은 0~1 사이여야 합니다");
            }

            return new Product(this);
        }



    }


    // 차후 불변 객체에서 데이터에 직접 접근할때 사용
    /**예시
     *
     *  System.out.println("상품명: " + product.getName());
     *         System.out.println("가격: " + product.getPrice());
     *         System.out.println("재고: " + product.getStock());
     * */

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getPrice() { return price; }
    public int getStock() { return stock; }
    public String getCategory() { return category; }
    public String getBrand() { return brand; }
    public boolean isFeatured() { return featured; }
    public double getDiscountRate() { return discountRate; }
    public String getImageUrl() { return imageUrl; }

    public int getFinalPrice() {
        return (int)(price * (1 - discountRate));
    }

    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", finalPrice=" + getFinalPrice() +
                ", stock=" + stock +
                '}';
    }
}