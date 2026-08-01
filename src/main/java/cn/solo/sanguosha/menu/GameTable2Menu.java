package cn.solo.sanguosha.menu;

import cn.solo.sanguosha.block.TableBlockEntity;
import cn.solo.sanguosha.game.GameRoomManager;
import cn.solo.sanguosha.item.HealthCardItem;
import cn.solo.sanguosha.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public final class GameTable2Menu
extends AbstractContainerMenu {
    public static final int TABLE_SLOTS = 7;
    public static final int HEALTH_SLOT = 5;
    private static final int PLAYER_SLOTS = 36;
    private final TableBlockEntity table;
    private final ContainerLevelAccess access;
    private final BlockPos clickedPos;
    private final BlockPos roomAnchor;
    private final Player viewer;

    public GameTable2Menu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, buffer.m_130135_(), buffer.m_130135_());
    }

    public GameTable2Menu(int id, Inventory inventory, TableBlockEntity table) {
        this(id, inventory, table.m_58899_(), GameRoomManager.clientOrServerAnchor(inventory.f_35978_, table.m_58899_()));
    }

    public GameTable2Menu(int id, Inventory inventory, BlockPos clickedPos, BlockPos roomAnchor) {
        super((MenuType)ModMenus.GAME_TABLE_2.get(), id);
        TableBlockEntity found;
        this.clickedPos = clickedPos.m_7949_();
        this.roomAnchor = roomAnchor.m_7949_();
        this.viewer = inventory.f_35978_;
        BlockEntity blockEntity = inventory.f_35978_.m_9236_().m_7702_(clickedPos);
        this.table = blockEntity instanceof TableBlockEntity ? (found = (TableBlockEntity)blockEntity) : null;
        this.access = ContainerLevelAccess.m_39289_((Level)inventory.f_35978_.m_9236_(), (BlockPos)clickedPos);
        if (this.table != null) {
            for (int slot = 0; slot < 7; ++slot) {
                this.m_38897_((Slot)new TableSlot(this.table, slot, 13 + slot * 25, 38));
            }
        } else {
            ItemStackHandler empty = new ItemStackHandler(7);
            for (int slot = 0; slot < 7; ++slot) {
                this.m_38897_((Slot)new ReadOnlyTableSlot(empty, slot, 13 + slot * 25, 38));
            }
        }
        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 9; ++column) {
                this.m_38897_(new Slot((Container)inventory, column + row * 9 + 9, 15 + column * 18, 86 + row * 18));
            }
        }
        for (int column = 0; column < 9; ++column) {
            this.m_38897_(new Slot((Container)inventory, column, 15 + column * 18, 144));
        }
    }

    public BlockPos blockPos() {
        return this.clickedPos;
    }

    public BlockPos roomAnchor() {
        return this.roomAnchor;
    }

    public boolean canFlipIdentity() {
        return this.table != null && this.table.canFlipIdentity();
    }

    public boolean identityFaceUp() {
        return this.table == null || this.table.isFaceUp();
    }

    public boolean isSlotEmpty(int slot) {
        return this.table == null || slot < 0 || slot >= 7 || this.table.items().getStackInSlot(slot).m_41619_();
    }

    public int getRotation(int slot) {
        return this.table == null ? 0 : this.table.getRotation(slot);
    }

    @Deprecated
    public boolean isHorizontal(int slot) {
        return this.getRotation(slot) != 0;
    }

    public boolean hasAuthoritativeRoom() {
        return GameRoomManager.menuHasRoom(this.viewer, this.clickedPos, this.roomAnchor);
    }

    public int healthValue() {
        ItemStack stack = this.table == null ? ItemStack.f_41583_ : this.table.items().getStackInSlot(5);
        return stack.m_41720_() instanceof HealthCardItem ? HealthCardItem.getHealth(stack) : 1;
    }

    public boolean hasHealthCard() {
        return this.table != null && GameRoomManager.isHealthCard(this.table.items().getStackInSlot(5));
    }

    public boolean m_6366_(Player player, int id) {
        ServerPlayer server;
        if (id != 0 || player.m_9236_().f_46443_) {
            return false;
        }
        return player instanceof ServerPlayer && GameRoomManager.flipIdentity(server = (ServerPlayer)player, this.clickedPos, this.roomAnchor);
    }

    public boolean m_6875_(Player player) {
        return (Boolean)this.access.m_39299_((level, target) -> GameRoomManager.isTableBlock(level, target) && player.m_20275_((double)target.m_123341_() + 0.5, (double)target.m_123342_() + 0.5, (double)target.m_123343_() + 0.5) <= 64.0, (Object)true);
    }

    public ItemStack m_7648_(Player player, int index) {
        int target;
        if (index < 0 || index >= this.f_38839_.size()) {
            return ItemStack.f_41583_;
        }
        Slot source = (Slot)this.f_38839_.get(index);
        if (!source.m_6657_() || !source.m_8010_(player)) {
            return ItemStack.f_41583_;
        }
        ItemStack original = source.m_7993_().m_41777_();
        ItemStack moving = source.m_7993_();
        if (index < 7 ? !this.m_38903_(moving, 7, 43, true) : (target = GameTable2Menu.matchingSlot(moving)) < 0 || !((Slot)this.f_38839_.get(target)).m_5857_(moving) || !this.m_38903_(moving, target, target + 1, false)) {
            return ItemStack.f_41583_;
        }
        if (moving.m_41619_()) {
            source.m_5852_(ItemStack.f_41583_);
        } else {
            source.m_6654_();
        }
        source.m_142406_(player, moving);
        return original;
    }

    private static int matchingSlot(ItemStack stack) {
        for (int slot = 0; slot < 7; ++slot) {
            if (!TableBlockEntity.isItemValid(slot, stack.m_255036_(1))) continue;
            return slot;
        }
        return -1;
    }

    private final class TableSlot
    extends SlotItemHandler {
        private TableSlot(TableBlockEntity t, int s, int x, int y) {
            super((IItemHandler)t.items(), s, x, y);
        }

        public boolean m_5857_(ItemStack stack) {
            return GameTable2Menu.this.hasAuthoritativeRoom() && stack.m_41613_() >= 1 && TableBlockEntity.isItemValid(this.getSlotIndex(), stack.m_255036_(1));
        }

        public boolean m_8010_(Player player) {
            return GameTable2Menu.this.hasAuthoritativeRoom();
        }

        public int m_6641_() {
            return 1;
        }

        public int m_5866_(ItemStack stack) {
            return 1;
        }

        public void m_6654_() {
            super.m_6654_();
            if (this.getSlotIndex() == 5 && GameTable2Menu.this.table != null && !this.m_7993_().m_41619_()) {
                GameTable2Menu.this.table.assignHealthOwner(GameTable2Menu.this.viewer.m_20148_());
            }
        }
    }

    private static final class ReadOnlyTableSlot
    extends SlotItemHandler {
        private ReadOnlyTableSlot(ItemStackHandler e, int s, int x, int y) {
            super((IItemHandler)e, s, x, y);
        }

        public boolean m_5857_(ItemStack s) {
            return false;
        }

        public boolean m_8010_(Player p) {
            return false;
        }
    }
}

