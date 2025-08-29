// file: Filter.java
package xyz.n501yhappy.happyfilter.utils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Filter {
    private AhoCorasick acAutomaton = new AhoCorasick();
    private List<String> filterList = new ArrayList<>();

    public void buildAcAutomaton(List<String> filters) {
        this.filterList = new ArrayList<>(filters);
        acAutomaton = new AhoCorasick();

        for (String filter : filters) {
            acAutomaton.insert(filter);
        }
        acAutomaton.build();
    }

    public Filtered filterText(String message, List<String> filters) {
        if (!isFilterListEqual(this.filterList, filters)) {
            buildAcAutomaton(filters);
        }

        List<Integer> leftIndexes = new ArrayList<>();
        List<Integer> rightIndexes = new ArrayList<>();
        List<AhoCorasick.Hit> hits = acAutomaton.search(message);

        for (AhoCorasick.Hit hit : hits) {
            leftIndexes.add(hit.start);
            rightIndexes.add(hit.end);
        }

        return new Filtered(leftIndexes, rightIndexes, !hits.isEmpty());
    }

    public Filtered filterRegex(String message, List<String> regexPatterns) {
        List<Integer> leftIndexes = new ArrayList<>();
        List<Integer> rightIndexes = new ArrayList<>();

        for (String regexStr : regexPatterns) {
            Pattern pattern = Pattern.compile(regexStr);
            Matcher matcher = pattern.matcher(message);

            while (matcher.find()) {
                leftIndexes.add(matcher.start());
                rightIndexes.add(matcher.end() - 1);
            }
        }

        return new Filtered(leftIndexes, rightIndexes, !leftIndexes.isEmpty());
    }

    private boolean isFilterListEqual(List<String> list1, List<String> list2) {
        if (list1 == list2) return true;
        if (list1 == null || list2 == null) return false;
        if (list1.size() != list2.size()) return false;

        List<String> sortedList1 = new ArrayList<>(list1);
        List<String> sortedList2 = new ArrayList<>(list2);
        Collections.sort(sortedList1);
        Collections.sort(sortedList2);

        return sortedList1.equals(sortedList2);
    }
}
