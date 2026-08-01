package cn.solo.sanguosha.game;

import cn.solo.sanguosha.block.TableBlockEntity;
import cn.solo.sanguosha.config.GeneralAssetManager;
import cn.solo.sanguosha.game.MilitaryRoomSavedData;
import cn.solo.sanguosha.game.MilitaryTableLayout;
import cn.solo.sanguosha.item.GeneralCardItem;
import cn.solo.sanguosha.item.HealthCardItem;
import cn.solo.sanguosha.menu.GameTable2Menu;
import cn.solo.sanguosha.network.ModNetwork;
import cn.solo.sanguosha.registry.ModBlocks;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

public final class GameRoomManager {
    public static final int MAX_MEMBERS = 8;
    public static final int MIN_PLAYERS = 2;
    public static final int OFFER_SIZE = 5;
    public static final double TABLE_DISTANCE = 8.0;

    private GameRoomManager() {
    }

    public static boolean isTable(ServerLevel level, BlockPos pos) {
        return level.m_46749_(pos) && GameRoomManager.isTableBlock((Level)level, pos);
    }

    public static boolean isTableBlock(Level level, BlockPos pos) {
        return level.m_8055_(pos).m_60713_((Block)ModBlocks.GAME_TABLE_2.get());
    }

    public static boolean isHealthCard(ItemStack stack) {
        return !stack.m_41619_() && stack.m_41613_() >= 1 && stack.m_41720_() instanceof HealthCardItem && ForgeRegistries.ITEMS.getKey(stack.m_41720_()) != null && "sanguosha:health_card".equals(ForgeRegistries.ITEMS.getKey(stack.m_41720_()).toString());
    }

    public static boolean menuHasRoom(Player player, BlockPos clickedPos, BlockPos roomAnchor) {
        if (player.m_9236_().f_46443_) {
            return true;
        }
        Level level = player.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return false;
        }
        ServerLevel level2 = (ServerLevel)level;
        MilitaryTableLayout layout = GameRoomManager.layout(level2, clickedPos);
        if (layout == null || !layout.contains(clickedPos) || !layout.anchor().equals((Object)roomAnchor)) {
            return false;
        }
        MilitaryRoomSavedData.Room room = MilitaryRoomSavedData.get(level2).get(layout.anchor());
        return room != null && room.tables().equals(Set.copyOf(layout.members()));
    }

    public static BlockPos clientOrServerAnchor(Player player, BlockPos clickedPos) {
        ServerLevel level;
        MilitaryTableLayout layout;
        Level level2 = player.m_9236_();
        if (level2 instanceof ServerLevel && (layout = GameRoomManager.layout(level = (ServerLevel)level2, clickedPos)) != null) {
            return layout.anchor();
        }
        return clickedPos;
    }

    public static MilitaryTableLayout layout(ServerLevel level, BlockPos touched) {
        return MilitaryTableLayout.detect(level, touched, pos -> GameRoomManager.isTable(level, pos)).orElse(null);
    }

    public static boolean inRange(ServerPlayer player, MilitaryTableLayout layout) {
        return layout.members().stream().anyMatch(pos -> player.m_20275_((double)pos.m_123341_() + 0.5, (double)pos.m_123342_() + 0.5, (double)pos.m_123343_() + 0.5) <= 64.0);
    }

    public static void open(ServerPlayer player, BlockPos touched) {
        Level level = player.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        MilitaryTableLayout layout = GameRoomManager.layout(level2, touched);
        if (layout == null || !GameRoomManager.inRange(player, layout)) {
            player.m_213846_((Component)Component.m_237115_((String)"message.sanguosha.military.invalid_component").m_130940_(ChatFormatting.RED));
            return;
        }
        MilitaryRoomSavedData.Room room = MilitaryRoomSavedData.get(level2).resolve(layout);
        GameRoomManager.cleanup(level2, layout, room);
        BlockEntity blockEntity = level2.m_7702_(touched);
        if (!(blockEntity instanceof TableBlockEntity)) {
            player.m_213846_((Component)Component.m_237115_((String)"message.sanguosha.military.no_game_table").m_130940_(ChatFormatting.RED));
            return;
        }
        TableBlockEntity table = (TableBlockEntity)blockEntity;
        NetworkHooks.openScreen((ServerPlayer)player, (MenuProvider)table, buffer -> {
            buffer.m_130064_(touched);
            buffer.m_130064_(layout.anchor());
        });
        GameRoomManager.sendState(player, room);
    }

    public static boolean toggleHorizontal(ServerPlayer player, BlockPos clickedPos, BlockPos claimedAnchor, int slot) {
        TableBlockEntity table;
        GameTable2Menu menu;
        ServerLevel level;
        block8: {
            block7: {
                Level level2 = player.m_9236_();
                if (!(level2 instanceof ServerLevel)) break block7;
                level = (ServerLevel)level2;
                if (slot >= 0 && slot < 7) break block8;
            }
            return false;
        }
        MilitaryTableLayout layout = GameRoomManager.layout(level, clickedPos);
        if (!(layout != null && layout.contains(clickedPos) && layout.anchor().equals((Object)claimedAnchor) && GameRoomManager.inRange(player, layout))) {
            return false;
        }
        MilitaryRoomSavedData.Room room = MilitaryRoomSavedData.get(level).get(claimedAnchor);
        if (room == null || !room.tables().equals(Set.copyOf(layout.members()))) {
            return false;
        }
        AbstractContainerMenu abstractContainerMenu = player.f_36096_;
        if (!(abstractContainerMenu instanceof GameTable2Menu && (menu = (GameTable2Menu)abstractContainerMenu).blockPos().equals((Object)clickedPos) && menu.roomAnchor().equals((Object)claimedAnchor) && menu.m_6875_((Player)player))) {
            return false;
        }
        BlockEntity blockEntity = level.m_7702_(clickedPos);
        if (!(blockEntity instanceof TableBlockEntity) || (table = (TableBlockEntity)blockEntity).items().getStackInSlot(slot).m_41619_()) {
            return false;
        }
        return table.rotateSlot(slot);
    }

    public static void action(ServerPlayer player, BlockPos clickedPos, BlockPos claimedAnchor, Action action) {
        MilitaryRoomSavedData.Room room;
        MilitaryRoomSavedData data;
        MilitaryTableLayout layout;
        ServerLevel level;
        block20: {
            block19: {
                Level level2 = player.m_9236_();
                if (!(level2 instanceof ServerLevel)) {
                    return;
                }
                level = (ServerLevel)level2;
                layout = GameRoomManager.layout(level, clickedPos);
                if (layout == null || !GameRoomManager.inRange(player, layout)) {
                    GameRoomManager.reject(player, null, "message.sanguosha.military.action_invalid", new Object[0]);
                    return;
                }
                data = MilitaryRoomSavedData.get(level);
                MilitaryRoomSavedData.Room room2 = room = action == Action.SYNC ? data.resolve(layout) : data.get(layout.anchor());
                if (room == null || !room.tables().equals(Set.copyOf(layout.members()))) {
                    GameRoomManager.reject(player, null, "message.sanguosha.military.action_invalid", new Object[0]);
                    return;
                }
                GameRoomManager.cleanup(level, layout, room);
                AbstractContainerMenu abstractContainerMenu = player.f_36096_;
                if (!(abstractContainerMenu instanceof GameTable2Menu)) break block19;
                GameTable2Menu menu = (GameTable2Menu)abstractContainerMenu;
                if (layout.contains(clickedPos) && menu.blockPos().equals((Object)clickedPos) && menu.roomAnchor().equals((Object)claimedAnchor) && claimedAnchor.equals((Object)layout.anchor()) && level.m_7702_(clickedPos) instanceof TableBlockEntity && menu.m_6875_((Player)player)) break block20;
            }
            GameRoomManager.reject(player, room, "message.sanguosha.military.menu_invalid", new Object[0]);
            return;
        }
        switch (action) {
            case SYNC: {
                GameRoomManager.sendState(player, room);
                return;
            }
            case READY: {
                if (room.phase() != MilitaryRoomSavedData.Phase.LOBBY) {
                    GameRoomManager.reject(player, room, "message.sanguosha.military.not_lobby", new Object[0]);
                    return;
                }
                if (!room.members().containsKey(player.m_20148_()) && room.members().size() >= 8) {
                    GameRoomManager.reject(player, room, "message.sanguosha.military.room_full", new Object[0]);
                    return;
                }
                room.ready(player.m_20148_(), player.m_36316_().getName(), level.m_46467_());
                break;
            }
            case UNREADY: {
                if (room.phase() != MilitaryRoomSavedData.Phase.LOBBY) {
                    GameRoomManager.reject(player, room, "message.sanguosha.military.not_lobby", new Object[0]);
                    return;
                }
                room.unready(player.m_20148_());
                break;
            }
            case START: {
                if (!player.m_20148_().equals(room.owner())) {
                    GameRoomManager.reject(player, room, "message.sanguosha.military.owner_only", new Object[0]);
                    return;
                }
                if (!GameRoomManager.canStart(level, layout, room)) {
                    GameRoomManager.reject(player, room, "message.sanguosha.military.start_requirements", 2);
                    return;
                }
                GameRoomManager.start(level, layout, room, player);
                break;
            }
            case DISBAND: {
                if (!GameRoomManager.canDisband(true, player.m_20148_(), room.owner(), room.phase())) {
                    GameRoomManager.reject(player, room, "message.sanguosha.military.owner_only", new Object[0]);
                    return;
                }
                GameRoomManager.disband(level, layout, room);
                return;
            }
        }
        data.m_77762_();
        GameRoomManager.sync(level, layout, room);
    }

    private static void disband(ServerLevel level, MilitaryTableLayout layout, MilitaryRoomSavedData.Room room) {
        UUID owner;
        MilitaryRoomSavedData data = MilitaryRoomSavedData.get(level);
        if (data.get(room.anchor()) != room || data.remove(room.anchor()) == null) {
            return;
        }
        LinkedHashMap<UUID, List<ItemStack>> returns = new LinkedHashMap<>();
        for (BlockPos tablePos : layout.members()) {
            TableBlockEntity table;
            ItemStack stack;
            BlockEntity blockEntity = level.m_7702_(tablePos);
            if (!(blockEntity instanceof TableBlockEntity) || (owner = TableBlockEntity.healthOwner(stack = (table = (TableBlockEntity)blockEntity).items().getStackInSlot(5))) == null || !GameRoomManager.isHealthCard(stack)) continue;
            returns.computeIfAbsent(owner, ignored -> new ArrayList<>()).add(stack.m_255036_(1));
            table.items().setStackInSlot(5, ItemStack.f_41583_);
        }
        LinkedHashSet<UUID> notify = new LinkedHashSet<UUID>(room.members().keySet());
        notify.addAll(room.participants());
        notify.addAll(returns.keySet());
        if (room.owner() != null) {
            notify.add(room.owner());
        }
        for (ServerPlayer observer : level.m_7654_().m_6846_().m_11314_()) {
            GameTable2Menu menu;
            AbstractContainerMenu openMenu = observer.f_36096_;
            if (observer.m_9236_() != level || !(openMenu instanceof GameTable2Menu) || !layout.contains((menu = (GameTable2Menu)openMenu).blockPos())) continue;
            notify.add(observer.m_20148_());
        }
        for (UUID id : notify) {
            ServerPlayer target = level.m_7654_().m_6846_().m_11259_(id);
            List<ItemStack> stacks = returns.getOrDefault(id, List.of());
            if (target != null && target.m_9236_() == level) {
                for (ItemStack stack : stacks) {
                    GameRoomManager.giveOrDropAtAnchor(level, target, layout.anchor(), stack);
                }
                ModNetwork.sendRoomDisbanded(target, room.anchor());
                AbstractContainerMenu openMenu = target.f_36096_;
                if (openMenu instanceof GameTable2Menu && layout.contains(((GameTable2Menu) openMenu).blockPos())) {
                    target.m_6915_();
                }
                continue;
            }
            for (ItemStack stack : stacks) {
                data.queueReturn(id, stack);
            }
        }
        data.m_77762_();
    }

    private static void giveOrDropAtAnchor(ServerLevel level, ServerPlayer player, BlockPos anchor, ItemStack stack) {
        ItemStack one = stack.m_255036_(1);
        if (!player.m_150109_().m_36054_(one)) {
            Containers.m_18992_((Level)level, (double)((double)anchor.m_123341_() + 0.5), (double)((double)anchor.m_123342_() + 1.0), (double)((double)anchor.m_123343_() + 0.5), (ItemStack)one);
        }
        player.m_150109_().m_6596_();
        player.f_36095_.m_38946_();
        if (player.f_36096_ != player.f_36095_) {
            player.f_36096_.m_38946_();
        }
    }

    public static void login(ServerPlayer player) {
        Level level = player.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        for (ItemStack stack : MilitaryRoomSavedData.get(level2).takeReturns(player.m_20148_())) {
            if (!GameRoomManager.isHealthCard(stack)) continue;
            GameRoomManager.giveOrDropAtAnchor(level2, player, player.m_20183_(), stack);
        }
    }

    private static void start(ServerLevel level, MilitaryTableLayout layout, MilitaryRoomSavedData.Room room, ServerPlayer player) {
        GameRoomManager.cleanup(level, layout, room);
        if (!player.m_20148_().equals(room.owner())) {
            return;
        }
        if (!GameRoomManager.canStart(level, layout, room)) {
            player.m_213846_((Component)Component.m_237110_((String)"message.sanguosha.military.start_requirements", (Object[])new Object[]{2}).m_130940_(ChatFormatting.RED));
            return;
        }
        int participantCount = room.members().size();
        List<String> pool = GameRoomManager.generalPool();
        if (pool.size() < 5 || pool.size() < participantCount) {
            player.m_213846_((Component)Component.m_237110_((String)"message.sanguosha.military.pool_small", (Object[])new Object[]{pool.size(), participantCount, 5}).m_130940_(ChatFormatting.RED));
            return;
        }
        Collections.shuffle(pool, new Random(level.f_46441_.m_188505_()));
        Map<UUID, List<String>> offers = GameRoomManager.allocateOffers(new ArrayList<UUID>(room.members().keySet()), pool);
        room.begin(offers);
        MilitaryRoomSavedData.get(level).m_77762_();
        for (UUID id : room.members().keySet()) {
            ServerPlayer target = level.m_7654_().m_6846_().m_11259_(id);
            if (target == null) continue;
            ModNetwork.sendGeneralOffer(target, room.anchor(), room.candidates(id));
        }
    }

    public static boolean readyButtonActive(boolean stateReceived, int phase) {
        return stateReceived && phase == MilitaryRoomSavedData.Phase.LOBBY.ordinal();
    }

    public static boolean startButtonVisible(boolean stateReceived, boolean owner, int phase) {
        return stateReceived && owner && phase == MilitaryRoomSavedData.Phase.LOBBY.ordinal();
    }

    public static boolean disbandButtonVisible(boolean stateReceived, boolean owner, int phase) {
        return stateReceived && owner && GameRoomManager.validPhaseOrdinal(phase);
    }

    static boolean validPhaseOrdinal(int phase) {
        return phase >= 0 && phase < MilitaryRoomSavedData.Phase.values().length;
    }

    static boolean canDisband(boolean roomExists, UUID actor, UUID owner, MilitaryRoomSavedData.Phase phase) {
        return roomExists && actor != null && actor.equals(owner) && phase != null;
    }

    static boolean canOperateTable(boolean roomExists, MilitaryRoomSavedData.Phase phase) {
        return roomExists && phase != null;
    }

    public static boolean shouldRequestState(boolean stateReceived) {
        return !stateReceived;
    }

    static boolean canStart(MilitaryRoomSavedData.Phase phase, int eligibleReadyMembers) {
        return phase == MilitaryRoomSavedData.Phase.LOBBY && eligibleReadyMembers >= 2;
    }

    private static boolean canStart(ServerLevel level, MilitaryTableLayout layout, MilitaryRoomSavedData.Room room) {
        int eligibleReadyMembers = 0;
        for (MilitaryRoomSavedData.Member member : room.members().values()) {
            ServerPlayer participant = level.m_7654_().m_6846_().m_11259_(member.id());
            if (!member.ready() || participant == null || participant.m_9236_() != level || !GameRoomManager.inRange(participant, layout)) continue;
            ++eligibleReadyMembers;
        }
        return GameRoomManager.canStart(room.phase(), eligibleReadyMembers);
    }

    static Map<UUID, List<String>> allocateOffers(List<UUID> players, List<String> shuffledPool) {
        LinkedHashMap<UUID, List<String>> offers = new LinkedHashMap<UUID, List<String>>();
        boolean globallyDistinct = shuffledPool.size() >= players.size() * 5;
        for (int playerIndex = 0; playerIndex < players.size(); ++playerIndex) {
            ArrayList<String> offer = new ArrayList<String>(5);
            int start = globallyDistinct ? playerIndex * 5 : playerIndex;
            for (int i = 0; i < 5; ++i) {
                offer.add(shuffledPool.get((start + i) % shuffledPool.size()));
            }
            offers.put(players.get(playerIndex), List.copyOf(offer));
        }
        return offers;
    }

    public static void select(ServerPlayer player, BlockPos claimedAnchor, String generalId) {
        Level level = player.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        MilitaryTableLayout layout = GameRoomManager.layout(level2, claimedAnchor);
        if (layout == null || !layout.anchor().equals((Object)claimedAnchor) || !GameRoomManager.inRange(player, layout)) {
            GameRoomManager.selectionFailure(player, claimedAnchor, "\u623f\u95f4\u6216\u8ddd\u79bb\u6821\u9a8c\u5931\u8d25");
            return;
        }
        MilitaryRoomSavedData data = MilitaryRoomSavedData.get(level2);
        MilitaryRoomSavedData.Room room = data.get(claimedAnchor);
        if (room == null || !room.tables().equals(Set.copyOf(layout.members()))) {
            GameRoomManager.selectionFailure(player, claimedAnchor, "\u623f\u95f4\u5df2\u5931\u6548");
            return;
        }
        GameRoomManager.cleanup(level2, layout, room);
        UUID id = player.m_20148_();
        if (!GeneralAssetManager.contains(generalId)) {
            GameRoomManager.selectionFailure(player, claimedAnchor, "\u65e0\u6548\u6b66\u5c06 ID");
            return;
        }
        MilitaryRoomSavedData.SelectResult result = room.select(id, generalId);
        if (result != MilitaryRoomSavedData.SelectResult.ACCEPTED) {
            String reason = switch (result) {
                case NOT_SELECTING -> "\u5f53\u524d\u4e0d\u5728\u9009\u5c06\u9636\u6bb5";
                case NOT_PARTICIPANT -> "\u4f60\u4e0d\u662f\u672c\u5c40\u51bb\u7ed3\u7684\u53c2\u6218\u8005";
                case ALREADY_SELECTED -> "\u4f60\u5df2\u7ecf\u786e\u8ba4\u8fc7\u6b66\u5c06";
                case NOT_OFFERED -> "\u8be5\u6b66\u5c06\u4e0d\u5728\u4f60\u7684\u5019\u9009\u4e2d";
                case TAKEN -> "\u8be5\u6b66\u5c06\u5df2\u88ab\u5176\u4ed6\u73a9\u5bb6\u9009\u62e9";
                default -> "\u9009\u62e9\u5931\u8d25";
            };
            GameRoomManager.selectionFailure(player, claimedAnchor, reason);
            if (result == MilitaryRoomSavedData.SelectResult.TAKEN) {
                GameRoomManager.refreshUnselectedOffers(level2, room);
            }
            return;
        }
        GameRoomManager.awardOne(level2, layout, room, player, generalId);
        data.m_77762_();
        boolean complete = room.phase() == MilitaryRoomSavedData.Phase.COMPLETE;
        ModNetwork.sendGeneralSelectionResult(player, room.anchor(), true, complete, "\u5df2\u786e\u8ba4 " + generalId);
        if (complete) {
            GameRoomManager.broadcastSelectionComplete(level2, room);
        } else {
            GameRoomManager.refreshUnselectedOffers(level2, room);
        }
        GameRoomManager.sync(level2, layout, room);
    }

    private static void selectionFailure(ServerPlayer player, BlockPos anchor, String reason) {
        ModNetwork.sendGeneralSelectionResult(player, anchor, false, false, reason);
    }

    private static void refreshUnselectedOffers(ServerLevel level, MilitaryRoomSavedData.Room room) {
        List<String> available = GameRoomManager.generalPool().stream().filter(id -> !room.selectedGenerals().contains(id)).toList();
        for (UUID id2 : room.participants()) {
            if (room.selection(id2) != null) continue;
            ArrayList<String> preferred = new ArrayList<String>(room.candidates(id2).stream().filter(available::contains).distinct().toList());
            for (String candidate : available) {
                if (preferred.contains(candidate) || preferred.size() >= 5) continue;
                preferred.add(candidate);
            }
            if (preferred.size() > 5) {
                preferred = new ArrayList(preferred.subList(0, 5));
            }
            room.replaceCandidates(id2, preferred);
            ServerPlayer target = level.m_7654_().m_6846_().m_11259_(id2);
            if (target == null) continue;
            ModNetwork.sendGeneralOffer(target, room.anchor(), preferred);
        }
        MilitaryRoomSavedData.get(level).m_77762_();
    }

    private static List<String> generalPool() {
        return new ArrayList<String>(GeneralAssetManager.assets().stream().map(GeneralAssetManager.GeneralAsset::id).distinct().toList());
    }

    private static void awardOne(ServerLevel level, MilitaryTableLayout layout, MilitaryRoomSavedData.Room room, ServerPlayer player, String generalId) {
        UUID id = player.m_20148_();
        if (room.awarded(id)) {
            return;
        }
        ItemStack stack = GeneralCardItem.create(generalId, 1);
        GeneralCardItem.setGeneral(stack, generalId);
        room.markAwarded(id);
        MilitaryRoomSavedData.get(level).m_77762_();
        if (!player.m_150109_().m_36054_(stack)) {
            ItemStack remainder = stack.m_41777_();
            player.m_36176_(remainder, false);
        }
        player.m_150109_().m_6596_();
        player.f_36095_.m_38946_();
    }

    private static void broadcastSelectionComplete(ServerLevel level, MilitaryRoomSavedData.Room room) {
        for (UUID id : room.participants()) {
            ServerPlayer target = level.m_7654_().m_6846_().m_11259_(id);
            if (target == null) continue;
            ModNetwork.sendGeneralSelectionResult(target, room.anchor(), true, true, "\u672c\u5c40\u9009\u5c06\u5b8c\u6210");
        }
    }

    public static void cleanup(ServerLevel level, MilitaryTableLayout layout) {
        MilitaryRoomSavedData data = MilitaryRoomSavedData.get(level);
        MilitaryRoomSavedData.Room room = data.get(layout.anchor());
        if (room != null) {
            GameRoomManager.cleanup(level, layout, room);
        }
    }

    private static void cleanup(ServerLevel level, MilitaryTableLayout layout, MilitaryRoomSavedData.Room room) {
        if (!room.tables().equals(Set.copyOf(layout.members()))) {
            return;
        }
        ArrayList<UUID> remove = new ArrayList<>();
        room.members().keySet().forEach(id -> {
            ServerPlayer player = level.m_7654_().m_6846_().m_11259_(id);
            if (player == null || player.m_9236_() != level || !GameRoomManager.inRange(player, layout)) {
                remove.add(id);
            }
        });
        boolean wasSelecting = room.phase() == MilitaryRoomSavedData.Phase.SELECTING;
        remove.forEach(room::remove);
        if (!remove.isEmpty()) {
            MilitaryRoomSavedData.get(level).m_77762_();
            if (wasSelecting && room.phase() == MilitaryRoomSavedData.Phase.COMPLETE) {
                GameRoomManager.broadcastSelectionComplete(level, room);
            }
        }
    }

    public static void invalidateAt(ServerLevel level, BlockPos broken) {
        MilitaryRoomSavedData data = MilitaryRoomSavedData.get(level);
        List<MilitaryRoomSavedData.Room> affected = data.snapshot().stream().filter(room -> room.tables().contains(broken)).toList();
        for (MilitaryRoomSavedData.Room old : affected) {
            data.remove(old.anchor());
            ArrayList<MilitaryTableLayout> components = new ArrayList<MilitaryTableLayout>();
            for (BlockPos seed : old.tables()) {
                MilitaryTableLayout component2;
                if (seed.equals((Object)broken) || !GameRoomManager.isTable(level, seed) || (component2 = GameRoomManager.layout(level, seed)) == null || !components.stream().noneMatch(found -> found.anchor().equals((Object)component2.anchor()))) continue;
                components.add(component2);
            }
            components.forEach(data::resolve);
            for (MilitaryRoomSavedData.Member member : old.members().values()) {
                MilitaryTableLayout destination;
                ServerPlayer player = level.m_7654_().m_6846_().m_11259_(member.id());
                if (player == null || player.m_9236_() != level || (destination = (MilitaryTableLayout)components.stream().filter(component -> GameRoomManager.inRange(player, component)).min(Comparator.comparingDouble(component -> GameRoomManager.nearestDistance(player, component))).orElse(null)) == null || !member.ready()) continue;
                data.resolve(destination).ready(member.id(), member.name(), level.m_46467_());
            }
        }
        data.m_77762_();
    }

    private static double nearestDistance(ServerPlayer player, MilitaryTableLayout layout) {
        return layout.members().stream().mapToDouble(pos -> player.m_20275_((double)pos.m_123341_() + 0.5, (double)pos.m_123342_() + 0.5, (double)pos.m_123343_() + 0.5)).min().orElse(Double.MAX_VALUE);
    }

    public static void tick(ServerPlayer player) {
        ServerLevel level;
        Level level2 = player.m_9236_();
        if (!(level2 instanceof ServerLevel) || (level = (ServerLevel)level2).m_46467_() % 20L != 0L) {
            return;
        }
        MilitaryRoomSavedData data = MilitaryRoomSavedData.get(level);
        for (MilitaryRoomSavedData.Room room : data.snapshot()) {
            MilitaryTableLayout current;
            if (!room.members().containsKey(player.m_20148_())) continue;
            BlockPos seed = room.tables().stream().filter(pos -> GameRoomManager.isTable(level, pos)).findFirst().orElse(null);
            MilitaryTableLayout militaryTableLayout = current = seed == null ? null : GameRoomManager.layout(level, seed);
            if (current == null) {
                data.invalidate(room.anchor());
                continue;
            }
            MilitaryRoomSavedData.Room resolved = data.resolve(current);
            GameRoomManager.cleanup(level, current, resolved);
            GameRoomManager.sync(level, current, resolved);
        }
    }

    public static void disconnect(ServerPlayer player) {
        Level level = player.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        MilitaryRoomSavedData data = MilitaryRoomSavedData.get(level2);
        for (MilitaryRoomSavedData.Room room : data.snapshot()) {
            MilitaryTableLayout current;
            if (!room.members().containsKey(player.m_20148_()) && !room.participants().contains(player.m_20148_())) continue;
            boolean wasSelecting = room.phase() == MilitaryRoomSavedData.Phase.SELECTING;
            room.remove(player.m_20148_());
            BlockPos seed = room.tables().stream().filter(pos -> GameRoomManager.isTable(level2, pos)).findFirst().orElse(null);
            MilitaryTableLayout militaryTableLayout = current = seed == null ? null : GameRoomManager.layout(level2, seed);
            if (wasSelecting && room.phase() == MilitaryRoomSavedData.Phase.COMPLETE) {
                GameRoomManager.broadcastSelectionComplete(level2, room);
            }
            if (current == null) continue;
            GameRoomManager.sync(level2, current, room);
        }
        data.m_77762_();
    }

    public static void clear() {
    }

    public static void syncRoom(ServerLevel level, MilitaryTableLayout layout, MilitaryRoomSavedData.Room room) {
        GameRoomManager.sync(level, layout, room);
    }

    public static boolean flipIdentity(ServerPlayer player, BlockPos clickedPos, BlockPos claimedAnchor) {
        TableBlockEntity table;
        GameTable2Menu menu;
        Level level = player.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return false;
        }
        ServerLevel level2 = (ServerLevel)level;
        MilitaryTableLayout layout = GameRoomManager.layout(level2, clickedPos);
        if (!(layout != null && layout.anchor().equals((Object)claimedAnchor) && layout.contains(clickedPos) && GameRoomManager.inRange(player, layout))) {
            return false;
        }
        AbstractContainerMenu abstractContainerMenu = player.f_36096_;
        if (!(abstractContainerMenu instanceof GameTable2Menu && (menu = (GameTable2Menu)abstractContainerMenu).blockPos().equals((Object)clickedPos) && menu.roomAnchor().equals((Object)claimedAnchor) && menu.m_6875_((Player)player))) {
            return false;
        }
        MilitaryRoomSavedData.Room room = MilitaryRoomSavedData.get(level2).get(layout.anchor());
        if (room == null || !room.tables().equals(Set.copyOf(layout.members())) || !GameRoomManager.canOperateTable(true, room.phase())) {
            return false;
        }
        BlockEntity blockEntity = level2.m_7702_(clickedPos);
        return blockEntity instanceof TableBlockEntity && (table = (TableBlockEntity)blockEntity).toggleIdentityFace();
    }

    public static void setRoomHealth(ServerPlayer player, BlockPos clickedPos, BlockPos claimedAnchor, int slot, int health) {
        GameTable2Menu menu;
        ServerLevel level;
        block9: {
            block8: {
                Level level2 = player.m_9236_();
                if (!(level2 instanceof ServerLevel)) break block8;
                level = (ServerLevel)level2;
                if (slot == 5 && health >= 1 && health <= 5) break block9;
            }
            return;
        }
        MilitaryTableLayout layout = GameRoomManager.layout(level, clickedPos);
        if (!(layout != null && layout.anchor().equals((Object)claimedAnchor) && layout.contains(clickedPos) && GameRoomManager.inRange(player, layout))) {
            return;
        }
        AbstractContainerMenu abstractContainerMenu = player.f_36096_;
        if (!(abstractContainerMenu instanceof GameTable2Menu && (menu = (GameTable2Menu)abstractContainerMenu).blockPos().equals((Object)clickedPos) && menu.roomAnchor().equals((Object)claimedAnchor) && menu.m_6875_((Player)player))) {
            return;
        }
        MilitaryRoomSavedData.Room room = MilitaryRoomSavedData.get(level).get(layout.anchor());
        if (room == null || !room.tables().equals(Set.copyOf(layout.members())) || !GameRoomManager.canOperateTable(true, room.phase())) {
            return;
        }
        BlockEntity blockEntity = level.m_7702_(clickedPos);
        if (!(blockEntity instanceof TableBlockEntity)) {
            return;
        }
        TableBlockEntity table = (TableBlockEntity)blockEntity;
        ItemStack stack = table.items().getStackInSlot(5);
        if (!GameRoomManager.isHealthCard(stack) || stack.m_41613_() != 1) {
            return;
        }
        HealthCardItem.setHealth(stack, health);
        table.assignHealthOwner(player.m_20148_());
        table.sync();
        ModNetwork.sendRoomHealth(player, clickedPos, room.anchor(), health);
        GameRoomManager.sync(level, layout, room);
    }

    private static void sync(ServerLevel level, MilitaryTableLayout layout, MilitaryRoomSavedData.Room room) {
        for (ServerPlayer target : level.m_7654_().m_6846_().m_11314_()) {
            GameTable2Menu menu;
            boolean observing;
            if (target.m_9236_() != level) continue;
            boolean member = room.members().containsKey(target.m_20148_());
            AbstractContainerMenu abstractContainerMenu = target.f_36096_;
            boolean bl = observing = abstractContainerMenu instanceof GameTable2Menu && layout.contains((menu = (GameTable2Menu)abstractContainerMenu).blockPos()) && GameRoomManager.inRange(target, layout);
            if (!member && !observing) continue;
            GameRoomManager.sendState(target, room);
        }
    }

    private static void sendState(ServerPlayer player, MilitaryRoomSavedData.Room room) {
        TableBlockEntity table;
        ItemStack health;
        GameTable2Menu menu;
        List<String> names = room.members().values().stream().map(m -> (m.ready() ? "\u00a7a" : "\u00a77") + m.name()).toList();
        MilitaryRoomSavedData.Member member = room.members().get(player.m_20148_());
        ModNetwork.sendMilitaryRoomState(player, room.anchor(), player.m_20148_().equals(room.owner()), member != null && member.ready(), room.phase().ordinal(), names);
        AbstractContainerMenu openMenu = player.f_36096_;
        if (openMenu instanceof GameTable2Menu && (menu = (GameTable2Menu)openMenu).roomAnchor().equals(room.anchor())) {
            BlockEntity tableEntity = player.m_9236_().m_7702_(menu.blockPos());
            if (tableEntity instanceof TableBlockEntity && GameRoomManager.isHealthCard(health = (table = (TableBlockEntity)tableEntity).items().getStackInSlot(5))) {
                ModNetwork.sendRoomHealth(player, menu.blockPos(), room.anchor(), HealthCardItem.getHealth(health));
            }
        }
    }

    private static void reject(ServerPlayer player, MilitaryRoomSavedData.Room room, String key, Object ... args) {
        player.m_213846_((Component)Component.m_237110_((String)key, (Object[])args).m_130940_(ChatFormatting.RED));
        if (room != null) {
            GameRoomManager.sendState(player, room);
        }
    }

    public static enum Action {
        READY,
        UNREADY,
        START,
        SYNC,
        DISBAND;

    }
}

