package cn.solo.sanguosha.network;

import cn.solo.sanguosha.client.HandContainerCountClient;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public record HandContainerCountS2CPacket(int targetEntityId, int count) {
    static void encode(HandContainerCountS2CPacket packet, FriendlyByteBuf buffer) {
        buffer.m_130130_(packet.targetEntityId);
        buffer.m_130130_(Math.max(0, packet.count));
    }

    static HandContainerCountS2CPacket decode(FriendlyByteBuf buffer) {
        return new HandContainerCountS2CPacket(buffer.m_130242_(), buffer.m_130242_());
    }

    static void handle(HandContainerCountS2CPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> HandContainerCountClient.accept(packet.targetEntityId, packet.count)));
        context.setPacketHandled(true);
    }
}

