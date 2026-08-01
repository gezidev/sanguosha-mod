/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.fml.DistExecutor
 *  net.minecraftforge.network.NetworkDirection
 *  net.minecraftforge.network.NetworkEvent$Context
 *  net.minecraftforge.network.NetworkRegistry
 *  net.minecraftforge.network.simple.SimpleChannel
 *  net.minecraftforge.server.ServerLifecycleHooks
 */
package cn.solo.sanguosha.network;

import cn.solo.sanguosha.client.ClientGeneralCatalog;
import cn.solo.sanguosha.client.ClientImageTransferManager;
import cn.solo.sanguosha.client.ClientScreens;
import cn.solo.sanguosha.config.GeneralAssetManager;
import cn.solo.sanguosha.config.GeneralDefinition;
import cn.solo.sanguosha.config.GeneralManager;
import cn.solo.sanguosha.deck.PlayerDeckDrawConfig;
import cn.solo.sanguosha.game.GameRoomManager;
import cn.solo.sanguosha.game.HandPouchSessionManager;
import cn.solo.sanguosha.image.ImageDataValidator;
import cn.solo.sanguosha.image.ServerImageStore;
import cn.solo.sanguosha.item.GeneralCardItem;
import cn.solo.sanguosha.item.HealthCardItem;
import cn.solo.sanguosha.network.ChessboardActionC2SPacket;
import cn.solo.sanguosha.network.FlipGameTable2IdentityC2SPacket;
import cn.solo.sanguosha.network.HandContainerCountS2CPacket;
import cn.solo.sanguosha.network.OpenGameTable2C2SPacket;
import cn.solo.sanguosha.network.PlaceHandCardC2SPacket;
import cn.solo.sanguosha.network.RequestDeckDrawConfigC2SPacket;
import cn.solo.sanguosha.network.SaveDeckDrawConfigC2SPacket;
import cn.solo.sanguosha.network.SelectHandCardC2SPacket;
import cn.solo.sanguosha.network.SyncDeckDrawConfigS2CPacket;
import cn.solo.sanguosha.network.ToggleTableSlotHorizontalC2SPacket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;

public final class ModNetwork {
    private static final String VERSION = "20";
    static final double HAND_PLACE_DISTANCE = 5.0;
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel((ResourceLocation)new ResourceLocation("sanguosha", "main"), () -> VERSION, ModNetwork::isCompatibleProtocol, ModNetwork::isCompatibleProtocol);
    private static final List<MessageRegistration<?>> MESSAGES = List.of(ModNetwork.message(SelectGeneralPacket.class, SelectGeneralPacket::encode, SelectGeneralPacket::decode, SelectGeneralPacket::handle, NetworkDirection.PLAY_TO_SERVER), ModNetwork.message(UploadGeneralImageChunkC2SPacket.class, UploadGeneralImageChunkC2SPacket::encode, UploadGeneralImageChunkC2SPacket::decode, UploadGeneralImageChunkC2SPacket::handle, NetworkDirection.PLAY_TO_SERVER), ModNetwork.message(CreateCustomGeneralPacket.class, CreateCustomGeneralPacket::encode, CreateCustomGeneralPacket::decode, CreateCustomGeneralPacket::handle, NetworkDirection.PLAY_TO_SERVER), ModNetwork.message(SyncCustomGeneralsPacket.class, SyncCustomGeneralsPacket::encode, SyncCustomGeneralsPacket::decode, SyncCustomGeneralsPacket::handle, NetworkDirection.PLAY_TO_CLIENT), ModNetwork.message(RequestGeneralImageC2SPacket.class, RequestGeneralImageC2SPacket::encode, RequestGeneralImageC2SPacket::decode, RequestGeneralImageC2SPacket::handle, NetworkDirection.PLAY_TO_SERVER), ModNetwork.message(GeneralImageChunkS2CPacket.class, GeneralImageChunkS2CPacket::encode, GeneralImageChunkS2CPacket::decode, GeneralImageChunkS2CPacket::handle, NetworkDirection.PLAY_TO_CLIENT), ModNetwork.message(SetHealthPacket.class, SetHealthPacket::encode, SetHealthPacket::decode, SetHealthPacket::handle, NetworkDirection.PLAY_TO_SERVER), ModNetwork.message(MilitaryRoomActionC2SPacket.class, MilitaryRoomActionC2SPacket::encode, MilitaryRoomActionC2SPacket::decode, MilitaryRoomActionC2SPacket::handle, NetworkDirection.PLAY_TO_SERVER), ModNetwork.message(MilitaryRoomStateS2CPacket.class, MilitaryRoomStateS2CPacket::encode, MilitaryRoomStateS2CPacket::decode, MilitaryRoomStateS2CPacket::handle, NetworkDirection.PLAY_TO_CLIENT), ModNetwork.message(GeneralOfferPacket.class, GeneralOfferPacket::encode, GeneralOfferPacket::decode, GeneralOfferPacket::handle, NetworkDirection.PLAY_TO_CLIENT), ModNetwork.message(SelectOfferedGeneralPacket.class, SelectOfferedGeneralPacket::encode, SelectOfferedGeneralPacket::decode, SelectOfferedGeneralPacket::handle, NetworkDirection.PLAY_TO_SERVER), ModNetwork.message(GeneralSelectionResultS2CPacket.class, GeneralSelectionResultS2CPacket::encode, GeneralSelectionResultS2CPacket::decode, GeneralSelectionResultS2CPacket::handle, NetworkDirection.PLAY_TO_CLIENT), ModNetwork.message(SelectHandCardC2SPacket.class, SelectHandCardC2SPacket::encode, SelectHandCardC2SPacket::decode, SelectHandCardC2SPacket::handle, NetworkDirection.PLAY_TO_SERVER), ModNetwork.message(PlaceHandCardC2SPacket.class, PlaceHandCardC2SPacket::encode, PlaceHandCardC2SPacket::decode, PlaceHandCardC2SPacket::handle, NetworkDirection.PLAY_TO_SERVER), ModNetwork.message(RequestHandPouchSessionPacket.class, RequestHandPouchSessionPacket::encode, RequestHandPouchSessionPacket::decode, RequestHandPouchSessionPacket::handle, NetworkDirection.PLAY_TO_SERVER), ModNetwork.message(HandPouchSessionPacket.class, HandPouchSessionPacket::encode, HandPouchSessionPacket::decode, HandPouchSessionPacket::handle, NetworkDirection.PLAY_TO_CLIENT), ModNetwork.message(ChooseHandPouchCardPacket.class, ChooseHandPouchCardPacket::encode, ChooseHandPouchCardPacket::decode, ChooseHandPouchCardPacket::handle, NetworkDirection.PLAY_TO_SERVER), ModNetwork.message(RequestDeckDrawConfigC2SPacket.class, RequestDeckDrawConfigC2SPacket::encode, RequestDeckDrawConfigC2SPacket::decode, RequestDeckDrawConfigC2SPacket::handle, NetworkDirection.PLAY_TO_SERVER), ModNetwork.message(SyncDeckDrawConfigS2CPacket.class, SyncDeckDrawConfigS2CPacket::encode, SyncDeckDrawConfigS2CPacket::decode, SyncDeckDrawConfigS2CPacket::handle, NetworkDirection.PLAY_TO_CLIENT), ModNetwork.message(SaveDeckDrawConfigC2SPacket.class, SaveDeckDrawConfigC2SPacket::encode, SaveDeckDrawConfigC2SPacket::decode, SaveDeckDrawConfigC2SPacket::handle, NetworkDirection.PLAY_TO_SERVER), ModNetwork.message(OpenGameTable2C2SPacket.class, OpenGameTable2C2SPacket::encode, OpenGameTable2C2SPacket::decode, OpenGameTable2C2SPacket::handle, NetworkDirection.PLAY_TO_SERVER), ModNetwork.message(FlipGameTable2IdentityC2SPacket.class, FlipGameTable2IdentityC2SPacket::encode, FlipGameTable2IdentityC2SPacket::decode, FlipGameTable2IdentityC2SPacket::handle, NetworkDirection.PLAY_TO_SERVER), ModNetwork.message(ToggleTableSlotHorizontalC2SPacket.class, ToggleTableSlotHorizontalC2SPacket::encode, ToggleTableSlotHorizontalC2SPacket::decode, ToggleTableSlotHorizontalC2SPacket::handle, NetworkDirection.PLAY_TO_SERVER), ModNetwork.message(SetRoomHealthC2SPacket.class, SetRoomHealthC2SPacket::encode, SetRoomHealthC2SPacket::decode, SetRoomHealthC2SPacket::handle, NetworkDirection.PLAY_TO_SERVER), ModNetwork.message(RoomHealthS2CPacket.class, RoomHealthS2CPacket::encode, RoomHealthS2CPacket::decode, RoomHealthS2CPacket::handle, NetworkDirection.PLAY_TO_CLIENT), ModNetwork.message(RoomDisbandedS2CPacket.class, RoomDisbandedS2CPacket::encode, RoomDisbandedS2CPacket::decode, RoomDisbandedS2CPacket::handle, NetworkDirection.PLAY_TO_CLIENT), ModNetwork.message(HandContainerCountS2CPacket.class, HandContainerCountS2CPacket::encode, HandContainerCountS2CPacket::decode, HandContainerCountS2CPacket::handle, NetworkDirection.PLAY_TO_CLIENT), ModNetwork.message(ChessboardActionC2SPacket.class, ChessboardActionC2SPacket::encode, ChessboardActionC2SPacket::decode, ChessboardActionC2SPacket::handle, NetworkDirection.PLAY_TO_SERVER));
    private static boolean initialized;

    private ModNetwork() {
    }

    private static boolean isCompatibleProtocol(String remoteVersion) {
        return VERSION.equals(remoteVersion);
    }

    public static synchronized void register() {
        if (initialized) {
            return;
        }
        for (int id = 0; id < MESSAGES.size(); ++id) {
            ModNetwork.registerMessage(MESSAGES.get(id), id);
        }
        initialized = true;
    }

    private static <T> MessageRegistration<T> message(Class<T> type, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, BiConsumer<T, Supplier<NetworkEvent.Context>> handler, NetworkDirection direction) {
        return new MessageRegistration<T>(type, encoder, decoder, handler, direction);
    }

    private static <T> void registerMessage(MessageRegistration<T> registration, int id) {
        CHANNEL.messageBuilder(registration.type(), id, registration.direction()).encoder(registration.encoder()).decoder(registration.decoder()).consumerMainThread(registration.handler()).add();
    }

    public static void selectGeneral(InteractionHand hand, String generalId) {
        CHANNEL.sendToServer((Object)new SelectGeneralPacket(hand, generalId));
    }

    public static void setHealth(InteractionHand hand, int health) {
        CHANNEL.sendToServer((Object)new SetHealthPacket(hand, health));
    }

    public static void selectHandCard(InteractionHand hand, int index) {
        CHANNEL.sendToServer((Object)new SelectHandCardC2SPacket(hand, index));
    }

    public static void placeSelectedCard(InteractionHand hand, int index) {
        CHANNEL.sendToServer((Object)new PlaceHandCardC2SPacket(hand, index));
    }

    public static void requestDeckDrawConfig(int entityId) {
        CHANNEL.sendToServer((Object)new RequestDeckDrawConfigC2SPacket(entityId));
    }

    public static void saveDeckDrawConfig(int mask) {
        CHANNEL.sendToServer((Object)new SaveDeckDrawConfigC2SPacket(mask));
    }

    public static void openGameTable2(BlockPos pos) {
        CHANNEL.sendToServer((Object)new OpenGameTable2C2SPacket(pos));
    }

    public static void flipGameTable2Identity(BlockPos pos) {
        CHANNEL.sendToServer((Object)new FlipGameTable2IdentityC2SPacket(pos));
    }

    public static void toggleTableSlotHorizontal(BlockPos clickedPos, BlockPos roomAnchor, int slot) {
        CHANNEL.sendToServer((Object)new ToggleTableSlotHorizontalC2SPacket(clickedPos, roomAnchor, slot));
    }

    public static void chessboardAction(BlockPos pos, ChessboardActionC2SPacket.Action action, String code) {
        CHANNEL.sendToServer((Object)new ChessboardActionC2SPacket(pos, action, code));
    }

    public static void setRoomHealth(BlockPos clickedPos, BlockPos anchor, int slot, int health) {
        CHANNEL.sendToServer((Object)new SetRoomHealthC2SPacket(clickedPos, anchor, slot, health));
    }

    public static void sendRoomHealth(ServerPlayer player, BlockPos clickedPos, BlockPos anchor, int health) {
        CHANNEL.sendTo((Object)new RoomHealthS2CPacket(clickedPos, anchor, health), player.f_8906_.f_9742_, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendRoomDisbanded(ServerPlayer player, BlockPos anchor) {
        CHANNEL.sendTo((Object)new RoomDisbandedS2CPacket(anchor), player.f_8906_.f_9742_, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendHandContainerCount(ServerPlayer player, int targetEntityId, int count) {
        CHANNEL.sendTo((Object)new HandContainerCountS2CPacket(targetEntityId, count), player.f_8906_.f_9742_, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendDeckDrawConfig(ServerPlayer player) {
        CHANNEL.sendTo((Object)new SyncDeckDrawConfigS2CPacket(PlayerDeckDrawConfig.get((Player)player).mask()), player.f_8906_.f_9742_, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void requestHandPouchSession(int targetEntityId) {
        CHANNEL.sendToServer((Object)new RequestHandPouchSessionPacket(targetEntityId));
    }

    public static void chooseHandPouchCard(UUID token, boolean draw, int index) {
        CHANNEL.sendToServer((Object)new ChooseHandPouchCardPacket(token, draw ? HandPouchSessionManager.Action.DRAW : HandPouchSessionManager.Action.DISCARD, index));
    }

    public static void sendHandPouchSession(ServerPlayer player, UUID token, String targetName, int count) {
        CHANNEL.sendTo((Object)new HandPouchSessionPacket(token, targetName, count), player.f_8906_.f_9742_, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void uploadAndCreateCustomGeneral(InteractionHand hand, GeneralDefinition def, byte[] imageData) {
        if (def == null || !def.hasImage() || imageData == null || imageData.length == 0 || imageData.length > 0x1000000 || !ImageDataValidator.sha256(imageData).equals(def.imageId())) {
            return;
        }
        int total = (imageData.length + 24576 - 1) / 24576;
        for (int index = 0; index < total; ++index) {
            int from = index * 24576;
            int to = Math.min(imageData.length, from + 24576);
            byte[] chunk = Arrays.copyOfRange(imageData, from, to);
            CHANNEL.sendToServer((Object)new UploadGeneralImageChunkC2SPacket(def.imageId(), def.imageFormat(), imageData.length, total, index, chunk));
        }
        CHANNEL.sendToServer((Object)new CreateCustomGeneralPacket(hand, def));
    }

    public static void requestGeneralImage(String contentId, String format) {
        CHANNEL.sendToServer((Object)new RequestGeneralImageC2SPacket(contentId, format));
    }

    public static void militaryRoomAction(BlockPos clickedPos, BlockPos anchor, GameRoomManager.Action action) {
        CHANNEL.sendToServer((Object)new MilitaryRoomActionC2SPacket(clickedPos, anchor, action));
    }

    public static void selectOfferedGeneral(BlockPos anchor, String generalId) {
        CHANNEL.sendToServer((Object)new SelectOfferedGeneralPacket(anchor, generalId));
    }

    public static void sendMilitaryRoomState(ServerPlayer player, BlockPos anchor, boolean owner, boolean ready, int phase, List<String> members) {
        CHANNEL.sendTo((Object)new MilitaryRoomStateS2CPacket(anchor, owner, ready, phase, members), player.f_8906_.f_9742_, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendGeneralOffer(ServerPlayer player, BlockPos anchor, List<String> choices) {
        CHANNEL.sendTo((Object)new GeneralOfferPacket(anchor, choices), player.f_8906_.f_9742_, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendGeneralSelectionResult(ServerPlayer player, BlockPos anchor, boolean success, boolean complete, String message) {
        CHANNEL.sendTo((Object)new GeneralSelectionResultS2CPacket(anchor, success, complete, message), player.f_8906_.f_9742_, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendSyncCustomGenerals(ServerPlayer player) {
        CHANNEL.sendTo((Object)new SyncCustomGeneralsPacket(GeneralManager.byCategory("\u81ea\u5b9a\u4e49")), player.f_8906_.f_9742_, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void broadcastSyncCustomGenerals() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        SyncCustomGeneralsPacket packet = new SyncCustomGeneralsPacket(GeneralManager.byCategory("\u81ea\u5b9a\u4e49"));
        for (ServerPlayer player : server.m_6846_().m_11314_()) {
            CHANNEL.sendTo((Object)packet, player.f_8906_.f_9742_, NetworkDirection.PLAY_TO_CLIENT);
        }
    }

    public static void syncHeldContainer(ServerPlayer player) {
        player.m_150109_().m_6596_();
        player.f_36095_.m_38946_();
        if (player.f_36096_ != player.f_36095_) {
            player.f_36096_.m_38946_();
        }
    }

    private static void writeDefinition(FriendlyByteBuf buffer, GeneralDefinition def) {
        buffer.m_130072_(def.id(), 64);
        buffer.m_130072_(def.name(), 32);
        buffer.m_130072_(def.kingdom(), 16);
        buffer.writeInt(def.health());
        buffer.m_130072_(def.imageId(), 64);
        buffer.m_130072_(def.imageFormat(), 3);
        buffer.m_130130_(Math.min(def.skills().size(), 2));
        for (int i = 0; i < Math.min(def.skills().size(), 2); ++i) {
            GeneralDefinition.Skill skill = def.skills().get(i);
            buffer.m_130072_(skill.name(), 32);
            buffer.m_130072_(skill.description(), 160);
        }
    }

    private static GeneralDefinition readDefinition(FriendlyByteBuf buffer) {
        String id = buffer.m_130136_(64);
        String name = buffer.m_130136_(32);
        String kingdom = buffer.m_130136_(16);
        int health = buffer.readInt();
        String imageId = buffer.m_130136_(64);
        String format = buffer.m_130136_(3);
        int count = Math.min(buffer.m_130242_(), 2);
        ArrayList<GeneralDefinition.Skill> skills = new ArrayList<GeneralDefinition.Skill>(count);
        for (int i = 0; i < count; ++i) {
            skills.add(new GeneralDefinition.Skill(buffer.m_130136_(32), buffer.m_130136_(160)));
        }
        return new GeneralDefinition(id, name, kingdom, health, imageId, format, skills);
    }

    private record MessageRegistration<T>(Class<T> type, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, BiConsumer<T, Supplier<NetworkEvent.Context>> handler, NetworkDirection direction) {
    }

    public record SelectGeneralPacket(InteractionHand hand, String generalId) {
        private static void encode(SelectGeneralPacket packet, FriendlyByteBuf buffer) {
            buffer.m_130068_((Enum)packet.hand);
            buffer.m_130072_(packet.generalId, 64);
        }

        private static SelectGeneralPacket decode(FriendlyByteBuf buffer) {
            return new SelectGeneralPacket((InteractionHand)buffer.m_130066_(InteractionHand.class), buffer.m_130136_(64));
        }

        private static void handle(SelectGeneralPacket packet, Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player == null) {
                    return;
                }
                if (!GeneralManager.contains(packet.generalId) && !GeneralAssetManager.contains(packet.generalId)) {
                    return;
                }
                ItemStack held = player.m_21120_(packet.hand);
                if (!(held.m_41720_() instanceof GeneralCardItem)) {
                    return;
                }
                ItemStack updated = held.m_41777_();
                GeneralCardItem.setGeneral(updated, packet.generalId);
                player.m_21008_(packet.hand, updated);
                player.m_150109_().m_6596_();
                player.f_36095_.m_38946_();
                if (player.f_36096_ != player.f_36095_) {
                    player.f_36096_.m_38946_();
                }
            });
            context.setPacketHandled(true);
        }
    }

    public record SetHealthPacket(InteractionHand hand, int health) {
        private static void encode(SetHealthPacket packet, FriendlyByteBuf buffer) {
            buffer.m_130068_((Enum)packet.hand);
            buffer.m_130130_(packet.health);
        }

        private static SetHealthPacket decode(FriendlyByteBuf buffer) {
            return new SetHealthPacket((InteractionHand)buffer.m_130066_(InteractionHand.class), buffer.m_130242_());
        }

        private static void handle(SetHealthPacket packet, Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player == null) {
                    return;
                }
                ItemStack held = player.m_21120_(packet.hand);
                if (!(held.m_41720_() instanceof HealthCardItem)) {
                    return;
                }
                HealthCardItem.setHealth(held, packet.health);
                player.m_150109_().m_6596_();
                player.f_36095_.m_38946_();
            });
            context.setPacketHandled(true);
        }
    }

    public record SetRoomHealthC2SPacket(BlockPos clickedPos, BlockPos anchor, int slot, int health) {
        private static void encode(SetRoomHealthC2SPacket p, FriendlyByteBuf b) {
            b.m_130064_(p.clickedPos);
            b.m_130064_(p.anchor);
            b.m_130130_(p.slot);
            b.m_130130_(p.health);
        }

        private static SetRoomHealthC2SPacket decode(FriendlyByteBuf b) {
            return new SetRoomHealthC2SPacket(b.m_130135_(), b.m_130135_(), b.m_130242_(), b.m_130242_());
        }

        private static void handle(SetRoomHealthC2SPacket p, Supplier<NetworkEvent.Context> s) {
            NetworkEvent.Context c = s.get();
            c.enqueueWork(() -> {
                if (c.getSender() != null) {
                    GameRoomManager.setRoomHealth(c.getSender(), p.clickedPos, p.anchor, p.slot, p.health);
                }
            });
            c.setPacketHandled(true);
        }
    }

    public record RoomHealthS2CPacket(BlockPos clickedPos, BlockPos anchor, int health) {
        private static void encode(RoomHealthS2CPacket p, FriendlyByteBuf b) {
            b.m_130064_(p.clickedPos);
            b.m_130064_(p.anchor);
            b.m_130130_(p.health);
        }

        private static RoomHealthS2CPacket decode(FriendlyByteBuf b) {
            return new RoomHealthS2CPacket(b.m_130135_(), b.m_130135_(), b.m_130242_());
        }

        private static void handle(RoomHealthS2CPacket p, Supplier<NetworkEvent.Context> s) {
            NetworkEvent.Context c = s.get();
            c.enqueueWork(() -> DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> ClientScreens.updateRoomHealth(p.clickedPos, p.anchor, p.health)));
            c.setPacketHandled(true);
        }
    }

    public record RoomDisbandedS2CPacket(BlockPos anchor) {
        private static void encode(RoomDisbandedS2CPacket p, FriendlyByteBuf b) {
            b.m_130064_(p.anchor);
        }

        private static RoomDisbandedS2CPacket decode(FriendlyByteBuf b) {
            return new RoomDisbandedS2CPacket(b.m_130135_());
        }

        private static void handle(RoomDisbandedS2CPacket p, Supplier<NetworkEvent.Context> s) {
            NetworkEvent.Context c = s.get();
            c.enqueueWork(() -> DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> ClientScreens.roomDisbanded(p.anchor)));
            c.setPacketHandled(true);
        }
    }

    public record RequestHandPouchSessionPacket(int targetEntityId) {
        private static void encode(RequestHandPouchSessionPacket p, FriendlyByteBuf b) {
            b.m_130130_(p.targetEntityId);
        }

        private static RequestHandPouchSessionPacket decode(FriendlyByteBuf b) {
            return new RequestHandPouchSessionPacket(b.m_130242_());
        }

        private static void handle(RequestHandPouchSessionPacket p, Supplier<NetworkEvent.Context> s) {
            NetworkEvent.Context c = s.get();
            c.enqueueWork(() -> {
                if (c.getSender() != null) {
                    HandPouchSessionManager.open(c.getSender(), p.targetEntityId);
                }
            });
            c.setPacketHandled(true);
        }
    }

    public record ChooseHandPouchCardPacket(UUID token, HandPouchSessionManager.Action action, int index) {
        private static void encode(ChooseHandPouchCardPacket p, FriendlyByteBuf b) {
            b.m_130077_(p.token);
            b.m_130068_((Enum)p.action);
            b.m_130130_(p.index);
        }

        private static ChooseHandPouchCardPacket decode(FriendlyByteBuf b) {
            return new ChooseHandPouchCardPacket(b.m_130259_(), (HandPouchSessionManager.Action)b.m_130066_(HandPouchSessionManager.Action.class), b.m_130242_());
        }

        private static void handle(ChooseHandPouchCardPacket p, Supplier<NetworkEvent.Context> s) {
            NetworkEvent.Context c = s.get();
            c.enqueueWork(() -> {
                if (c.getSender() != null) {
                    HandPouchSessionManager.execute(c.getSender(), p.token, p.action, p.index);
                }
            });
            c.setPacketHandled(true);
        }
    }

    public record HandPouchSessionPacket(UUID token, String targetName, int count) {
        private static void encode(HandPouchSessionPacket p, FriendlyByteBuf b) {
            b.m_130077_(p.token);
            b.m_130072_(p.targetName, 64);
            b.m_130130_(p.count);
        }

        private static HandPouchSessionPacket decode(FriendlyByteBuf b) {
            return new HandPouchSessionPacket(b.m_130259_(), b.m_130136_(64), b.m_130242_());
        }

        private static void handle(HandPouchSessionPacket p, Supplier<NetworkEvent.Context> s) {
            NetworkEvent.Context c = s.get();
            c.enqueueWork(() -> DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> ClientScreens.openHandPouchSession(p.token, p.targetName, p.count)));
            c.setPacketHandled(true);
        }
    }

    public record UploadGeneralImageChunkC2SPacket(String contentId, String format, int totalBytes, int totalChunks, int index, byte[] data) {
        private static void encode(UploadGeneralImageChunkC2SPacket p, FriendlyByteBuf b) {
            b.m_130072_(p.contentId, 64);
            b.m_130072_(p.format, 3);
            b.m_130130_(p.totalBytes);
            b.m_130130_(p.totalChunks);
            b.m_130130_(p.index);
            b.m_130087_(p.data);
        }

        private static UploadGeneralImageChunkC2SPacket decode(FriendlyByteBuf b) {
            return new UploadGeneralImageChunkC2SPacket(b.m_130136_(64), b.m_130136_(3), b.m_130242_(), b.m_130242_(), b.m_130242_(), b.m_130101_(24576));
        }

        private static void handle(UploadGeneralImageChunkC2SPacket p, Supplier<NetworkEvent.Context> s) {
            NetworkEvent.Context c = s.get();
            c.enqueueWork(() -> {
                ServerPlayer player = c.getSender();
                if (player != null) {
                    ServerImageStore.accept(player, p.contentId, p.format, p.totalBytes, p.totalChunks, p.index, p.data);
                }
            });
            c.setPacketHandled(true);
        }
    }

    public record CreateCustomGeneralPacket(InteractionHand hand, GeneralDefinition definition) {
        private static void encode(CreateCustomGeneralPacket packet, FriendlyByteBuf buffer) {
            buffer.m_130068_((Enum)packet.hand);
            ModNetwork.writeDefinition(buffer, packet.definition);
        }

        private static CreateCustomGeneralPacket decode(FriendlyByteBuf buffer) {
            return new CreateCustomGeneralPacket((InteractionHand)buffer.m_130066_(InteractionHand.class), ModNetwork.readDefinition(buffer));
        }

        private static void handle(CreateCustomGeneralPacket packet, Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player == null || packet.definition.id() == null) {
                    return;
                }
                if (!packet.definition.id().matches("custom_[a-f0-9]{32}") || !packet.definition.hasImage() || packet.definition.skills().size() > 2 || packet.definition.health() < 1 || packet.definition.health() > 99 || ServerImageStore.read(player.m_20194_(), packet.definition.imageId(), packet.definition.imageFormat()).length == 0) {
                    return;
                }
                GeneralManager.saveCustom(packet.definition);
                ModNetwork.broadcastSyncCustomGenerals();
                ItemStack held = player.m_21120_(packet.hand);
                if (held.m_41720_() instanceof GeneralCardItem) {
                    ItemStack updated = held.m_41777_();
                    GeneralCardItem.setGeneral(updated, packet.definition.id());
                    player.m_21008_(packet.hand, updated);
                    player.m_150109_().m_6596_();
                    player.f_36095_.m_38946_();
                    if (player.f_36096_ != player.f_36095_) {
                        player.f_36096_.m_38946_();
                    }
                }
            });
            context.setPacketHandled(true);
        }
    }

    public record RequestGeneralImageC2SPacket(String contentId, String format) {
        private static void encode(RequestGeneralImageC2SPacket p, FriendlyByteBuf b) {
            b.m_130072_(p.contentId, 64);
            b.m_130072_(p.format, 3);
        }

        private static RequestGeneralImageC2SPacket decode(FriendlyByteBuf b) {
            return new RequestGeneralImageC2SPacket(b.m_130136_(64), b.m_130136_(3));
        }

        private static void handle(RequestGeneralImageC2SPacket p, Supplier<NetworkEvent.Context> s) {
            NetworkEvent.Context c = s.get();
            c.enqueueWork(() -> {
                ServerPlayer player = c.getSender();
                if (player == null) {
                    return;
                }
                boolean referenced = GeneralManager.all().stream().anyMatch(def -> def.imageId().equals(p.contentId) && def.imageFormat().equals(p.format));
                if (!referenced) {
                    return;
                }
                byte[] data = ServerImageStore.read(player.m_20194_(), p.contentId, p.format);
                int total = (data.length + 24576 - 1) / 24576;
                if (data.length == 0 || total > 683) {
                    return;
                }
                for (int index = 0; index < total; ++index) {
                    int from = index * 24576;
                    int to = Math.min(data.length, from + 24576);
                    CHANNEL.sendTo((Object)new GeneralImageChunkS2CPacket(p.contentId, p.format, data.length, total, index, Arrays.copyOfRange(data, from, to)), player.f_8906_.f_9742_, NetworkDirection.PLAY_TO_CLIENT);
                }
            });
            c.setPacketHandled(true);
        }
    }

    public record MilitaryRoomActionC2SPacket(BlockPos clickedPos, BlockPos anchor, GameRoomManager.Action action) {
        private static void encode(MilitaryRoomActionC2SPacket p, FriendlyByteBuf b) {
            b.m_130064_(p.clickedPos);
            b.m_130064_(p.anchor);
            b.m_130068_((Enum)p.action);
        }

        private static MilitaryRoomActionC2SPacket decode(FriendlyByteBuf b) {
            return new MilitaryRoomActionC2SPacket(b.m_130135_(), b.m_130135_(), (GameRoomManager.Action)b.m_130066_(GameRoomManager.Action.class));
        }

        private static void handle(MilitaryRoomActionC2SPacket p, Supplier<NetworkEvent.Context> s) {
            NetworkEvent.Context c = s.get();
            c.enqueueWork(() -> {
                if (c.getSender() != null) {
                    GameRoomManager.action(c.getSender(), p.clickedPos, p.anchor, p.action);
                }
            });
            c.setPacketHandled(true);
        }
    }

    public record SelectOfferedGeneralPacket(BlockPos anchor, String generalId) {
        private static void encode(SelectOfferedGeneralPacket p, FriendlyByteBuf b) {
            b.m_130064_(p.anchor);
            b.m_130072_(p.generalId, 64);
        }

        private static SelectOfferedGeneralPacket decode(FriendlyByteBuf b) {
            return new SelectOfferedGeneralPacket(b.m_130135_(), b.m_130136_(64));
        }

        private static void handle(SelectOfferedGeneralPacket p, Supplier<NetworkEvent.Context> s) {
            NetworkEvent.Context c = s.get();
            c.enqueueWork(() -> {
                if (c.getSender() != null) {
                    GameRoomManager.select(c.getSender(), p.anchor, p.generalId);
                }
            });
            c.setPacketHandled(true);
        }
    }

    public record MilitaryRoomStateS2CPacket(BlockPos anchor, boolean owner, boolean ready, int phase, List<String> members) {
        private static void encode(MilitaryRoomStateS2CPacket p, FriendlyByteBuf b) {
            b.m_130064_(p.anchor);
            b.writeBoolean(p.owner);
            b.writeBoolean(p.ready);
            b.m_130130_(p.phase);
            b.m_130130_(p.members.size());
            p.members.forEach(n -> b.m_130072_(n, 64));
        }

        private static MilitaryRoomStateS2CPacket decode(FriendlyByteBuf b) {
            BlockPos a = b.m_130135_();
            boolean o = b.readBoolean();
            boolean r = b.readBoolean();
            int ph = b.m_130242_();
            int n = b.m_130242_();
            ArrayList<String> m = new ArrayList<String>();
            for (int i = 0; i < n; ++i) {
                m.add(b.m_130136_(64));
            }
            return new MilitaryRoomStateS2CPacket(a, o, r, ph, m);
        }

        private static void handle(MilitaryRoomStateS2CPacket p, Supplier<NetworkEvent.Context> s) {
            NetworkEvent.Context c = s.get();
            c.enqueueWork(() -> DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> ClientScreens.updateMilitaryRoom(p.anchor, p.owner, p.ready, p.phase, p.members)));
            c.setPacketHandled(true);
        }
    }

    public record GeneralOfferPacket(BlockPos anchor, List<String> generals) {
        private static void encode(GeneralOfferPacket p, FriendlyByteBuf b) {
            b.m_130064_(p.anchor);
            b.m_130130_(p.generals.size());
            p.generals.forEach(id -> b.m_130072_(id, 64));
        }

        private static GeneralOfferPacket decode(FriendlyByteBuf b) {
            BlockPos a = b.m_130135_();
            int n = b.m_130242_();
            ArrayList<String> ids = new ArrayList<String>();
            for (int i = 0; i < n; ++i) {
                ids.add(b.m_130136_(64));
            }
            return new GeneralOfferPacket(a, ids);
        }

        private static void handle(GeneralOfferPacket p, Supplier<NetworkEvent.Context> s) {
            NetworkEvent.Context c = s.get();
            c.enqueueWork(() -> DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> ClientScreens.openFiveGeneralSelection(p.anchor, p.generals)));
            c.setPacketHandled(true);
        }
    }

    public record GeneralSelectionResultS2CPacket(BlockPos anchor, boolean success, boolean complete, String message) {
        private static void encode(GeneralSelectionResultS2CPacket p, FriendlyByteBuf b) {
            b.m_130064_(p.anchor);
            b.writeBoolean(p.success);
            b.writeBoolean(p.complete);
            b.m_130072_(p.message, 128);
        }

        private static GeneralSelectionResultS2CPacket decode(FriendlyByteBuf b) {
            return new GeneralSelectionResultS2CPacket(b.m_130135_(), b.readBoolean(), b.readBoolean(), b.m_130136_(128));
        }

        private static void handle(GeneralSelectionResultS2CPacket p, Supplier<NetworkEvent.Context> s) {
            NetworkEvent.Context c = s.get();
            c.enqueueWork(() -> DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> ClientScreens.updateGeneralSelection(p.anchor, p.success, p.complete, p.message)));
            c.setPacketHandled(true);
        }
    }

    public record SyncCustomGeneralsPacket(List<GeneralDefinition> generals) {
        private static void encode(SyncCustomGeneralsPacket packet, FriendlyByteBuf buffer) {
            buffer.m_130130_(Math.min(packet.generals.size(), 4096));
            for (int i = 0; i < Math.min(packet.generals.size(), 4096); ++i) {
                ModNetwork.writeDefinition(buffer, packet.generals.get(i));
            }
        }

        private static SyncCustomGeneralsPacket decode(FriendlyByteBuf buffer) {
            int count = Math.min(buffer.m_130242_(), 4096);
            ArrayList<GeneralDefinition> definitions = new ArrayList<GeneralDefinition>(count);
            for (int i = 0; i < count; ++i) {
                definitions.add(ModNetwork.readDefinition(buffer));
            }
            return new SyncCustomGeneralsPacket(definitions);
        }

        private static void handle(SyncCustomGeneralsPacket packet, Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> {
                for (GeneralDefinition def : packet.generals) {
                    ClientGeneralCatalog.registerCustomGeneral(def, null);
                }
            }));
            context.setPacketHandled(true);
        }
    }

    public record GeneralImageChunkS2CPacket(String contentId, String format, int totalBytes, int totalChunks, int index, byte[] data) {
        private static void encode(GeneralImageChunkS2CPacket p, FriendlyByteBuf b) {
            b.m_130072_(p.contentId, 64);
            b.m_130072_(p.format, 3);
            b.m_130130_(p.totalBytes);
            b.m_130130_(p.totalChunks);
            b.m_130130_(p.index);
            b.m_130087_(p.data);
        }

        private static GeneralImageChunkS2CPacket decode(FriendlyByteBuf b) {
            return new GeneralImageChunkS2CPacket(b.m_130136_(64), b.m_130136_(3), b.m_130242_(), b.m_130242_(), b.m_130242_(), b.m_130101_(24576));
        }

        private static void handle(GeneralImageChunkS2CPacket p, Supplier<NetworkEvent.Context> s) {
            NetworkEvent.Context c = s.get();
            c.enqueueWork(() -> DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> ClientImageTransferManager.accept(p.contentId, p.format, p.totalBytes, p.totalChunks, p.index, p.data)));
            c.setPacketHandled(true);
        }
    }
}

