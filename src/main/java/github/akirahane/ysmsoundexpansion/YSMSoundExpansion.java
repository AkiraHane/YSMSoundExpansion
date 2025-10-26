package github.akirahane.ysmsoundexpansion;

import github.akirahane.ysmsoundexpansion.core.command.YsmSoundCommand;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(YSMSoundExpansion.MODID)
public class YSMSoundExpansion {
    public static final String MODID = "ysmsoundexpansion";

    public YSMSoundExpansion(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent e) ->
                YsmSoundCommand.register(e.getDispatcher())
        );
    }
}
