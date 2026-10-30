package com.yutaka.apotheosis_self_affix.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.yutaka.apotheosis_self_affix.ApotheosisSelfAffix;
import com.yutaka.apotheosis_self_affix.menu.SoulMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class SoulScreen extends AbstractContainerScreen<SoulMenu> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(ApotheosisSelfAffix.MODID, "textures/gui/soul_inventory.png");
    private static final ResourceLocation SLOT_ICON = new ResourceLocation(ApotheosisSelfAffix.MODID, "textures/slot/empty_soul_slot.png");

    public SoulScreen(SoulMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 146;
        this.inventoryLabelY = 52;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int left = this.leftPos;
        int top = this.topPos;

        // Render main GUI background
        guiGraphics.blit(TEXTURE, left, top, 0, 0, this.imageWidth, this.imageHeight);

        // If the soul slot (slot 0) is empty, render the ghostly soul icon
        if (!this.menu.getSlot(0).hasItem()) {
            guiGraphics.blit(SLOT_ICON, left + 80, top + 26, 0, 0, 16, 16, 16, 16);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
