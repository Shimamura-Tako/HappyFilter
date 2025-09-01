package xyz.n501yhappy.happyfilter.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

import static xyz.n501yhappy.happyfilter.HappyFilter.plugin;
import static xyz.n501yhappy.happyfilter.config.PluginConfig.*;

public class CommandHandler implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(permissions.get("admin"))) return false;

        if (args.length < 1) {
            showHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                reloadConfig();
                sender.sendMessage("§a配置已重载");
                break;
            case "help":
                showHelp(sender);
                break;
            case "enable":
                isEnable = true;
                config.set("enabled", true);
                sender.sendMessage("§a插件已启用");
                break;
            case "disable":
                isEnable = false;
                config.set("enabled", false);
                sender.sendMessage("§a插件已禁用");
                break;
            default:
                sender.sendMessage("§c未知命令!");
        }
        return true;
    }

    private void reloadConfig() {
        plugin.reloadConfig();

        loadConfig();
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage("§aHappyFilter 帮助");
        sender.sendMessage("§a/happyfilter reload - 重载配置");
        sender.sendMessage("§a/happyfilter help - 显示帮助");
        sender.sendMessage("§a/happyfilter enable - 启用违禁词拦截");
        sender.sendMessage("§a/happyfilter disable - 禁用违禁词拦截");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            completions.add("reload");
            completions.add("help");
            completions.add("enable");
            completions.add("disable");
            return completions;
        }
        return new ArrayList<>();
    }
}
