/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 */
package cn.solo.sanguosha.client;

import cn.solo.sanguosha.client.ClientGeneralCatalog;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class GeneralCardPreviewScreen
extends Screen {
    private final ItemStack stack;

    public GeneralCardPreviewScreen(ItemStack stack) {
        super(stack.m_41786_());
        this.stack = stack.m_255036_(1);
    }

    public void m_88315_(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.m_280273_(graphics);
        ResourceLocation texture = ClientGeneralCatalog.texture(this.stack);
        int availableWidth = Math.max(1, this.f_96543_ - 20);
        int availableHeight = Math.max(1, this.f_96544_ - 36);
        float scale = (float)Math.min((double)availableWidth / 512.0, (double)availableHeight / 720.0);
        int imageWidth = Math.max(1, Math.round(512.0f * scale));
        int imageHeight = Math.max(1, Math.round(720.0f * scale));
        int x = (this.f_96543_ - imageWidth) / 2;
        int y = 20 + (availableHeight - imageHeight) / 2;
        graphics.m_280168_().m_85836_();
        graphics.m_280168_().m_252880_((float)x, (float)y, 0.0f);
        graphics.m_280168_().m_85841_(scale, scale, 1.0f);
        graphics.m_280163_(texture, 0, 0, 0.0f, 0.0f, 512, 720, 512, 720);
        graphics.m_280168_().m_85849_();
        graphics.m_280653_(this.f_96547_, this.f_96539_, this.f_96543_ / 2, 7, 0xFFFFFF);
        super.m_88315_(graphics, mouseX, mouseY, partialTick);
    }

    public boolean m_7043_() {
        return false;
    }
}

