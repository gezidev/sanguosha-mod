package cn.solo.sanguosha.chessboard.client.renderer;

import cn.solo.sanguosha.chessboard.ModChessboards;
import cn.solo.sanguosha.chessboard.block.ChessboardBlock;
import cn.solo.sanguosha.chessboard.blockentity.ChessboardBlockEntity;
import cn.solo.sanguosha.chessboard.game.BoardGameLogic;
import cn.solo.sanguosha.chessboard.game.ChineseChessLogic;
import cn.solo.sanguosha.chessboard.game.GomokuLogic;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * 通用棋盘棋子渲染器 —— 框架层。
 * 通过 {@link BoardGameLogic} 适配不同棋类，负责模型加载、动画和文字渲染。
 */
public class ChessboardRenderer
implements BlockEntityRenderer<ChessboardBlockEntity> {
    private static final float LIFT = 0.045f;
    private static final int LIFT_MS = 150;
    private static final int MOVE_MS = 250;
    private static final ResourceLocation HINT = new ResourceLocation("sanguosha", "textures/block/move_hint.png");

    private final Font font;
    private final Map<BlockPos, AnimData> animMap = new HashMap<>();

    public ChessboardRenderer(BlockEntityRendererProvider.Context ctx) {
        this.font = ctx.m_173586_();
    }

    private static class AnimData {
        long selMs;
        long unselMs;
        long moveMs;
        int[] prevPieces;
        int prevSelRow = -1;
        int prevSelCol = -1;
        int unselRow = -1;
        int unselCol = -1;
        int fromRow = -1;
        int fromCol = -1;
        int toRow = -1;
        int toCol = -1;
    }

    private static BlockState pieceState(BoardGameLogic g, int piece) {
        Block block;
        if (g instanceof ChineseChessLogic) {
            block = ModChessboards.CHESS_PIECE_MODEL.get();
        } else if (g instanceof GomokuLogic) {
            block = g.side(piece) == 0 ? ModChessboards.GOMOKU_PIECE_BLACK.get() : ModChessboards.GOMOKU_PIECE_WHITE.get();
        } else {
            block = ModChessboards.TICTACTOE_PIECE_MODEL.get();
        }
        return block.m_49966_();
    }

    @Override
    public void m_6922_(ChessboardBlockEntity entity, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        BoardGameLogic g = entity.gameLogic();
        int[] pieces = entity.pieces();
        int rows = g.rows();
        int cols = g.cols();
        int selRow = entity.selRow();
        int selCol = entity.selCol();
        Direction facing = entity.m_58900_().m_61143_(ChessboardBlock.FACING);

        AnimData a = this.animMap.computeIfAbsent(entity.m_58899_(), k -> new AnimData());
        if (a.prevPieces == null || a.prevPieces.length != pieces.length) {
            a.prevPieces = new int[pieces.length];
        }
        long now = System.currentTimeMillis();
        boolean changed = !Arrays.equals(pieces, a.prevPieces);
        if (changed) {
            a.fromRow = -1;
            a.fromCol = -1;
            a.toRow = -1;
            a.toCol = -1;
            int moved = 0;
            for (int i = 0; i < pieces.length; i++) {
                if (a.prevPieces[i] != 0 && pieces[i] == 0) {
                    a.fromRow = i / cols;
                    a.fromCol = i % cols;
                    moved = a.prevPieces[i];
                    break;
                }
            }
            for (int i = 0; i < pieces.length; i++) {
                if (pieces[i] == moved && a.prevPieces[i] != moved) {
                    a.toRow = i / cols;
                    a.toCol = i % cols;
                    break;
                }
            }
            a.moveMs = now;
            System.arraycopy(pieces, 0, a.prevPieces, 0, pieces.length);
        }
        if (a.prevSelRow != selRow || a.prevSelCol != selCol) {
            if (!changed && a.prevSelRow >= 0 && (selRow < 0 || selRow != a.prevSelRow || selCol != a.prevSelCol)) {
                a.unselRow = a.prevSelRow;
                a.unselCol = a.prevSelCol;
                a.unselMs = now;
            }
            a.selMs = now;
            a.prevSelRow = selRow;
            a.prevSelCol = selCol;
        }
        float selT = clamp01((now - a.selMs) / (float) LIFT_MS);
        float lift = selRow >= 0 ? LIFT * selT : 0.0f;
        float unlift = LIFT * (1.0f - clamp01((now - a.unselMs) / (float) LIFT_MS));
        float moveT = clamp01((now - a.moveMs) / (float) MOVE_MS);

        BlockRenderDispatcher brd = Minecraft.m_91087_().m_91289_();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int piece = pieces[row * cols + col];
                if (piece == 0) continue;
                if (moveT < 1.0f && a.toRow == row && a.toCol == col) continue;
                boolean sel = selRow == row && selCol == col;
                float liftHere = sel ? lift : 0.0f;
                if (a.unselRow == row && a.unselCol == col && unlift > 0.0f && liftHere == 0.0f) {
                    liftHere = unlift;
                }
                float[] pos = gridPos(facing, g, row, col);
                ChessboardRenderer.renderPiece(brd, pose, buffers, g, piece, pos[0], pos[1], liftHere, light, overlay, facing);
                ChessboardRenderer.renderText(this.font, pose, buffers, g, piece, pos[0], pos[1], liftHere, light, facing);
            }
        }
        if (moveT < 1.0f && a.fromRow >= 0 && a.toRow >= 0) {
            int p = pieces[a.toRow * cols + a.toCol];
            if (p != 0) {
                float[] from = gridPos(facing, g, a.fromRow, a.fromCol);
                float[] to = gridPos(facing, g, a.toRow, a.toCol);
                float wx = lerp(from[0], to[0], moveT);
                float wz = lerp(from[1], to[1], moveT);
                ChessboardRenderer.renderPiece(brd, pose, buffers, g, p, wx, wz, LIFT * (1.0f - moveT), light, overlay, facing);
                ChessboardRenderer.renderText(this.font, pose, buffers, g, p, wx, wz, LIFT * (1.0f - moveT), light, facing);
            }
        }
        if (selRow >= 0 && g instanceof ChineseChessLogic) {
            for (int[] m : g.legalMoves(pieces, selRow, selCol)) {
                ChessboardRenderer.renderHint(pose, buffers, g, m[0], m[1], facing, light);
            }
        }
    }

    private static void renderHint(PoseStack pose, MultiBufferSource buffers, BoardGameLogic g, int row, int col, Direction facing, int light) {
        float[] pos = gridPos(facing, g, row, col);
        float y = g.pieceHeight() + 0.006f;
        // 单个格子的边长（块单位），提示正好盖住一格
        float cellW = g.gridSpan() / (float)(g.cols() - 1) / 16.0f;
        float cellH = g.gridSpan() / (float)(g.rows() - 1) / 16.0f;
        float x0 = pos[0] - cellW * 0.3f;
        float x1 = pos[0] + cellW * 0.3f;
        float z0 = pos[1] - cellH * 0.3f;
        float z1 = pos[1] + cellH * 0.3f;
        VertexConsumer consumer = buffers.m_6299_(RenderType.m_110470_((ResourceLocation)ChessboardRenderer.HINT));
        Matrix4f matrix = pose.m_85850_().m_252922_();
        Matrix3f normal = pose.m_85850_().m_252943_();
        consumer.m_252986_(matrix, x0, y, z0).m_6122_(80, 220, 80, 255).m_7421_(0.0f, 0.0f).m_86008_(OverlayTexture.f_118083_).m_85969_(light).m_252939_(normal, 0.0f, 1.0f, 0.0f).m_5752_();
        consumer.m_252986_(matrix, x0, y, z1).m_6122_(80, 220, 80, 255).m_7421_(0.0f, 1.0f).m_86008_(OverlayTexture.f_118083_).m_85969_(light).m_252939_(normal, 0.0f, 1.0f, 0.0f).m_5752_();
        consumer.m_252986_(matrix, x1, y, z1).m_6122_(80, 220, 80, 255).m_7421_(1.0f, 1.0f).m_86008_(OverlayTexture.f_118083_).m_85969_(light).m_252939_(normal, 0.0f, 1.0f, 0.0f).m_5752_();
        consumer.m_252986_(matrix, x1, y, z0).m_6122_(80, 220, 80, 255).m_7421_(1.0f, 0.0f).m_86008_(OverlayTexture.f_118083_).m_85969_(light).m_252939_(normal, 0.0f, 1.0f, 0.0f).m_5752_();
    }

    private static void renderPiece(BlockRenderDispatcher brd, PoseStack pose, MultiBufferSource buffers,
                                    BoardGameLogic g, int piece, float wx, float wz, float lift,
                                    int light, int overlay, Direction facing) {
        float y = g.pieceHeight() + lift;
        float cx = g.pieceCenterX() / 16.0f;
        float cz = g.pieceCenterZ() / 16.0f;
        float sc = g.pieceScale();
        pose.m_85836_();
        pose.m_85837_(wx, y, wz);
        pose.m_252781_(Axis.f_252436_.m_252977_((float) ChessboardRenderer.facingYaw(facing)));
        if (g.pieceFlipX(piece)) {
            pose.m_252781_(Axis.f_252529_.m_252977_(180.0f));
        }
        pose.m_85841_(sc, sc, sc);
        pose.m_85837_(-cx, 0.0f, -cz);
        brd.m_110912_(ChessboardRenderer.pieceState(g, piece), pose, buffers, light, overlay);
        pose.m_85849_();
    }

    private static void renderText(Font font, PoseStack pose, MultiBufferSource buffers,
                                   BoardGameLogic g, int piece, float wx, float wz, float lift,
                                   int light, Direction facing) {
        String name = g.pieceName(piece);
        if (name.isEmpty()) {
            return;
        }
        Component text = Component.m_237113_(name);
        float textH = g.pieceHeight() + lift + 0.02f;
        pose.m_85836_();
        pose.m_85837_(wx, textH, wz);
        pose.m_252781_(Axis.f_252436_.m_252977_((float) ChessboardRenderer.textFacingYaw(facing)));
        if (g.side(piece) != 0) {
            pose.m_252781_(Axis.f_252436_.m_252977_(180.0f));
        }
        pose.m_252781_(Axis.f_252529_.m_252977_(90.0f));
        pose.m_85841_(0.006f, 0.006f, 0.006f);
        float tx = -font.m_92852_(text) / 2.0f + 0.5f;
        float ty = -font.f_92710_ / 2.0f + 0.5f;
        font.m_272077_(text, tx, ty, g.textColor(piece), false, pose.m_85850_().m_252922_(), buffers, Font.DisplayMode.POLYGON_OFFSET, 0, light);
        pose.m_85849_();
    }

    private static int facingYaw(Direction facing) {
        switch (facing) {
            case WEST: return -90;
            case NORTH: return 180;
            case EAST: return 90;
            default: return 0;
        }
    }

    private static int textFacingYaw(Direction facing) {
        switch (facing) {
            case WEST: return 90;
            case NORTH: return 180;
            case EAST: return -90;
            default: return 0;
        }
    }

    private static float[] gridPos(Direction facing, BoardGameLogic g, int row, int col) {
        float gx = g.colPixel(col) / 16.0f;
        float gz = g.rowPixel(row) / 16.0f;
        switch (facing) {
            case WEST: return new float[]{gz, 1.0f - gx};
            case NORTH: return new float[]{1.0f - gx, 1.0f - gz};
            case EAST: return new float[]{1.0f - gz, gx};
            default: return new float[]{gx, gz};
        }
    }

    private static float clamp01(float v) {
        return v < 0.0f ? 0.0f : (v > 1.0f ? 1.0f : v);
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}
