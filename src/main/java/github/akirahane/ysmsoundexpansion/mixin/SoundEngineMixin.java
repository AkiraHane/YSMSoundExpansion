package github.akirahane.ysmsoundexpansion.mixin;

import com.mojang.logging.LogUtils;
import github.akirahane.ysmsoundexpansion.client.event.PlaySoundHandler;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.HashMap;

@Mixin(SoundEngine.class)
public class SoundEngineMixin {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final HashMap<Class<?>, Field> ENTITY_FIELD_CACHE = new HashMap<>();

    @Nullable
    private static Entity tryGetEntityFast(Object sound) {
        try {
            Class<?> clazz = sound.getClass();
            Field entityField = ENTITY_FIELD_CACHE.get(clazz);

            if (entityField == null) {
                // 首次扫描：寻找 Entity 字段
                for (Field f : clazz.getDeclaredFields()) {
                    if (Entity.class.isAssignableFrom(f.getType())) {
                        f.setAccessible(true);
                        ENTITY_FIELD_CACHE.put(clazz, f);
                        entityField = f;
                        break;
                    }
                }
                if (entityField == null) {
                    ENTITY_FIELD_CACHE.put(clazz, null); // 记下无结果
                    return null;
                }
            }

            return (Entity) entityField.get(sound);

        } catch (Exception ignored) {
        }
        return null;
    }


    @Inject(method = "play", at = @At("HEAD"), cancellable = true)
    private void onPlay(SoundInstance p_120313_, CallbackInfo ci) {
        Entity e = tryGetEntityFast(p_120313_);
        if (e instanceof Player || (e != null && e.getType().toString().contains("maid"))) {
            printSoundInfo(p_120313_);
            SoundEvent soundEvent = PlaySoundHandler.onEntityPlaySound(
                    SoundEvent.createVariableRangeEvent(ResourceLocation.parse(p_120313_.getLocation().toString())),
                    e, p_120313_.getSource(), 1.0f, 1.0f
            );
            if (soundEvent == null) {
                LOGGER.debug("取消播放 {}", p_120313_.getLocation());
                ci.cancel();
                return;
            }
        }
    }

    private static void printSoundInfo(SoundInstance sound) {
        LOGGER.debug("----- {} -----", sound.getClass().getName());
        LOGGER.debug("ID: {}", sound.getLocation());
        LOGGER.debug("Pos: ({}, {}, {})%n", sound.getX(), sound.getY(), sound.getZ());
        LOGGER.debug("Source: {}", sound.getSource());

        if (sound instanceof TickableSoundInstance tsi) {
            LOGGER.debug("Tickable: stopped={}", tsi.isStopped());
        }

        // 反射打印自定义信息
        for (Field f : sound.getClass().getDeclaredFields()) {
            f.setAccessible(true);
            try {
                Object val = f.get(sound);
                if (val == null) continue;
                if (val instanceof Entity e)
                    LOGGER.debug("⚙️ 可能的实体: {} ({})", e.getName().getString(), e.getType().toShortString());
                else if (val instanceof BlockPos pos)
                    LOGGER.debug("\uD83E\uDDF1 可能的方块位置: {}", pos);
                else if (val instanceof Level l)
                    LOGGER.debug("\uD83C\uDF0D 所在维度: {}", l.dimension().location());
                else if (val instanceof String s)
                    LOGGER.debug("\uD83D\uDCAC 字符串字段: {}={}", f.getName(), s);
            } catch (Exception ignored) {
            }
        }
    }

}
