/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.context.UseOnContext
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 */
package cn.solo.sanguosha.item;

import cn.solo.sanguosha.entity.GroundCardEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class CardDeckItem
extends Item {
    private static final String CARDS_TAG = "DeckCards";
    private final DeckType deckType;

    public CardDeckItem(DeckType deckType, Item.Properties properties) {
        super(properties);
        this.deckType = deckType;
    }

    public DeckType deckType() {
        return this.deckType;
    }

    private static List<ItemStack> readSavedCards(ItemStack stack) {
        if (!(stack.m_41720_() instanceof CardDeckItem)) {
            return null;
        }
        CompoundTag tag = stack.m_41783_();
        if (tag == null || !tag.m_128425_(CARDS_TAG, 9)) {
            return null;
        }
        ListTag list = tag.m_128437_(CARDS_TAG, 10);
        ArrayList<ItemStack> result = new ArrayList<ItemStack>(list.size());
        for (int i = 0; i < list.size(); ++i) {
            ItemStack card = ItemStack.m_41712_((CompoundTag)list.m_128728_(i));
            if (!GroundCardEntity.isCollectibleCard(card)) {
                return null;
            }
            result.add(card.m_255036_(1));
        }
        return result;
    }

    public Component m_7626_(ItemStack stack) {
        return Component.m_237115_((String)(this.deckType == DeckType.IDENTITY ? "item.sanguosha.identity_deck" : (this.deckType == DeckType.GENERAL ? "item.sanguosha.general_deck" : "item.sanguosha.non_identity_deck")));
    }

    public InteractionResult m_6225_(UseOnContext context) {
        if (context.m_43719_() != Direction.UP) {
            return InteractionResult.PASS;
        }
        Level level = context.m_43725_();
        Vec3 location = context.m_43720_();
        if (!level.f_46443_) {
            GroundCardEntity deck;
            List<ItemStack> saved = CardDeckItem.readSavedCards(context.m_43722_());
            if (saved != null) {
                deck = GroundCardEntity.deckFromCards(level, location.f_82479_, location.f_82480_ + 0.0125, location.f_82481_, saved, this.deckType, context.m_43723_() == null ? 0.0f : context.m_43723_().m_146908_());
            } else {
                deck = GroundCardEntity.emptyDeck(level, location.f_82479_, location.f_82480_ + 0.0125, location.f_82481_, this.deckType, context.m_43723_() == null ? 0.0f : context.m_43723_().m_146908_());
            }
            if (!level.m_7967_((Entity)deck)) {
                return InteractionResult.FAIL;
            }
            if (context.m_43723_() == null || !context.m_43723_().m_150110_().f_35937_) {
                context.m_43722_().m_41774_(1);
            }
        }
        return InteractionResult.m_19078_((boolean)level.f_46443_);
    }

    public static enum DeckType {
        IDENTITY,
        NON_IDENTITY,
        GENERAL;

    }
}

