/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package cn.solo.sanguosha.network;

import cn.solo.sanguosha.network.DeckTargetValidator;
import cn.solo.sanguosha.network.ModNetwork;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public record RequestDeckDrawConfigC2SPacket(int entityId) {
    public static void encode(RequestDeckDrawConfigC2SPacket packet, FriendlyByteBuf buffer) {
        buffer.m_130130_(packet.entityId);
    }

    public static RequestDeckDrawConfigC2SPacket decode(FriendlyByteBuf buffer) {
        return new RequestDeckDrawConfigC2SPacket(buffer.m_130242_());
    }

    public static void handle(RequestDeckDrawConfigC2SPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (DeckTargetValidator.validate(player, packet.entityId) != null) {
                ModNetwork.sendDeckDrawConfig(player);
            }
        });
        context.setPacketHandled(true);
    }
}

