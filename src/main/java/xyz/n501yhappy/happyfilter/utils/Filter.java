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

        // 构建指针
        acAutomaton.build();
    }

    public Filtered filter_1(String message, List<String> Filter_list) {
        // 如果过滤词列表change->重构
        if (!this.filterList.equals(Filter_list)) {
            buildAcAutomaton(Filter_list);
        }
        List<Integer> LIndexes = new ArrayList<>();
        List<Integer> RIndexes = new ArrayList<>();

        // aczdj -> sousuo
        List<AhoCorasick.Hit> hits = acAutomaton.search(message);

        boolean isFiltered = !hits.isEmpty();
        for (AhoCorasick.Hit hit : hits) {
            LIndexes.add(hit.start);
            RIndexes.add(hit.end);
        }

        return new Filtered(LIndexes, RIndexes, isFiltered);
    }
    public Filtered filter_regex(String message, List<String> regex) {
        Pattern pattern = Pattern.compile("");
        Matcher matcher = pattern.matcher(message);
        List<Integer> LIndexes = new ArrayList<>();
        List<Integer> RIndexes = new ArrayList<>();
        boolean isFiltered = false;
        for (String regex_str : regex){
            pattern = Pattern.compile(regex_str);
            matcher = pattern.matcher(message);
            if (matcher.find()){
                if (isFiltered == false) isFiltered = true;
                LIndexes.add(matcher.start());
                RIndexes.add(matcher.end() -1);
            }
        }
        return new Filtered(LIndexes, RIndexes, isFiltered);
    }
}
