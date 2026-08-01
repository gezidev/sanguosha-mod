/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.LongTag
 *  net.minecraft.nbt.StringTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.saveddata.SavedData
 */
package cn.solo.sanguosha.game;

import cn.solo.sanguosha.game.MilitaryTableLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;

public final class MilitaryRoomSavedData
extends SavedData {
    private static final String NAME = "sanguosha_military_rooms";
    private final Map<BlockPos, Room> rooms = new LinkedHashMap<BlockPos, Room>();
    private final Map<UUID, List<ItemStack>> pendingReturns = new LinkedHashMap<UUID, List<ItemStack>>();

    public static MilitaryRoomSavedData get(ServerLevel level) {
        return (MilitaryRoomSavedData)level.m_8895_().m_164861_(MilitaryRoomSavedData::load, MilitaryRoomSavedData::new, NAME);
    }

    public Room resolve(MilitaryTableLayout layout) {
        LinkedHashSet<BlockPos> current = new LinkedHashSet<BlockPos>(layout.members());
        List<Room> overlaps = this.rooms.values().stream().filter(room -> room.overlaps(current)).toList();
        Room room2 = overlaps.stream().max(Comparator.<Room>comparingInt(candidate -> candidate.overlapCount(current)).thenComparing(candidate -> candidate.anchor, MilitaryTableLayout.STABLE_POS_ORDER.reversed())).orElse(null);
        if (room2 == null) {
            room2 = new Room(layout.anchor(), current);
        }
        boolean structuralChange = !room2.anchor.equals((Object)layout.anchor()) || !room2.tables.equals(current) || overlaps.size() > 1;
        overlaps.forEach(old -> this.rooms.remove(old.anchor));
        for (Room old2 : overlaps) {
            if (old2 == room2) continue;
            room2.absorbLobbyMembers(old2);
        }
        room2.anchor = layout.anchor().m_7949_();
        room2.tables.clear();
        room2.tables.addAll(current);
        if (structuralChange && overlaps.size() > 1) {
            room2.resetToLobby();
        }
        this.rooms.put(room2.anchor, room2);
        this.m_77762_();
        return room2;
    }

    public Room get(BlockPos anchor) {
        return this.rooms.get(anchor);
    }

    public Iterable<Room> rooms() {
        return this.rooms.values();
    }

    public List<Room> snapshot() {
        return new ArrayList<Room>(this.rooms.values());
    }

    public void invalidate(BlockPos anchor) {
        if (this.rooms.remove(anchor) != null) {
            this.m_77762_();
        }
    }

    public Room remove(BlockPos anchor) {
        Room removed = this.rooms.remove(anchor);
        if (removed != null) {
            this.m_77762_();
        }
        return removed;
    }

    public void queueReturn(UUID player, ItemStack stack) {
        if (stack.m_41619_()) {
            return;
        }
        this.pendingReturns.computeIfAbsent(player, ignored -> new ArrayList()).add(stack.m_255036_(1));
        this.m_77762_();
    }

    public List<ItemStack> takeReturns(UUID player) {
        List<ItemStack> returns = this.pendingReturns.remove(player);
        if (returns != null) {
            this.m_77762_();
        }
        return returns == null ? List.of() : returns;
    }

    public CompoundTag m_7176_(CompoundTag root) {
        ListTag list = new ListTag();
        this.rooms.values().forEach(room -> list.add(room.save()));
        root.m_128365_("Rooms", (Tag)list);
        ListTag pending = new ListTag();
        this.pendingReturns.forEach((id, stacks) -> stacks.forEach(stack -> {
            CompoundTag entry = new CompoundTag();
            entry.m_128362_("Player", id);
            entry.m_128365_("Stack", (Tag)stack.m_41739_(new CompoundTag()));
            pending.add(entry);
        }));
        root.m_128365_("PendingReturns", (Tag)pending);
        return root;
    }

    private static MilitaryRoomSavedData load(CompoundTag root) {
        MilitaryRoomSavedData data = new MilitaryRoomSavedData();
        ListTag list = root.m_128437_("Rooms", 10);
        for (int i = 0; i < list.size(); ++i) {
            Room room = Room.load(list.m_128728_(i));
            data.rooms.put(room.anchor, room);
        }
        ListTag pending = root.m_128437_("PendingReturns", 10);
        for (int i = 0; i < pending.size(); ++i) {
            ItemStack stack;
            CompoundTag entry = pending.m_128728_(i);
            if (!entry.m_128403_("Player") || (stack = ItemStack.m_41712_((CompoundTag)entry.m_128469_("Stack"))).m_41619_()) continue;
            data.pendingReturns.computeIfAbsent(entry.m_128342_("Player"), ignored -> new ArrayList()).add(stack);
        }
        return data;
    }

    public static final class Room {
        private BlockPos anchor;
        private final LinkedHashSet<BlockPos> tables = new LinkedHashSet();
        private UUID owner;
        private Phase phase = Phase.LOBBY;
        private final LinkedHashMap<UUID, Member> members = new LinkedHashMap();
        private final LinkedHashSet<UUID> participants = new LinkedHashSet();
        private final Map<UUID, List<String>> candidates = new LinkedHashMap<UUID, List<String>>();
        private final Map<UUID, String> selections = new LinkedHashMap<UUID, String>();
        private final LinkedHashSet<UUID> awarded = new LinkedHashSet();
        private final Map<UUID, ItemStack> health = new LinkedHashMap<UUID, ItemStack>();

        private Room(BlockPos anchor, Set<BlockPos> tables) {
            this.anchor = anchor.m_7949_();
            this.tables.addAll(tables);
        }

        public BlockPos anchor() {
            return this.anchor;
        }

        public Set<BlockPos> tables() {
            return Set.copyOf(this.tables);
        }

        public UUID owner() {
            return this.owner;
        }

        public Phase phase() {
            return this.phase;
        }

        public Map<UUID, Member> members() {
            return this.members;
        }

        public Set<UUID> participants() {
            return Collections.unmodifiableSet(new LinkedHashSet<UUID>(this.participants));
        }

        public List<String> candidates(UUID player) {
            return this.candidates.getOrDefault(player, List.of());
        }

        public String selection(UUID player) {
            return this.selections.get(player);
        }

        public Set<String> selectedGenerals() {
            return new LinkedHashSet<String>(this.selections.values());
        }

        public boolean awarded(UUID player) {
            return this.awarded.contains(player);
        }

        public void markAwarded(UUID player) {
            this.awarded.add(player);
        }

        public ItemStack health(UUID player) {
            return this.health.getOrDefault(player, ItemStack.f_41583_);
        }

        public void health(UUID player, ItemStack stack) {
            if (stack.m_41619_()) {
                this.health.remove(player);
            } else {
                this.health.put(player, stack.m_255036_(1));
            }
        }

        public Map<UUID, ItemStack> healthCards() {
            return Collections.unmodifiableMap(this.health);
        }

        public boolean allParticipantsSelected() {
            return !this.participants.isEmpty() && this.selections.keySet().containsAll(this.participants);
        }

        private boolean overlaps(Set<BlockPos> component) {
            return this.tables.stream().anyMatch(component::contains);
        }

        private int overlapCount(Set<BlockPos> component) {
            return (int)this.tables.stream().filter(component::contains).count();
        }

        public void ready(UUID player, String name, long order) {
            Member member = this.members.computeIfAbsent(player, id -> new Member((UUID)id, name, order, false));
            member.name = name;
            member.ready = true;
            if (this.owner == null) {
                this.owner = player;
            }
        }

        public void unready(UUID player) {
            this.remove(player);
        }

        public void remove(UUID player) {
            this.members.remove(player);
            if (this.phase == Phase.SELECTING) {
                this.participants.remove(player);
                this.candidates.remove(player);
                if (this.allParticipantsSelected()) {
                    this.phase = Phase.COMPLETE;
                }
            } else if (this.phase == Phase.LOBBY) {
                this.participants.remove(player);
                this.candidates.remove(player);
                this.selections.remove(player);
                this.awarded.remove(player);
            }
            if (player.equals(this.owner)) {
                this.transferOwner();
            }
        }

        private void transferOwner() {
            this.owner = this.members.values().stream().filter(m -> m.ready).min(Comparator.comparingLong(m -> m.order)).map(m -> m.id).orElse(null);
        }

        public boolean canStart() {
            return this.phase == Phase.LOBBY && this.members.size() >= 2 && this.members.values().stream().allMatch(m -> m.ready);
        }

        public void begin(Map<UUID, List<String>> offers) {
            this.phase = Phase.SELECTING;
            this.participants.clear();
            this.participants.addAll(offers.keySet());
            this.candidates.clear();
            this.candidates.putAll(offers);
            this.selections.clear();
            this.awarded.clear();
        }

        public void replaceCandidates(UUID player, List<String> offer) {
            if (this.participants.contains(player)) {
                this.candidates.put(player, List.copyOf(offer));
            }
        }

        public SelectResult select(UUID player, String id) {
            if (this.phase != Phase.SELECTING) {
                return SelectResult.NOT_SELECTING;
            }
            if (!this.participants.contains(player)) {
                return SelectResult.NOT_PARTICIPANT;
            }
            if (this.selections.containsKey(player)) {
                return SelectResult.ALREADY_SELECTED;
            }
            if (!this.candidates(player).contains(id)) {
                return SelectResult.NOT_OFFERED;
            }
            if (this.selections.containsValue(id)) {
                return SelectResult.TAKEN;
            }
            this.selections.put(player, id);
            if (this.allParticipantsSelected()) {
                this.phase = Phase.COMPLETE;
            }
            return SelectResult.ACCEPTED;
        }

        private void absorbLobbyMembers(Room other) {
            other.members.values().forEach(member -> this.members.merge(member.id, new Member(member.id, member.name, member.order, member.ready), (left, right) -> left.order <= right.order ? left : right));
            this.transferOwner();
        }

        private void resetToLobby() {
            this.phase = Phase.LOBBY;
            this.participants.clear();
            this.candidates.clear();
            this.selections.clear();
            this.awarded.clear();
            this.transferOwner();
        }

        private CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.m_128356_("Anchor", this.anchor.m_121878_());
            ListTag tableList = new ListTag();
            this.tables.forEach(pos -> tableList.add(LongTag.m_128882_((long)pos.m_121878_())));
            tag.m_128365_("Tables", (Tag)tableList);
            if (this.owner != null) {
                tag.m_128362_("Owner", this.owner);
            }
            tag.m_128359_("Phase", this.phase.name());
            ListTag memberList = new ListTag();
            this.members.values().forEach(member -> memberList.add(member.save()));
            tag.m_128365_("Members", (Tag)memberList);
            ListTag participantList = new ListTag();
            this.participants.forEach(id -> participantList.add(StringTag.m_129297_((String)id.toString())));
            tag.m_128365_("Participants", (Tag)participantList);
            ListTag awardedList = new ListTag();
            this.awarded.forEach(id -> awardedList.add(StringTag.m_129297_((String)id.toString())));
            tag.m_128365_("Awarded", (Tag)awardedList);
            CompoundTag offerTag = new CompoundTag();
            this.candidates.forEach((id, offer) -> {
                ListTag ids = new ListTag();
                offer.forEach(value -> ids.add(StringTag.m_129297_((String)value)));
                offerTag.m_128365_(id.toString(), (Tag)ids);
            });
            tag.m_128365_("Candidates", (Tag)offerTag);
            CompoundTag selectedTag = new CompoundTag();
            this.selections.forEach((id, general) -> selectedTag.m_128359_(id.toString(), general));
            tag.m_128365_("Selections", (Tag)selectedTag);
            CompoundTag healthTag = new CompoundTag();
            this.health.forEach((id, stack) -> healthTag.m_128365_(id.toString(), (Tag)stack.m_41739_(new CompoundTag())));
            tag.m_128365_("Health", (Tag)healthTag);
            return tag;
        }

        private static Room load(CompoundTag tag) {
            BlockPos anchor = BlockPos.m_122022_((long)tag.m_128454_("Anchor"));
            LinkedHashSet<BlockPos> tables = new LinkedHashSet<BlockPos>();
            ListTag tableList = tag.m_128437_("Tables", 4);
            for (Tag value : tableList) {
                tables.add(BlockPos.m_122022_((long)((LongTag)value).m_7046_()));
            }
            if (tables.isEmpty()) {
                tables.add(anchor);
            }
            Room room = new Room(anchor, tables);
            if (tag.m_128403_("Owner")) {
                room.owner = tag.m_128342_("Owner");
            }
            try {
                room.phase = Phase.valueOf(tag.m_128461_("Phase"));
            }
            catch (IllegalArgumentException value) {
                // empty catch block
            }
            ListTag members = tag.m_128437_("Members", 10);
            for (int i = 0; i < members.size(); ++i) {
                Member member = Member.load(members.m_128728_(i));
                room.members.put(member.id, member);
            }
            ListTag participants = tag.m_128437_("Participants", 8);
            for (Tag value : participants) {
                try {
                    room.participants.add(UUID.fromString(value.m_7916_()));
                }
                catch (IllegalArgumentException illegalArgumentException) {}
            }
            ListTag awarded = tag.m_128437_("Awarded", 8);
            for (Tag value : awarded) {
                try {
                    room.awarded.add(UUID.fromString(value.m_7916_()));
                }
                catch (IllegalArgumentException illegalArgumentException) {}
            }
            CompoundTag offers = tag.m_128469_("Candidates");
            for (Object key : offers.m_128431_()) {
                try {
                    UUID id = UUID.fromString((String)key);
                    ListTag ids = offers.m_128437_((String)key, 8);
                    ArrayList<String> values = new ArrayList<String>();
                    for (Tag value : ids) {
                        values.add(value.m_7916_());
                    }
                    room.candidates.put(id, List.copyOf(values));
                }
                catch (IllegalArgumentException id) {}
            }
            CompoundTag selected = tag.m_128469_("Selections");
            for (String key : selected.m_128431_()) {
                try {
                    room.selections.put(UUID.fromString(key), selected.m_128461_(key));
                }
                catch (IllegalArgumentException ids) {}
            }
            CompoundTag health = tag.m_128469_("Health");
            for (String key : health.m_128431_()) {
                try {
                    ItemStack stack = ItemStack.m_41712_((CompoundTag)health.m_128469_(key));
                    if (stack.m_41619_()) continue;
                    room.health.put(UUID.fromString(key), stack.m_255036_(1));
                }
                catch (IllegalArgumentException illegalArgumentException) {}
            }
            if (room.phase == Phase.SELECTING && room.participants.isEmpty()) {
                room.participants.addAll(room.candidates.keySet());
            }
            if (room.owner == null || !room.members.containsKey(room.owner)) {
                room.transferOwner();
            }
            return room;
        }
    }

    public static final class Member {
        private final UUID id;
        private String name;
        private final long order;
        private boolean ready;

        private Member(UUID id, String name, long order, boolean ready) {
            this.id = id;
            this.name = name;
            this.order = order;
            this.ready = ready;
        }

        public UUID id() {
            return this.id;
        }

        public String name() {
            return this.name;
        }

        public boolean ready() {
            return this.ready;
        }

        private CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.m_128362_("Id", this.id);
            tag.m_128359_("Name", this.name);
            tag.m_128356_("Order", this.order);
            tag.m_128379_("Ready", this.ready);
            return tag;
        }

        private static Member load(CompoundTag tag) {
            return new Member(tag.m_128342_("Id"), tag.m_128461_("Name"), tag.m_128454_("Order"), tag.m_128471_("Ready"));
        }
    }

    public static enum SelectResult {
        ACCEPTED,
        NOT_SELECTING,
        NOT_PARTICIPANT,
        ALREADY_SELECTED,
        NOT_OFFERED,
        TAKEN;

    }

    public static enum Phase {
        LOBBY,
        SELECTING,
        COMPLETE,
        PLAYING;

    }
}

