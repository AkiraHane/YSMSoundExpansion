package github.akirahane.ysmsoundexpansion.utils;

import net.minecraft.world.level.block.SoundType;

import java.lang.reflect.Field;
import java.util.HashMap;

public class SoundTypeUtil {
    private static final HashMap<SoundType, String> cache = new HashMap<>();

    static {
        for (Field field : SoundType.class.getDeclaredFields()) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) &&
                    field.getType() == SoundType.class) {
                try {
                    SoundType value = (SoundType) field.get(null);
                    cache.put(value, field.getName());
                } catch (IllegalAccessException ignored) {
                }
            }
        }
    }

    public static String getName(SoundType soundType) {
        return cache.get(soundType);
    }
}
