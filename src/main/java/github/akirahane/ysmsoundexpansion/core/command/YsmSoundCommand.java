package github.akirahane.ysmsoundexpansion.core.command;

import com.mojang.brigadier.CommandDispatcher;
import github.akirahane.ysmsoundexpansion.client.common.EntityModelTracker;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class YsmSoundCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ysmsound")
                .then(Commands.literal("refresh")
                        .executes(ctx -> {
                            EntityModelTracker.clearModel();
                            ctx.getSource().sendSuccess(() -> Component.literal("§a模型缓存已清除。"), true);
                            return 1;
                        }))
        );
    }
}
