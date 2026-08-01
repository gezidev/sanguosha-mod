/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.item.ItemStack
 */
package cn.solo.sanguosha.client;

import cn.solo.sanguosha.client.DeckDrawOptionsScreen;
import cn.solo.sanguosha.client.FiveGeneralSelectionScreen;
import cn.solo.sanguosha.client.GameTable2Screen;
import cn.solo.sanguosha.client.GeneralCardPreviewScreen;
import cn.solo.sanguosha.client.GeneralCategoryScreen;
import cn.solo.sanguosha.client.HandPouchActionScreen;
import cn.solo.sanguosha.client.HealthCardScreen;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public final class ClientScreens {
    private static PendingMilitaryRoom pendingMilitaryRoom;

    private ClientScreens() {
    }

    public static void openGameMode(InteractionHand hand) {
        ClientScreens.openGeneralCard(hand);
    }

    static void openGeneralCardDirect(InteractionHand hand) {
        ClientScreens.openGeneralCard(hand);
    }

    public static void openGeneralPreview(ItemStack stack) {
        Minecraft minecraft = Minecraft.m_91087_();
        if (minecraft.f_91074_ != null && !stack.m_41619_()) {
            minecraft.m_91152_((Screen)new GeneralCardPreviewScreen(stack));
        }
    }

    public static void updateMilitaryRoom(BlockPos anchor, boolean owner, boolean ready, int phase, List<String> members) {
        PendingMilitaryRoom state = new PendingMilitaryRoom(anchor, owner, ready, phase, List.copyOf(members));
        Screen screen = Minecraft.m_91087_().f_91080_;
        if (screen instanceof GameTable2Screen) {
            GameTable2Screen screen2 = (GameTable2Screen)screen;
            state.apply(screen2);
        } else {
            pendingMilitaryRoom = state;
        }
    }

    public static void applyPendingMilitaryRoom(GameTable2Screen screen) {
        if (pendingMilitaryRoom == null) {
            return;
        }
        pendingMilitaryRoom.apply(screen);
        pendingMilitaryRoom = null;
    }

    public static void openFiveGeneralSelection(BlockPos anchor, List<String> ids) {
        FiveGeneralSelectionScreen screen;
        Minecraft minecraft = Minecraft.m_91087_();
        Screen screen2 = minecraft.f_91080_;
        if (screen2 instanceof FiveGeneralSelectionScreen && (screen = (FiveGeneralSelectionScreen)screen2).matches(anchor)) {
            screen.updateOffers(ids);
        } else {
            minecraft.m_91152_((Screen)new FiveGeneralSelectionScreen(anchor, ids));
        }
    }

    public static void updateGeneralSelection(BlockPos anchor, boolean success, boolean complete, String message) {
        FiveGeneralSelectionScreen screen;
        Minecraft minecraft = Minecraft.m_91087_();
        Screen screen2 = minecraft.f_91080_;
        if (screen2 instanceof FiveGeneralSelectionScreen && (screen = (FiveGeneralSelectionScreen)screen2).matches(anchor)) {
            screen.applyResult(success, complete, message);
            if (complete) {
                minecraft.m_91152_(null);
            }
        }
    }

    public static void openGeneralCard(InteractionHand hand) {
        Minecraft minecraft = Minecraft.m_91087_();
        if (minecraft.f_91074_ != null) {
            minecraft.m_91152_((Screen)new GeneralCategoryScreen(hand));
        }
    }

    public static void updateRoomHealth(BlockPos clickedPos, BlockPos anchor, int health) {
        GameTable2Screen screen;
        Minecraft minecraft = Minecraft.m_91087_();
        Screen screen2 = minecraft.f_91080_;
        if (screen2 instanceof GameTable2Screen && (screen = (GameTable2Screen)screen2).matches(clickedPos, anchor)) {
            screen.updateHealth(health);
        }
    }

    public static void roomDisbanded(BlockPos anchor) {
        FiveGeneralSelectionScreen selection;
        GameTable2Screen table;
        boolean close;
        Minecraft minecraft = Minecraft.m_91087_();
        Screen screen = minecraft.f_91080_;
        boolean bl = close = screen instanceof GameTable2Screen && (table = (GameTable2Screen)screen).matches(anchor) || (screen = minecraft.f_91080_) instanceof FiveGeneralSelectionScreen && (selection = (FiveGeneralSelectionScreen)screen).matches(anchor);
        if (close) {
            minecraft.m_91152_(null);
        }
        if (minecraft.f_91074_ != null) {
            minecraft.f_91074_.m_5661_((Component)Component.m_237115_((String)"message.sanguosha.military.disbanded"), false);
        }
        pendingMilitaryRoom = null;
    }

    public static void openHealthCard(InteractionHand hand, int health) {
        Minecraft minecraft = Minecraft.m_91087_();
        if (minecraft.f_91074_ != null) {
            minecraft.m_91152_((Screen)new HealthCardScreen(hand, health));
        }
    }

    public static void openHandPouchSession(UUID token, String name, int count) {
        Minecraft minecraft = Minecraft.m_91087_();
        if (minecraft.f_91074_ != null) {
            minecraft.m_91152_((Screen)new HandPouchActionScreen(token, name, count));
        }
    }

    public static void openDeckDrawOptions(int serverMask) {
        Minecraft minecraft = Minecraft.m_91087_();
        if (minecraft.f_91074_ != null) {
            minecraft.m_91152_((Screen)new DeckDrawOptionsScreen(serverMask));
        }
    }

    private record PendingMilitaryRoom(BlockPos anchor, boolean owner, boolean ready, int phase, List<String> members) {
        private void apply(GameTable2Screen screen) {
            screen.updateRoom(this.anchor, this.owner, this.ready, this.phase, this.members);
        }
    }
}

