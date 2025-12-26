package xyz.n501yhappy.happyfilter.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import net.md_5.bungee.api.ChatColor;
import xyz.n501yhappy.happyfilter.HappyFilter;
import xyz.n501yhappy.happyfilter.utils.Filter;
import xyz.n501yhappy.happyfilter.utils.Filtered;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static xyz.n501yhappy.happyfilter.config.PluginConfig.*;

public class ChatListener implements Listener {
    private final Filter filter = new Filter();
    private final Random random = new Random();
    private final Map<Player, List<PlayerMessage>> messageHistory = new ConcurrentHashMap<>();

    private static class PlayerMessage {
        final String message;
        final long time;
        PlayerMessage(String message, long time) {
            this.message = message;
            this.time = time;
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        // 检测前置条件部分
        if (!isEnable) return;
        Player player = event.getPlayer();
        if (player.hasPermission(permissions.get("bypass"))) return;
        
        // 消息合并
        String message = event.getMessage();
        String mergedMessage = mergeHistory(player, message);
        
        // 干扰字符处理
        String solvedMessage = mergedMessage;
        List<Integer> indexMapping = new ArrayList<>();
        if (anti_interference_enabled) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mergedMessage.length(); i++) {
                char c = mergedMessage.charAt(i);
                if (!interferenceChars.contains(c)) {
                    indexMapping.add(i);
                    sb.append(c);
                }
            }
            solvedMessage = sb.toString();
        }
        if (to_lower) {
            solvedMessage = solvedMessage.toLowerCase();
        }
        
        Filtered result = filter.filterText(solvedMessage, filterWords);
        if (regex_enabled) {
            result = result.merge(filter.filterRegex(solvedMessage, regexPatterns));
        }
        
        // 过滤处理
        if (result.isFiltered()) {
            event.setMessage(solveMessages(message, mergedMessage, result, player, indexMapping, solvedMessage));
            if (enableWarning) {
                player.sendMessage(PREFIX + WARNING_MESSAGE);
            }
            messageHistory.remove(player);
        } else {
            updateMessageHistory(player, message);
        }
    }
    
    private String mergeHistory(Player player, String currentMessage) {
        List<PlayerMessage> history = messageHistory.computeIfAbsent(player, k -> new ArrayList<>());
        long now = System.currentTimeMillis();
        history.removeIf(msg -> now - msg.time > 2000);

        StringBuilder merged = new StringBuilder();
        for (PlayerMessage msg : history) {
            merged.append(msg.message);
        }
        merged.append(currentMessage);
        return merged.toString();
    }

    private void updateMessageHistory(Player player, String message) {
        List<PlayerMessage> history = messageHistory.computeIfAbsent(player, k -> new ArrayList<>());
        history.add(new PlayerMessage(message, System.currentTimeMillis()));
        while (history.size() > 20) history.remove(0);
    }

    private String solveMessages(String message, String mergedMessage, Filtered result, Player player, List<Integer> indexMapping, String solvedMessage) {
        int startIndex = mergedMessage.length() - message.length();
        StringBuilder retMessage = new StringBuilder(message);
        
        // 从后往前处理，避免位置偏移问题
        List<ReplacementInfo> replacements = new ArrayList<>();
        
        for (int i = 0; i < result.getRIndexes().size(); i++) {
            int l_index = result.getLIndexes().get(i);
            int r_index = result.getRIndexes().get(i);
            String bad_word = solvedMessage.substring(l_index, r_index);
            
            // 检查是否为特殊屏蔽词
            String replacement = getReplacement(bad_word, r_index - l_index);
            
            // 计算在原始消息中的起始和结束位置
            int originalStart = -1;
            int originalEnd = -1;
            
            if (l_index < indexMapping.size()) {
                originalStart = indexMapping.get(l_index) - startIndex;
            }
            
            if (r_index > 0 && r_index - 1 < indexMapping.size()) {
                originalEnd = indexMapping.get(r_index - 1) - startIndex + 1;
            }
            
            // 如果找到有效位置，添加替换信息
            if (originalStart >= 0 && originalEnd > originalStart && originalEnd <= message.length()) {
                replacements.add(new ReplacementInfo(originalStart, originalEnd, replacement, bad_word));
            }
        }
        
        // 按起始位置从大到小排序，从后往前替换
        replacements.sort((a, b) -> Integer.compare(b.start, a.start));
        
        for (ReplacementInfo rep : replacements) {
            if (rep.start >= 0 && rep.start < retMessage.length() && rep.end <= retMessage.length()) {
                retMessage.replace(rep.start, rep.end, rep.replacement);
                
                // 记录日志
                if (log_to_console) {
                    HappyFilter.plugin.getLogger().info(LOG_INFO
                        .replace("{w}", rep.badWord)
                        .replace("{player}", player.getName()));
                }
            }
        }
        
        return retMessage.toString();
    }

    private String getReplacement(String badWord, int originalLength) {
        // 优先检查特殊屏蔽词
        if (SP_K.contains(badWord)) {
            String specialReplace = special_replaces.get(badWord);
            if (specialReplace != null && !specialReplace.isEmpty()) {
                return specialReplace;
            }
        }
        
        // 普通替换逻辑
        if (!replace_enabled || originalLength <= 0) {
            // 如果不启用替换，则用*号填充
            return "*".repeat(originalLength);
        }
        
        StringBuilder sb = new StringBuilder();
        while (sb.length() < originalLength) {
            String word = replaceWords.get(random.nextInt(replaceWords.size()));
            sb.append(word.substring(0, Math.min(originalLength - sb.length(), word.length())));
        }
        return sb.toString();
    }

    private static class ReplacementInfo {
        final int start;
        final int end;
        final String replacement;
        final String badWord;
        
        ReplacementInfo(int start, int end, String replacement, String badWord) {
            this.start = start;
            this.end = end;
            this.replacement = replacement;
            this.badWord = badWord;
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        messageHistory.remove(event.getPlayer());
    }
}
