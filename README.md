# YSMSoundExpansion - YSM 音效拓展模组

> 通过资源包系统为 YSM 角色模型提供高度自定义的声音替换与屏蔽功能。
>
> 此模组是仅客户端模组

![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.1-green)
![Platform](https://img.shields.io/badge/Platform-NeoForge-blue)
![License](https://img.shields.io/badge/License-MIT-orange)


## ⚠️ 本模组处于实验，可能会有失效的问题

-  我不知道该怎么获取当前YSM模型的ID或者捕获切换YSM模型的事件，因此本模组采用“首次触发模型音效即视为使用该模型”的策略。
-  如果你关闭了模型或切换到无音效模型，上一个模型的音效配置会残留，请手动执行命令刷新状态：

```mcfunction
/ysmsound refresh
```

此命令会清除当前玩家的模型配置绑定状态，恢复默认音效（无配置策略）。

---
## 这是什么?

根据所选YSM模型以及配置的条件，进行音效替换或屏蔽的模组

---

## 为什么需要这个

~~我做的模型脚步声和原版脚步声重叠了听着难受~~

目前YSM模型虽然支持资源包，但是无法根据模型替换乃至屏蔽原版的声音，这导致一些想法没法做到。
包括不仅限于：脚步声、吃东西、被攻击……等声音的自定义需求。

这个模组可以通过监听声音事件，匹配声音id，并通过条件配置屏蔽或转换为其他的声音（仅限玩家和女仆发出的声音）。

---

## 如何使用？

### 第一步：准备资源包

基于原版的资源包，首先需要了解这个：[Minecraft Wiki](https://zh.minecraft.wiki/w/Tutorial:%E5%88%B6%E4%BD%9C%E8%B5%84%E6%BA%90%E5%8C%85/%E9%9F%B3%E6%95%88%E5%92%8C%E9%9F%B3%E4%B9%90)

本模组会读取符合特定格式的资源包来加载音效规则和音频文件。

#### 资源包目录结构示例

```
assets/
└─ ysmsoundexpansion/                  ← 命名空间（不可更改）
    │
    ├── sounds.json                     ← Minecraft 音效注册表
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

> 💡 **建议命名规范**：`角色名_作者名_也许需要的后缀`（例如：`mingya_akirahane_v1`），确保全局唯一性，避免与其他模型冲突。

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
          "sound": "ysmsoundexpansion:mingya_akirahane_v1.block.stone.step",
          "volume": 0.9,
          "pitch": 1.05
        }
      },
      {
        "conditions": {
          "block_tag": ".*:wool"
        },
        "replace_sound": {
          "sound": "ysmsoundexpansion:mingya_akirahane_v1.block.wool.step",
          "volume": 0.7,
          "pitch": 1.2
        }
      }
    ],
    "default_sound": {
      "sound": "ysmsoundexpansion:mingya_akirahane_v1.block.default.step",
      "volume": 0.8,
      "pitch": 1.0
    }
  }
]
```

##### 字段说明：

| 字段                                       | 说明                                                    |
|------------------------------------------|-------------------------------------------------------|
| `replace_patterns`                       | 正则数组，匹配需被替换的原始音效 ID，支持其他模组的音效                         |
| `conditions`                             | 所有条件必须全部满足才生效                                         |
| `block_id`                               | 脚下方块的 ID（正则）                                          |
| `block_tag`                              | 脚下方块的标签（正则）                                           |
| `block_type`                             | 方块音效类型（如 `WOOD`, `GRASS`, `STONE` 等，见 `SoundType` 枚举） |
| `item_id`                                | 手持物品 ID（正则）                                           |
| `weathers`                               | 天气限制（`clear`, `rain`, `thunder`）                      |
| `can_see_sky`                            | 上方是否可见天空（`true`, `false`）                             |
| `in_water`                               | 是否需要在水里（`true`, `false`）                              |
| `times`                                  | 时间范围（以游戏刻为单位，0~24000）                                 |
| `dimensions`                             | 维度名称正则（如 `minecraft:the_nether`）                      |
| `entity.health/hunger/air/xp_level`      | 实体属性数值区间                                              |
| `replace_sound.sound`                    | 替代音效 ID（可跨模组）                                         |
| `replace_sound.volume`                   | 音量（0.0 ~ 1.0）                                         |
| `replace_sound.pitch`                    | 音调（1.0 为基准）                                           |
| `default_sound`                          | 所有 `target` 条件都不满足时的兜底音效                              |

> ⚠️ 若 `replace_sound.sound` 或 `default_sound.sound`为空，且原音效以 `.step` 结尾，则自动播放对应材质的默认脚步声。

---

### 第三步：使用音效

在你的 YSM 动画事件中，直接使用你定义的音效 ID 播放声音（需要在sounds.json中注册）：

```json
"ysmsoundexpansion:mingya_akirahane_v1.block.door.step"
```

注意：我不知道如何监听和处理模型切换事件，所以必须发出过一个模型的音效后，模组才能知道用了哪个模型

---

## 🛠 开发工具建议

- 使用附带的res_bak.py脚本一键生成 `sounds.json` 中的音效路径（根据文件夹结构自动推导 ID）。
- 将开放示例默认模型，以展示音效配置文件的结构。

---

## 未来

暂无更新计划

~~这东西说不定官方马上就支持了呢x~~


---

## 📜 许可证

MIT License

---