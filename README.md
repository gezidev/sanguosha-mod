# SanguoshaForge — 三国杀卡牌（Forge 1.20.1）

Forge 1.20.1 模组 `SanguoshaForge` 的源码项目（mod id `sanguosha`，包名 `cn.solo.sanguosha`）。
在 Minecraft 中游玩完整的《三国杀》卡牌游戏：标准包 + 军争篇共 160 张牌、身份牌、武将牌、血量牌，
以及牌堆系统、游戏桌、手牌容器和多种桌游棋盘。

## 功能特性

- **完整卡牌玩法**：基本牌 / 锦囊牌 / 装备牌（标准包 + 军争篇），身份系统（主公/忠臣/反贼/内奸），武将牌与血量牌
- **牌堆系统**：身份牌堆、非身份牌堆、武将牌堆；支持按花色、从底部抽牌等抽牌配置
- **牌堆收集器**：收取范围内的地面散牌，并按类型（身份/非身份/武将/散牌）重新生成牌堆
- **牌堆收回**：空手潜行 + 右键，将牌堆收回为物品，剩余卡牌完整保留
- **游戏桌与手牌容器**：卡牌槽位管理、手牌存放与出牌
- **自定义武将**：支持上传自定义武将图片与技能
- **桌游棋盘**：中国象棋、五子棋、井字棋，附重置/悔棋/导入/复制棋局码

## 项目结构

```
src/main/java/      模组源码（cn.solo.sanguosha.*）
src/main/resources/ 资源（assets/ data/ config/ META-INF/mods.toml）
tools/ApplyAt.java  对客户端 jar 应用 Forge access transformer 的小工具
libs/               编译期依赖（Jade API 等）
build.py            构建脚本（AT → 编译 → 打包）
build.bat           Windows 构建入口
```

## 构建

1. 安装 **JDK 17** 与 **Python 3**（`java`/`javac`/`python` 需在 PATH 中）。
2. 设置两个环境变量指向你本地的 Forge 1.20.1 实例：
   - `SANGUOSHA_GAME_DIR`：实例文件夹路径（包含版本 JSON）
   - `SANGUOSHA_MC_LIBS`：Minecraft 的 `libraries` 文件夹路径
3. 运行 `python build.py`（Windows 下也可用 `build.bat`）。

构建产物为 `build/libs/SanguoshaForge-rebuilt.jar`。

> 说明：本项目源码以 SRG 命名（`m_`/`f_`）引用 Minecraft，构建时直接编译到运行时 jar
> （`client-...-srg.jar` + Forge API jar）。`build.py` 会自动用 `tools/ApplyAt.java`
> 对客户端 jar 应用 Forge 的 access transformer，效果等同于 Forge 开发环境。

## 安装

将构建出的 jar 放入 Forge 1.20.1 实例的 `mods/` 文件夹即可。
Jade（`snownee.jade`）为可选前置：未安装时兼容插件自动跳过，不影响使用。

## 操作速查

| 操作 | 方法 |
|---|---|
| 放置卡牌 | 对准方块顶面右键 |
| 叠放卡牌 | 手持卡牌右键地面牌堆 |
| 抽牌 | 空手右键牌堆取顶牌 |
| 洗牌 / 翻牌 | Shift+左键 洗牌；Shift+右键 翻顶牌 |
| 收回牌堆 | 空手潜行 + 右键 身份/非身份/武将牌堆 |
| 收集器收取 | 右键空气，收取 20 格内地面卡牌 |
| 收集器生成 | 潜行 + 右键，按类型生成牌堆 |
| 棋盘落子 | 右键棋盘 |
| 棋盘管理 | 潜行 + 右键棋盘（重置/悔棋/导入/复制） |
