package xyz.n501yhappy.happyfilter.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import xyz.n501yhappy.happyfilter.utils.Filter;
import xyz.n501yhappy.happyfilter.utils.Filtered;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
        if (!isEnable) return;

        Player player = event.getPlayer();
        if (player.hasPermission(permissions.get("bypass"))) return;

        String message = event.getMessage();
        String mergedMessage = mergeHistory(player, message);

        Filtered result = filter.filterText(mergedMessage, filterWords)
                .merge(filter.filterRegex(mergedMessage, regexPatterns));

        if (result.isFiltered()) {
            event.setMessage(replaceFilteredWords(message, mergedMessage, result));
            if (enableWarning) {
                player.sendMessage(PREFIX +  WARNING_MESSAGE);
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
        messageHistory.remove(player);
        messageHistory.put(player, history);//upd
        return merged.toString();
    }

    private void updateMessageHistory(Player player, String message) {
        List<PlayerMessage> history = messageHistory.computeIfAbsent(player, k -> new ArrayList<>());
        history.add(new PlayerMessage(message, System.currentTimeMillis()));

        if (history.size() > 20) history.remove(0);
        messageHistory.remove(player);
        messageHistory.put(player, history);
    }

    private String replaceFilteredWords(String message, String mergedMessage, Filtered result) {
        int startIndex = mergedMessage.length() - message.length();
        StringBuilder result_message = new StringBuilder(message);

        for (int i = result.getLIndexes().size() - 1; i >= 0; i--) {
            int l = result.getLIndexes().get(i);
            int r = result.getRIndexes().get(i);
            if (log_to_console){
                plugin.getLogger().info(LOG_INFO.replace("{l}", String.valueOf(l)).replace("{r}", String.valueOf(r)).replace("{w}", mergedMessage.substring(l, r + 1)));
            }
            if (l >= startIndex) {
                int localL = l - startIndex;
                int localR = r - startIndex;
                if (localL >= 0 && localR < message.length() && localL <= localR) {
                    result_message.replace(localL, localR + 1, getReplace(localR - localL + 1));
                }
            } else if (r >= startIndex) {
                int localL = 0;
                int localR = r - startIndex;
                if (localR >= 0 && localR < message.length()) {
                    result_message.replace(localL, localR + 1, getReplace(localR - localL + 1));
                }
            }
        }

        return result_message.toString();
    }


    private String getReplace(int length) {
        StringBuilder sb = new StringBuilder();
        while (sb.length() < length) {
            String word = replaceWords.get(random.nextInt(replaceWords.size()));
            sb.append(word.substring(0, Math.min(length - sb.length(), word.length())));
        }
        return sb.toString();
    }
}
