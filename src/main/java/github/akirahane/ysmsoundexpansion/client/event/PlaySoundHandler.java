package github.akirahane.ysmsoundexpansion.client.event;

import com.mojang.logging.LogUtils;
import github.akirahane.ysmsoundexpansion.YSMSoundExpansion;
import github.akirahane.ysmsoundexpansion.client.common.EntityModelTracker;
import github.akirahane.ysmsoundexpansion.client.common.YSMSoundInstance;
import github.akirahane.ysmsoundexpansion.client.model.YSMSound;
import github.akirahane.ysmsoundexpansion.client.model.YSMSoundConfigModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.PlayLevelSoundEvent;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = YSMSoundExpansion.MODID, value = Dist.CLIENT)
public class PlaySoundHandler {
    private static boolean isHandling = false;
    private static final HashMap<String, List<Pattern>> modelIdToBlockPatterns = new HashMap<>();
    private static final HashMap<String, List<YSMSoundConfigModel>> modelIdToSoundConfig = new HashMap<>();
    private static final LinkedHashMap<String, List<YSMSoundConfigModel>> modelSoundIdToSoundConfigCache = new LinkedHashMap<>(
            100, 0.75f, true
    ) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, List<YSMSoundConfigModel>> eldest) {
            return size() > 1000; // 保持缓存大小在1000条以内
        }
    };
    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onEntityPlaySound(PlayLevelSoundEvent.AtEntity event) {
        if (isHandling) return;
        try {
            isHandling = true;
            Entity entity = event.getEntity();
            Holder<SoundEvent> soundHolder = event.getSound();

            // 取得声音 ID
            if (soundHolder == null) return;
            SoundEvent sound = soundHolder.value();
            sound = onEntityPlaySound(sound, entity, event.getSource(), event.getNewVolume(), event.getNewPitch());
            if (sound == null) {
                event.setCanceled(true);
            }
        } finally {
            isHandling = false;
        }
    }

    // 提取出处理和判断逻辑
    public static SoundEvent onEntityPlaySound(SoundEvent sound, Entity entity, SoundSource source, float volume, float pitch) {
        if (sound == null) {
            LOGGER.debug("[YSMSOUND] 播放声音失败: {}", sound);
            return null;
        }
        UUID id = entity.getUUID();

        // 取得声音 ID
        ResourceLocation soundRes = sound.getLocation();

        // 如果是 YSM 声音, 给这个实体绑定模型
        LOGGER.debug("[YSMSOUND] 声音id: {}:{}", soundRes.getNamespace(), soundRes.getPath());
        if ("ysmsoundexpansion".equals(soundRes.getNamespace())) {
            String path = soundRes.getPath();
            String modelId = path.contains(".") ? path.substring(0, path.indexOf('.')) : path;
            EntityModelTracker.setModel(id, modelId);
            LOGGER.debug("[YSM] 记录模型: {} -> {}", entity.getName().getString(), modelId);
        }

        // 如果这个玩家没有模型，则正常播放声音
        String modelId = EntityModelTracker.getModel(id);
        if (modelId == null) {
            LOGGER.debug("[YSMSOUND] 玩家没有模型: {}", entity.getName().getString());
            return sound;
        }

        String soundId = soundRes.getNamespace() + ":" + soundRes.getPath();
        // 如果是需要屏蔽的声音, 取消播放
        if (modelIdToBlockPatterns.containsKey(modelId) &&
                modelIdToBlockPatterns.get(modelId).stream().anyMatch(pattern -> pattern.matcher(soundId).matches())
        ) {
            LOGGER.debug("[YSMSOUND] 屏蔽声音: {}", soundId);
            return null;
        }

        // 考虑替换声音
        String key = modelId + "." + soundId;
        List<YSMSoundConfigModel> soundConfigs = modelSoundIdToSoundConfigCache.getOrDefault(key, null);
        if (soundConfigs == null && modelIdToSoundConfig.containsKey(modelId)) {
            modelSoundIdToSoundConfigCache.put(
                    key,
                    modelIdToSoundConfig.get(modelId).stream()
                            .filter(soundConfig -> soundConfig.isReplace(soundId))
                            .toList()
            );
            soundConfigs = modelSoundIdToSoundConfigCache.get(key);
        }
        // 如果不是需要替换的声音，则正常播放声音
        if (ObjectUtils.isEmpty(soundConfigs)) {
            LOGGER.debug("[YSMSOUND] 正常播放声音: {}", soundId);
            return sound;
        }

        Level level = entity.level();
        BlockPos pos = entity.getOnPos(); // 实体所站立的方块, 跳跃需要是空气
        BlockState state = level.getBlockState(pos);
        String blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
        List<String> blockTags = state.getBlockHolder().tags().map(
                tag -> tag.location().toString()
        ).collect(Collectors.toList());
        String mainHandItemId;
        if (entity instanceof LivingEntity living) {
            ItemStack mainHand = living.getMainHandItem();
            // 获取物品 ID
            mainHandItemId = BuiltInRegistries.ITEM.getKey(mainHand.getItem()).toString();
        } else {
            mainHandItemId = null;
        }
        boolean isRaining = level.isRaining();
        boolean isThundering = level.isThundering();
        String weather;
        if (isThundering) {
            weather = "thunder";
        } else if (isRaining) {
            weather = "rain";
        } else {
            weather = "clear";
        }
        int time = Math.toIntExact(level.getDayTime() % 24000L);
        String dimensionId = level.dimension().location().toString();
        LivingEntity living = entity instanceof LivingEntity ? (LivingEntity) entity : null;
        Player player = entity instanceof Player ? (Player) entity : null;
        Integer health = living != null ? (int) living.getHealth() : null;
        Integer air = living != null ? living.getAirSupply() : null;
        Integer food = player != null ? player.getFoodData().getFoodLevel() : null;
        Integer xpLevel = player != null ? player.experienceLevel : null;

        List<YSMSound> targetSounds = new ArrayList<>();
        for (YSMSoundConfigModel soundConfig : soundConfigs) {
            LOGGER.debug("[YSMSOUND] 检查条件: {} 个", soundConfig.targets().size());
            targetSounds.addAll(soundConfig.checkConditions(
                    blockId, blockTags, mainHandItemId, weather, time, dimensionId, health, air, food, xpLevel
            ));
        }
        // 如果没有符合条件的替换声音, 正常播放
        if (targetSounds.isEmpty()) {
            LOGGER.debug("[YSMSOUND] 忽略声音: {}", soundId);
            return sound;
        }
        LOGGER.debug("[YSMSOUND] 替换声音: {} -> {}", soundId, targetSounds);
        for (YSMSound soundItem : targetSounds) {
            Minecraft.getInstance().getSoundManager().play(
                    new YSMSoundInstance(
                            soundItem.sound(),
                            source,
                            volume * soundItem.volume(),
                            pitch * soundItem.pitch(),
                            entity,
                            entity.getId() * 31L + System.currentTimeMillis()
                    )
            );
        }
        return null;
    }

    public static void addBlockPatterns(String modelId, List<Pattern> patterns) {
        if (patterns == null || patterns.isEmpty()) {
            return;
        }
        modelIdToBlockPatterns.put(modelId, patterns);
    }

    public static void clearBlockPatterns(String modelId) {
        modelIdToBlockPatterns.remove(modelId);
    }

    public static void clearBlockPatterns() {
        modelIdToBlockPatterns.clear();
    }

    public static void addSoundConfig(String modelId, YSMSoundConfigModel soundConfig) {
        if (modelIdToSoundConfig.containsKey(modelId)) {
            modelIdToSoundConfig.get(modelId).add(soundConfig);
        } else {
            modelIdToSoundConfig.put(modelId, new ArrayList<>(List.of(soundConfig)));
        }
    }

    public static void clearSoundConfig(String modelId) {
        modelIdToSoundConfig.remove(modelId);
    }

    public static void clearSoundConfig() {
        modelIdToSoundConfig.clear();
    }

    public static void clearModelSoundIdToSoundConfigCache() {
        modelSoundIdToSoundConfigCache.clear();
    }
}
