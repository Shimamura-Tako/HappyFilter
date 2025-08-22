package xyz.n501yhappy.happyfilter.utils;

import java.util.*;

import static xyz.n501yhappy.happyfilter.config.PluginConfigs.n;


public class NGramTextGenerator {
    private static final int N = n; // 使用3-gram
    private Map<String, List<String>> nGramMap = new HashMap<>();
    private Random random = new Random();

    // 构建 N-gram 模型
    public void buildNGramModel(String[] texts) {
        for (String text : texts) {
            List<String> tokens = tokenize(text);
            List<String> paddedTokens = new ArrayList<>();
            for (int i = 0; i < N - 1; i++) {
                paddedTokens.add(""); // 使用空字符串作为开始标记
            }
            paddedTokens.addAll(tokens);
            paddedTokens.add(""); // 结束标记

            for (int i = 0; i < paddedTokens.size() - N + 1; i++) {
                StringBuilder keyBuilder = new StringBuilder();
                for (int j = 0; j < N - 1; j++) {
                    keyBuilder.append(paddedTokens.get(i + j));
                }
                String key = keyBuilder.toString();
                String value = paddedTokens.get(i + N - 1);
                nGramMap.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
            }
        }
    }
    public String generateText(int length) {
        StringBuilder result = new StringBuilder();
        StringBuilder initialKeyBuilder = new StringBuilder();
        for (int i = 0; i < N - 1; i++) {
            initialKeyBuilder.append("");
        }
        String currentKey = initialKeyBuilder.toString();

        int maxAttempts = 1000;
        int attempts = 0;

        while (result.length() < length && attempts < maxAttempts) {
            List<String> possibleValues = nGramMap.get(currentKey);
            if (possibleValues == null || possibleValues.isEmpty()) {
                StringBuilder keyBuilder = new StringBuilder();
                for (int i = 0; i < N - 1; i++) {
                    keyBuilder.append("");
                }
                currentKey = keyBuilder.toString();
                attempts++;
                continue;
            }

            String nextToken = possibleValues.get(random.nextInt(possibleValues.size()));
            if (nextToken.isEmpty()) {
                if (result.length() >= length * 0.7) {
                    break;
                } else {
                    attempts++;
                    continue;
                }
            }
            if (result.length() + nextToken.length() <= length) {
                result.append(nextToken);
            } else if (result.length() >= length * 0.9) {
                // 如果已经接近长度
                break;
            }
            List<String> tokens = tokenize(result.toString());
            if (tokens.size() >= N - 1) {
                StringBuilder keyBuilder = new StringBuilder();
                for (int i = tokens.size() - N + 1; i < tokens.size(); i++) {
                    keyBuilder.append(tokens.get(i));
                }
                currentKey = keyBuilder.toString();
            } else {
                StringBuilder keyBuilder = new StringBuilder();
                for (int i = 0; i < tokens.size(); i++) {
                    keyBuilder.append(tokens.get(i));
                }
                for (int i = tokens.size(); i < N - 1; i++) {
                    keyBuilder.append("");
                }
                currentKey = keyBuilder.toString();
            }

            attempts++;
        }

        return result.toString();
    }

    private List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        for (int i = 0; i < text.length(); i++) {
            tokens.add(String.valueOf(text.charAt(i)));
        }
        return tokens;
    }

}
