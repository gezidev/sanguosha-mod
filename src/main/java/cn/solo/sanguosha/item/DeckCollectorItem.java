/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.item.context.UseOnContext
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  org.jetbrains.annotations.Nullable
 */
package cn.solo.sanguosha.item;

import cn.solo.sanguosha.entity.GroundCardEntity;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class DeckCollectorItem
extends Item {
    public static final double SEARCH_RADIUS = 20.0;
    public static final int MAX_STORED_CARDS = 4096;
    public static final String CARDS_TAG = "CollectedCards";

    public DeckCollectorItem(Item.Properties properties) {
        super(properties);
    }

    private static List<ItemStack> readValidated(ItemStack collector) {
        if (!(collector.m_41720_() instanceof DeckCollectorItem)) {
            return null;
        }
        CompoundTag tag = collector.m_41783_();
        if (tag == null || !tag.m_128425_(CARDS_TAG, 9)) {
            return new ArrayList<ItemStack>();
        }
        ListTag list = tag.m_128437_(CARDS_TAG, 10);
        if (list.size() > 4096) {
            return null;
        }
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

    private static void write(ItemStack collector, List<ItemStack> cards) {
        ListTag stored = new ListTag();
        for (ItemStack card : cards) {
            stored.add(card.m_255036_(1).m_41739_(new CompoundTag()));
        }
        CompoundTag tag = collector.m_41784_();
        if (stored.isEmpty()) {
            tag.m_128473_(CARDS_TAG);
        } else {
            tag.m_128365_(CARDS_TAG, (Tag)stored);
        }
        if (tag.m_128456_()) {
            collector.m_41751_(null);
        }
    }

    public static int storedCount(ItemStack collector) {
        List<ItemStack> cards = DeckCollectorItem.readValidated(collector);
        return cards == null ? 0 : cards.size();
    }

    private static InteractionResult collect(ServerPlayer player, ItemStack collector) {
        List<ItemStack> stored = DeckCollectorItem.readValidated(collector);
        if (stored == null) {
            player.m_5661_((Component)Component.m_237115_((String)"message.sanguosha.deck_collector.invalid_nbt"), true);
            return InteractionResult.FAIL;
        }
        ServerLevel level = player.m_284548_();
        Vec3 center = player.m_20182_();
        AABB bounds = player.m_20191_().m_82400_(20.0);
        List<GroundCardEntity> nearby = level.m_6443_(GroundCardEntity.class, bounds, entity -> entity.m_6084_() && entity.m_20238_(center) <= 400.0);
        nearby.sort(Comparator.comparingDouble(entity -> entity.m_20238_(center)));
        ArrayList<GroundCardEntity> acceptedEntities = new ArrayList<GroundCardEntity>();
        int collected = 0;
        for (GroundCardEntity entity2 : nearby) {
            List<ItemStack> pile = entity2.copyCollectibleCards();
            if (pile.isEmpty() || stored.size() + pile.size() > 4096) continue;
            stored.addAll(pile);
            acceptedEntities.add(entity2);
            collected += pile.size();
        }
        if (collected == 0) {
            player.m_5661_((Component)Component.m_237115_((String)"message.sanguosha.deck_collector.none"), true);
            return InteractionResult.PASS;
        }
        DeckCollectorItem.write(collector, stored);
        acceptedEntities.forEach(Entity2 -> Entity2.m_146870_());
        player.m_5661_((Component)Component.m_237110_((String)"message.sanguosha.deck_collector.collected", (Object[])new Object[]{collected}), true);
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult deploy(ServerPlayer player, ItemStack collector, Vec3 position) {
        List<ItemStack> stored = DeckCollectorItem.readValidated(collector);
        if (stored == null) {
            player.m_5661_((Component)Component.m_237115_((String)"message.sanguosha.deck_collector.invalid_nbt"), true);
            return InteractionResult.FAIL;
        }
        if (stored.isEmpty()) {
            player.m_5661_((Component)Component.m_237115_((String)"message.sanguosha.deck_collector.empty"), true);
            return InteractionResult.PASS;
        }
        ArrayList<ItemStack> identity = new ArrayList<ItemStack>();
        ArrayList<ItemStack> standard = new ArrayList<ItemStack>();
        ArrayList<ItemStack> general = new ArrayList<ItemStack>();
        ArrayList<ItemStack> other = new ArrayList<ItemStack>();
        for (ItemStack card : stored) {
            Item item = card.m_41720_();
            if (item instanceof IdentityCardItem) {
                identity.add(card);
                continue;
            }
            if (item instanceof GeneralCardItem) {
                general.add(card);
                continue;
            }
            if (item instanceof StandardCardItem) {
                standard.add(card);
                continue;
            }
            other.add(card);
        }
        Level level = player.m_9236_();
        float rotation = player.m_146908_();
        int deployed = 0;
        int group = 0;
        double[] dx = {0.0, 1.0, 0.0, 1.0};
        double[] dz = {0.0, 0.0, 1.0, 1.0};
        if (!identity.isEmpty()) {
            GroundCardEntity deck = GroundCardEntity.deckFromCards(level, position.f_82479_ + dx[group], position.f_82480_, position.f_82481_ + dz[group], identity, 1, rotation);
            if (level.m_7967_((Entity)deck)) {
                ++deployed;
            }
            ++group;
        }
        if (!standard.isEmpty()) {
            GroundCardEntity deck = GroundCardEntity.deckFromCards(level, position.f_82479_ + dx[group], position.f_82480_, position.f_82481_ + dz[group], standard, 2, rotation);
            if (level.m_7967_((Entity)deck)) {
                ++deployed;
            }
            ++group;
        }
        if (!general.isEmpty()) {
            GroundCardEntity deck = GroundCardEntity.deckFromCards(level, position.f_82479_ + dx[group], position.f_82480_, position.f_82481_ + dz[group], general, 3, rotation);
            if (level.m_7967_((Entity)deck)) {
                ++deployed;
            }
            ++group;
        }
        if (!other.isEmpty()) {
            GroundCardEntity deck = GroundCardEntity.deckFromCards(level, position.f_82479_ + dx[group], position.f_82480_, position.f_82481_ + dz[group], other, 0, rotation);
            if (level.m_7967_((Entity)deck)) {
                ++deployed;
            }
            ++group;
        }
        if (deployed == 0) {
            player.m_5661_((Component)Component.m_237115_((String)"message.sanguosha.deck_collector.spawn_failed"), true);
            return InteractionResult.FAIL;
        }
        DeckCollectorItem.write(collector, List.of());
        player.m_5661_((Component)Component.m_237110_((String)"message.sanguosha.deck_collector.deployed", (Object[])new Object[]{deployed, stored.size()}), true);
        return InteractionResult.SUCCESS;
    }

    public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
        ItemStack collector = player.m_21120_(hand);
        if (level.f_46443_) {
            return InteractionResultHolder.m_19092_(collector, (boolean)true);
        }
        if (!(player instanceof ServerPlayer)) {
            return InteractionResultHolder.m_19100_(collector);
        }
        ServerPlayer serverPlayer = (ServerPlayer)player;
        InteractionResult result = player.m_6047_() ? DeckCollectorItem.deploy(serverPlayer, collector, player.m_20182_().m_82520_(0.0, 0.0125, 0.0)) : DeckCollectorItem.collect(serverPlayer, collector);
        return new InteractionResultHolder(result, (Object)collector);
    }

    public InteractionResult m_6225_(UseOnContext context) {
        Player player = context.m_43723_();
        if (player == null || !player.m_6047_()) {
            return InteractionResult.PASS;
        }
        if (context.m_43725_().f_46443_) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer)) {
            return InteractionResult.FAIL;
        }
        ServerPlayer serverPlayer = (ServerPlayer)player;
        Vec3 clicked = context.m_43720_().m_82520_(0.0, 0.0125, 0.0);
        return DeckCollectorItem.deploy(serverPlayer, context.m_43722_(), clicked);
    }

    public void m_7373_(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        lines.add((Component)Component.m_237110_((String)"tooltip.sanguosha.deck_collector.count", (Object[])new Object[]{DeckCollectorItem.storedCount(stack)}).m_130940_(ChatFormatting.GRAY));
        lines.add((Component)Component.m_237110_((String)"tooltip.sanguosha.deck_collector.controls", (Object[])new Object[]{20}).m_130940_(ChatFormatting.DARK_GRAY));
    }
}

