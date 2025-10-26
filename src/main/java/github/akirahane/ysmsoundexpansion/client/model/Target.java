package github.akirahane.ysmsoundexpansion.client.model;


import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.List;

public record Target(SoundEvent sound, Conditions conditions) {

    public Target(JsonObject jsonObject) {
        this(
                BuiltInRegistries.SOUND_EVENT.get(
                        ResourceLocation.parse(jsonObject.get("sound").getAsString())
                ),
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
        if (conditions == null) {
            return true;
        }
        if (conditions.blockId() != null && !conditions.blockId().matcher(blockId).matches()) {
            return false;
        }
        // 检查Tag
        if (conditions.blockTag() != null && blockTags.stream().noneMatch(tag -> conditions.blockTag().matcher(tag).matches())) {
            return false;
        }
        if (conditions.itemId() != null && (mainHandItemId == null || !conditions.itemId().matcher(mainHandItemId).matches())) {
            return false;
        }
        // 判断天气
        if (!conditions.weathers().isEmpty() && !conditions.weathers().contains(weather)) {
            return false;
        }
        // 判断时间
        if (!conditions.times().isEmpty() && conditions.times().stream().noneMatch(range -> range.contains(time))) {
            return false;
        }
        // 判断维度
        if (!conditions.dimensions().matcher(dimensionId).matches()) {
            return false;
        }
        if (conditions.entity() != null) {
            EntityConditions entityConditions = conditions.entity();
            // 判断生命值
            if (entityConditions.health() != null && !entityConditions.health().contains(health)) {
                return false;
            }
            // 判断空气
            if (entityConditions.air() != null && !entityConditions.air().contains(air)) {
                return false;
            }
            //判断饥饿值
            if (entityConditions.hunger() != null && !entityConditions.hunger().contains(food)) {
                return false;
            }
            // 获取经验等级
            if (entityConditions.xpLevel() != null && !entityConditions.xpLevel().contains(xpLevel)) {
                return false;
            }
        }
        return true;
    }
}