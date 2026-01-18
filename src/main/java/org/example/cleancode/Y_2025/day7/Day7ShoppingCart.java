package org.example.cleancode.Y_2025.day7;

import java.util.ArrayList;
import java.util.List;

/**
 * 1. 일급 컬렉션(First Class Collection) 패턴
 * List<Product>를 직접 사용하는 대신, Products 같은 컬렉션 wrapper 클래스 생성
 *
 * 2. 불변성(Immutability) 보장
 * Product 클래스의 필드를 final로 선언, Getter/Setter 메서드 구성
 *
 *
 * 3.책임 분리
 * 각 클래스가 자신의 데이터만 다루도록 설계
 *
 * 4. Stream Api 활용
 * 반복문 대신 Stream API 활용 (선언적인 코드 사용)
 * */

public class Day7ShoppingCart {
        public static void main(String[] args) {
            Products carts = new Products(List.of(
                    new Product("노트북", 1500000, 1),
                    new Product("마우스", 30000, 2)
            ));

            System.out.println("총 상품 개수: " + carts.getTotalQuantity());
            System.out.println("총 금액: " + carts.getTotalPrice() + "원");
            System.out.println("평균 가격: " + carts.getAveragePrice() + "원");
            System.out.println("가장 비싼 상품: " + carts.getMostExpensiveProduct());
        }
    }

    class Product {
        private final String name;
        private final int price;
        private final int quantity;

        Product(String name, int price, int quantity) {
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }

        public String getName() {
            return name;
        }

        public int getPrice() {
            return price;
        }

        public int getQuantity() {
            return quantity;
        }

        public int getTotalPrice() {
            return price * quantity;
        }
    }

    class Products {
        private final List<Product> products;

        public Products(List<Product> products) {
            this.products = new ArrayList<>(products);
        }


        public int getTotalQuantity() {
          return products.stream()
                  .mapToInt(Product::getQuantity)
                  .sum();
        }

        public int getTotalPrice() {
            return products.stream()
                    .mapToInt(Product::getTotalPrice)
                    .sum();
        }

        public double getAveragePrice() {
          if(products.isEmpty()) {
             return 0;
          }

          return (double) getTotalPrice() / getTotalQuantity();
        }

        public String getMostExpensiveProduct() {
            return products.stream()
                    .max((p1, p2) -> Integer.compare(p1.getPrice(), p2.getPrice()))
                    .map(Product::getName)
                    .orElse("없음");
        }

    }