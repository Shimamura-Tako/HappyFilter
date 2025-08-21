package xyz.n501yhappy.happyfilter.utils;

import java.util.*;

import static xyz.n501yhappy.happyfilter.config.PluginConfigs.filter_level;
import static xyz.n501yhappy.happyfilter.config.PluginConfigs.interference_characters;

public class AhoCorasick {

    private final Node root = new Node();//根节点

    private List<Integer> old_pos = new ArrayList<>();
    private String processed_text;

    private void Preprocess(String text){
        StringBuilder new_text = new StringBuilder();
        List<Integer> old_positions = new ArrayList<>();
        Character c;
        for (int i = 0; i < text.length(); i++) {
            c = text.charAt(i);
            if (interference_characters.contains(c)){
                new_text.append(c);
                old_positions.add(i);
            }
        }
        processed_text = new_text.toString();
        old_pos = old_positions;
    }

    // build tire
    public void insert(String word) {
        int[] codePoints = word.codePoints().toArray();//弄成码点数组
        Node cur = root;
        for (int cp : codePoints) {
            cur = cur.next.computeIfAbsent(cp, c -> new Node());
        }
        cur.len = word.length();
    }

    // bfs构建失败指针
    public void build() {
        Queue<Node> q = new ArrayDeque<>();
        root.fail = root;
        for (Node v : root.next.values()) {
            v.fail = root;
            q.add(v);
        }
        while (!q.isEmpty()) {
            Node u = q.poll();
            int cp;
            Node v,f;//值和失陪指针
            for (Map.Entry<Integer, Node> e : u.next.entrySet()) {
                cp = e.getKey();
                v = e.getValue();
                f = u.fail;
                while (f != root && !f.next.containsKey(cp)) f = f.fail;
                if (f.next.containsKey(cp)) f = f.next.get(cp);
                v.fail = f;
                q.add(v);
            }
        }
    }

    public List<Hit> search(String text) {
        processed_text = text;
        if (filter_level > 1) Preprocess(text);
        text = processed_text;
        int[] textCp = text.codePoints().toArray();
        List<Hit> res = new ArrayList<>();
        Node cur = root;
        for (int i = 0; i < textCp.length; i++) {
            int cp = textCp[i];
            while (cur != root && !cur.next.containsKey(cp)) cur = cur.fail;
            if (cur.next.containsKey(cp)) cur = cur.next.get(cp);
            for (Node p = cur; p != root && p.len > 0; p = p.fail) {
                res.add(new Hit(old_pos.get(p.len - 1), old_pos.get(i)));
            }
        }
        return res;
    }
    public static class Hit {
        public final int start;
        public final int end;   //[start,end]
        public Hit(int s, int e) { start = s; end = e; }
        // @Override public String toString() { return "[" + start + "," + end + "]"; }
    }
    private static class Node {
        Map<Integer, Node> next = new HashMap<>();
        Node fail;//失陪指针
        int len;
    }
}