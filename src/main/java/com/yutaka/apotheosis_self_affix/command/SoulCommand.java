package com.yutaka.apotheosis_self_affix.command;

import com.mojang.brigadier.CommandDispatcher;
import com.yutaka.apotheosis_self_affix.menu.SoulMenu;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.network.NetworkHooks;

public class SoulCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("soul")
                .executes(context -> {
                    if (context.getSource().getEntity() instanceof ServerPlayer player) {
                        NetworkHooks.openScreen(player, new SimpleMenuProvider(
                                (containerId, playerInv, p) -> new SoulMenu(containerId, playerInv),
                                Component.translatable("gui.apotheosis_self_affix.soul_menu")
                        ));
                        return 1;
                    }
                    return 0;
                })
        );
    }
}
