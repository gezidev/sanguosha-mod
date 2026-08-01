/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.fml.DistExecutor
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package cn.solo.sanguosha.network;

import cn.solo.sanguosha.client.ClientScreens;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public record SyncDeckDrawConfigS2CPacket(int mask) {
    public static void encode(SyncDeckDrawConfigS2CPacket packet, FriendlyByteBuf buffer) {
        buffer.writeByte(packet.mask);
    }

    public static SyncDeckDrawConfigS2CPacket decode(FriendlyByteBuf buffer) {
        return new SyncDeckDrawConfigS2CPacket(buffer.readUnsignedByte());
    }

    public static void handle(SyncDeckDrawConfigS2CPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            if ((packet.mask & 0xFFFFFFE0) == 0) {
                DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> ClientScreens.openDeckDrawOptions(packet.mask));
            }
        });
        context.setPacketHandled(true);
    }
}

