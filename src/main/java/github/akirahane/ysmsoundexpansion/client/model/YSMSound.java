package github.akirahane.ysmsoundexpansion.client.model;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public record YSMSound(SoundEvent sound, float volume, float pitch) {
    public YSMSound(JsonObject jsonObject, float defaultVolume, float defaultPitch) {
        this(
                SoundEvent.createVariableRangeEvent(ResourceLocation.parse(jsonObject.get("sound").getAsString())),
                jsonObject.get("volume") != null ? jsonObject.get("volume").getAsFloat() : 1.0f,
                jsonObject.get("pitch") != null ? jsonObject.get("pitch").getAsFloat() : 1.0f
        );
    }

}
