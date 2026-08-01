package cn.solo.sanguosha.network;

import cn.solo.sanguosha.deck.DeckDrawConfig;
import cn.solo.sanguosha.deck.PlayerDeckDrawConfig;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public record SaveDeckDrawConfigC2SPacket(int mask) {
    public static void encode(SaveDeckDrawConfigC2SPacket packet, FriendlyByteBuf buffer) {
        buffer.writeByte(packet.mask);
    }

    public static SaveDeckDrawConfigC2SPacket decode(FriendlyByteBuf buffer) {
        return new SaveDeckDrawConfigC2SPacket(buffer.readUnsignedByte());
    }

    public static void handle(SaveDeckDrawConfigC2SPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || (packet.mask & 0xFFFFFFE0) != 0) {
                return;
            }
            PlayerDeckDrawConfig.set((Player)player, DeckDrawConfig.fromNetwork(packet.mask));
        });
        context.setPacketHandled(true);
    }
}

