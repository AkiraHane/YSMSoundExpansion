package github.akirahane.ysmsoundexpansion.client.event;

import github.akirahane.ysmsoundexpansion.YSMSoundExpansion;
import github.akirahane.ysmsoundexpansion.client.common.EntityModelTracker;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = YSMSoundExpansion.MODID, value = Dist.CLIENT)
public class EntityCleanupHandler {
    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        EntityModelTracker.clearModel(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onEntityLeave(EntityLeaveLevelEvent event) {
        EntityModelTracker.clearModel(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        EntityModelTracker.clearModel(event.getEntity().getUUID());
    }
}
