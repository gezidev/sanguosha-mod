/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package cn.solo.sanguosha.network;

import cn.solo.sanguosha.game.GameRoomManager;
import cn.solo.sanguosha.registry.ModBlocks;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

public record OpenGameTable2C2SPacket(BlockPos pos) {
    private static final double MAX_DISTANCE = 6.0;

    public static void encode(OpenGameTable2C2SPacket packet, FriendlyByteBuf buffer) {
        buffer.m_130064_(packet.pos);
    }

    public static OpenGameTable2C2SPacket decode(FriendlyByteBuf buffer) {
        return new OpenGameTable2C2SPacket(buffer.m_130135_());
    }

    public static void handle(OpenGameTable2C2SPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !player.m_6084_() || player.m_5833_()) {
                return;
            }
            if (!player.m_9236_().m_46749_(packet.pos) || !player.m_9236_().m_8055_(packet.pos).m_60713_((Block)ModBlocks.GAME_TABLE_2.get())) {
                return;
            }
            Vec3 center = Vec3.m_82512_((Vec3i)packet.pos);
            if (player.m_146892_().m_82557_(center) > 36.0) {
                return;
            }
            Vec3 eye = player.m_146892_();
            Vec3 lookEnd = eye.m_82549_(player.m_20154_().m_82490_(6.0));
            BlockHitResult hit = player.m_9236_().m_45547_(new ClipContext(eye, lookEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)player));
            if (hit.m_6662_() != HitResult.Type.BLOCK || !hit.m_82425_().equals((Object)packet.pos)) {
                return;
            }
            GameRoomManager.open(player, packet.pos);
        });
        context.setPacketHandled(true);
    }
}

