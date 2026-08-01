package cn.solo.sanguosha.network;

import cn.solo.sanguosha.game.GameRoomManager;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public record ToggleTableSlotHorizontalC2SPacket(BlockPos clickedPos, BlockPos roomAnchor, int slot) {
    public static void encode(ToggleTableSlotHorizontalC2SPacket packet, FriendlyByteBuf buffer) {
        buffer.m_130064_(packet.clickedPos);
        buffer.m_130064_(packet.roomAnchor);
        buffer.m_130130_(packet.slot);
    }

    public static ToggleTableSlotHorizontalC2SPacket decode(FriendlyByteBuf buffer) {
        return new ToggleTableSlotHorizontalC2SPacket(buffer.m_130135_(), buffer.m_130135_(), buffer.m_130242_());
    }

    public static void handle(ToggleTableSlotHorizontalC2SPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && player.m_6084_() && !player.m_5833_()) {
                GameRoomManager.toggleHorizontal(player, packet.clickedPos, packet.roomAnchor, packet.slot);
            }
        });
        context.setPacketHandled(true);
    }
}

