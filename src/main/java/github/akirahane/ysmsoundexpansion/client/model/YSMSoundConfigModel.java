package github.akirahane.ysmsoundexpansion.client.model;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public record YSMSoundConfigModel(YSMSound defaultSound, List<Target> targets, List<Pattern> replacePatterns) {
    // 非规范构造函数：接受 JSON，然后“委托”给主构造函数
    public YSMSoundConfigModel(JsonArray targets, JsonArray replacePatterns, YSMSound defaultSound) {
        this(
                defaultSound,
                targets != null && !targets.isJsonNull() ? targets.asList().stream()
                        .map(JsonElement::getAsJsonObject)
                        .filter(target -> target.has("replace_sound"))
                        .map(item -> new Target(item, defaultSound))
                        .collect(Collectors.toList())
                        : Collections.emptyList(),
                replacePatterns != null && !replacePatterns.isJsonNull() ? replacePatterns.asList().stream()
                        .map(JsonElement::getAsString)
                        .map(Pattern::compile)
                        .collect(Collectors.toList())
                        : Collections.emptyList()
        );
    }

    public boolean isReplace(String soundId) {
        return replacePatterns.stream().anyMatch(pattern -> pattern.matcher(soundId).matches());
    }

    public List<YSMSound> checkConditions(
            String blockId,
            List<String> blockTags,
            String mainHandItemId,
            String weather,
            Integer time,
            String dimensionId,
            Integer health,
            Integer air,
            Integer food,
            Integer xpLevel
    ) {
        List<YSMSound> result = targets.stream()
                .filter(target -> target.getSound() != null &&
                        target.checkConditions(
                                blockId, blockTags, mainHandItemId, weather, time, dimensionId, health, air, food, xpLevel
                        )
                )
                .map(Target::getSound)
                .collect(Collectors.toList());

        // 如果没有匹配项，则返回默认声音
        if (result.isEmpty() && defaultSound != null) {
            result.add(defaultSound);
        }

        return result;
    }
}
