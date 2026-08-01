package cn.solo.sanguosha.network;

import cn.solo.sanguosha.chessboard.blockentity.ChessboardBlockEntity;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

/**
 * 棋盘管理界面操作（重置/悔棋/导入棋局码）—— 直接走数据包调用服务端，
 * 不依赖 /chessboard 命令（命令走聊天签名通道，环境不满足时不会执行）。
 */
public record ChessboardActionC2SPacket(BlockPos pos, Action action, String code) {

    public enum Action {
        RESET,
        UNDO,
        IMPORT
    }

    public static void encode(ChessboardActionC2SPacket packet, FriendlyByteBuf buffer) {
        buffer.m_130064_(packet.pos);
        buffer.m_130068_((Enum)packet.action);
        buffer.m_130072_(packet.code, 256);
    }

    public static ChessboardActionC2SPacket decode(FriendlyByteBuf buffer) {
        return new ChessboardActionC2SPacket(buffer.m_130135_(), (Action)buffer.m_130066_(Action.class), buffer.m_130136_(256));
    }

    public static void handle(ChessboardActionC2SPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !player.m_6084_() || player.m_5833_()) {
                return;
            }
            Vec3 center = Vec3.m_82512_((Vec3i)packet.pos);
            if (player.m_146892_().m_82557_(center) > 64.0) {
                return;
            }
            if (player.m_9236_().m_7702_(packet.pos) instanceof ChessboardBlockEntity board) {
                switch (packet.action) {
                    case RESET -> board.resetBoard();
                    case UNDO -> board.undoMove();
                    case IMPORT -> board.importCode(packet.code);
                }
            }
        });
        context.setPacketHandled(true);
    }
}
