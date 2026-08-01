/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 */
package cn.solo.sanguosha.client;

import cn.solo.sanguosha.client.HandPouchCardBackScreen;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class HandPouchActionScreen
extends Screen {
    private final UUID token;
    private final String targetName;
    private final int count;

    public HandPouchActionScreen(UUID token, String targetName, int count) {
        super((Component)Component.m_237113_((String)"\u624b\u724c\u4ea4\u4e92"));
        this.token = token;
        this.targetName = targetName;
        this.count = Math.max(0, count);
    }

    protected void m_7856_() {
        int y = this.f_96544_ / 2;
        this.m_142416_(Button.m_253074_((Component)Component.m_237113_((String)"\u987a\u724c"), b -> this.open(true)).m_252987_(this.f_96543_ / 2 - 105, y, 100, 20).m_253136_());
        this.m_142416_(Button.m_253074_((Component)Component.m_237113_((String)"\u62c6\u724c"), b -> this.open(false)).m_252987_(this.f_96543_ / 2 + 5, y, 100, 20).m_253136_());
    }

    private void open(boolean draw) {
        Minecraft.m_91087_().m_91152_((Screen)new HandPouchCardBackScreen(this.token, this.targetName, this.count, draw));
    }

    public void m_88315_(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.m_280273_(graphics);
        super.m_88315_(graphics, mouseX, mouseY, partialTick);
        graphics.m_280137_(this.f_96547_, this.targetName, this.f_96543_ / 2, this.f_96544_ / 2 - 38, 0xFFFFFF);
        graphics.m_280137_(this.f_96547_, "\u624b\u724c\u6570\u91cf\uff1a" + this.count, this.f_96543_ / 2, this.f_96544_ / 2 - 22, 12568533);
    }
}

