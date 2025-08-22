package xyz.n501yhappy.happyfilter.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import xyz.n501yhappy.happyfilter.config.PluginConfigs;

import java.util.ArrayList;
import java.util.List;

import static xyz.n501yhappy.happyfilter.HappyFilter.plugin;
import static xyz.n501yhappy.happyfilter.config.PluginConfigs.*;

public class command implements CommandExecutor, TabExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!commandSender.hasPermission(permissions.get("admin"))) return false;
        if (strings.length < 1) handlerHelp(commandSender);
        if (strings[0].equalsIgnoreCase("reload")) {
            reloadConfig();
            commandSender.sendMessage("§a重载成功");
        } else if (strings[0].equalsIgnoreCase("help")) {
            handlerHelp(commandSender);
        } else if (strings[0].equalsIgnoreCase("enable")) {
            enable_plugin = true;
            config.set("enabled",enable_plugin);
            commandSender.sendMessage("§a插件已启用");
        } else if (strings[0].equalsIgnoreCase("disable")) {
            enable_plugin = false;
            config.set("enabled",enable_plugin);
            commandSender.sendMessage("§a插件已禁用");
        }else {
            commandSender.sendMessage("§c未知命令! 输入/happyfilter help寻求帮助！");
        }
        return true;
    }
    private void reloadConfig() {
        config = plugin.getConfig();
        plugin.saveConfig();
        PluginConfigs.getPluginConfig();
    }
    private void handlerHelp(CommandSender commandSender){
        commandSender.sendMessage("§aHappyFilter Help");
        commandSender.sendMessage("§a/happyfilter reload - 重载配置");
        commandSender.sendMessage("§a/happyfilter help - 显示帮助");
        commandSender.sendMessage("§a/happyfilter enable - 启用违禁词拦截");
        commandSender.sendMessage("§a/happyfilter disable - 禁用违禁词拦截");
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        List<String> result = new ArrayList<>();
        if (strings.length == 1){
            result.add("reload");
            result.add("help");
            result.add("enable");
            result.add("disable");
        }
        return result;
    }
}
