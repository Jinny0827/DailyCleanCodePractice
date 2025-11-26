package org.example.cleancode.day34;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Day 34: 페이지네이션 시스템
 *
 * 페이지 정보 캡슐화: 페이지 번호, 크기, 총 개수 등을 하나의 객체로 관리
 * 계산 로직 분리: offset, 페이지 수 계산 등을 별도 메서드로
 * 불변 객체: 페이지 정보는 불변으로 유지
 * 유효성 검증: 잘못된 페이지 번호 처리
 */


public class Day34PaginationSystem {

    public static void main(String[] args) {

        ProductRepository repo = new ProductRepository();

        PageRequest request1 = new PageRequest(1, 5);
        Page<Product> page1 = repo.findProducts(request1);

        System.out.println("=== 1페이지 ===");
        page1.getContent().forEach(System.out::println);
        System.out.println("hasNext : " + page1.hasNext());


        PageRequest request2 = new PageRequest(2, 5);
        Page<Product> page2 = repo.findProducts(request2);

        System.out.println("=== 2페이지 ===");
        page2.getContent().forEach(System.out::println);
        System.out.println("\n총 " + page2.getTotalPages() + "페이지");
    }

}

class Product {
    private String id;
    private String name;

    public Product(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return id + " - " + name;
    }
}

class PageRequest {
    private final int pageNumber;
    private final int pageSize;

    public PageRequest(int pageNumber, int pageSize) {
        // 유효성 검증
        if(pageNumber < 1) {
            throw new IllegalArgumentException("페이지 번호는 1이상이어야 합니다.");
        }

        if(pageSize < 1) {
            throw new IllegalArgumentException("페이지 크기는 1 이상이어야 합니다");
        }

        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
    }

    public int getOffset() {
        return (pageNumber - 1) * pageSize;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }
}

class Page<T> {
    private final List<T> content;
    private final int totalElements;
    private final int pageNumber;
    private final int pageSize;

    public Page(List<T> content, int totalElements, int pageNumber, int pageSize) {
        // 방어적 복사(페이지 리스트에 대한)
        this.content = new ArrayList<>(content);
        this.totalElements = totalElements;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
    }

    public List<T> getContent() {
        return new ArrayList<>(content);
    }

    // 총 페이지 수 계산
    public int getTotalPages (){
        return (int) Math.ceil((double) totalElements / pageSize);
    }

    // 다음 페이지 존재 여부
    public boolean hasNext() {
        return pageNumber < getTotalPages();
    }
    
    // 이전 페이지 존재 여부
    public boolean hasPrevious() {
        return pageNumber > 1;
    }
    
    // 첫 페이지 여부
    public boolean isFirst() {
        return pageNumber == 1;
    }

    // 마지막 페이지 여부
    public boolean isLast() {
        return pageNumber == getTotalPages();
    }

    public int getTotalElements() {
        return totalElements;
    }
}



class ProductRepository {
    private final List<Product> database;

    public ProductRepository() {
        database = Arrays.asList(
                new Product("P001", "노트북"),
                new Product("P002", "마우스"),
                new Product("P003", "키보드"),
                new Product("P004", "모니터"),
                new Product("P005", "헤드셋"),
                new Product("P006", "웹캠"),
                new Product("P007", "스피커"),
                new Product("P008", "마이크"),
                new Product("P009", "태블릿"),
                new Product("P010", "충전기")
        );
    }

    // 문제 1: offset 계산이 호출하는 쪽에서 반복됨
    public Page<Product> findProducts(PageRequest pageRequest) {
        int offset = pageRequest.getOffset();
        int pageSize = pageRequest.getPageSize();
        int totalElements = database.size();

        if(offset >= totalElements) {
            return new Page<>(
                    new ArrayList<>(),
                    totalElements,
                    pageRequest.getPageNumber(),
                    pageSize
            );
        }

        int end = Math.min(offset + pageSize, totalElements);
        List<Product> content = database.subList(offset, end);

        return new Page<>(
                content,
                totalElements,
                pageRequest.getPageNumber(),
                pageSize
        );
    }
}