/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package cn.solo.sanguosha.network;

import cn.solo.sanguosha.entity.GroundCardEntity;
import cn.solo.sanguosha.item.HandContainerItem;
import cn.solo.sanguosha.network.ModNetwork;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

public record PlaceHandCardC2SPacket(InteractionHand hand, int index) {
    public static void encode(PlaceHandCardC2SPacket packet, FriendlyByteBuf buffer) {
        buffer.m_130068_((Enum)packet.hand);
        buffer.m_130130_(packet.index);
    }

    public static PlaceHandCardC2SPacket decode(FriendlyByteBuf buffer) {
        return new PlaceHandCardC2SPacket((InteractionHand)buffer.m_130066_(InteractionHand.class), buffer.m_130242_());
    }

    public static void handle(PlaceHandCardC2SPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !player.m_6084_() || player.m_5833_()) {
                return;
            }
            ItemStack held = player.m_21120_(packet.hand);
            if (!(held.m_41720_() instanceof HandContainerItem)) {
                return;
            }
            if (packet.index != HandContainerItem.selected(held)) {
                return;
            }
            ItemStack card = HandContainerItem.get(held, packet.index);
            if (!HandContainerItem.isCard(card) || card.m_41613_() != 1) {
                return;
            }
            Vec3 eye = player.m_146892_();
            Vec3 end = eye.m_82549_(player.m_20252_(1.0f).m_82490_(5.0));
            BlockHitResult hit = player.m_9236_().m_45547_(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, (Entity)player));
            if (hit.m_6662_() != HitResult.Type.BLOCK) {
                return;
            }
            Vec3 target = hit.m_82450_();
            double maxDistance = 5.0;
            if (eye.m_82557_(target) > maxDistance * maxDistance + 0.01) {
                return;
            }
            GroundCardEntity entity = new GroundCardEntity(player.m_9236_(), target.f_82479_, target.f_82480_ + 0.0125, target.f_82481_, card, player.m_146908_());
            if (!player.m_9236_().m_7967_((Entity)entity)) {
                return;
            }
            ItemStack removed = HandContainerItem.remove(held, packet.index);
            if (removed.m_41619_()) {
                entity.m_146870_();
                return;
            }
            ModNetwork.syncHeldContainer(player);
        });
        context.setPacketHandled(true);
    }
}

