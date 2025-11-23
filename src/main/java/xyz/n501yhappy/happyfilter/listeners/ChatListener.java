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
        List<Integer> indexMapping = new ArrayList<>();indexMapping.clear();
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
            event.setMessage(AsolveMessages(message, mergedMessage, result, player,indexMapping,solvedMessage));
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
        StringBuilder ret_message = new StringBuilder(message);
        int startIndex = mergedMessage.length() - message.length();
        
        player.sendMessage(ChatColor.LIGHT_PURPLE + "Debug - Original: " + message);
        player.sendMessage(ChatColor.LIGHT_PURPLE + "Debug - Merged: " + mergedMessage);
        player.sendMessage(ChatColor.LIGHT_PURPLE + "Debug - Solved: " + solvedMessage);
        player.sendMessage(ChatColor.LIGHT_PURPLE + "Debug - IndexMapping: " + indexMapping);
        player.sendMessage(ChatColor.LIGHT_PURPLE + "Debug - result: " + result.toString());
        for (int i = 0; i < result.getRIndexes().size(); i++) {
            int l_index = result.getLIndexes().get(i);//2
            int r_index = result.getRIndexes().get(i);//5
            int len = r_index - l_index;//3
            String bad_word = solvedMessage.substring(l_index, r_index);//cnm
            String replaces = getReplace(len, bad_word);//喵喵喵
            // player.sendMessage(ChatColor.GREEN + "Debug - Replace: " +replaces);
            if (log_to_console) {
                HappyFilter.plugin.getLogger().info(LOG_INFO
                    .replace("{w}", bad_word)
                    .replace("{player}",player.getName()));
            }
            //player.sendMessage(ChatColor.YELLOW + "Debug - BadWord: " + bad_word + " at [" + l_index + "," + r_index + ") -> " + replaces);
            for (int solvedPos = l_index; solvedPos < r_index; solvedPos++) {//2-4
                int relativePos = solvedPos - l_index;
                if (relativePos >= replaces.length()) break;
                if (solvedPos < indexMapping.size()) {
                    int originalPos = Math.max(0,indexMapping.get(solvedPos)-startIndex);
                    player.sendMessage(ChatColor.GRAY + "OP: " + originalPos);
                    if (originalPos < ret_message.length()) {
                        ret_message.setCharAt(originalPos, replaces.charAt(relativePos));
                        player.sendMessage(ChatColor.GREEN + "Debug - Replace: solvedPos=" + solvedPos + 
                                        " -> originalPos=" + originalPos + 
                                        " with '" + replaces.charAt(relativePos) + "'");
                    }
                }
            }
        }
        
        //player.sendMessage(ChatColor.LIGHT_PURPLE + "Debug - Result: " + ret_message.toString());
        return ret_message.toString();
    }

    private String getReplace(int length,String bad_word) {
        if (SP_K.contains(bad_word)) {
            if (special_replaces.get(bad_word).length() == bad_word.length()) {
                return special_replaces.get(bad_word);
            }
        }
        if (!replace_enabled || length <= 0) return "";
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