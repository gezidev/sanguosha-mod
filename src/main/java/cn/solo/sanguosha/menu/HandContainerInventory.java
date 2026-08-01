/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.NonNullList
 *  net.minecraft.world.Container
 *  net.minecraft.world.ContainerHelper
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 */
package cn.solo.sanguosha.menu;

import cn.solo.sanguosha.item.HandContainerItem;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

final class HandContainerInventory
implements Container {
    private final NonNullList<ItemStack> items = NonNullList.m_122780_(160, ItemStack.f_41583_);
    private final ItemStack container;
    private boolean loading;

    HandContainerInventory(ItemStack container) {
        this.container = container;
        this.loading = true;
        List<ItemStack> stored = HandContainerItem.cards(container);
        for (int i = 0; i < stored.size() && i < this.items.size(); ++i) {
            this.items.set(i, stored.get(i).m_255036_(1));
        }
        this.loading = false;
    }

    boolean isBoundTo(ItemStack current) {
        return this.container.m_41619_() || current == this.container;
    }

    public int m_6643_() {
        return this.items.size();
    }

    public boolean m_7983_() {
        return this.items.stream().allMatch(ItemStack::m_41619_);
    }

    public ItemStack m_8020_(int slot) {
        return slot >= 0 && slot < this.items.size() ? (ItemStack)this.items.get(slot) : ItemStack.f_41583_;
    }

    public ItemStack m_7407_(int slot, int amount) {
        ItemStack result = ContainerHelper.m_18969_(this.items, (int)slot, (int)amount);
        if (!result.m_41619_()) {
            this.m_6596_();
        }
        return result;
    }

    public ItemStack m_8016_(int slot) {
        ItemStack result = ContainerHelper.m_18966_(this.items, (int)slot);
        if (!result.m_41619_()) {
            this.m_6596_();
        }
        return result;
    }

    public void m_6836_(int slot, ItemStack stack) {
        if (slot < 0 || slot >= this.items.size()) {
            return;
        }
        this.items.set(slot, (HandContainerItem.isCard(stack) ? stack.m_255036_(1) : ItemStack.f_41583_));
        this.m_6596_();
    }

    public void m_6596_() {
        if (!this.loading && !this.container.m_41619_()) {
            HandContainerItem.writeSlots(this.container, this.items);
        }
    }

    public boolean m_6542_(Player player) {
        return true;
    }

    public void m_6211_() {
        for (int i = 0; i < this.items.size(); ++i) {
            this.items.set(i, ItemStack.f_41583_);
        }
        this.m_6596_();
    }

    public boolean m_7013_(int slot, ItemStack stack) {
        return HandContainerItem.isCard(stack);
    }
}

