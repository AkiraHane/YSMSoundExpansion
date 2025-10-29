📌 说明：本文档由开发者撰写框架并由AI辅助润色与内容扩展，依据《人工智能生成合成内容标识办法》特此标注。

---

# 🎵 YSMSoundExpansion - YSM 音效拓展模组

> 通过资源包系统为 YSM 角色模型提供高度自定义的声音替换与屏蔽功能。

![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.1-green)
![Platform](https://img.shields.io/badge/Platform-NeoForge-blue)
![License](https://img.shields.io/badge/License-MIT-orange)

**YSMSoundExpansion** 是一个 Minecraft 模组，旨在扩展 [YSM（Yes Steve Model）](https://link.mcmod.cn/target/aHR0cHM6Ly9tb2RyaW50aC5jb20vbW9kL3llcy1zdGV2ZS1tb2RlbA==) 的音效能力。由于 YSM 本身闭源且未提供公开 API，本模组通过监听声音触发行为并结合自定义资源包机制，实现基于玩家所选模型的动态音效替换与屏蔽。

你可以为每个角色模型配置独特的脚步声、交互音效甚至环境响应音效。

---

## 🔧 功能特性

- ✅ **按模型切换音效规则**：不同角色使用不同的音效配置。
- ✅ **精准音效替换**：支持正则匹配原版或模组音效 ID 并进行替换。
- ✅ **条件化播放**：可根据方块类型、天气、时间、维度、手持物品等条件决定播放哪个音效。
- ✅ **音效屏蔽机制**：可屏蔽特定音效（如原版脚步声），避免冲突。

---

## 📦 如何使用

### 第一步：准备资源包

由于 YSM 项目闭源，无法直接获取当前模型信息，因此推荐将音效文件打包成 **资源包（Resource Pack）** 加载。

本模组会读取符合特定格式的资源包来加载音效规则和音频文件。

#### 资源包目录结构示例

```
assets/
└─ ysmsoundexpansion/                  ← 命名空间（不可更改）
    │
    ├── sounds.json                     ← Minecraft 官方音效注册表
    │
    ├── lang/
    │   └── zh_cn.json                  ← 可选语言文件（用于字幕显示等）
    │
    └── sounds/
        └── kagamimiyahane_akirahane_v1/ ← 模型唯一 ID（全小写英文下划线）
            │
            ├── ban_sound_patterns.json     ← 屏蔽音效规则
            ├── config.json                 ← 主音效替换规则
            ├── config_exp.json             ← 替换规则模板与详细说明（参考用）
            │
            └── block/
                ├── door/step/*.ogg         ← 实际音效文件
                ├── grass/step/*.ogg
                └── wool/step/*.ogg
```

> 💡 **建议命名规范**：`角色名_作者名_版本号`（例如：`kagamimiyahane_akirahane_v1`），确保全局唯一性，避免与其他创作者冲突。

---

### 第二步：配置音效规则

#### 1. 屏蔽音效 (`ban_sound_patterns.json`)

此文件定义当使用该模型时需要屏蔽的音效列表，使用 **正则表达式** 匹配音效 ID。

```json
[
  "^minecraft:block..*.step$"
]
```

> 上述配置可屏蔽所有原版方块脚步声。

---

#### 2. 音效替换规则 (`config.json`)

这是一个数组，每项包含一组“匹配 → 替换”规则。

```json
[
  {
    "replace_patterns": [
      "ysmsoundexpansion:test.block.step",
      "^minecraft:block..*.step$"
    ],
    "target": [
      {
        "conditions": {
          "block_id": "^minecraft:.*stone$",
          "block_type": "STONE",
          "weathers": ["clear"],
          "times": [{ "min": 6000, "max": 20000 }],
          "dimensions": "minecraft:overworld",
          "entity": {
            "hunger": { "min": 6, "max": 20 }
          }
        },
        "replace_sound": {
          "sound": "ysmsoundexpansion:kagamimiyahane_akirahane_v1.block.stone.step",
          "volume": 0.9,
          "pitch": 1.05
        }
      },
      {
        "conditions": {
          "block_tag": ".*:wool"
        },
        "replace_sound": {
          "sound": "ysmsoundexpansion:kagamimiyahane_akirahane_v1.block.wool.step",
          "volume": 0.7,
          "pitch": 1.2
        }
      }
    ],
    "default_sound": {
      "sound": "ysmsoundexpansion:kagamimiyahane_akirahane_v1.block.default.step",
      "volume": 0.8,
      "pitch": 1.0
    }
  }
]
```

##### 字段说明：

| 字段                                              | 说明                                                    |
|-------------------------------------------------|-------------------------------------------------------|
| `replace_patterns`                              | 正则数组，匹配需被替换的原始音效 ID                                   |
| `conditions`                                    | 所有条件必须全部满足才生效                                         |
| &nbsp;&nbsp;`block_id`                          | 脚下方块的 ID（正则）                                          |
| &nbsp;&nbsp;`block_tag`                         | 脚下方块的标签（正则）                                           |
| &nbsp;&nbsp;`block_type`                        | 方块音效类型（如 `WOOD`, `GRASS`, `STONE` 等，见 `SoundType` 枚举） |
| &nbsp;&nbsp;`item_id`                           | 手持物品 ID（正则）                                           |
| &nbsp;&nbsp;`weathers`                          | 天气限制（`clear`, `rain`, `thunder`）                      |
| &nbsp;&nbsp;`can_see_sky`                       | 上方是否可见天空（`true`, `false`）                             |
| &nbsp;&nbsp;`in_water`                          | 是否需要在水里（`true`, `false`）                              |
| &nbsp;&nbsp;`times`                             | 时间范围（以游戏刻为单位，0~24000）                                 |
| &nbsp;&nbsp;`dimensions`                        | 维度名称正则（如 `minecraft:the_nether`）                      |
| &nbsp;&nbsp;`entity.health/hunger/air/xp_level` | 实体属性数值区间                                              |
| `replace_sound.sound`                           | 替代音效 ID（可跨模组）                                         |
| `replace_sound.volume`                          | 音量（0.0 ~ 1.0）                                         |
| `replace_sound.pitch`                           | 音调（1.0 为基准）                                           |
| `default_sound`                                 | 所有 `target` 条件都不满足时的兜底音效                              |

> ⚠️ 若 `replace_sound.sound` 或 `default_sound.sound`为空，且原音效以 `.step` 结尾，则自动播放对应材质的默认脚步声。

---

### 第三步：在模型中调用音效

在你的 YSM 动画事件中，直接使用你定义的音效 ID 播放声音（需要在sounds.json中注册）：

```json
"ysmsoundexpansion:kagamimiyahane_akirahane_v1.block.door.step"
```

✅ **效果说明**：
- 若仅安装资源包但未装模组：仍可正常播放模型指定音效（原版行为）。
- 若安装模组 + 对应资源包：完整的替换与屏蔽逻辑。

---

## ⚠️ 注意事项

- ❗ 当前 YSM **未开放模型切换事件或当前模型 ID 接口**，因此本模组采用“首次触发模型音效即视为使用该模型”的策略。
- 🔄 如果你关闭了模型或切换到无音效模型，请手动执行命令刷新状态：

```mcfunction
/ysmsound refresh
```

此命令会清除当前玩家的模型绑定状态，恢复默认音效。

---

## 🛠 开发工具建议

- 使用附带的res_bak.py脚本一键生成 `sounds.json` 中的音效路径（根据文件夹结构自动推导 ID）。
- 将开放示例默认模型，以展示音效配置文件的结构。

---

## 🌱 未来展望

希望 YSM 能逐步开放接口，一些会用到的优化点：

1. ✅ 提供 **当前玩家使用的模型 ID** 查询接口；
2. ✅ 发送 **模型切换事件**，便于实时更新状态；
3. ✅ 允许访问 **Molang 解析器**，获取模型变量；
4. ✅ 支持从外部触发 **动画事件** ！

> 若以上功能得以实现，YSMSoundExpansion 将能进一步做到「动作驱动音效」、「状态感知响应」等高级功能。

---

## 📜 许可证

MIT License

---

> Made with ❤️ for all YSM creators and players.

--- 

如需生成 `config_exp.json` 示例文件或自动化脚本模板，也可以告诉我，我可以继续为你编写。
