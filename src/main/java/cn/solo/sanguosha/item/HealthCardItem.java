/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.Nullable
 */
package cn.solo.sanguosha.item;

import cn.solo.sanguosha.client.ClientScreens;
import cn.solo.sanguosha.item.PlaceableCardItem;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class HealthCardItem
extends PlaceableCardItem {
    public static final String HEALTH_TAG = "Health";

    public HealthCardItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    protected boolean canFlipWhenPlaced() {
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.m_21120_(hand);
        if (player.m_6047_()) {
            return super.m_7203_(level, player, hand);
        }
        if (level.f_46443_) {
            ClientScreens.openHealthCard(hand, HealthCardItem.getHealth(stack));
        }
        return InteractionResultHolder.m_19092_(stack, (boolean)level.f_46443_);
    }

    public static int getHealth(ItemStack stack) {
        CompoundTag tag = stack.m_41783_();
        return tag != null ? tag.m_128451_(HEALTH_TAG) : 1;
    }

    public static void setHealth(ItemStack stack, int health) {
        stack.m_41784_().m_128405_(HEALTH_TAG, Math.max(1, Math.min(5, health)));
    }

    public Component m_7626_(ItemStack stack) {
        int hp = HealthCardItem.getHealth(stack);
        return Component.m_237113_((String)("\u8840\u91cf\u724c (" + hp + "\u8840)"));
    }

    public void m_7373_(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        int hp = HealthCardItem.getHealth(stack);
        lines.add((Component)Component.m_237113_((String)("\u5f53\u524d\u8840\u91cf\uff1a" + hp)).m_130940_(ChatFormatting.RED));
        lines.add((Component)Component.m_237113_((String)"\u53f3\u952e\u6253\u5f00\u8bbe\u7f6e\u754c\u9762").m_130940_(ChatFormatting.GRAY));
        super.m_7373_(stack, level, lines, flag);
    }
}

