/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.client.extensions.common.IClientItemExtensions
 *  net.minecraftforge.registries.RegistryObject
 *  org.jetbrains.annotations.Nullable
 */
package cn.solo.sanguosha.item;

import cn.solo.sanguosha.client.CardItemClientExtension;
import cn.solo.sanguosha.item.PlaceableCardItem;
import cn.solo.sanguosha.item.StandardCardItem;
import cn.solo.sanguosha.registry.ModItems;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

public final class GenericCardItem
extends PlaceableCardItem {
    public static final String CARD_ID = "SanguoshaCardId";
    public static final String SUIT = "SanguoshaSuit";
    public static final String RANK = "SanguoshaRank";
    public static final String NAME = "SanguoshaName";
    public static final String TYPE = "SanguoshaType";
    public static final String EX = "SanguoshaEx";

    public GenericCardItem(Item.Properties properties) {
        super(properties);
    }

    public static ItemStack of(String id, String suit, String rank, String type, boolean ex, int modelData) {
        ItemStack stack = new ItemStack((ItemLike)ModItems.STANDARD_CARD.get());
        CompoundTag tag = stack.m_41784_();
        tag.m_128359_(CARD_ID, id);
        tag.m_128359_(SUIT, suit);
        tag.m_128359_(RANK, rank);
        tag.m_128359_(NAME, id);
        tag.m_128359_(TYPE, type);
        tag.m_128379_(EX, ex);
        tag.m_128405_("CustomModelData", modelData);
        return stack;
    }

    public static ItemStack fromLegacy(ItemStack legacy) {
        Item item = legacy.m_41720_();
        if (!(item instanceof StandardCardItem)) {
            return legacy;
        }
        StandardCardItem old = (StandardCardItem)item;
        ItemStack result = ModItems.createStandardCard(old.legacyId());
        if (legacy.m_41783_() != null && legacy.m_41783_().m_128441_("SanguoshaFaceDown")) {
            result.m_41784_().m_128379_("SanguoshaFaceDown", legacy.m_41783_().m_128471_("SanguoshaFaceDown"));
        }
        return result;
    }

    public static String id(ItemStack stack) {
        return stack.m_41784_().m_128461_(CARD_ID);
    }

    public static ItemStack legacyRenderStack(ItemStack stack) {
        String cardId = GenericCardItem.id(stack);
        int index = 0;
        for (RegistryObject<? extends Item> registered : ModItems.STANDARD_CARDS) {
            StandardCardItem standard;
            Object object = registered.get();
            if (object instanceof StandardCardItem && (standard = (StandardCardItem)((Object)object)).legacyId().equals(cardId)) {
                ItemStack rendered = new ItemStack((ItemLike)standard);
                if (stack.m_41783_() != null && stack.m_41783_().m_128441_("SanguoshaFaceDown")) {
                    rendered.m_41784_().m_128379_("SanguoshaFaceDown", stack.m_41783_().m_128471_("SanguoshaFaceDown"));
                }
                return rendered;
            }
            ++index;
        }
        return stack;
    }

    public Component m_7626_(ItemStack stack) {
        String legacyId = stack.m_41784_().m_128461_(NAME);
        return legacyId.isEmpty() ? Component.m_237115_((String)"item.sanguosha.standard_card") : Component.m_237115_((String)("item.sanguosha." + legacyId));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(CardItemClientExtension.INSTANCE);
    }

    public void m_7373_(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        String suit;
        CompoundTag tag = stack.m_41784_();
        String symbol = switch (suit = tag.m_128461_(SUIT)) {
            case "h" -> "\u2665";
            case "d" -> "\u2666";
            case "s" -> "\u2660";
            default -> "\u2663";
        };
        ChatFormatting color = suit.equals("h") || suit.equals("d") ? ChatFormatting.RED : ChatFormatting.DARK_GRAY;
        lines.add((Component)Component.m_237113_((String)(symbol + tag.m_128461_(RANK) + " \u00b7 " + tag.m_128461_(TYPE) + (tag.m_128471_(EX) ? " \u00b7 EX" : ""))).m_130940_(color));
        lines.add((Component)Component.m_237115_((String)"tooltip.sanguosha.ground_controls").m_130940_(ChatFormatting.DARK_GRAY));
    }
}

