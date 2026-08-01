package cn.solo.sanguosha.chessboard;

import cn.solo.sanguosha.chessboard.blockentity.ChessboardBlockEntity;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

/**
 * /chessboard 指令 —— 远程操作棋盘：click / undo / reset / import。
 */
public final class ChessboardCommands {
    private ChessboardCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var chessboard = Commands.m_82127_("chessboard");
        var click = Commands.m_82127_("click")
                .then(Commands.m_82129_("x", IntegerArgumentType.integer())
                        .then(Commands.m_82129_("y", IntegerArgumentType.integer())
                                .then(Commands.m_82129_("z", IntegerArgumentType.integer())
                                        .then(Commands.m_82129_("row", IntegerArgumentType.integer())
                                                .then(Commands.m_82129_("col", IntegerArgumentType.integer())
                                                        .executes(ctx -> {
                                                            BlockPos pos = ChessboardCommands.readPos(ctx);
                                                            int row = IntegerArgumentType.getInteger(ctx, "row");
                                                            int col = IntegerArgumentType.getInteger(ctx, "col");
                                                            if (ctx.getSource().m_81372_().m_7702_(pos) instanceof ChessboardBlockEntity board) {
                                                                board.handleClick(row, col);
                                                            }
                                                            return 1;
                                                        }))))));
        var undo = Commands.m_82127_("undo")
                .then(Commands.m_82129_("x", IntegerArgumentType.integer())
                        .then(Commands.m_82129_("y", IntegerArgumentType.integer())
                                .then(Commands.m_82129_("z", IntegerArgumentType.integer())
                                        .executes(ctx -> {
                                            BlockPos pos = ChessboardCommands.readPos(ctx);
                                            if (ctx.getSource().m_81372_().m_7702_(pos) instanceof ChessboardBlockEntity board && board.undoMove()) {
                                                ctx.getSource().m_288197_(() -> Component.m_237113_("已悔棋"), true);
                                            } else {
                                                ctx.getSource().m_81352_(Component.m_237113_("没有可以悔棋的步骤"));
                                            }
                                            return 1;
                                        }))));
        var reset = Commands.m_82127_("reset")
                .then(Commands.m_82129_("x", IntegerArgumentType.integer())
                        .then(Commands.m_82129_("y", IntegerArgumentType.integer())
                                .then(Commands.m_82129_("z", IntegerArgumentType.integer())
                                        .executes(ctx -> {
                                            BlockPos pos = ChessboardCommands.readPos(ctx);
                                            if (ctx.getSource().m_81372_().m_7702_(pos) instanceof ChessboardBlockEntity board) {
                                                board.resetBoard();
                                                ctx.getSource().m_288197_(() -> Component.m_237113_("棋盘已重置"), true);
                                            }
                                            return 1;
                                        }))));
        var importCmd = Commands.m_82127_("import")
                .then(Commands.m_82129_("x", IntegerArgumentType.integer())
                        .then(Commands.m_82129_("y", IntegerArgumentType.integer())
                                .then(Commands.m_82129_("z", IntegerArgumentType.integer())
                                        .then(Commands.m_82129_("code", StringArgumentType.greedyString())
                                                .executes(ctx -> {
                                                    BlockPos pos = ChessboardCommands.readPos(ctx);
                                                    String code = StringArgumentType.getString(ctx, "code");
                                                    if (ctx.getSource().m_81372_().m_7702_(pos) instanceof ChessboardBlockEntity board) {
                                                        board.importCode(code);
                                                        ctx.getSource().m_288197_(() -> Component.m_237113_("已导入"), true);
                                                    }
                                                    return 1;
                                                })))));
        dispatcher.register(chessboard.then(click).then(undo).then(reset).then(importCmd));
    }

    private static BlockPos readPos(CommandContext<CommandSourceStack> ctx) {
        return new BlockPos(IntegerArgumentType.getInteger(ctx, "x"),
                IntegerArgumentType.getInteger(ctx, "y"),
                IntegerArgumentType.getInteger(ctx, "z"));
    }
}
