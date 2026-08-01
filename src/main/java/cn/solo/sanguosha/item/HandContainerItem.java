package cn.solo.sanguosha.item;

import cn.solo.sanguosha.item.PlaceableCardItem;
import cn.solo.sanguosha.menu.HandContainerMenu;
import cn.solo.sanguosha.network.ModNetwork;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public final class HandContainerItem
extends Item {
    public static final String CARDS_TAG = "HandCards";
    public static final String SELECTED_TAG = "SelectedCard";
    public static final int MAX_CARDS = 160;

    public HandContainerItem(Item.Properties properties) {
        super(properties);
    }

    public static boolean isCard(ItemStack stack) {
        return !stack.m_41619_() && stack.m_41720_() instanceof PlaceableCardItem;
    }

    public static List<ItemStack> cards(ItemStack container) {
        CompoundTag tag = container.m_41783_();
        if (!(container.m_41720_() instanceof HandContainerItem) || tag == null || !tag.m_128425_(CARDS_TAG, 9)) {
            return Collections.emptyList();
        }
        ListTag stored = tag.m_128437_(CARDS_TAG, 10);
        ArrayList<ItemStack> result = new ArrayList<ItemStack>(Math.min(stored.size(), 160));
        for (int i = 0; i < stored.size() && result.size() < 160; ++i) {
            ItemStack card = ItemStack.m_41712_((CompoundTag)stored.m_128728_(i));
            if (!HandContainerItem.isCard(card)) continue;
            result.add(card.m_255036_(1));
        }
        return Collections.unmodifiableList(result);
    }

    public static int size(ItemStack container) {
        return HandContainerItem.cards(container).size();
    }

    public static ItemStack get(ItemStack container, int index) {
        List<ItemStack> cards = HandContainerItem.cards(container);
        return index >= 0 && index < cards.size() ? cards.get(index).m_41777_() : ItemStack.f_41583_;
    }

    public static int selected(ItemStack container) {
        int count = HandContainerItem.size(container);
        if (count == 0) {
            return 0;
        }
        CompoundTag tag = container.m_41783_();
        int selected = tag == null ? 0 : tag.m_128451_(SELECTED_TAG);
        return Math.max(0, Math.min(count - 1, selected));
    }

    public static boolean setSelected(ItemStack container, int index) {
        int count = HandContainerItem.size(container);
        if (count == 0) {
            container.m_41784_().m_128405_(SELECTED_TAG, 0);
            return index == 0;
        }
        if (index < 0 || index >= count) {
            return false;
        }
        container.m_41784_().m_128405_(SELECTED_TAG, index);
        return true;
    }

    public static boolean cycle(ItemStack container, int delta) {
        int count = HandContainerItem.size(container);
        if (count == 0 || delta == 0) {
            return false;
        }
        return HandContainerItem.setSelected(container, Math.floorMod(HandContainerItem.selected(container) + Integer.signum(delta), count));
    }

    public static boolean add(ItemStack container, ItemStack card) {
        if (!(container.m_41720_() instanceof HandContainerItem) || !HandContainerItem.isCard(card)) {
            return false;
        }
        ArrayList<ItemStack> current = new ArrayList<ItemStack>(HandContainerItem.cards(container));
        if (current.size() >= 160) {
            return false;
        }
        current.add(card.m_255036_(1));
        HandContainerItem.write(container, current, HandContainerItem.selected(container));
        return true;
    }

    public static ItemStack remove(ItemStack container, int index) {
        ArrayList<ItemStack> current = new ArrayList<ItemStack>(HandContainerItem.cards(container));
        if (index < 0 || index >= current.size()) {
            return ItemStack.f_41583_;
        }
        ItemStack removed = ((ItemStack)current.remove(index)).m_41777_();
        int next = current.isEmpty() ? 0 : Math.min(index, current.size() - 1);
        HandContainerItem.write(container, current, next);
        return removed;
    }

    public static boolean insert(ItemStack container, int index, ItemStack card) {
        if (!(container.m_41720_() instanceof HandContainerItem) || !HandContainerItem.isCard(card)) {
            return false;
        }
        ArrayList<ItemStack> current = new ArrayList<ItemStack>(HandContainerItem.cards(container));
        if (current.size() >= 160 || index < 0 || index > current.size()) {
            return false;
        }
        current.add(index, card.m_255036_(1));
        HandContainerItem.write(container, current, Math.min(HandContainerItem.selected(container), current.size() - 1));
        return true;
    }

    private static void write(ItemStack container, List<ItemStack> cards, int selected) {
        HandContainerItem.writeSlots(container, cards, selected);
    }

    public static void writeSlots(ItemStack container, List<ItemStack> slots) {
        HandContainerItem.writeSlots(container, slots, HandContainerItem.selected(container));
    }

    private static void writeSlots(ItemStack container, List<ItemStack> slots, int selected) {
        if (!(container.m_41720_() instanceof HandContainerItem)) {
            return;
        }
        ListTag stored = new ListTag();
        for (ItemStack card : slots) {
            if (stored.size() >= 160) break;
            if (!HandContainerItem.isCard(card)) continue;
            stored.add(card.m_255036_(1).m_41739_(new CompoundTag()));
        }
        CompoundTag tag = container.m_41784_();
        tag.m_128365_(CARDS_TAG, (Tag)stored);
        tag.m_128405_(SELECTED_TAG, stored.isEmpty() ? 0 : Math.max(0, Math.min(stored.size() - 1, selected)));
    }

    public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
        ItemStack container = player.m_21120_(hand);
        if (!player.m_6047_()) {
            if (HandContainerItem.size(container) == 0) {
                return InteractionResultHolder.m_19098_(container);
            }
            if (level.f_46443_) {
                ModNetwork.placeSelectedCard(hand, HandContainerItem.selected(container));
            }
            return InteractionResultHolder.m_19092_(container, (boolean)level.f_46443_);
        }
        if (!level.f_46443_ && player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)player;
            NetworkHooks.openScreen((ServerPlayer)serverPlayer, (MenuProvider)new SimpleMenuProvider((id, inventory, ignored) -> new HandContainerMenu(id, inventory, hand, container), (Component)Component.m_237115_((String)"screen.sanguosha.hand_container")), buffer -> buffer.m_130068_((Enum)hand));
        }
        return InteractionResultHolder.m_19092_(container, (boolean)level.f_46443_);
    }

    public void m_7373_(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        lines.add((Component)Component.m_237110_((String)"tooltip.sanguosha.hand_container.count", (Object[])new Object[]{HandContainerItem.size(stack)}).m_130940_(ChatFormatting.GRAY));
        lines.add((Component)Component.m_237115_((String)"tooltip.sanguosha.hand_container.controls").m_130940_(ChatFormatting.DARK_GRAY));
    }
}

