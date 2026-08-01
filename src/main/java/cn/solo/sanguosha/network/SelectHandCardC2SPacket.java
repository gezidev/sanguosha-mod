/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package cn.solo.sanguosha.network;

import cn.solo.sanguosha.item.HandContainerItem;
import cn.solo.sanguosha.network.ModNetwork;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public record SelectHandCardC2SPacket(InteractionHand hand, int index) {
    public static void encode(SelectHandCardC2SPacket packet, FriendlyByteBuf buffer) {
        buffer.m_130068_((Enum)packet.hand);
        buffer.m_130130_(packet.index);
    }

    public static SelectHandCardC2SPacket decode(FriendlyByteBuf buffer) {
        return new SelectHandCardC2SPacket((InteractionHand)buffer.m_130066_(InteractionHand.class), buffer.m_130242_());
    }

    public static void handle(SelectHandCardC2SPacket packet, Supplier<NetworkEvent.Context> supplier) {
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
            if (!HandContainerItem.setSelected(held, packet.index)) {
                return;
            }
            ModNetwork.syncHeldContainer(player);
        });
        context.setPacketHandled(true);
    }
}

