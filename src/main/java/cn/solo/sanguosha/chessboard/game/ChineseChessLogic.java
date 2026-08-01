package cn.solo.sanguosha.chessboard.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 中国象棋规则：10×9 棋盘，红/黑双方各 16 枚棋子。
 * 红方（side 0）在上方（第 0~3 行），黑方（side 1）在下方（第 6~9 行）。
 * 包含标准走子规范：帅/将、仕/士、相/象、马、车、炮、兵/卒。
 */
public class ChineseChessLogic implements BoardGameLogic {

    public static final ChineseChessLogic INSTANCE = new ChineseChessLogic();

    private static final int ROWS = 10, COLS = 9;

    @Override public int rows() { return ROWS; }
    @Override public int cols() { return COLS; }

    @Override
    public void initBoard(int[] p) {
        Arrays.fill(p, 0);
        int[] back = {5,4,3,2,1,2,3,4,5};
        for (int c = 0; c < COLS; c++) p[idx(0,c)] = pack(0, back[c]);
        p[idx(2,1)] = pack(0,6); p[idx(2,7)] = pack(0,6);
        for (int c = 0; c < COLS; c+=2) p[idx(3,c)] = pack(0,7);
        for (int c = 0; c < COLS; c+=2) p[idx(6,c)] = pack(1,7);
        p[idx(7,1)] = pack(1,6); p[idx(7,7)] = pack(1,6);
        for (int c = 0; c < COLS; c++) p[idx(9,c)] = pack(1, back[c]);
    }

    @Override
    public String pieceName(int piece) {
        if (piece == 0) return "";
        int t = type(piece), s = side(piece);
        return switch (t) {
            case 1 -> s == 0 ? "帅" : "将";
            case 2 -> s == 0 ? "仕" : "士";
            case 3 -> s == 0 ? "相" : "象";
            case 4 -> "马"; case 5 -> "车"; case 6 -> "炮";
            case 7 -> s == 0 ? "兵" : "卒";
            default -> "";
        };
    }

    @Override public int textColor(int piece) { return side(piece) == 0 ? 0xFFCC2222 : 0xFF1A1A1A; }
    @Override public int side(int piece) { return (piece >> 3) & 1; }
    @Override public String codePrefix() { return "xq"; }
    @Override public String pieceModelPath(int piece) { return "sanguosha:block/chinese_chesspiece"; }

    /**
     * 判断 (fromRow,fromCol) 的棋子能否走到 (toRow,toCol)。
     * 遵循标准中国象棋走法，且目的地不能是己方棋子。
     */
    public boolean isLegalMove(int[] pieces, int fromRow, int fromCol, int toRow, int toCol) {
        int piece = pieces[idx(fromRow, fromCol)];
        if (piece == 0) return false;
        if (fromRow == toRow && fromCol == toCol) return false;
        int target = pieces[idx(toRow, toCol)];
        if (target != 0 && side(target) == side(piece)) return false;
        int t = type(piece), s = side(piece);
        int dr = toRow - fromRow, dc = toCol - fromCol;
        switch (t) {
            case 1: // 帅/将：九宫内走一格（横竖）
                if (!inPalace(toRow, toCol, s)) return false;
                return Math.abs(dr) + Math.abs(dc) == 1;
            case 2: // 仕/士：九宫内斜走一格
                if (!inPalace(toRow, toCol, s)) return false;
                return Math.abs(dr) == 1 && Math.abs(dc) == 1;
            case 3: // 相/象：斜走两格，不能过河，象眼不能堵
                if (s == 0 && toRow > 4) return false;
                if (s == 1 && toRow < 5) return false;
                if (Math.abs(dr) != 2 || Math.abs(dc) != 2) return false;
                return pieces[idx(fromRow + dr / 2, fromCol + dc / 2)] == 0;
            case 4: // 马：走日字，马腿不能堵
                if (Math.abs(dr) == 2 && Math.abs(dc) == 1) {
                    return pieces[idx(fromRow + dr / 2, fromCol)] == 0;
                }
                if (Math.abs(dr) == 1 && Math.abs(dc) == 2) {
                    return pieces[idx(fromRow, fromCol + dc / 2)] == 0;
                }
                return false;
            case 5: // 车：直线任意距离，中间不能有子
                if (dr != 0 && dc != 0) return false;
                return pathClear(pieces, fromRow, fromCol, toRow, toCol);
            case 6: // 炮：直线，吃子需隔一个炮架，否则需通路
                if (dr != 0 && dc != 0) return false;
                int blockers = countBetween(pieces, fromRow, fromCol, toRow, toCol);
                if (target != 0) return blockers == 1;
                return blockers == 0;
            case 7: // 兵/卒：只能前进一格，过河后可横走，不能后退
                int forward = s == 0 ? 1 : -1;
                if (dr == forward && dc == 0) return true;
                boolean crossed = s == 0 ? fromRow >= 5 : fromRow <= 4;
                if (crossed && dr == 0 && Math.abs(dc) == 1) return true;
                return false;
            default:
                return false;
        }
    }

    /** 返回 (row,col) 处棋子的所有合法落点。 */
    public List<int[]> legalMoves(int[] pieces, int row, int col) {
        ArrayList<int[]> moves = new ArrayList<>();
        if (pieces[idx(row, col)] == 0) return moves;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (this.isLegalMove(pieces, row, col, r, c)) {
                    moves.add(new int[]{r, c});
                }
            }
        }
        return moves;
    }

    @Override
    public ClickResult onClick(int[] pieces, int selRow, int selCol, int clickRow, int clickCol) {
        int cp = pieces[idx(clickRow, clickCol)];
        if (selRow < 0) {
            return cp != 0 ? new ClickResult.Select(clickRow, clickCol) : new ClickResult.None();
        }
        int sp = pieces[idx(selRow, selCol)];
        if (cp != 0 && side(cp) == side(sp)) {
            return new ClickResult.Select(clickRow, clickCol);
        }
        if (!this.isLegalMove(pieces, selRow, selCol, clickRow, clickCol)) {
            return new ClickResult.None();
        }
        pieces[idx(selRow, selCol)] = 0;
        pieces[idx(clickRow, clickCol)] = sp;
        return new ClickResult.Move(selRow, selCol, clickRow, clickCol);
    }

    // ── 工具 ──

    private boolean inPalace(int r, int c, int side) {
        return c >= 3 && c <= 5 && (side == 0 ? r >= 0 && r <= 2 : r >= 7 && r <= 9);
    }

    private boolean pathClear(int[] pieces, int fr, int fc, int tr, int tc) {
        int stepR = Integer.signum(tr - fr);
        int stepC = Integer.signum(tc - fc);
        int r = fr + stepR, c = fc + stepC;
        while (r != tr || c != tc) {
            if (pieces[idx(r, c)] != 0) return false;
            r += stepR;
            c += stepC;
        }
        return true;
    }

    private int countBetween(int[] pieces, int fr, int fc, int tr, int tc) {
        int stepR = Integer.signum(tr - fr);
        int stepC = Integer.signum(tc - fc);
        int r = fr + stepR, c = fc + stepC;
        int count = 0;
        while (r != tr || c != tc) {
            if (pieces[idx(r, c)] != 0) count++;
            r += stepR;
            c += stepC;
        }
        return count;
    }

    public static int pack(int side, int type) { return (side << 3) | type; }
    public static int type(int piece) { return piece & 7; }
    public static int idx(int row, int col) { return row * COLS + col; }
}
