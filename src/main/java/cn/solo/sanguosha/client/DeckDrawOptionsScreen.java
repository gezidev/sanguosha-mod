package cn.solo.sanguosha.client;

import cn.solo.sanguosha.network.ModNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class DeckDrawOptionsScreen
extends Screen {
    private final int serverMask;
    private Checkbox fromBottom;
    private Checkbox hearts;
    private Checkbox diamonds;
    private Checkbox clubs;
    private Checkbox spades;

    public DeckDrawOptionsScreen(int serverMask) {
        super((Component)Component.m_237115_((String)"screen.sanguosha.deck_draw_options"));
        this.serverMask = serverMask;
    }

    protected void m_7856_() {
        int left = this.f_96543_ / 2 - 75;
        int top = this.f_96544_ / 2 - 72;
        int remembered = this.serverMask;
        this.fromBottom = (Checkbox)this.m_142416_(new Checkbox(left, top, 150, 20, (Component)Component.m_237115_((String)"screen.sanguosha.deck_draw.from_bottom"), (remembered & 1) != 0));
        this.hearts = (Checkbox)this.m_142416_(new Checkbox(left, top + 22, 150, 20, (Component)Component.m_237115_((String)"screen.sanguosha.deck_draw.hearts"), (remembered & 2) != 0));
        this.diamonds = (Checkbox)this.m_142416_(new Checkbox(left, top + 44, 150, 20, (Component)Component.m_237115_((String)"screen.sanguosha.deck_draw.diamonds"), (remembered & 4) != 0));
        this.clubs = (Checkbox)this.m_142416_(new Checkbox(left, top + 66, 150, 20, (Component)Component.m_237115_((String)"screen.sanguosha.deck_draw.clubs"), (remembered & 8) != 0));
        this.spades = (Checkbox)this.m_142416_(new Checkbox(left, top + 88, 150, 20, (Component)Component.m_237115_((String)"screen.sanguosha.deck_draw.spades"), (remembered & 0x10) != 0));
        this.m_142416_(Button.m_253074_((Component)Component.m_237115_((String)"screen.sanguosha.deck_draw.execute"), button -> this.execute()).m_252987_(this.f_96543_ / 2 - 50, top + 116, 100, 20).m_253136_());
    }

    private void execute() {
        int mask = (this.fromBottom.m_93840_() ? 1 : 0) | (this.hearts.m_93840_() ? 2 : 0) | (this.diamonds.m_93840_() ? 4 : 0) | (this.clubs.m_93840_() ? 8 : 0) | (this.spades.m_93840_() ? 16 : 0);
        ModNetwork.saveDeckDrawConfig(mask);
        this.m_7379_();
    }

    public void m_88315_(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.m_280273_(graphics);
        graphics.m_280653_(this.f_96547_, this.f_96539_, this.f_96543_ / 2, this.f_96544_ / 2 - 94, 0xFFFFFF);
        super.m_88315_(graphics, mouseX, mouseY, partialTick);
    }
}

