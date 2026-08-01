package cn.solo.sanguosha.game;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public record MilitaryTableLayout(BlockPos anchor, List<BlockPos> members) {
    public static final int MIN_TABLE_COUNT = 8;
    public static final Comparator<BlockPos> STABLE_POS_ORDER = Comparator.<BlockPos>comparingInt(pos -> pos.m_123341_()).thenComparingInt(pos -> pos.m_123342_()).thenComparingInt(pos -> pos.m_123343_());

    public MilitaryTableLayout {
        anchor = anchor.m_7949_();
        members = members.stream().map(BlockPos::m_7949_).sorted(STABLE_POS_ORDER).toList();
        if (members.size() < 8 || !members.contains(anchor)) {
            throw new IllegalArgumentException("\u6709\u6548\u519b\u516b\u623f\u95f4\u5fc5\u987b\u5305\u542b\u951a\u70b9\u4e14\u81f3\u5c11\u6709\u516b\u5f20\u684c\u5b50");
        }
    }

    public static Optional<MilitaryTableLayout> detect(ServerLevel level, BlockPos touched, Predicate<BlockPos> isTable) {
        return MilitaryTableLayout.detect(touched, isTable);
    }

    static Optional<MilitaryTableLayout> detect(BlockPos touched, Predicate<BlockPos> isTable) {
        if (!isTable.test(touched)) {
            return Optional.empty();
        }
        HashSet<BlockPos> visited = new HashSet<BlockPos>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<BlockPos>();
        BlockPos start = touched.m_7949_();
        visited.add(start);
        queue.add(start);
        while (!queue.isEmpty()) {
            BlockPos current = (BlockPos)queue.removeFirst();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = current.m_121945_(direction).m_7949_();
                if (next.m_123342_() != touched.m_123342_() || visited.contains(next) || !isTable.test(next)) continue;
                visited.add(next);
                queue.addLast(next);
            }
        }
        if (visited.size() < 8) {
            return Optional.empty();
        }
        ArrayList<BlockPos> members = new ArrayList<BlockPos>(visited);
        members.sort(STABLE_POS_ORDER);
        return Optional.of(new MilitaryTableLayout((BlockPos)members.get(0), members));
    }

    public boolean contains(BlockPos pos) {
        return this.members.contains(pos);
    }
}

