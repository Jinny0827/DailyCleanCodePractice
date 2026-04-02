package org.example.cleancode.Y_2026.first_half.april.day104;

import java.util.List;

public class Day104Order {

    private final String id;
    private final String userId;
    private final List<String> items;
    private final int totalAmount;

    public Day104Order(String id, String userId, List<String> items, int totalAmount) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("주문 ID는 필수입니다.");
        }
        if (totalAmount < 0) {
            throw new IllegalArgumentException("금액은 0 이상이어야 합니다.");
        }
        this.id = id;
        this.userId = userId;
        this.items = List.copyOf(items);
        this.totalAmount = totalAmount;
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public List<String> getItems() { return items; }
    public int getTotalAmount() { return totalAmount; }

}
