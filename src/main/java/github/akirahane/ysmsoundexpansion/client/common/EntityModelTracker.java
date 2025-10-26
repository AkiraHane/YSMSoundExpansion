package github.akirahane.ysmsoundexpansion.client.common;

import java.util.HashMap;
import java.util.UUID;

public class EntityModelTracker {
    private static final HashMap<UUID, String> MODEL_MAP = new HashMap<>();

    public static void setModel(UUID uuid, String modelId) {
        MODEL_MAP.put(uuid, modelId);
    }

    public static void clearModel(UUID uuid) {
        MODEL_MAP.remove(uuid);
    }

    public static void clearModel() {
        MODEL_MAP.clear();
    }

    public static String getModel(UUID uuid) {
        return MODEL_MAP.getOrDefault(uuid, null);
    }

    public static boolean hasModel(UUID uuid) {
        return MODEL_MAP.containsKey(uuid);
    }

    /**
     * 调试用：返回当前已记录的实体数量
     */
    public static int trackedCount() {
        return MODEL_MAP.size();
    }
}
