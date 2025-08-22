package xyz.n501yhappy.happyfilter.utils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Filter {
    private AhoCorasick acAutomaton = new AhoCorasick();
    private List<String> filterList = new ArrayList<>();

    public void buildAcAutomaton(List<String> filterList) {
        this.filterList = new ArrayList<>(filterList);
        acAutomaton = new AhoCorasick();

        // 插入所有过滤词到AC自动机
        for (String filter : filterList) {
            acAutomaton.insert(filter);
        }

        // 构建失败指针
        acAutomaton.build();
    }

    public Filtered filter_1(String message, List<String> filterList) {
        // 如果过滤词列表改变->重构
        if (!isFilterListEqual(this.filterList, filterList)) {
            buildAcAutomaton(filterList);
        }

        List<Integer> leftIndexes = new ArrayList<>();
        List<Integer> rightIndexes = new ArrayList<>();

        // AC自动机搜索
        List<AhoCorasick.Hit> hits = acAutomaton.search(message);

        boolean isFiltered = !hits.isEmpty();
        for (AhoCorasick.Hit hit : hits) {
            leftIndexes.add(hit.start);
            rightIndexes.add(hit.end);
        }

        return new Filtered(leftIndexes, rightIndexes, isFiltered);
    }

    public Filtered filter_regex(String message, List<String> regexPatterns) {
        List<Integer> leftIndexes = new ArrayList<>();
        List<Integer> rightIndexes = new ArrayList<>();
        boolean isFiltered = false;

        // 预编译正则表达式以提高性能
        for (String regexStr : regexPatterns) {
            Pattern pattern = Pattern.compile(regexStr);
            Matcher matcher = pattern.matcher(message);

            while (matcher.find()) {
                isFiltered = true;
                leftIndexes.add(matcher.start());
                rightIndexes.add(matcher.end() - 1);
            }
        }

        return new Filtered(leftIndexes, rightIndexes, isFiltered);
    }

    /**
     * 比较两个过滤词列表是否相等
     * @param list1 第一个列表
     * @param list2 第二个列表
     * @return 如果两个列表相等返回true，否则返回false
     */
    private boolean isFilterListEqual(List<String> list1, List<String> list2) {
        if (list1 == list2) {
            return true;
        }

        if (list1 == null || list2 == null) {
            return false;
        }

        if (list1.size() != list2.size()) {
            return false;
        }

        // 由于顺序可能影响AC自动机构建，这里比较排序后的列表
        List<String> sortedList1 = new ArrayList<>(list1);
        List<String> sortedList2 = new ArrayList<>(list2);
        Collections.sort(sortedList1);
        Collections.sort(sortedList2);

        return sortedList1.equals(sortedList2);
    }
}
