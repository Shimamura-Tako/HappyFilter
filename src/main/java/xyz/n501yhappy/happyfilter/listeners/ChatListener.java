package xyz.n501yhappy.happyfilter.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import xyz.n501yhappy.happyfilter.utils.Filter;
import xyz.n501yhappy.happyfilter.utils.Filtered;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import static xyz.n501yhappy.happyfilter.HappyFilter.plugin;
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
        if (anti_interference_enabled) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mergedMessage.length(); i++) {
                char c = mergedMessage.charAt(i);
                if (!interferenceChars.contains(c)) {
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
            event.setMessage(AsolveMessages(message, mergedMessage, result, player));
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

    private String AsolveMessages(String message, String mergedMessage, Filtered result,Player player) {
        int startIndex = mergedMessage.length() - message.length();//本条消息在消息历史中的index
        StringBuilder result_message = new StringBuilder(message);//返回结果

        for (int i = result.getLIndexes().size() - 1; i >= 0; i--) {
            int l = result.getLIndexes().get(i);
            int r = result.getRIndexes().get(i);
            
            if (log_to_console && isValidIndex(l, r, mergedMessage.length())) {
                String filteredWord = mergedMessage.substring(l, Math.min(r + 1, mergedMessage.length()));
                plugin.getLogger().info(LOG_INFO
                .replace("{l}", String.valueOf(l))
                    .replace("{r}", String.valueOf(r))
                    .replace("{w}", filteredWord)
                    .replace("{player}",player.getName()));
            }
            
            //替换
            if (l >= startIndex) {
                int localL = l - startIndex;
                int localR = r - startIndex;
                if (isValidIndex(localL, localR, message.length()) && localL <= localR) {
                    result_message.replace(localL, localR + 1, getReplace(localR - localL + 1));
                }
            } else if (r >= startIndex) {
                int localL = 0;
                int localR = r - startIndex;
                if (isValidIndex(localL, localR, message.length()) && localR >= 0) {
                    result_message.replace(localL, localR + 1, getReplace(localR - localL + 1));
                }
            }
        }

        return result_message.toString();
    }
    private boolean isValidIndex(int start, int end, int maxLength) {
        return start >= 0 && end >= start && start < maxLength && end < maxLength;
    }

    private String getReplace(int length) {
        if (!replace_enabled || length <= 0) return "";
        StringBuilder sb = new StringBuilder();
        while (sb.length() < length) {
            String word = replaceWords.get(random.nextInt(replaceWords.size()));
            sb.append(word.substring(0, Math.min(length - sb.length(), word.length())));
        }
        return sb.toString();
    }
}
