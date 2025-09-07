package xyz.n501yhappy.happyfilter.config;

import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import xyz.n501yhappy.happyfilter.HappyFilter;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static xyz.n501yhappy.happyfilter.HappyFilter.plugin;

public class PluginConfig {
    public static Configuration messagesConfig;
    //
    public static Configuration config;
    public static List<String> filterWords, regexPatterns;
    public static List<Character> interferenceChars;
    public static boolean enableWarning;
    public static String warningMessage;
    public static List<String> replaceWords;
    public static Map<String, String> permissions = new HashMap<>();
    public static boolean isEnable = true;
    public static boolean log_to_console = true;
    //message
    public static String PREFIX;
    public static String RELOAD_SUCCESS;
    public static String PLUGIN_ENABLED;
    public static String PLUGIN_DISABLED;
    public static String UNKNOWN_COMMAND;
    public static String HELP_HEADER;
    public static String HELP_RELOAD;
    public static String HELP_HELP;
    public static String HELP_ENABLE;
    public static String HELP_DISABLE;
    public static String WARNING_MESSAGE;
    public static String NO_PERMISSION;
    public static String LOG_INFO;

    public static void loadMessages() {
        HappyFilter.plugin.saveResource("messages.yml", false);
        messagesConfig = YamlConfiguration.loadConfiguration(new File(HappyFilter.plugin.getDataFolder(), "messages.yml"));

        loadMessagesFromConfig();
    }
    private static void loadMessagesFromConfig() {
        PREFIX = messagesConfig.getString("prefix", "§a[HappyFilter] ");
        RELOAD_SUCCESS = messagesConfig.getString("commands.reload_success", "§a配置已重载");
        PLUGIN_ENABLED = messagesConfig.getString("commands.plugin_enabled", "§a插件已启用");
        PLUGIN_DISABLED = messagesConfig.getString("commands.plugin_disabled", "§a插件已禁用");
        UNKNOWN_COMMAND = messagesConfig.getString("commands.unknown_command", "§c未知命令!");
        HELP_HEADER = messagesConfig.getString("commands.help.header", "§aHappyFilter 帮助");
        HELP_RELOAD = messagesConfig.getString("commands.help.reload", "§a/happyfilter reload - 重载配置");
        HELP_HELP = messagesConfig.getString("commands.help.help", "§a/happyfilter help - 显示帮助");
        HELP_ENABLE = messagesConfig.getString("commands.help.enable", "§a/happyfilter enable - 启用违禁词拦截");
        HELP_DISABLE = messagesConfig.getString("commands.help.disable", "§a/happyfilter disable - 禁用违禁词拦截");
        WARNING_MESSAGE = messagesConfig.getString("warning.message", "§c不要发布敏感信息!");
        NO_PERMISSION = messagesConfig.getString("commands.no_permission", "§c你没有权限执行此命令!");
        LOG_INFO = messagesConfig.getString("log", "Left index: {l} Right index: {r} ");
    }

    public static void loadConfig() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
        loadMessages();
        filterWords = config.getStringList("filter_words").stream()
                .map(StringEscapeUtils::unescapeJava)
                .collect(Collectors.toList());

        replaceWords = config.getStringList("filter_rules.replace.replace_words").stream()
                .map(StringEscapeUtils::unescapeJava)
                .collect(Collectors.toList());

        enableWarning = config.getBoolean("warning.enabled");
        interferenceChars = config.getCharacterList("filter_rules.interference_characters");

        permissions.put("bypass", "happyfilter.bypass");
        permissions.put("admin", "happyfilter.admin");

        isEnable = config.getBoolean("enabled");
        regexPatterns = config.getStringList("filter_rules.regex");
        log_to_console = config.getBoolean("log_to_console");
    }
}
