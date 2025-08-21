package xyz.n501yhappy.happyfilter.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import xyz.n501yhappy.happyfilter.utils.Filter;
import xyz.n501yhappy.happyfilter.utils.Filtered;

import java.util.Random;

import static xyz.n501yhappy.happyfilter.config.PluginConfigs.*;

public class listenChat implements Listener {
    public Filter filter = new Filter();
    public Random random = new Random();
    @EventHandler
    public void onChat(org.bukkit.event.player.AsyncPlayerChatEvent event) {
        if (!enable_plugin) return;
        String message = event.getMessage();
        Player player = event.getPlayer();
        if (player.hasPermission(permissions.get("bypass"))) return;
        Filtered result = filter.filter_1(message,filter_words);
        Filtered result2 = filter.filter_regex(message,regex);
        result = result.merge(result2);
        if (result.isFiltered()) {
            int l,r;
            String replace_word;
            for (int i = 0; i < result.getLIndexes().size(); i++) {
                l = result.getLIndexes().get(i);
                r = result.getRIndexes().get(i);
                replace_word = get_replace_words(l,r);
                message = message.replace(message.substring(l,r+1),replace_word);
            }
        }else {
            return;
        }
        event.setMessage(message);
        if (enable_warning) {
            event.getPlayer().sendMessage(warning_message);
        }
    }
    private String get_replace_words(int l,int r) {
        int len = r - l + 1;
        StringBuilder sb = new StringBuilder();
        String replace_text;
        sb.setLength(0);//清空
        if (enable_ngram) {
            sb.append(nGramTextGenerator.generateText(len));
        }else {
            while (sb.length() < len){
                replace_text = replace_words.get(random.nextInt(replace_words.size()));
                sb.append(replace_text.substring(0,Math.min(len - sb.length(),replace_text.length())));//取一个随机的替换上去
            }
        }
        return sb.toString();
    }
}
