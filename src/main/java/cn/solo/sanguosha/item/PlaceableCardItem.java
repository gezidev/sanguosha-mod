/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.context.UseOnContext
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.client.extensions.common.IClientItemExtensions
 */
package cn.solo.sanguosha.item;

import cn.solo.sanguosha.client.CardItemClientExtension;
import cn.solo.sanguosha.entity.GroundCardEntity;
import cn.solo.sanguosha.item.IdentityCardItem;
import java.util.function.Consumer;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public class PlaceableCardItem
extends Item {
    public static final String FACE_DOWN_TAG = "SanguoshaFaceDown";
    private static final int FLIP_COOLDOWN_TICKS = 5;

    public PlaceableCardItem(Item.Properties properties) {
        super(properties);
    }

    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(CardItemClientExtension.INSTANCE);
    }

    public static boolean isFaceDown(ItemStack stack) {
        CompoundTag tag = stack.m_41783_();
        if (tag != null && tag.m_128441_(FACE_DOWN_TAG)) {
            return tag.m_128471_(FACE_DOWN_TAG);
        }
        return stack.m_41720_() instanceof IdentityCardItem;
    }

    public static boolean canFlip(ItemStack stack) {
        PlaceableCardItem card;
        Item item;
        return !stack.m_41619_() && (item = stack.m_41720_()) instanceof PlaceableCardItem && (card = (PlaceableCardItem)item).canFlipWhenPlaced();
    }

    protected boolean canFlipWhenPlaced() {
        return true;
    }

    public static void toggleFace(ItemStack stack) {
        stack.m_41784_().m_128379_(FACE_DOWN_TAG, !PlaceableCardItem.isFaceDown(stack));
    }

    public static void toggleFaceAndNotify(ItemStack stack, Player player, boolean clientSide) {
        PlaceableCardItem.toggleFace(stack);
        if (!clientSide) {
            String face = PlaceableCardItem.isFaceDown(stack) ? "\u80cc\u9762" : "\u6b63\u9762";
            player.m_5661_((Component)Component.m_237113_((String)("\u5361\u724c\u5df2\u7ffb\u5230" + face)), true);
        }
    }

    public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.m_21120_(hand);
        if (player.m_6047_()) {
            if (!level.f_46443_ && player instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer)player;
                if (stack.m_41720_() instanceof PlaceableCardItem && !player.m_36335_().m_41519_(stack.m_41720_())) {
                    PlaceableCardItem.toggleFaceAndNotify(stack, player, false);
                    player.m_36335_().m_41524_(stack.m_41720_(), 5);
                    player.m_150109_().m_6596_();
                    serverPlayer.f_36095_.m_38946_();
                    if (serverPlayer.f_36096_ != serverPlayer.f_36095_) {
                        serverPlayer.f_36096_.m_38946_();
                    }
                }
            }
            return InteractionResultHolder.m_19092_(stack, (boolean)level.f_46443_);
        }
        return InteractionResultHolder.m_19098_(stack);
    }

    public InteractionResult m_6225_(UseOnContext context) {
        Player player = context.m_43723_();
        if (player != null && player.m_6047_()) {
            this.m_7203_(context.m_43725_(), player, context.m_43724_());
            return InteractionResult.m_19078_((boolean)context.m_43725_().f_46443_);
        }
        if (context.m_43719_() != Direction.UP) {
            return InteractionResult.PASS;
        }
        Level level = context.m_43725_();
        Vec3 location = context.m_43720_();
        if (!level.f_46443_) {
            GroundCardEntity card;
            boolean placed;
            ItemStack placedStack = context.m_43722_().m_255036_(1);
            CompoundTag placedTag = placedStack.m_41783_();
            if (!(this instanceof IdentityCardItem || placedTag != null && placedTag.m_128441_(FACE_DOWN_TAG))) {
                placedStack.m_41784_().m_128379_(FACE_DOWN_TAG, false);
            }
            if ((placed = level.m_7967_((Entity)(card = new GroundCardEntity(level, location.f_82479_, location.f_82480_ + 0.0125, location.f_82481_, placedStack, player == null ? 0.0f : player.m_146908_())))) && (player == null || !player.m_150110_().f_35937_)) {
                context.m_43722_().m_41774_(1);
            }
            if (!placed) {
                return InteractionResult.FAIL;
            }
        }
        return InteractionResult.m_19078_((boolean)level.f_46443_);
    }
}

