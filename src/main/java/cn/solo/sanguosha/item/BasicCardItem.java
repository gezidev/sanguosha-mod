package cn.solo.sanguosha.item;

import cn.solo.sanguosha.item.PlaceableCardItem;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class BasicCardItem
extends PlaceableCardItem {
    public BasicCardItem(Item.Properties properties) {
        super(properties);
    }

    public void m_7373_(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        lines.add((Component)Component.m_237115_((String)"tooltip.sanguosha.ground_controls").m_130940_(ChatFormatting.DARK_GRAY));
    }
}

