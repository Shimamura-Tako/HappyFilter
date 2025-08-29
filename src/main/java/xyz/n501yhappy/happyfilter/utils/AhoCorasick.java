package xyz.n501yhappy.happyfilter.utils;

import java.util.*;

import static xyz.n501yhappy.happyfilter.config.PluginConfig.interferenceChars;

public class AhoCorasick {
    private final Node root = new Node();
    private List<Integer> oldPositions = new ArrayList<>();
    private String processedText;

    private void preprocess(String text) {
        StringBuilder newText = new StringBuilder();
        List<Integer> positions = new ArrayList<>();
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (!interferenceChars.contains(c)) {
                newText.append(c);
                positions.add(i);
            }
        }
        
        processedText = newText.toString();
        oldPositions = positions;
    }

    public void insert(String word) {
        int[] codePoints = word.codePoints().toArray();
        Node current = root;
        
        for (int cp : codePoints) {
            current = current.next.computeIfAbsent(cp, k -> new Node());
        }
        current.length = word.length();
    }

    public void build() {
        Queue<Node> queue = new ArrayDeque<>();
        root.fail = root;
        
        for (Node node : root.next.values()) {
            node.fail = root;
            queue.add(node);
        }
        
        while (!queue.isEmpty()) {
            Node u = queue.poll();
            
            for (Map.Entry<Integer, Node> entry : u.next.entrySet()) {
                int codePoint = entry.getKey();
                Node v = entry.getValue();
                Node fail = u.fail;
                
                while (fail != root && !fail.next.containsKey(codePoint)) {
                    fail = fail.fail;
                }
                
                if (fail.next.containsKey(codePoint)) {
                    fail = fail.next.get(codePoint);
                }
                v.fail = fail;
                queue.add(v);
            }
        }
    }

    public List<Hit> search(String text) {
        preprocess(text);
        int[] textCodePoints = processedText.codePoints().toArray();
        List<Hit> results = new ArrayList<>();
        Node current = root;
        
        for (int i = 0; i < textCodePoints.length; i++) {
            int cp = textCodePoints[i];
            
            while (current != root && !current.next.containsKey(cp)) {
                current = current.fail;
            }
            
            if (current.next.containsKey(cp)) {
                current = current.next.get(cp);
            }
            
            for (Node node = current; node != root && node.length > 0; node = node.fail) {
                if (!oldPositions.isEmpty() && 
                    (i - node.length + 1) >= 0 && 
                    (i - node.length + 1) < oldPositions.size() && 
                    i < oldPositions.size()) {
                    
                    int start = oldPositions.get(i - node.length + 1);
                    int end = oldPositions.get(i);
                    results.add(new Hit(start, end));
                }
            }
        }
        return results;
    }

    public static class Hit {
        public final int start;
        public final int end;

        public Hit(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

    private static class Node {
        Map<Integer, Node> next = new HashMap<>();
        Node fail;
        int length;
    }
}
