package xyz.n501yhappy.happyfilter;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.n501yhappy.happyfilter.commands.command;
import xyz.n501yhappy.happyfilter.listeners.listenChat;
import xyz.n501yhappy.happyfilter.utils.NGramTextGenerator;

import static xyz.n501yhappy.happyfilter.config.PluginConfigs.*;

public final class HappyFilter extends JavaPlugin {//TODO 把regex do it
    public static HappyFilter plugin;
    @Override
    public void onEnable() {
        plugin = this;
        saveDefaultConfig();
        getPluginConfig();
        if (enable_ngram){
            nGramTextGenerator = new NGramTextGenerator();
            nGramTextGenerator.buildNGramModel(n_gram_texts.toArray(new String[n_gram_texts.size()]));
        }
        getServer().getPluginManager().registerEvents(new listenChat(), this);
        PluginCommand command = getCommand("happyfilter");
        command.setExecutor(new command());
        command.setTabCompleter(new command());
    }
    @Override
    public void onDisable() {
        return;
    }
}
