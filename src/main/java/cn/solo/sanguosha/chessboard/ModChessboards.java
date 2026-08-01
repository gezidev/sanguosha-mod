package cn.solo.sanguosha.chessboard;

import cn.solo.sanguosha.chessboard.block.ChessboardBlock;
import cn.solo.sanguosha.chessboard.blockentity.ChessboardBlockEntity;
import cn.solo.sanguosha.chessboard.game.BoardGameLogic;
import cn.solo.sanguosha.chessboard.game.ChineseChessLogic;
import cn.solo.sanguosha.chessboard.game.GomokuLogic;
import cn.solo.sanguosha.chessboard.game.TicTacToeLogic;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModChessboards {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create((ResourceKey)Registries.f_256747_, (String)"sanguosha");
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create((ResourceKey)Registries.f_256913_, (String)"sanguosha");
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create((ResourceKey)Registries.f_256922_, (String)"sanguosha");

    private static BlockBehaviour.Properties boardProps() {
        return BlockBehaviour.Properties.m_284310_().m_284180_(MapColor.f_283825_).m_60913_(2.0f, 3.0f).m_60918_(SoundType.f_56736_).m_60955_();
    }

    private static BlockItem boardItem(RegistryObject<ChessboardBlock> block) {
        return new BlockItem(block.get(), new Item.Properties()) {
            @Override
            public void m_7373_(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
                lines.add((Component)Component.m_237115_((String)"tooltip.sanguosha.chessboard").m_130940_(ChatFormatting.DARK_GRAY));
            }
        };
    }

    // ── 棋盘方块 ──

    public static final RegistryObject<ChessboardBlock> CHINESE_CHESSBOARD = ModChessboards.BLOCKS.register("chinese_chessboard", () -> new ChessboardBlock(ModChessboards.boardProps(), (BoardGameLogic)ChineseChessLogic.INSTANCE));
    public static final RegistryObject<ChessboardBlock> GOMOKU_BOARD = ModChessboards.BLOCKS.register("gomoku_board", () -> new ChessboardBlock(ModChessboards.boardProps(), (BoardGameLogic)GomokuLogic.INSTANCE));
    public static final RegistryObject<ChessboardBlock> TICTACTOE_BOARD = ModChessboards.BLOCKS.register("tictactoe_board", () -> new ChessboardBlock(ModChessboards.boardProps(), (BoardGameLogic)TicTacToeLogic.INSTANCE));

    // 棋子模型方块（纯渲染用）
    public static final RegistryObject<Block> CHESS_PIECE_MODEL = ModChessboards.BLOCKS.register("chess_piece", () -> new Block(ModChessboards.boardProps()));
    public static final RegistryObject<Block> GOMOKU_PIECE_BLACK = ModChessboards.BLOCKS.register("gomoku_piece_black", () -> new Block(ModChessboards.boardProps()));
    public static final RegistryObject<Block> GOMOKU_PIECE_WHITE = ModChessboards.BLOCKS.register("gomoku_piece_white", () -> new Block(ModChessboards.boardProps()));
    public static final RegistryObject<Block> TICTACTOE_PIECE_MODEL = ModChessboards.BLOCKS.register("tictactoe_piece", () -> new Block(ModChessboards.boardProps()));

    // ── 物品（棋盘） ──

    public static final RegistryObject<BlockItem> CHINESE_CHESSBOARD_ITEM = ModChessboards.ITEMS.register("chinese_chessboard", () -> ModChessboards.boardItem(ModChessboards.CHINESE_CHESSBOARD));
    public static final RegistryObject<BlockItem> GOMOKU_BOARD_ITEM = ModChessboards.ITEMS.register("gomoku_board", () -> ModChessboards.boardItem(ModChessboards.GOMOKU_BOARD));
    public static final RegistryObject<BlockItem> TICTACTOE_BOARD_ITEM = ModChessboards.ITEMS.register("tictactoe_board", () -> ModChessboards.boardItem(ModChessboards.TICTACTOE_BOARD));

    // ── 方块实体（所有棋盘共用一种类型） ──

    public static final RegistryObject<BlockEntityType<ChessboardBlockEntity>> CHESSBOARD_BE = ModChessboards.BLOCK_ENTITIES.register("board_game", () -> {
        BlockEntityType<ChessboardBlockEntity> t = BlockEntityType.Builder.m_155273_(ChessboardBlockEntity::new, (Block[])(new Block[]{ModChessboards.CHINESE_CHESSBOARD.get(), ModChessboards.GOMOKU_BOARD.get(), ModChessboards.TICTACTOE_BOARD.get()})).m_58966_(null);
        ChessboardBlockEntity.TYPE = t;
        return t;
    });

    private ModChessboards() {
    }
}
