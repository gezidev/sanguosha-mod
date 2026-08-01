/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.Connection
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientGamePacketListener
 *  net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
 *  net.minecraft.world.MenuProvider
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraftforge.common.capabilities.Capability
 *  net.minecraftforge.common.capabilities.ForgeCapabilities
 *  net.minecraftforge.common.util.LazyOptional
 *  net.minecraftforge.items.ItemStackHandler
 *  net.minecraftforge.registries.ForgeRegistries
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package cn.solo.sanguosha.block;

import cn.solo.sanguosha.item.GeneralCardItem;
import cn.solo.sanguosha.item.HealthCardItem;
import cn.solo.sanguosha.item.IdentityCardItem;
import cn.solo.sanguosha.item.StandardCardItem;
import cn.solo.sanguosha.menu.GameTable2Menu;
import java.util.Arrays;
import java.util.BitSet;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class TableBlockEntity
extends BlockEntity
implements MenuProvider {
    public static final int IDENTITY = 0;
    public static final int PLUS_ONE_MOUNT = 1;
    public static final int MINUS_ONE_MOUNT = 2;
    public static final int WEAPON = 3;
    public static final int ARMOR = 4;
    public static final int HEALTH = 5;
    public static final int GENERAL = 6;
    public static final int SLOT_COUNT = 7;
    public static final int NORMAL_SLOT_COUNT = 6;
    public static final String HEALTH_OWNER_TAG = "TableHealthOwner";
    private static final String ROTATIONS_TAG = "Rotations";
    private static final String LEGACY_HORIZONTAL_TAG = "Horizontal";
    private boolean faceUp = true;
    private boolean loading;
    private final byte[] rotations = new byte[7];
    private final boolean[] occupied = new boolean[7];
    private final ItemStackHandler items = new ItemStackHandler(7){

        protected int getStackLimit(int slot, @NotNull ItemStack stack) {
            return 1;
        }

        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return TableBlockEntity.isItemValid(slot, stack);
        }

        protected void onContentsChanged(int slot) {
            if (TableBlockEntity.this.loading) {
                return;
            }
            ItemStack current = this.getStackInSlot(slot);
            boolean firstInsertion = !TableBlockEntity.this.occupied[slot] && !current.m_41619_();
            boolean bl = TableBlockEntity.this.occupied[slot] = !current.m_41619_();
            if (slot == 0 && firstInsertion && current.m_41720_() instanceof IdentityCardItem) {
                current.m_41784_().m_128379_("SanguoshaFaceDown", true);
                TableBlockEntity.this.faceUp = false;
            }
            if (slot == 0) {
                TableBlockEntity.this.enforceIdentityRules();
            }
            if (slot == 5 && !current.m_41619_()) {
                HealthCardItem.setHealth(current, HealthCardItem.getHealth(current));
            }
            TableBlockEntity.this.rotations[slot] = 0;
            TableBlockEntity.this.markAndSync();
        }
    };
    private LazyOptional<ItemStackHandler> itemCapability = LazyOptional.of(() -> this.items);

    protected TableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public ItemStackHandler items() {
        return this.items;
    }

    public int getRotation(int slot) {
        return slot >= 0 && slot < 7 ? Byte.toUnsignedInt(this.rotations[slot]) * 90 : 0;
    }

    @Deprecated
    public boolean isHorizontal(int slot) {
        return this.getRotation(slot) != 0;
    }

    public boolean rotateSlot(int slot) {
        if (this.f_58857_ == null || this.f_58857_.f_46443_ || slot < 0 || slot >= 7 || this.items.getStackInSlot(slot).m_41619_()) {
            return false;
        }
        this.rotations[slot] = (byte)(Byte.toUnsignedInt(this.rotations[slot]) + 1 & 3);
        this.markAndSync();
        return true;
    }

    @Deprecated
    public boolean toggleHorizontal(int slot) {
        return this.rotateSlot(slot);
    }

    public boolean isFaceUp() {
        return this.isLord() || this.faceUp;
    }

    public boolean hasIdentity() {
        return !this.items.getStackInSlot(0).m_41619_();
    }

    public boolean canFlipIdentity() {
        return this.hasIdentity() && !this.isLord();
    }

    public boolean toggleIdentityFace() {
        if (this.f_58857_ == null || this.f_58857_.f_46443_ || !this.canFlipIdentity()) {
            return false;
        }
        this.faceUp = !this.faceUp;
        this.markAndSync();
        return true;
    }

    public void assignHealthOwner(UUID owner) {
        ItemStack stack = this.items.getStackInSlot(5);
        if (!stack.m_41619_() && TableBlockEntity.isItemValid(5, stack)) {
            stack.m_41784_().m_128362_(HEALTH_OWNER_TAG, owner);
            this.markAndSync();
        }
    }

    public static UUID healthOwner(ItemStack stack) {
        return stack.m_41782_() && stack.m_41783_().m_128403_(HEALTH_OWNER_TAG) ? stack.m_41783_().m_128342_(HEALTH_OWNER_TAG) : null;
    }

    public void sync() {
        this.markAndSync();
    }

    private boolean isLord() {
        IdentityCardItem card;
        ItemStack identity = this.items.getStackInSlot(0);
        Item item = identity.m_41720_();
        return item instanceof IdentityCardItem && "lord".equals((card = (IdentityCardItem)item).identity());
    }

    private void enforceIdentityRules() {
        if (this.isLord() || !this.hasIdentity()) {
            this.faceUp = true;
        }
    }

    private void markAndSync() {
        this.m_6596_();
        if (this.f_58857_ != null && !this.f_58857_.f_46443_) {
            BlockState state = this.m_58900_();
            this.f_58857_.m_7260_(this.f_58858_, state, state, 3);
        }
    }

    public static boolean isItemValid(int slot, ItemStack stack) {
        if (stack.m_41619_()) {
            return false;
        }
        if (slot == 5) {
            return stack.m_41720_() instanceof HealthCardItem && "sanguosha:health_card".equals(String.valueOf(ForgeRegistries.ITEMS.getKey(stack.m_41720_())));
        }
        if (slot == 0) {
            return stack.m_41720_() instanceof IdentityCardItem;
        }
        if (slot == 6) {
            return stack.m_41720_() instanceof GeneralCardItem;
        }
        Item item = stack.m_41720_();
        if (!(item instanceof StandardCardItem)) {
            return false;
        }
        StandardCardItem card = (StandardCardItem)item;
        return switch (slot) {
            case 1 -> {
                if (card.equipmentType() == StandardCardItem.EquipmentType.PLUS_ONE_MOUNT) {
                    yield true;
                }
                yield false;
            }
            case 2 -> {
                if (card.equipmentType() == StandardCardItem.EquipmentType.MINUS_ONE_MOUNT) {
                    yield true;
                }
                yield false;
            }
            case 3 -> {
                if (card.equipmentType() == StandardCardItem.EquipmentType.WEAPON) {
                    yield true;
                }
                yield false;
            }
            case 4 -> {
                if (card.equipmentType() == StandardCardItem.EquipmentType.ARMOR) {
                    yield true;
                }
                yield false;
            }
            default -> false;
        };
    }

    protected void m_183515_(CompoundTag tag) {
        super.m_183515_(tag);
        tag.m_128365_("Items", (Tag)this.items.serializeNBT());
        tag.m_128379_("FaceUp", this.isFaceUp());
        byte[] savedRotations = Arrays.copyOf(this.rotations, 7);
        tag.m_128382_(ROTATIONS_TAG, savedRotations);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void m_142466_(CompoundTag tag) {
        super.m_142466_(tag);
        CompoundTag savedItems = tag.m_128469_("Items");
        this.loading = true;
        try {
            for (int slot = 0; slot < 7; ++slot) {
                this.items.setStackInSlot(slot, ItemStack.f_41583_);
            }
            if (savedItems.m_128425_("Items", 9)) {
                ListTag list = savedItems.m_128437_("Items", 10);
                for (int i = 0; i < list.size(); ++i) {
                    CompoundTag entry = list.m_128728_(i);
                    int slot = entry.m_128451_("Slot");
                    if (slot < 0 || slot >= 7) continue;
                    this.items.setStackInSlot(slot, ItemStack.m_41712_((CompoundTag)entry));
                }
            }
        }
        finally {
            this.loading = false;
        }
        this.faceUp = !tag.m_128441_("FaceUp") || tag.m_128471_("FaceUp");
        Arrays.fill(this.rotations, (byte)0);
        if (tag.m_128425_(ROTATIONS_TAG, 7)) {
            byte[] savedRotations = tag.m_128463_(ROTATIONS_TAG);
            for (int slot = 0; slot < Math.min(7, savedRotations.length); ++slot) {
                this.rotations[slot] = (byte)(Byte.toUnsignedInt(savedRotations[slot]) & 3);
            }
        } else if (tag.m_128425_(LEGACY_HORIZONTAL_TAG, 12)) {
            BitSet legacyHorizontal = BitSet.valueOf(tag.m_128467_(LEGACY_HORIZONTAL_TAG));
            for (int slot = 0; slot < 7; ++slot) {
                if (!legacyHorizontal.get(slot)) continue;
                this.rotations[slot] = 1;
            }
        }
        for (int slot = 0; slot < 7; ++slot) {
            ItemStack stack = this.items.getStackInSlot(slot);
            boolean bl = this.occupied[slot] = !stack.m_41619_();
            if (stack.m_41619_()) {
                this.rotations[slot] = 0;
                continue;
            }
            if (stack.m_41613_() == 1) continue;
            stack.m_41764_(1);
        }
        this.enforceIdentityRules();
    }

    public CompoundTag m_5995_() {
        return this.m_187482_();
    }

    public void handleUpdateTag(CompoundTag tag) {
        this.m_142466_(tag);
    }

    @Nullable
    public Packet<ClientGamePacketListener> m_58483_() {
        return ClientboundBlockEntityDataPacket.m_195640_((BlockEntity)this);
    }

    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        if (packet.m_131708_() != null) {
            this.m_142466_(packet.m_131708_());
        }
    }

    public Component m_5446_() {
        return Component.m_237115_((String)"screen.sanguosha.game_table2");
    }

    @Nullable
    public AbstractContainerMenu m_7208_(int id, Inventory inventory, Player player) {
        return new GameTable2Menu(id, inventory, this);
    }

    @NotNull
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return this.itemCapability.cast();
        }
        return super.getCapability(capability, side);
    }

    public void invalidateCaps() {
        super.invalidateCaps();
        this.itemCapability.invalidate();
    }

    public void reviveCaps() {
        super.reviveCaps();
        this.itemCapability = LazyOptional.of(() -> this.items);
    }
}

