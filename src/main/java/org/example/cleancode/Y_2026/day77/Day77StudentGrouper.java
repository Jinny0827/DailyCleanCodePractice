package org.example.cleancode.Y_2026.day77;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.List;

/**
 * Day 77 과제 — Guava Collections 활용
 *
 * null 체크 후 리스트 초기화 — 반복되는 방어 코드
 * 문자열 += 반복 → 성능 낭비
 * split + trim 조합을 직접 구현 → Guava면 한 줄
 */
public class Day77StudentGrouper {

    public static void main(String[] args) {
        Day77StudentGrouper grouper = new Day77StudentGrouper();

        String raw = "Alice, CS\n" +
                "Bob, Math\n" +
                "Charlie, CS\n" +
                "Diana, Math\n" +
                "Eve, CS";

        // 1. 파싱
        List<String[]> students = grouper.parseRawData(raw);

        // 2. 그룹핑
        Multimap<String, String> grouped = grouper.groupByDepartment(students);

        // 3. 출력
        for (String dept : grouped.keySet()) {
            System.out.println(dept + ": " + grouper.formatGroup(new ArrayList<>(grouped.get(dept))));
        }
    }

    public Multimap<String, String> groupByDepartment(List<String[]> students) {
        Multimap<String, String> result = ArrayListMultimap.create();

        for (String[] student : students) {
          String name = student[0];
          String dept = student[1];
          // null 체크 없이 put
          result.put(dept, name);
        }

        return result;
    }

    public String formatGroup(List<String> names) {
        // Guava 통한 리팩터링
        return Joiner.on(", ").join(names);
    }

    public List<String[]> parseRawData(String raw) {
        List<String[]> result = new ArrayList<>();
        String[] lines = raw.split("\n");
        for (String line : lines) {
           // Guava 통한 리팩터링
           List<String> parts = Splitter.on(",").trimResults().splitToList(line);
           result.add(new String[] {
                   parts.get(0),
                   parts.get(1)
           });
            
        }
        return result;
    }
}


