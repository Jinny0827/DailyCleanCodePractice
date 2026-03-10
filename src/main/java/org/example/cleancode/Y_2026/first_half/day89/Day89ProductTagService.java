package org.example.cleancode.Y_2026.first_half.day89;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;

import java.util.*;

/**
 * Day 89 과제: Guava로 컬렉션 처리 개선하기
 *
 * Multimap 활용: Map<String, List<String>> 대신 ArrayListMultimap을 사용하여 if 체크 로직을 제거하세요.
 *
 * ImmutableList 활용: getSpecialCategories()의 장황한 리스트 생성을 ImmutableList.of(...) 한 줄로 줄여보세요.
 *
 * 데이터 검증: addTag 등에서 파라미터가 null인지 확인할 때 Preconditions.checkNotNull()을 사용해 보세요.
 */
public class Day89ProductTagService {

    private Multimap<String, String> tagMap = ArrayListMultimap.create();

    public void addTag(String category, String tag) {
        Preconditions.checkNotNull(category, "category는 null일 수 없습니다.");
        Preconditions.checkNotNull(tag, "tag는 null일 수 없습니다.");

        tagMap.put(category, tag);
    }

    public List<String> getTags(String category) {
        // tagMap 기반으로 반환하는 컬렉션을 완벽한 불변 복사처리
        // null 체크는 자동 처리
        return ImmutableList.copyOf(tagMap.get(category));
    }

    public List<String> getSpecialCategories() {
        return ImmutableList.of("SALE", "EVENT", "LIMITED");
    }

    public static void main(String[] args) {

        Day89ProductTagService service = new Day89ProductTagService();

        // 데이터 추가 테스트
        service.addTag("Electronics", "Laptop");
        service.addTag("Electronics", "Smartphone");
        service.addTag("Books", "Clean Code");

        // 1. Multimap & getTags 테스트
        System.out.println("--- Electronics Tags ---");
        service.getTags("Electronics").forEach(System.out::println);

        // 2. ImmutableList.of 테스트
        System.out.println("\n--- Special Categories ---");
        System.out.println(service.getSpecialCategories());

        // 3. 방어적 복사 테스트 (에러 발생 확인)
        try {
            List<String> tags = service.getTags("Electronics");
            tags.add("Tablet"); // ImmutableList이므로 UnsupportedOperationException 발생
        } catch (UnsupportedOperationException e) {
            System.out.println("\n[안전] 반환된 리스트는 수정할 수 없습니다.");
        }

        // 4. Preconditions 테스트 (에러 발생 확인)
        try {
            service.addTag(null, "Test");
        } catch (NullPointerException e) {
            System.out.println("[검증 성공] " + e.getMessage());
        }

    }
}


