/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.AbstractContainerMenu
 */
package cn.solo.sanguosha.client;

import cn.solo.sanguosha.menu.HandContainerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public final class HandContainerScreen
extends AbstractContainerScreen<HandContainerMenu> {
    private static final int PANEL = -14670805;
    private static final int SLOT = -13354429;
    private static final int EDGE = -8945781;

    public HandContainerScreen(HandContainerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.f_97726_ = 378;
        this.f_97727_ = 252;
        this.f_97730_ = 108;
        this.f_97731_ = 164;
        this.f_97728_ = 9;
        this.f_97729_ = 6;
    }

    protected void m_7286_(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int column;
        int row;
        int left = this.f_97735_;
        int top = this.f_97736_;
        graphics.m_280509_(left, top, left + this.f_97726_, top + this.f_97727_, -15855338);
        graphics.m_280509_(left + 3, top + 3, left + this.f_97726_ - 3, top + this.f_97727_ - 3, -14670805);
        for (row = 0; row < 8; ++row) {
            for (column = 0; column < 20; ++column) {
                HandContainerScreen.slot(graphics, left + 8 + column * 18, top + 17 + row * 18);
            }
        }
        for (row = 0; row < 3; ++row) {
            for (column = 0; column < 9; ++column) {
                HandContainerScreen.slot(graphics, left + 107 + column * 18, top + 175 + row * 18);
            }
        }
        for (int column2 = 0; column2 < 9; ++column2) {
            HandContainerScreen.slot(graphics, left + 107 + column2 * 18, top + 233);
        }
    }

    private static void slot(GuiGraphics graphics, int x, int y) {
        graphics.m_280509_(x, y, x + 18, y + 18, -8945781);
        graphics.m_280509_(x + 1, y + 1, x + 17, y + 17, -13354429);
    }

    public void m_88315_(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.m_280273_(graphics);
        super.m_88315_(graphics, mouseX, mouseY, partialTick);
        this.m_280072_(graphics, mouseX, mouseY);
    }
}

