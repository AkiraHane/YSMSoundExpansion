package github.akirahane.ysmsoundexpansion.client.common;

import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class YSMSoundInstance extends EntityBoundSoundInstance {
    public YSMSoundInstance(SoundEvent p_235080_, SoundSource p_235081_, float p_235082_, float p_235083_, Entity p_235084_, long p_235085_) {
        super(p_235080_, p_235081_, p_235082_, p_235083_, p_235084_, p_235085_);
    }
}
