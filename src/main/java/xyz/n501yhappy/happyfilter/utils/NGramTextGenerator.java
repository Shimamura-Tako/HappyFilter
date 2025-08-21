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
            // 添加开始标记以改善生成质量
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

    // 生成指定长度的文本
// 生成指定长度的文本（按字符数计算）
    public String generateText(int length) {
        StringBuilder result = new StringBuilder();

        // 创建初始键（N-1个空字符串）
        StringBuilder initialKeyBuilder = new StringBuilder();
        for (int i = 0; i < N - 1; i++) {
            initialKeyBuilder.append("");
        }
        String currentKey = initialKeyBuilder.toString();

        int maxAttempts = 1000; // 防止无限循环
        int attempts = 0;

        while (result.length() < length && attempts < maxAttempts) {
            List<String> possibleValues = nGramMap.get(currentKey);
            if (possibleValues == null || possibleValues.isEmpty()) {
                // 如果当前键没有后续值，重新开始
                StringBuilder keyBuilder = new StringBuilder();
                for (int i = 0; i < N - 1; i++) {
                    keyBuilder.append("");
                }
                currentKey = keyBuilder.toString();
                attempts++;
                continue;
            }

            String nextToken = possibleValues.get(random.nextInt(possibleValues.size()));

            // 处理结束标记
            if (nextToken.isEmpty()) {
                if (result.length() >= length * 0.7) { // 如果已经接近目标长度，则结束
                    break;
                } else { // 否则继续生成
                    attempts++;
                    continue;
                }
            }

            // 检查添加这个token是否会超过长度限制
            if (result.length() + nextToken.length() <= length) {
                result.append(nextToken);
            } else if (result.length() >= length * 0.7) {
                // 如果已经接近目标长度，可以结束
                break;
            }

            // 更新当前键
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
                // 补充空字符串以确保键长度为N-1
                for (int i = tokens.size(); i < N - 1; i++) {
                    keyBuilder.append("");
                }
                currentKey = keyBuilder.toString();
            }

            attempts++;
        }

        return result.toString();
    }


    // 将文本分割成字符列表
    private List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        // 按照示例文本的模式进行分词
        StringBuilder currentToken = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            currentToken.append(c);//TODO fenci
        }

        // 添加剩余的字符
        if (currentToken.length() > 0) {
            tokens.add(currentToken.toString());
        }

        return tokens;
    }
}
