package github.akirahane.ysmsoundexpansion.client.model;


import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;

import java.util.List;

public record Target(SoundEvent sound, Conditions conditions) {
    private static final Logger LOGGER = LogUtils.getLogger();

    public Target(JsonObject jsonObject) {
        this(
                SoundEvent.createVariableRangeEvent(ResourceLocation.parse(jsonObject.get("replace_sound_id").getAsString())),
                jsonObject.get("conditions") != null ? new Conditions(jsonObject.get("conditions").getAsJsonObject()) : null
        );
    }

    public SoundEvent getSound() {
        return sound;
    }

    public boolean checkConditions(
            String blockId,
            List<String> blockTags,
            String mainHandItemId,
            String weather,
            int time,
            String dimensionId,
            int health,
            int air,
            int food,
            int xpLevel
    ) {
        if (ObjectUtils.isEmpty(conditions)) {
            LOGGER.info("[YSMSOUND_TARGET] 无条件替换");
            return true;
        }
        if (!ObjectUtils.isEmpty(conditions.blockId()) && !conditions.blockId().matcher(blockId).matches()) {
            LOGGER.info("[YSMSOUND_TARGET] 不替换声音: blockId: {}", blockId);
            return false;
        }
        // 检查Tag
        if (!ObjectUtils.isEmpty(conditions.blockTag()) && blockTags.stream().noneMatch(tag -> conditions.blockTag().matcher(tag).matches())) {
            LOGGER.info("[YSMSOUND_TARGET] 不替换声音: blockTag: {}", blockTags);
            return false;
        }
        if (!ObjectUtils.isEmpty(conditions.itemId()) && (mainHandItemId == null || !conditions.itemId().matcher(mainHandItemId).matches())) {
            LOGGER.info("[YSMSOUND_TARGET] 不替换声音: itemId: {}", mainHandItemId);
            return false;
        }
        // 判断天气
        if (!ObjectUtils.isEmpty(conditions.weathers()) && !conditions.weathers().contains(weather)) {
            LOGGER.info("[YSMSOUND_TARGET] 不替换声音: weather: {}", weather);
            return false;
        }
        // 判断时间
        if (!ObjectUtils.isEmpty(conditions.times()) && conditions.times().stream().noneMatch(range -> range.contains(time))) {
            LOGGER.info("[YSMSOUND_TARGET] 不替换声音: time: {}", time);
            return false;
        }
        // 判断维度
        if (!ObjectUtils.isEmpty(conditions.dimensions()) && !conditions.dimensions().matcher(dimensionId).matches()) {
            LOGGER.info("[YSMSOUND_TARGET] 不替换声音: dimensionId: {}", dimensionId);
            return false;
        }
        if (conditions.entity() != null) {
            EntityConditions entityConditions = conditions.entity();
            // 判断生命值
            if (entityConditions.health() != null && !entityConditions.health().contains(health)) {
                LOGGER.info("[YSMSOUND_TARGET] 不替换声音: health: {}", health);
                return false;
            }
            // 判断空气
            if (entityConditions.air() != null && !entityConditions.air().contains(air)) {
                LOGGER.info("[YSMSOUND_TARGET] 不替换声音: air: {}", air);
                return false;
            }
            //判断饥饿值
            if (entityConditions.hunger() != null && !entityConditions.hunger().contains(food)) {
                LOGGER.info("[YSMSOUND_TARGET] 不替换声音: food: {}", food);
                return false;
            }
            // 获取经验等级
            if (entityConditions.xpLevel() != null && !entityConditions.xpLevel().contains(xpLevel)) {
                LOGGER.info("[YSMSOUND_TARGET] 不替换声音: xpLevel: {}", xpLevel);
                return false;
            }
        }
        return true;
    }
}