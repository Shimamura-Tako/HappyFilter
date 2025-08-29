package xyz.n501yhappy.happyfilter.config;

import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.configuration.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static xyz.n501yhappy.happyfilter.HappyFilter.plugin;

public class PluginConfig {
    public static Configuration config;
    public static List<String> filterWords, regexPatterns;
    public static List<Character> interferenceChars;
    public static boolean enableWarning;
    public static String warningMessage;
    public static List<String> replaceWords;
    public static Map<String, String> permissions = new HashMap<>();
    public static boolean isEnable = true;

    public static void loadConfig() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
        filterWords = config.getStringList("filter_words").stream()
                .map(StringEscapeUtils::unescapeJava)
                .collect(Collectors.toList());

        replaceWords = config.getStringList("filter_rules.replace.replace_words").stream()
                .map(StringEscapeUtils::unescapeJava)
                .collect(Collectors.toList());

        enableWarning = config.getBoolean("warning.enabled");
        warningMessage = config.getString("warning.message");
        interferenceChars = config.getCharacterList("filter_rules.interference_characters");

        permissions.put("bypass", "happyfilter.bypass");
        permissions.put("admin", "happyfilter.admin");

        isEnable = config.getBoolean("enabled");
        regexPatterns = config.getStringList("filter_rules.regex").stream()
                .map(StringEscapeUtils::unescapeJava)
                .collect(Collectors.toList());
    }
}
