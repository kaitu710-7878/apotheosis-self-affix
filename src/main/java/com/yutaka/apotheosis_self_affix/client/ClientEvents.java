package com.yutaka.apotheosis_self_affix.client;

import com.yutaka.apotheosis_self_affix.ApotheosisSelfAffix;
import com.yutaka.apotheosis_self_affix.client.screen.SoulScreen;
import com.yutaka.apotheosis_self_affix.network.ModPacketHandler;
import com.yutaka.apotheosis_self_affix.network.OpenSoulMenuPacket;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientEvents {

    @Mod.EventBusSubscriber(modid = ApotheosisSelfAffix.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                MenuScreens.register(ApotheosisSelfAffix.SOUL_MENU.get(), SoulScreen::new);
            });
        }

        @SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(KeyInit.OPEN_SOUL_MENU_KEY);
        }
    }

    @Mod.EventBusSubscriber(modid = ApotheosisSelfAffix.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeBusEvents {

        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            while (KeyInit.OPEN_SOUL_MENU_KEY.consumeClick()) {
                ModPacketHandler.sendToServer(new OpenSoulMenuPacket());
            }
        }

        @SubscribeEvent
        public static void onScreenInit(ScreenEvent.Init.Post event) {
            if (event.getScreen() instanceof InventoryScreen screen) {
                int left = screen.getGuiLeft();
                int top = screen.getGuiTop();

                // Add a small button to open the soul screen
                Button soulButton = Button.builder(Component.literal("§bS§r"), b -> {
                            ModPacketHandler.sendToServer(new OpenSoulMenuPacket());
                        })
                        .pos(left + 65, top + 61)
                        .size(12, 12)
                        .tooltip(Tooltip.create(Component.translatable("tooltip.apotheosis_self_affix.open_soul_menu")))
                        .build();

                event.addListener(soulButton);
            }
        }
    }
}
