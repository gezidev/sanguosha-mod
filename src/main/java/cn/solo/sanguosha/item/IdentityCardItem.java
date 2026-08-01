/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.client.extensions.common.IClientItemExtensions
 *  org.jetbrains.annotations.Nullable
 */
package cn.solo.sanguosha.item;

import cn.solo.sanguosha.client.IdentityCardClientExtension;
import cn.solo.sanguosha.item.PlaceableCardItem;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

public final class IdentityCardItem
extends PlaceableCardItem {
    private final String identity;

    public IdentityCardItem(String identity, Item.Properties properties) {
        super(properties);
        this.identity = identity;
    }

    public String identity() {
        return this.identity;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(IdentityCardClientExtension.INSTANCE);
    }

    public void m_7373_(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        lines.add((Component)Component.m_237115_((String)("tooltip.sanguosha.identity." + this.identity)).m_130940_(ChatFormatting.GRAY));
        lines.add((Component)Component.m_237115_((String)"tooltip.sanguosha.flip").m_130940_(ChatFormatting.DARK_GRAY));
    }
}

