package org.example.cleancode.day11;

import java.util.ArrayList;
import java.util.List;

/**
 *  장바구니 시스템 - 에러 처리 개선
 *
 * 1. 에러 처리 부재(null, 빈 문자열, 음수 값 등 잘못된 입력에 대한 검증 없음)
 * 2. 예외 처리 미흡(try-catch 없이 모든 작업 수행)
 * 3. 유효성 검사 분산(검증 로직이 여러 곳에 흩어질 가능성)
 * 4. 사용자 친화적이지 않은 에러 메시지
 *
 * */

public class Day11ShoppingCartSystem {

    public static void main(String[] args) {
        ShoppingCart cart = new ShoppingCart();

        // 정상 케이스
        cart.addItem("노트북", 1500000, 1);
        cart.addItem("마우스", 30000, 2);

        // 문제 있는 케이스들
        cart.addItem(null, 50000, 1);
        cart.addItem("", 50000, 1);
        cart.addItem("키보드", -10000, 1);
        cart.addItem("모니터", 300000, 0);
        cart.addItem("의자", 200000, -1);

        cart.checkout();
    }
}

// 장바구니 기능 모음
class ShoppingCart {
    
    private List<CartItem> items = new ArrayList<>();

    public boolean addItem(String name, int price, int quantity) {
        try {
            validateItem(name, price, quantity);
            CartItem item = new CartItem(name, price, quantity);
            items.add(item);
            System.out.println(name + " 추가됨");
            return true;
        } catch(InvalidCartItemException e) {
            System.out.println("✗ 추가 실패: " + e.getMessage());
            return false;
        }
    }

    private void validateItem(String name, int price, int quantity) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidCartItemException("상품명은 필수입니다");
        }
        if (price <= 0) {
            throw new InvalidCartItemException("가격은 0보다 커야 합니다");
        }
        if (quantity <= 0) {
            throw new InvalidCartItemException("수량은 0보다 커야 합니다");
        }
    }


    public int getTotal() {
        return items.stream()
                .mapToInt(CartItem::getTotalPrice)
                .sum();
    }

    public void checkout() {
        if (items.size() == 0) {
            System.out.println("장바구니가 비어있습니다");
            return;
        }

        System.out.println("\n=== 주문 내역 ===");
        for (CartItem item : items) {
            System.out.println(item.getName() + " x" + item.getQuantity() + " = " + (item.getPrice() * item.getQuantity()) + "원");
        }
        System.out.println("총 금액: " + getTotal() + "원");
    }
}

class CartItem {
    private final String name;
    private final int price;
    private final int quantity;

    public CartItem(String name, int price, int quantity) {
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

// 도메인 특화 예외 처리 클래스
class InvalidCartItemException extends RuntimeException {
    public InvalidCartItemException(String message) {
        super(message);
    }
}

