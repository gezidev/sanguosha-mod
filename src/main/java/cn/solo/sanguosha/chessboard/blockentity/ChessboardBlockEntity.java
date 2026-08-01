package cn.solo.sanguosha.chessboard.blockentity;

import cn.solo.sanguosha.chessboard.block.ChessboardBlock;
import cn.solo.sanguosha.chessboard.game.BoardGameLogic;
import cn.solo.sanguosha.chessboard.game.BoardGameLogic.ClickResult;
import cn.solo.sanguosha.chessboard.game.GomokuLogic;
import cn.solo.sanguosha.chessboard.game.TicTacToeLogic;
import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 通用棋盘方块实体 —— 框架核心。
 * 负责棋子存储、选中、走棋历史、悔棋、重置、网络同步。
 * 具体规则通过 {@link ChessboardBlock#getGameLogic()} 获取。
 */
public class ChessboardBlockEntity
extends BlockEntity {
    public static BlockEntityType<ChessboardBlockEntity> TYPE;

    private BoardGameLogic logic;
    private int[] pieces;
    private int selRow = -1;
    private int selCol = -1;
    private final Deque<int[]> history = new ArrayDeque<>();

    // ── 动画字段 ──
    long selectTick;
    long unselectTick;
    long moveTick;
    int moveFromRow = -1;
    int moveFromCol = -1;
    int moveToRow = -1;
    int moveToCol = -1;
    int unselRow = -1;
    int unselCol = -1;

    public ChessboardBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }

    // ── 逻辑 ──

    public BoardGameLogic gameLogic() {
        if (this.logic == null) {
            if (this.m_58900_().m_60734_() instanceof ChessboardBlock cb) {
                this.logic = cb.getGameLogic();
                this.pieces = new int[this.logic.rows() * this.logic.cols()];
                this.logic.initBoard(this.pieces);
            }
        }
        return this.logic;
    }

    private int idx(int row, int col) {
        BoardGameLogic g = this.gameLogic();
        return row * g.cols() + col;
    }

    // ── 访问 ──

    public int[] pieces() {
        this.gameLogic();
        return this.pieces;
    }

    public int selRow() {
        return this.selRow;
    }

    public int selCol() {
        return this.selCol;
    }

    public boolean hasSelection() {
        return this.selRow >= 0;
    }

    public long selectTick() {
        return this.selectTick;
    }

    public long unselectTick() {
        return this.unselectTick;
    }

    public long moveTick() {
        return this.moveTick;
    }

    public int moveFromRow() {
        return this.moveFromRow;
    }

    public int moveFromCol() {
        return this.moveFromCol;
    }

    public int moveToRow() {
        return this.moveToRow;
    }

    public int moveToCol() {
        return this.moveToCol;
    }

    public int unselRow() {
        return this.unselRow;
    }

    public int unselCol() {
        return this.unselCol;
    }

    // ── 点击 ──

    public void handleClick(int clickRow, int clickCol) {
        BoardGameLogic g = this.gameLogic();
        long tick = this.m_58904_() != null ? this.m_58904_().m_46467_() : 0;
        int captured = this.pieces[this.idx(clickRow, clickCol)];

        ClickResult r = g.onClick(this.pieces, this.selRow, this.selCol, clickRow, clickCol);
        if (r instanceof ClickResult.Select sel) {
            this.selRow = sel.row();
            this.selCol = sel.col();
            this.selectTick = tick;
        } else if (r instanceof ClickResult.Move mv) {
            this.history.push(new int[]{mv.fromRow(), mv.fromCol(), mv.toRow(), mv.toCol(), captured});
            this.moveFromRow = mv.fromRow();
            this.moveFromCol = mv.fromCol();
            this.moveToRow = mv.toRow();
            this.moveToCol = mv.toCol();
            this.moveTick = tick;
            this.selRow = -1;
            this.selCol = -1;
        } else if (r instanceof ClickResult.Place pl) {
            this.history.push(new int[]{-1, -1, pl.row(), pl.col(), 0});
            this.moveFromRow = -1;
            this.moveFromCol = -1;
            this.moveToRow = pl.row();
            this.moveToCol = pl.col();
            this.moveTick = tick;
        } else if (r instanceof ClickResult.Reset) {
            this.resetBoard();
        }
        this.notifyChange();
    }

    public void clearSelection() {
        if (this.selRow >= 0) {
            this.unselRow = this.selRow;
            this.unselCol = this.selCol;
            this.unselectTick = this.m_58904_() != null ? this.m_58904_().m_46467_() : 0;
        }
        this.selRow = -1;
        this.selCol = -1;
        this.notifyChange();
    }

    // ── 悔棋/重置 ──

    public boolean undoMove() {
        if (this.history.isEmpty()) {
            return false;
        }
        int[] rec = this.history.pop();
        int fr = rec[0];
        int fc = rec[1];
        int tr = rec[2];
        int tc = rec[3];
        BoardGameLogic g = this.gameLogic();
        if (fr >= 0) {
            this.pieces[this.idx(fr, fc)] = this.pieces[this.idx(tr, tc)];
            this.pieces[this.idx(tr, tc)] = rec[4];
        } else {
            this.pieces[this.idx(tr, tc)] = 0;
        }
        if (g instanceof GomokuLogic gmk) {
            gmk.toggleSide();
        } else if (g instanceof TicTacToeLogic ttt) {
            ttt.toggleSide();
        }
        this.selRow = -1;
        this.selCol = -1;
        this.notifyChange();
        return true;
    }

    public void importCode(String code) {
        this.gameLogic().decodePieces(this.pieces, code);
        this.history.clear();
        this.selRow = -1;
        this.selCol = -1;
        this.notifyChange();
    }

    public void resetBoard() {
        this.gameLogic().initBoard(this.pieces);
        this.history.clear();
        this.selRow = -1;
        this.selCol = -1;
        this.notifyChange();
    }

    private void notifyChange() {
        this.m_6596_();
        if (this.m_58904_() != null) {
            this.m_58904_().m_7260_(this.m_58899_(), this.m_58900_(), this.m_58900_(), 3);
        }
    }

    // ── 持久化 ──

    @Override
    protected void m_183515_(CompoundTag tag) {
        super.m_183515_(tag);
        this.gameLogic();
        tag.m_128385_("pieces", this.pieces);
        tag.m_128405_("selRow", this.selRow);
        tag.m_128405_("selCol", this.selCol);
        int size = this.history.size();
        tag.m_128405_("histSize", size);
        if (size > 0) {
            int[] flat = new int[size * 5];
            int i = 0;
            for (int[] rec : this.history) {
                System.arraycopy(rec, 0, flat, i * 5, 5);
                i++;
            }
            tag.m_128385_("history", flat);
        }
    }

    @Override
    public void m_142466_(CompoundTag tag) {
        super.m_142466_(tag);
        this.gameLogic();
        int[] loaded = tag.m_128465_("pieces");
        if (loaded != null && loaded.length == this.pieces.length) {
            System.arraycopy(loaded, 0, this.pieces, 0, this.pieces.length);
        } else {
            this.gameLogic().initBoard(this.pieces);
        }
        this.selRow = tag.m_128451_("selRow");
        this.selCol = tag.m_128451_("selCol");
        this.history.clear();
        int histSize = tag.m_128451_("histSize");
        if (histSize > 0 && tag.m_128441_("history")) {
            int[] flat = tag.m_128465_("history");
            if (flat != null && flat.length >= histSize * 5) {
                for (int i = histSize - 1; i >= 0; i--) {
                    int off = i * 5;
                    this.history.push(new int[]{flat[off], flat[off + 1], flat[off + 2], flat[off + 3], flat[off + 4]});
                }
            }
        }
    }

    @Override
    public CompoundTag m_5995_() {
        CompoundTag tag = super.m_5995_();
        this.gameLogic();
        tag.m_128385_("pieces", this.pieces);
        tag.m_128405_("selRow", this.selRow);
        tag.m_128405_("selCol", this.selCol);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        this.gameLogic();
        int[] loaded = tag.m_128465_("pieces");
        if (loaded != null && loaded.length == this.pieces.length) {
            System.arraycopy(loaded, 0, this.pieces, 0, this.pieces.length);
        }
        this.selRow = tag.m_128451_("selRow");
        this.selCol = tag.m_128451_("selCol");
    }

    @Override
    public Packet<ClientGamePacketListener> m_58483_() {
        return ClientboundBlockEntityDataPacket.m_195640_((net.minecraft.world.level.block.entity.BlockEntity)this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        if (this.m_58904_() != null && this.m_58904_().f_46443_) {
            this.handleUpdateTag(pkt.m_131708_());
        }
    }
}
