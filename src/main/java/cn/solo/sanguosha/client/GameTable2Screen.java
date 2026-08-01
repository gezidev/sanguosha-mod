/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.ConfirmScreen
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.AbstractContainerMenu
 */
package cn.solo.sanguosha.client;

import cn.solo.sanguosha.client.ClientScreens;
import cn.solo.sanguosha.game.GameRoomManager;
import cn.solo.sanguosha.menu.GameTable2Menu;
import cn.solo.sanguosha.network.ModNetwork;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public final class GameTable2Screen
extends AbstractContainerScreen<GameTable2Menu> {
    private static final String[] LABELS = new String[]{"\u8eab\u4efd", "+1\u9a6c", "-1\u9a6c", "\u6b66\u5668", "\u9632\u5177", "\u8840\u91cf", "\u6b66\u5c06"};
    private final Button[] horizontalButtons = new Button[7];
    private Button flipButton;
    private Button readyButton;
    private Button startButton;
    private Button disbandButton;
    private BlockPos roomAnchor;
    private boolean roomOwner;
    private boolean roomReady;
    private boolean roomStateReceived;
    private int roomPhase;
    private int health = 1;
    private List<String> roomMembers = List.of();
    private int sliderX;
    private int sliderY;
    private boolean dragging;
    private long lastHealthSend;

    public GameTable2Screen(GameTable2Menu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.f_97726_ = 320;
        this.f_97727_ = 168;
        this.f_97730_ = 15;
        this.f_97731_ = 74;
    }

    protected void m_7856_() {
        super.m_7856_();
        for (int slot = 0; slot < this.horizontalButtons.length; ++slot) {
            int target = slot;
            this.horizontalButtons[slot] = (Button)this.m_142416_(Button.m_253074_((Component)Component.m_237113_((String)"\u21bb"), b -> {
                BlockPos anchor = this.roomStateReceived ? this.roomAnchor : ((GameTable2Menu)this.f_97732_).roomAnchor();
                ModNetwork.toggleTableSlotHorizontal(((GameTable2Menu)this.f_97732_).blockPos(), anchor, target);
            }).m_252987_(this.f_97735_ + 13 + slot * 25, this.f_97736_ + 58, 18, 14).m_253136_());
        }
        this.flipButton = (Button)this.m_142416_(Button.m_253074_((Component)this.buttonText(), b -> {
            if (((GameTable2Menu)this.f_97732_).canFlipIdentity()) {
                ModNetwork.flipGameTable2Identity(((GameTable2Menu)this.f_97732_).blockPos());
            }
        }).m_252987_(this.f_97735_ + 49, this.f_97736_ + 73, 66, 12).m_253136_());
        this.readyButton = (Button)this.m_142416_(Button.m_253074_((Component)Component.m_237115_((String)"screen.sanguosha.military.ready"), b -> ModNetwork.militaryRoomAction(((GameTable2Menu)this.f_97732_).blockPos(), this.roomStateReceived ? this.roomAnchor : ((GameTable2Menu)this.f_97732_).roomAnchor(), this.roomReady ? GameRoomManager.Action.UNREADY : GameRoomManager.Action.READY)).m_252987_(this.f_97735_ + 208, this.f_97736_ + 18, 100, 20).m_253136_());
        this.startButton = (Button)this.m_142416_(Button.m_253074_((Component)Component.m_237115_((String)"screen.sanguosha.military.start"), b -> ModNetwork.militaryRoomAction(((GameTable2Menu)this.f_97732_).blockPos(), this.roomAnchor, GameRoomManager.Action.START)).m_252987_(this.f_97735_ + 208, this.f_97736_ + 44, 100, 20).m_253136_());
        this.disbandButton = (Button)this.m_142416_(Button.m_253074_((Component)Component.m_237115_((String)"screen.sanguosha.military.disband"), b -> this.confirmDisband()).m_252987_(this.f_97735_ + 208, this.f_97736_ + 70, 100, 20).m_253136_());
        this.sliderX = this.f_97735_ + 151;
        this.sliderY = this.f_97736_ + 75;
        ClientScreens.applyPendingMilitaryRoom(this);
        if (GameRoomManager.shouldRequestState(this.roomStateReceived)) {
            ModNetwork.militaryRoomAction(((GameTable2Menu)this.f_97732_).blockPos(), ((GameTable2Menu)this.f_97732_).roomAnchor(), GameRoomManager.Action.SYNC);
        }
        this.refresh();
    }

    private void confirmDisband() {
        if (this.f_96541_ == null) {
            return;
        }
        this.f_96541_.m_91152_((Screen)new ConfirmScreen(ok -> {
            this.f_96541_.m_91152_((Screen)this);
            if (ok && this.roomAnchor != null) {
                ModNetwork.militaryRoomAction(((GameTable2Menu)this.f_97732_).blockPos(), this.roomAnchor, GameRoomManager.Action.DISBAND);
            }
        }, (Component)Component.m_237115_((String)"screen.sanguosha.military.disband_confirm"), (Component)Component.m_237115_((String)"screen.sanguosha.military.disband_warning")));
    }

    private Component buttonText() {
        return Component.m_237113_((String)(((GameTable2Menu)this.f_97732_).identityFaceUp() ? "\u8eab\u4efd\uff1a\u6b63\u9762" : "\u8eab\u4efd\uff1a\u80cc\u9762"));
    }

    private void refresh() {
        if (this.flipButton == null) {
            return;
        }
        this.flipButton.f_93623_ = this.roomStateReceived && ((GameTable2Menu)this.f_97732_).canFlipIdentity();
        this.flipButton.m_93666_(this.buttonText());
        for (int slot = 0; slot < this.horizontalButtons.length; ++slot) {
            this.horizontalButtons[slot].f_93623_ = this.roomStateReceived && !((GameTable2Menu)this.f_97732_).isSlotEmpty(slot);
            this.horizontalButtons[slot].m_93666_((Component)Component.m_237113_((String)("\u21bb" + ((GameTable2Menu)this.f_97732_).getRotation(slot) + "\u00b0")));
        }
        this.readyButton.f_93624_ = true;
        this.readyButton.f_93623_ = GameRoomManager.readyButtonActive(this.roomStateReceived, this.roomPhase);
        this.readyButton.m_93666_((Component)Component.m_237115_((String)(this.roomReady ? "screen.sanguosha.military.unready" : "screen.sanguosha.military.ready")));
        this.startButton.f_93623_ = this.startButton.f_93624_ = GameRoomManager.startButtonVisible(this.roomStateReceived, this.roomOwner, this.roomPhase);
        this.disbandButton.f_93623_ = this.disbandButton.f_93624_ = GameRoomManager.disbandButtonVisible(this.roomStateReceived, this.roomOwner, this.roomPhase);
    }

    protected void m_181908_() {
        super.m_181908_();
        this.refresh();
    }

    protected void m_7286_(GuiGraphics g, float pt, int mx, int my) {
        g.m_280509_(this.f_97735_, this.f_97736_, this.f_97735_ + this.f_97726_, this.f_97736_ + this.f_97727_, -15262944);
        g.m_280509_(this.f_97735_ + 3, this.f_97736_ + 3, this.f_97735_ + this.f_97726_ - 3, this.f_97736_ + this.f_97727_ - 3, -14143431);
        g.m_280509_(this.f_97735_ + 195, this.f_97736_ + 6, this.f_97735_ + 314, this.f_97736_ + this.f_97727_ - 6, -14867413);
        for (int slot = 0; slot < 7; ++slot) {
            int x = this.f_97735_ + 12 + slot * 25;
            g.m_280137_(this.f_96547_, LABELS[slot], x + 9, this.f_97736_ + 18, slot == 5 ? -34953 : -1649496);
            GameTable2Screen.drawSlot(g, x, this.f_97736_ + 37);
        }
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                GameTable2Screen.drawSlot(g, this.f_97735_ + 14 + col * 18, this.f_97736_ + 85 + row * 18);
            }
        }
        for (int col = 0; col < 9; ++col) {
            GameTable2Screen.drawSlot(g, this.f_97735_ + 14 + col * 18, this.f_97736_ + 143);
        }
        g.m_280509_(this.sliderX, this.sliderY, this.sliderX + 40, this.sliderY + 6, -11184811);
        int knob = this.sliderX + (this.health - 1) * 10;
        g.m_280509_(knob - 2, this.sliderY - 3, knob + 3, this.sliderY + 9, -43691);
        g.m_280488_(this.f_96547_, String.valueOf(this.health), this.sliderX + 47, this.sliderY - 1, -21846);
    }

    private static void drawSlot(GuiGraphics g, int x, int y) {
        g.m_280509_(x, y, x + 18, y + 18, -7760995);
        g.m_280509_(x + 1, y + 1, x + 17, y + 17, -12893107);
    }

    public void updateRoom(BlockPos anchor, boolean owner, boolean ready, int phase, List<String> members) {
        this.roomAnchor = anchor;
        this.roomOwner = owner;
        this.roomReady = ready;
        this.roomPhase = phase;
        this.roomStateReceived = true;
        this.roomMembers = List.copyOf(members);
        this.refresh();
    }

    public void updateHealth(int value) {
        this.health = Math.max(1, Math.min(5, value));
    }

    public boolean matches(BlockPos anchor) {
        return this.roomAnchor != null && this.roomAnchor.equals((Object)anchor);
    }

    public boolean matches(BlockPos clickedPos, BlockPos anchor) {
        return ((GameTable2Menu)this.f_97732_).blockPos().equals((Object)clickedPos) && this.matches(anchor);
    }

    private boolean sliderHit(double x, double y) {
        return this.roomStateReceived && ((GameTable2Menu)this.f_97732_).hasHealthCard() && x >= (double)(this.sliderX - 4) && x <= (double)(this.sliderX + 44) && y >= (double)(this.sliderY - 2) && y <= (double)(this.sliderY + 9);
    }

    private void dragHealth(double x, boolean force) {
        int value = Math.max(1, Math.min(5, 1 + (int)Math.round((x - (double)this.sliderX) / 10.0)));
        if (value != this.health) {
            this.health = value;
            long now = System.currentTimeMillis();
            if (force || now - this.lastHealthSend >= 100L) {
                this.lastHealthSend = now;
                ModNetwork.setRoomHealth(((GameTable2Menu)this.f_97732_).blockPos(), this.roomAnchor, 5, this.health);
            }
        }
    }

    public boolean m_6375_(double x, double y, int button) {
        if (button == 0 && this.sliderHit(x, y)) {
            this.dragging = true;
            this.dragHealth(x, true);
            return true;
        }
        return super.m_6375_(x, y, button);
    }

    public boolean m_7979_(double x, double y, int button, double dx, double dy) {
        if (this.dragging && button == 0) {
            this.dragHealth(x, false);
            return true;
        }
        return super.m_7979_(x, y, button, dx, dy);
    }

    public boolean m_6348_(double x, double y, int button) {
        if (this.dragging) {
            this.dragging = false;
            this.dragHealth(x, true);
            return true;
        }
        return super.m_6348_(x, y, button);
    }

    public void m_88315_(GuiGraphics g, int mx, int my, float pt) {
        this.m_280273_(g);
        super.m_88315_(g, mx, my, pt);
        if (this.roomAnchor != null) {
            g.m_280653_(this.f_96547_, (Component)Component.m_237110_((String)"screen.sanguosha.military.summary", (Object[])new Object[]{this.roomMembers.size()}), this.f_97735_ + 258, this.f_97736_ + 98, 16765802);
            int y = this.f_97736_ + 112;
            for (String name : this.roomMembers) {
                g.m_280488_(this.f_96547_, name, this.f_97735_ + 208, y, 0xFFFFFF);
                y += 10;
            }
        }
        this.m_280072_(g, mx, my);
    }
}

