# HappyFilter - 违禁词插件！

## 🌟 主要功能

### 🛡️ 超智能违禁词过滤
- 不只是简单的关键词匹配，还能识别用特殊字符分隔的词汇（比如 c/n/m）
- 支持正则表达式过滤，网址、广告统统拦下
- 历史消息追踪功能，分次发送的违禁词也会被拦截～

### 🎭 灵活替换系统
- 可自定义替换词汇，想换什么就换什么！
- 在replace_words随机选择替换

### ⚡ 高性能(存疑) 算法加持
- AC自动机算法，用了这个算法打OI可以AK！
- 异步处理聊天事件

## 🛠️ 配置文件详解
```yaml
enabled: true #是否启用这个插件
filter_words: #违禁词列表
  - "cnm"
  - "sb"
filter_rules:
  regex: #正则表达式匹配
    - "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}(?:\\.[a-zA-Z]{2,})?"
  interference_characters: #干扰字符
    - '/'
    - '\'
    - '.'
  replace:
    replace_words: #被替换的的词汇
      - "喵"

warning:
  enabled: true #是否启用警告功能
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
3. **性能优化**：只有在违禁词列表改变时才会重新构建匹配树！
4. **测试功能**：可以先用`disable`命令临时关闭，测试完再`enable`开启！

---

