package github.akirahane.ysmsoundexpansion.client.event;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import github.akirahane.ysmsoundexpansion.YSMSoundExpansion;
import github.akirahane.ysmsoundexpansion.client.model.YSMSoundConfigModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import org.slf4j.Logger;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Pattern;

@EventBusSubscriber(modid = YSMSoundExpansion.MODID, value = Dist.CLIENT)
public class YSMSoundResourceLoader {
    private static final Gson GSON = new Gson();
    private static final String ROOT_PATH = "sounds"; // 要扫描的文件夹
    private static final Logger LOGGER = LogUtils.getLogger();


    @SubscribeEvent
    public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) YSMSoundResourceLoader::loadAllSoundConfigs);
    }

    private static void loadAllSoundConfigs(ResourceManager resourceManager) {
        System.out.println("[Mod] ===== Reloading sound configurations =====");
        PlaySoundHandler.clearBlockPatterns();
        PlaySoundHandler.clearSoundConfig();
        PlaySoundHandler.clearModelSoundIdToSoundConfigCache();
        // 拿到 assets/modid/sounds/*/ 下的 config.json 和 banSoundPatterns.json 文件
        Map<ResourceLocation, Resource> resources = resourceManager.listResources(ROOT_PATH,
                loc -> loc.getPath().endsWith("config.json") || loc.getPath().endsWith("banSoundPatterns.json")
        );

        int count = 0;
        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            ResourceLocation loc = entry.getKey();
            try (InputStreamReader reader = new InputStreamReader(entry.getValue().open(), StandardCharsets.UTF_8)) {
                JsonArray json = GSON.fromJson(reader, JsonArray.class);
                handleSoundConfig(loc, json);
                count++;
            } catch (Exception e) {
                LOGGER.warn("[Mod] Failed to load sound config: {} — {}", loc, e.getMessage());
            }
        }
        System.out.println("[Mod] Loaded " + count + " sound config files.");
    }

    private static void handleSoundConfig(ResourceLocation loc, JsonArray json) {

        String[] path = loc.getPath().split("/", 3);
        String modelId = path[1];
        String fileName = path[2].replace(".json", "");
        if ("banSoundPatterns".equals(fileName)) {
            PlaySoundHandler.addBlockPatterns(
                    modelId,
                    json.asList().stream()
                            .map(jsonElement -> Pattern.compile(jsonElement.getAsString())).toList()
            );
            return;
        }
        if ("config".equals(fileName)) {
            for (JsonElement element : json) {
                JsonObject obj = element.getAsJsonObject();
                PlaySoundHandler.addSoundConfig(
                        modelId,
                        new YSMSoundConfigModel(
                                obj.getAsJsonArray("target"),
                                obj.getAsJsonArray("replace_patterns"),
                                obj.get("default_sound_id")
                        )
                );
            }
        }
    }
}
