package cn.solo.sanguosha.chessboard.client.screen;

import cn.solo.sanguosha.chessboard.blockentity.ChessboardBlockEntity;
import cn.solo.sanguosha.network.ChessboardActionC2SPacket;
import cn.solo.sanguosha.network.ModNetwork;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

/**
 * 棋盘管理界面 —— 悔棋、重置、导入/导出棋局码。
 */
public class ChessboardScreen
extends Screen {
    private final BlockPos boardPos;
    private EditBox codeField;

    public ChessboardScreen(BlockPos pos) {
        super(Component.m_237113_("棋盘管理"));
        this.boardPos = pos;
    }

    @Override
    protected void m_7856_() {
        int cx = this.f_96543_ / 2;
        int cy = this.f_96544_ / 2;

        String currentCode = this.getCurrentCode();
        this.codeField = new EditBox(this.f_96547_, cx - 90, cy - 50, 180, 20, Component.m_237113_(""));
        this.codeField.m_94208_(2000);
        String hint = currentCode.length() > 20 ? currentCode.substring(0, 20) + "..." : currentCode;
        this.codeField.m_257771_(Component.m_237113_(hint));
        this.m_142416_(this.codeField);

        this.m_142416_(Button.m_253074_(Component.m_237113_("导入"), btn -> {
            if (this.f_96541_ != null && this.f_96541_.f_91074_ != null) {
                String paste = this.codeField.m_94155_().isEmpty() ? currentCode : this.codeField.m_94155_();
                ModNetwork.chessboardAction(this.boardPos, ChessboardActionC2SPacket.Action.IMPORT, paste);
            }
            this.m_7379_();
        }).m_252987_(cx - 92, cy - 25, 60, 20).m_253136_());

        this.m_142416_(Button.m_253074_(Component.m_237113_("复制"), btn -> {
            this.f_96541_.f_91068_.m_90911_(currentCode);
            this.m_7379_();
        }).m_252987_(cx - 30, cy - 25, 60, 20).m_253136_());

        this.m_142416_(Button.m_253074_(Component.m_237113_("重置"), btn -> {
            ModNetwork.chessboardAction(this.boardPos, ChessboardActionC2SPacket.Action.RESET, "");
            this.m_7379_();
        }).m_252987_(cx + 32, cy - 25, 60, 20).m_253136_());

        this.m_142416_(Button.m_253074_(Component.m_237113_("悔棋"), btn -> {
            ModNetwork.chessboardAction(this.boardPos, ChessboardActionC2SPacket.Action.UNDO, "");
            this.m_7379_();
        }).m_252987_(cx - 50, cy + 5, 100, 20).m_253136_());
    }

    private String getCurrentCode() {
        if (this.f_96541_ == null || this.f_96541_.f_91073_ == null) {
            return "";
        }
        if (this.f_96541_.f_91073_.m_7702_(this.boardPos) instanceof ChessboardBlockEntity board) {
            return board.gameLogic().encodePieces(board.pieces());
        }
        return "";
    }

    @Override
    public boolean m_7043_() {
        return false;
    }
}
