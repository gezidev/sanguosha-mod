package cn.solo.sanguosha.chessboard.block;

import cn.solo.sanguosha.chessboard.blockentity.ChessboardBlockEntity;
import cn.solo.sanguosha.chessboard.game.BoardGameLogic;
import cn.solo.sanguosha.chessboard.game.ChineseChessLogic;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 通用棋盘方块 —— 框架层。
 * 棋盘为 1/16 格厚的薄板，支持水平朝向。
 * 子类构造时传入游戏逻辑。
 */
public class ChessboardBlock
extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.f_61374_;
    private static final VoxelShape SHAPE = Block.m_49796_(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);

    private final BoardGameLogic gameLogic;

    /** 客户端注入：Shift+右键打开管理界面的动作 */
    public static Consumer<BlockPos> openScreenAction = pos -> {};
    /** 客户端注入：管理界面按键是否按下（Shift+右键），服务端恒为 false */
    public static BooleanSupplier openMenuKeyDown = () -> false;

    public ChessboardBlock(Properties props, BoardGameLogic gameLogic) {
        super(props);
        this.gameLogic = gameLogic;
        this.m_49959_(this.f_49792_.m_61090_().m_61124_(FACING, Direction.SOUTH));
    }

    public BoardGameLogic getGameLogic() {
        return this.gameLogic != null ? this.gameLogic : ChineseChessLogic.INSTANCE;
    }

    @Override
    protected void m_7926_(StateDefinition.Builder<Block, BlockState> b) {
        b.m_61104_(FACING);
    }

    @Override
    public BlockState m_5573_(BlockPlaceContext ctx) {
        return this.m_49966_().m_61124_(FACING, ctx.m_8125_());
    }

    @Override
    public VoxelShape m_5940_(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) {
        return SHAPE;
    }

    @Override
    public BlockEntity m_142194_(BlockPos pos, BlockState state) {
        return new ChessboardBlockEntity(pos, state);
    }

    @Override
    public RenderShape m_7514_(BlockState s) {
        return RenderShape.MODEL;
    }

    @Override
    public List<ItemStack> m_49635_(BlockState state, LootParams.Builder params) {
        return Collections.emptyList(); // 由 playerWillDestroy 手动掉落（带 NBT）
    }

    @Override
    public void m_5707_(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.f_46443_ && !player.m_7500_()) {
            BlockEntity be = level.m_7702_(pos);
            if (be instanceof ChessboardBlockEntity board) {
                ItemStack stack = new ItemStack(this);
                net.minecraft.world.item.BlockItem.m_186338_(stack, board.m_58903_(), board.m_187481_());
                Block.m_49840_(level, pos, stack);
            }
        }
        super.m_5707_(level, pos, state, player);
    }

    @Override
    public InteractionResult m_6227_(BlockState state, Level level, BlockPos pos, Player player,
                                    InteractionHand hand, BlockHitResult hit) {
        // 手持物品时不拦截交互
        if (!player.m_21120_(hand).m_41619_()) {
            return InteractionResult.PASS;
        }
        if (level.f_46443_) {
            if (openMenuKeyDown.getAsBoolean()) {
                openScreenAction.accept(pos);
                return InteractionResult.CONSUME;
            }
            return InteractionResult.SUCCESS;
        }
        // 潜行+右键仅用于打开管理界面（客户端已处理），服务端不落子，避免误操作
        if (player.m_6047_()) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity be = level.m_7702_(pos);
        if (!(be instanceof ChessboardBlockEntity board)) {
            return InteractionResult.FAIL;
        }
        int[] mc = worldToModel(hit.m_82450_().f_82479_ - pos.m_123341_(),
                hit.m_82450_().f_82481_ - pos.m_123343_(), state.m_61143_(FACING), board.gameLogic());
        board.handleClick(mc[1], mc[0]); // row, col
        return InteractionResult.SUCCESS;
    }

    /** 世界坐标 → 棋盘行列（匹配 rowPixel/colPixel） */
    private static int[] worldToModel(double wx, double wz, Direction facing, BoardGameLogic g) {
        wx *= 16.0;
        wz *= 16.0;
        double mx;
        double mz;
        switch (facing) {
            case WEST: { mx = 16 - wz; mz = wx; break; }
            case NORTH: { mx = 16 - wx; mz = 16 - wz; break; }
            case EAST: { mx = wz; mz = 16 - wx; break; }
            default: { mx = wx; mz = wz; break; }
        }
        int bestCol = 0;
        int bestRow = 0;
        double bestDist = Double.MAX_VALUE;
        for (int r = 0; r < g.rows(); r++) {
            for (int c = 0; c < g.cols(); c++) {
                double dx = mx - g.colPixel(c);
                double dz = mz - g.rowPixel(r);
                double d = dx * dx + dz * dz;
                if (d < bestDist) {
                    bestDist = d;
                    bestRow = r;
                    bestCol = c;
                }
            }
        }
        return new int[]{bestCol, bestRow};
    }
}
