package xyz.n501yhappy.happyfilter.config;

import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.configuration.Configuration;
import org.bukkit.permissions.Permission;
import xyz.n501yhappy.happyfilter.utils.NGramTextGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static xyz.n501yhappy.happyfilter.HappyFilter.plugin;

public class PluginConfigs {
    public static Configuration config;
    public static List<String> filter_words,n_gram_texts;
    public static List<Character> interference_characters;
    public static NGramTextGenerator nGramTextGenerator;
    public static int filter_level;
    public static boolean enable_ngram;
    public static int n;
    public static boolean enable_warning;
    public static String warning_message;
    public static List<String> replace_words;
    public static Map<String, String> permissions = new HashMap<>();
    public static List<String> regex;

    public static boolean enable_plugin = true;
    public static void getPluginConfig(){
        config = plugin.getConfig();
        filter_words = config.getStringList("filter_words").stream().
                map(StringEscapeUtils::unescapeJava).collect(Collectors.toList());
        n_gram_texts = config.getStringList("filter_rules.ngram.words").stream()
                .map(StringEscapeUtils::unescapeJava).collect(Collectors.toList());
        filter_level = config.getInt("filter_level");
        enable_ngram = config.getBoolean("filter_rules.ngram.enable_ngram");
        n = config.getInt("filter_rules.ngram.n");
        replace_words = config.getStringList("filter_rules.replace.replace_words").stream()
                .map(StringEscapeUtils::unescapeJava).collect(Collectors.toList());
        enable_warning = config.getBoolean("warning.enabled");
        warning_message = config.getString("warning.message");
        interference_characters = config.getCharacterList("filter_rules.interference_characters");

        permissions.clear(); // 清空map避免重复添加
        permissions.put("bypass", "happyfilter.bypass");
        permissions.put("admin", "happyfilter.admin");

        enable_plugin = config.getBoolean("enabled");
        regex = config.getStringList("filter_rules.regex").stream()
                .map(StringEscapeUtils::unescapeJava).collect(Collectors.toList());
    }
}
