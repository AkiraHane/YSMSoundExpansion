package github.akirahane.ysmsoundexpansion.client.model;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public record YSMSound(SoundEvent sound, float volume, float pitch) {
    public YSMSound(JsonObject jsonObject, float defaultVolume, float defaultPitch) {
        this(
                jsonObject != null && jsonObject.get("sound") != null && !jsonObject.get("sound").isJsonNull()
                        ? SoundEvent.createVariableRangeEvent(ResourceLocation.parse(jsonObject.get("sound").getAsString()))
                        : null,
                jsonObject != null && jsonObject.get("volume") != null && !jsonObject.get("volume").isJsonNull()
                        ? jsonObject.get("volume").getAsFloat() : defaultVolume,
                jsonObject != null && jsonObject.get("pitch") != null && !jsonObject.get("pitch").isJsonNull()
                        ? jsonObject.get("pitch").getAsFloat() : defaultPitch
        );
    }

}
