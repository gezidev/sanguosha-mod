/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.item.ItemStack
 */
package cn.solo.sanguosha.client;

import cn.solo.sanguosha.network.ModNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public final class HealthCardScreen
extends Screen {
    private static final int MIN_HP = 1;
    private static final int MAX_HP = 5;
    private final InteractionHand hand;
    private final ItemStack stack;
    private int selectedHealth;
    private int sliderX;
    private int sliderY;
    private int sliderWidth;

    public HealthCardScreen(InteractionHand hand, int currentHealth) {
        super((Component)Component.m_237113_((String)"\u8840\u91cf\u8bbe\u7f6e"));
        this.hand = hand;
        this.stack = this.f_96541_ == null ? ItemStack.f_41583_ : this.f_96541_.f_91074_.m_21120_(hand);
        this.selectedHealth = Math.max(1, Math.min(5, currentHealth));
    }

    protected void m_7856_() {
        super.m_7856_();
        int titleY = 20;
        this.sliderX = this.f_96543_ / 2 - 100;
        this.sliderY = 60;
        this.sliderWidth = 200;
        int buttonY = this.sliderY + 60;
        this.m_142416_(Button.m_253074_((Component)Component.m_237113_((String)"\u786e\u8ba4"), button -> {
            if (this.f_96541_ != null && this.f_96541_.f_91074_ != null) {
                ModNetwork.setHealth(this.hand, this.selectedHealth);
            }
            this.m_7379_();
        }).m_252987_(this.f_96543_ / 2 - 105, buttonY, 100, 20).m_253136_());
        this.m_142416_(Button.m_253074_((Component)Component.m_237113_((String)"\u53d6\u6d88"), button -> this.m_7379_()).m_252987_(this.f_96543_ / 2 + 5, buttonY, 100, 20).m_253136_());
    }

    public void m_88315_(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.m_280273_(graphics);
        graphics.m_280653_(this.f_96547_, this.f_96539_, this.f_96543_ / 2, 20, 0xFFFFFF);
        String healthText = "\u5f53\u524d\u8840\u91cf\uff1a" + this.selectedHealth;
        graphics.m_280137_(this.f_96547_, healthText, this.f_96543_ / 2, this.sliderY - 15, 0xFF5555);
        graphics.m_280509_(this.sliderX, this.sliderY, this.sliderX + this.sliderWidth, this.sliderY + 20, 0x44444444);
        int segmentWidth = this.sliderWidth / 4;
        for (int hp = 1; hp <= 5; ++hp) {
            int x = this.sliderX + (hp - 1) * segmentWidth;
            graphics.m_280509_(x, this.sliderY - 5, x + 1, this.sliderY + 25, -5592406);
            graphics.m_280137_(this.f_96547_, String.valueOf(hp), x + segmentWidth / 2, this.sliderY + 25, 0xAAAAAA);
        }
        int sliderPos = this.sliderX + (this.selectedHealth - 1) * segmentWidth;
        graphics.m_280509_(sliderPos, this.sliderY - 5, sliderPos + segmentWidth, this.sliderY + 25, -3390396);
        this.renderHealthPreview(graphics, this.f_96543_ / 2, this.sliderY + 80, this.selectedHealth);
        super.m_88315_(graphics, mouseX, mouseY, partialTick);
    }

    private void renderHealthPreview(GuiGraphics graphics, int centerX, int centerY, int health) {
        int size = 32;
        graphics.m_280509_(centerX - size / 2, centerY - size / 2, centerX + size / 2, centerY + size / 2, this.getHealthColor(health));
        graphics.m_280137_(this.f_96547_, String.valueOf(health), centerX, centerY - 4, 0xFFFFFF);
    }

    private int getHealthColor(int hp) {
        return switch (hp) {
            case 1 -> -3399134;
            case 2 -> -3381726;
            case 3 -> -3364318;
            case 4 -> -14496734;
            case 5 -> -14540084;
            default -> -10066330;
        };
    }

    public boolean m_6375_(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseX >= (double)this.sliderX && mouseX <= (double)(this.sliderX + this.sliderWidth) && mouseY >= (double)(this.sliderY - 10) && mouseY <= (double)(this.sliderY + 30)) {
            this.updateHealthFromMouse((int)mouseX);
            return true;
        }
        return super.m_6375_(mouseX, mouseY, button);
    }

    public boolean m_7979_(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (mouseX >= (double)this.sliderX && mouseX <= (double)(this.sliderX + this.sliderWidth)) {
            this.updateHealthFromMouse((int)mouseX);
            return true;
        }
        return super.m_7979_(mouseX, mouseY, button, dragX, dragY);
    }

    private void updateHealthFromMouse(int mouseX) {
        int segmentWidth = this.sliderWidth / 4;
        int relativeX = mouseX - this.sliderX;
        int hp = 1 + Math.round((float)relativeX / (float)segmentWidth);
        this.selectedHealth = Math.max(1, Math.min(5, hp));
    }

    public boolean m_7043_() {
        return false;
    }
}

