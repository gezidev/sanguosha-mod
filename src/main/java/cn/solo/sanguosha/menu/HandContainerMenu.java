package cn.solo.sanguosha.menu;

import cn.solo.sanguosha.item.HandContainerItem;
import cn.solo.sanguosha.menu.HandContainerInventory;
import cn.solo.sanguosha.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class HandContainerMenu
extends AbstractContainerMenu {
    public static final int COLUMNS = 20;
    public static final int ROWS = 8;
    public static final int CONTAINER_SLOTS = 160;
    private static final int PLAYER_SLOTS = 36;
    private final HandContainerInventory cards;
    private final InteractionHand hand;
    private final int lockedInventorySlot;

    public HandContainerMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, (InteractionHand)buffer.m_130066_(InteractionHand.class), ItemStack.f_41583_, true);
    }

    public HandContainerMenu(int id, Inventory inventory, InteractionHand hand, ItemStack container) {
        this(id, inventory, hand, container, false);
    }

    private HandContainerMenu(int id, Inventory inventory, InteractionHand hand, ItemStack container, boolean clientSide) {
        super((MenuType)ModMenus.HAND_CONTAINER.get(), id);
        int column;
        this.hand = hand;
        this.lockedInventorySlot = hand == InteractionHand.MAIN_HAND ? inventory.f_35977_ : -1;
        this.cards = new HandContainerInventory(clientSide ? ItemStack.f_41583_ : container);
        this.cards.m_5856_(inventory.f_35978_);
        for (int row = 0; row < 8; ++row) {
            for (column = 0; column < 20; ++column) {
                this.m_38897_(new CardSlot(this.cards, row * 20 + column, 9 + column * 18, 18 + row * 18));
            }
        }
        int inventoryY = 176;
        for (int row = 0; row < 3; ++row) {
            for (int column2 = 0; column2 < 9; ++column2) {
                int inventoryIndex = column2 + row * 9 + 9;
                this.m_38897_(this.playerSlot(inventory, inventoryIndex, 108 + column2 * 18, inventoryY + row * 18));
            }
        }
        for (column = 0; column < 9; ++column) {
            this.m_38897_(this.playerSlot(inventory, column, 108 + column * 18, inventoryY + 58));
        }
    }

    private Slot playerSlot(Inventory inventory, int index, int x, int y) {
        return index == this.lockedInventorySlot ? new LockedSlot(inventory, index, x, y) : new Slot((Container)inventory, index, x, y);
    }

    public boolean m_6875_(Player player) {
        ItemStack held = player.m_21120_(this.hand);
        return player.m_6084_() && held.m_41720_() instanceof HandContainerItem && this.cards.isBoundTo(held);
    }

    public ItemStack m_7648_(Player player, int index) {
        if (index < 0 || index >= this.f_38839_.size()) {
            return ItemStack.f_41583_;
        }
        Slot source = (Slot)this.f_38839_.get(index);
        if (!source.m_6657_() || !source.m_8010_(player)) {
            return ItemStack.f_41583_;
        }
        ItemStack sourceStack = source.m_7993_();
        ItemStack original = sourceStack.m_41777_();
        if (index < 160 ? !this.m_38903_(sourceStack, 160, 196, true) : !HandContainerItem.isCard(sourceStack) || !this.m_38903_(sourceStack, 0, 160, false)) {
            return ItemStack.f_41583_;
        }
        if (sourceStack.m_41619_()) {
            source.m_5852_(ItemStack.f_41583_);
        } else {
            source.m_6654_();
        }
        source.m_142406_(player, sourceStack);
        this.cards.m_6596_();
        return original;
    }

    public void m_6877_(Player player) {
        this.cards.m_6596_();
        this.cards.m_5785_(player);
        super.m_6877_(player);
    }

    private static final class CardSlot
    extends Slot {
        private CardSlot(HandContainerInventory inventory, int index, int x, int y) {
            super((Container)inventory, index, x, y);
        }

        public boolean m_5857_(ItemStack stack) {
            return HandContainerItem.isCard(stack);
        }

        public int m_6641_() {
            return 1;
        }

        public int m_5866_(ItemStack stack) {
            return 1;
        }
    }

    private static final class LockedSlot
    extends Slot {
        private LockedSlot(Inventory inventory, int index, int x, int y) {
            super((Container)inventory, index, x, y);
        }

        public boolean m_8010_(Player player) {
            return false;
        }

        public boolean m_5857_(ItemStack stack) {
            return false;
        }
    }
}

