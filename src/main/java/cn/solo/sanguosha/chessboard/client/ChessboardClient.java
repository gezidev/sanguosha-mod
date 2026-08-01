package cn.solo.sanguosha.chessboard.client;

import cn.solo.sanguosha.chessboard.client.screen.ChessboardScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;

/**
 * 棋盘客户端入口 —— 按键绑定、渲染器注册、管理界面打开动作。
 */
public final class ChessboardClient {
    /** 打开棋盘管理界面的按键（默认 Shift，仅客户端有效） */
    public static final KeyMapping OPEN_MENU = new KeyMapping("key.sanguosha.chessboard.open_menu", 341, "key.categories.sanguosha");

    private ChessboardClient() {
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(cn.solo.sanguosha.chessboard.ModChessboards.CHESSBOARD_BE.get(), cn.solo.sanguosha.chessboard.client.renderer.ChessboardRenderer::new);
    }

    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(ChessboardClient.OPEN_MENU);
    }

    public static void openScreen(BlockPos pos) {
        net.minecraft.client.Minecraft.m_91087_().m_91152_(new ChessboardScreen(pos));
    }
}
