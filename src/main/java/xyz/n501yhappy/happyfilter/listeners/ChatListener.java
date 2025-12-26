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
        String solvedMessage = mergedMessage;  // slovedMessage是干净的消息
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
            event.setMessage(AsolveMessages(message, mergedMessage, result, player, indexMapping, solvedMessage));
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

    private String AsolveMessages(String message, String mergedMessage, Filtered result, Player player, List<Integer> indexMapping, String solvedMessage) {
        // 构建原始字符位置到替换字符的映射
        Map<Integer, Character> replaceMap = new HashMap<>();
        int startIndex = mergedMessage.length() - message.length();
        
        // Debug 信息
        // if (enableDebug) {
        //     player.sendMessage(ChatColor.LIGHT_PURPLE + "Debug - Original: " + message);
        //     player.sendMessage(ChatColor.LIGHT_PURPLE + "Debug - Merged: " + mergedMessage);
        //     player.sendMessage(ChatColor.LIGHT_PURPLE + "Debug - Solved: " + solvedMessage);
        //     player.sendMessage(ChatColor.LIGHT_PURPLE + "Debug - IndexMapping: " + indexMapping);
        //     player.sendMessage(ChatColor.LIGHT_PURPLE + "Debug - result: " + result.toString());
        // }
        
        // 收集所有需要替换的位置和对应的替换字符
        for (int i = 0; i < result.getRIndexes().size(); i++) {
            int l_index = result.getLIndexes().get(i);
            int r_index = result.getRIndexes().get(i);
            String bad_word = solvedMessage.substring(l_index, r_index);
            String replaces = getReplace(r_index - l_index, bad_word);
            
            // 记录日志
            if (log_to_console) {
                HappyFilter.plugin.getLogger().info(LOG_INFO
                    .replace("{w}", bad_word)
                    .replace("{player}", player.getName()));
            }
            
            // if (enableDebug) {
            //     player.sendMessage(ChatColor.YELLOW + "Debug - BadWord: " + bad_word + 
            //                      " at [" + l_index + "," + r_index + ") -> " + replaces);
            // }
            
            // 为每个字符位置映射替换字符
            for (int replaceIndex = 0; replaceIndex < replaces.length(); replaceIndex++) {
                int solvedPos = l_index + replaceIndex;
                if (solvedPos < r_index && solvedPos < indexMapping.size()) {
                    int originalPos = indexMapping.get(solvedPos) - startIndex;
                    if (originalPos >= 0 && originalPos < message.length()) {
                        replaceMap.put(originalPos, replaces.charAt(replaceIndex));
                    }
                }
            }
        }
        
        // 构建最终消息
        StringBuilder ret_message = new StringBuilder();
        for (int i = 0; i < message.length(); i++) {
            if (replaceMap.containsKey(i)) {
                ret_message.append(replaceMap.get(i));
            } else {
                ret_message.append(message.charAt(i));
            }
        }
        
        // if (enableDebug) {
        //     player.sendMessage(ChatColor.LIGHT_PURPLE + "Debug - Result: " + ret_message.toString());
        // }
        
        return ret_message.toString();
    }

    private String getReplace(int length, String bad_word) {
        // 优先检查特殊屏蔽词
        if (SP_K.contains(bad_word)) {
            return special_replaces.get(bad_word); // 直接返回特殊替换词，不检查长度
        }
        
        if (!replace_enabled || length <= 0) return "";
        
        // 普通替换逻辑
        StringBuilder sb = new StringBuilder();
        while (sb.length() < length) {
            String word = replaceWords.get(random.nextInt(replaceWords.size()));
            sb.append(word.substring(0, Math.min(length - sb.length(), word.length())));
        }
        return sb.toString();
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        messageHistory.remove(event.getPlayer());
    }
}
