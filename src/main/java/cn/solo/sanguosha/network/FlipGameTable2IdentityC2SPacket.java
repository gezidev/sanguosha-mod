/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package cn.solo.sanguosha.network;

import cn.solo.sanguosha.game.GameRoomManager;
import cn.solo.sanguosha.menu.GameTable2Menu;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

public record FlipGameTable2IdentityC2SPacket(BlockPos pos) {
    public static void encode(FlipGameTable2IdentityC2SPacket packet, FriendlyByteBuf buffer) {
        buffer.m_130064_(packet.pos);
    }

    public static FlipGameTable2IdentityC2SPacket decode(FriendlyByteBuf buffer) {
        return new FlipGameTable2IdentityC2SPacket(buffer.m_130135_());
    }

    public static void handle(FlipGameTable2IdentityC2SPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            GameTable2Menu menu;
            ServerPlayer player = context.getSender();
            if (player == null || !player.m_6084_() || player.m_5833_()) {
                return;
            }
            AbstractContainerMenu patt1125$temp = player.f_36096_;
            if (!(patt1125$temp instanceof GameTable2Menu) || !(menu = (GameTable2Menu)patt1125$temp).blockPos().equals((Object)packet.pos)) {
                return;
            }
            GameRoomManager.flipIdentity(player, packet.pos, menu.roomAnchor());
        });
        context.setPacketHandled(true);
    }
}

