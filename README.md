# [HappyFilter - 违禁词插件！](https://github.com/N501YHappy/HappyFilter)

## 🌟 主要功能

### 🛡️ 人性化的违禁词过滤
- 不只是简单的关键词匹配，还能识别用特殊字符分隔的词汇（比如 c/n/m）
- 支持正则表达式过滤，网址、广告统统拦下
- 历史消息追踪功能，分次发送的违禁词也会被拦截～
- 可以自定义特殊替换
- 可以检测大小写
- 支持在控制台输出违禁词
- 支持所有自定义提示消息


## 🛠️ 配置文件详解
```yaml
enabled: true
log_to_console: true #记录罪行到控制台
filter_words:
  - "cnm"
  - "sb"
  - "byd"
  - "nm"
filter_rules:
  to_lower: true #大小写检测
  regex:
    enable: true
    regexes:
    - "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}(?:\\.[a-zA-Z]{2,})?" #这里要转义
  anti_interference:
    enabled: true
    interference_characters: #过滤的字符喵
      - '/'
      - '\'
      - '.'
      - ','
      - '|'
      - ' '
  replace:
    enable: true
    replace_words:
      - "喵"
  special_replace: #把制定的词语替换为另一个词语
    enable: true
    matches:  #注意特殊替换的两个词长度要一样
      "sb": "笨蛋"
      "fw": "杂鱼"

warning: #给玩家提醒
  enabled: true

#如果有bug请在github交issus(https://github.com/N501YHappy/HappyFilter/issues),或加入QQ群1031612019
#有bug不要憋着不说QAQ
```

```yaml
prefix: "§7[§dHappy§bFilter§7]"  #插件提示的前缀
commands:
  reload_success: "§a配置已重载"
  plugin_enabled: "§a插件已启用"
  plugin_disabled: "§a插件已禁用"
  unknown_command: "§c未知命令!"
  no_permission: "§c你没有权限执行此命令!"
  help:
    header: "§aHappyFilter 帮助"
    reload: "§a/happyfilter reload - 重载配置"
    help: "§a/happyfilter help - 显示帮助"
    enable: "§a/happyfilter enable - 启用违禁词拦截"
    disable: "§a/happyfilter disable - 禁用违禁词拦截"

log: "Word: {w} Player: {player}"
warning:
  message: "§c不要发布敏感信息!"


```
## 🎮 命令使用指南

- `/happyfilter reload` - 重新加载配置文件
- `/happyfilter help` - 显示帮助信息
- `/happyfilter enable` - 启用过滤功能
- `/happyfilter disable` - 临时禁用过滤功能

## 🔐 权限系统

- `happyfilter.bypass` - 绕过过滤器
- `happyfilter.admin` - 管理员权限

## 💡 使用小贴士

1. **特殊字符转义**：配置文件中的regex部分要写成`\\`！
2. **添加新词汇**：直接在`filter_words`下面添加新行就行！
3. **性能优化**：只有在违禁词列表改变时才会重新构建树！
4. **测试功能**：可以先用`disable`命令临时关闭，测试完再`enable`开启！

---


![bstats](https://bstats.org/signatures/bukkit/HappyFilter.svg)

## bug提交: 1031612019 或在github提issue,欢迎Pull Request 谢谢你喵