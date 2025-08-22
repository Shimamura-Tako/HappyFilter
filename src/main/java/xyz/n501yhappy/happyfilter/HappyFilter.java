package xyz.n501yhappy.happyfilter;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.n501yhappy.happyfilter.commands.command;
import xyz.n501yhappy.happyfilter.listeners.listenChat;
import xyz.n501yhappy.happyfilter.utils.NGramTextGenerator;

import static xyz.n501yhappy.happyfilter.config.PluginConfigs.*;

public final class HappyFilter extends JavaPlugin {
    // 使用volatile保证线程安全
    public static volatile HappyFilter plugin;
    private volatile boolean nGramInitialized = false;

    @Override
    public void onEnable() {
        plugin = this;
        saveDefaultConfig();
        getPluginConfig();

        initializeNGramIfNeeded();

        getServer().getPluginManager().registerEvents(new listenChat(), this);
        PluginCommand command = getCommand("happyfilter");
        if (command != null) { // 添加null检查
            command.setExecutor(new command());
            command.setTabCompleter(new command());
        }
    }

    @Override
    public void onDisable() {
        // 清理资源
    }
    private void initializeNGramIfNeeded() {
        if (enable_ngram && !nGramInitialized) {
            synchronized (this) {
                if (enable_ngram && !nGramInitialized) {
                    try {
                        nGramTextGenerator = new NGramTextGenerator();
                        if (n_gram_texts != null && !n_gram_texts.isEmpty()) {
                            nGramTextGenerator.buildNGramModel(
                                n_gram_texts.toArray(new String[0])
                            );
                            nGramInitialized = true;
                        }
                    } catch (Exception e) {
                        getLogger().warning("Failed to initialize N-Gram model: " + e.getMessage());
                        nGramInitialized = false;
                    }
                }
            }
        }
    }
    public NGramTextGenerator getNGramTextGenerator() {
        initializeNGramIfNeeded();
        return nGramTextGenerator;
    }
    public boolean isNGramInitialized() {
        return nGramInitialized;
    }
}
