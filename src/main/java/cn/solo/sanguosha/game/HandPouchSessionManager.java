package cn.solo.sanguosha.game;

import cn.solo.sanguosha.entity.GroundCardEntity;
import cn.solo.sanguosha.item.HandContainerItem;
import cn.solo.sanguosha.network.ModNetwork;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public final class HandPouchSessionManager {
    public static final double RANGE = 5.0;
    private static final long TTL_TICKS = 100L;
    private static final double DISCARD_SEARCH_RANGE = 8.0;
    private static final Map<UUID, Session> SESSIONS = new HashMap<UUID, Session>();

    private HandPouchSessionManager() {
    }

    public static void open(ServerPlayer actor, int targetEntityId) {
        ServerPlayer target;
        if (!HandPouchSessionManager.validPlayer(actor)) {
            return;
        }
        Entity entity = actor.m_9236_().m_6815_(targetEntityId);
        if (!(entity instanceof ServerPlayer) || (target = (ServerPlayer)entity) == actor || !HandPouchSessionManager.validPlayer(target)) {
            return;
        }
        if (!HandPouchSessionManager.isStillAimedAt(actor, target)) {
            return;
        }
        ItemStack targetPouch = target.m_21205_();
        int count = HandContainerItem.size(targetPouch);
        UUID token = UUID.randomUUID();
        SESSIONS.put(actor.m_20148_(), new Session(token, target.m_20148_(), actor.m_9236_().m_46467_() + 100L));
        ModNetwork.sendHandPouchSession(actor, token, target.m_5446_().getString(), count);
    }

    public static void execute(ServerPlayer actor, UUID token, Action action, int index) {
        boolean success;
        Session session = SESSIONS.remove(actor.m_20148_());
        if (session == null || !session.token.equals(token)) {
            return;
        }
        if (actor.m_9236_().m_46467_() > session.expiresAt || !HandPouchSessionManager.validPlayer(actor)) {
            return;
        }
        ServerPlayer target = actor.f_8924_.m_6846_().m_11259_(session.targetId);
        if (target == null || !HandPouchSessionManager.validPlayer(target) || target.m_9236_() != actor.m_9236_() || !HandPouchSessionManager.isStillAimedAt(actor, target)) {
            return;
        }
        ItemStack actorPouch = actor.m_21205_();
        ItemStack targetPouch = target.m_21205_();
        int targetCount = HandContainerItem.size(targetPouch);
        if (index < 0 || index >= targetCount) {
            return;
        }
        ItemStack card = HandContainerItem.get(targetPouch, index);
        if (!HandContainerItem.isCard(card) || card.m_41613_() != 1) {
            return;
        }
        boolean bl = success = action == Action.DRAW ? HandPouchSessionManager.drawAtomically(actorPouch, targetPouch, index, card) : HandPouchSessionManager.discardAtomically(actor, target, targetPouch, index, card);
        if (!success) {
            return;
        }
        ModNetwork.syncHeldContainer(actor);
        ModNetwork.syncHeldContainer(target);
    }

    private static boolean drawAtomically(ItemStack actorPouch, ItemStack targetPouch, int index, ItemStack expected) {
        if (HandContainerItem.size(actorPouch) >= 160) {
            return false;
        }
        ItemStack removed = HandContainerItem.remove(targetPouch, index);
        if (!ItemStack.m_150942_((ItemStack)removed, (ItemStack)expected)) {
            if (!removed.m_41619_()) {
                HandContainerItem.insert(targetPouch, index, removed);
            }
            return false;
        }
        if (HandContainerItem.add(actorPouch, removed)) {
            return true;
        }
        HandContainerItem.insert(targetPouch, index, removed);
        return false;
    }

    private static boolean discardAtomically(ServerPlayer actor, ServerPlayer target, ItemStack targetPouch, int index, ItemStack expected) {
        GroundCardEntity pile = target.m_9236_().m_6443_(GroundCardEntity.class, target.m_20191_().m_82400_(8.0), GroundCardEntity::acceptsDiscard).stream().min(Comparator.comparingDouble(arg_0 -> ((ServerPlayer)target).m_20280_(arg_0))).orElse(null);
        if (pile != null) {
            ItemStack removed = HandContainerItem.remove(targetPouch, index);
            if (!ItemStack.m_150942_((ItemStack)removed, (ItemStack)expected)) {
                if (!removed.m_41619_()) {
                    HandContainerItem.insert(targetPouch, index, removed);
                }
                return false;
            }
            if (pile.addDiscard(removed)) {
                return true;
            }
            HandContainerItem.insert(targetPouch, index, removed);
            return false;
        }
        GroundCardEntity fallback = new GroundCardEntity(target.m_9236_(), target.m_20185_(), target.m_20186_() + 0.05, target.m_20189_(), expected, target.m_146908_());
        if (!target.m_9236_().m_7967_((Entity)fallback)) {
            return false;
        }
        ItemStack removed = HandContainerItem.remove(targetPouch, index);
        if (!ItemStack.m_150942_((ItemStack)removed, (ItemStack)expected)) {
            fallback.m_146870_();
            if (!removed.m_41619_()) {
                HandContainerItem.insert(targetPouch, index, removed);
            }
            return false;
        }
        actor.m_5661_((Component)Component.m_237113_((String)"\u9644\u8fd1\u65e0\u5f03\u724c\u5806\uff0c\u6240\u62c6\u5361\u724c\u5df2\u5b89\u5168\u6295\u5165\u4e16\u754c"), false);
        return true;
    }

    private static boolean validPlayer(ServerPlayer player) {
        return player.m_6084_() && !player.m_5833_() && player.m_21205_().m_41720_() instanceof HandContainerItem;
    }

    private static boolean isStillAimedAt(ServerPlayer actor, ServerPlayer target) {
        AABB search;
        Vec3 end;
        if (actor.m_9236_() != target.m_9236_() || actor.m_20280_((Entity)target) > 25.0 || !actor.m_142582_((Entity)target)) {
            return false;
        }
        Vec3 start = actor.m_146892_();
        EntityHitResult hit = ProjectileUtil.m_37287_((Entity)actor, (Vec3)start, (Vec3)(end = start.m_82549_(actor.m_20252_(1.0f).m_82490_(5.0))), (AABB)(search = actor.m_20191_().m_82369_(actor.m_20252_(1.0f).m_82490_(5.0)).m_82400_(1.0)), entity -> entity instanceof Player && entity.m_6087_() && !entity.m_5833_(), (double)25.0);
        return hit != null && hit.m_82443_() == target;
    }

    public static void disconnect(ServerPlayer player) {
        SESSIONS.remove(player.m_20148_());
        SESSIONS.entrySet().removeIf(entry -> ((Session)entry.getValue()).targetId.equals(player.m_20148_()));
    }

    public static void clear() {
        SESSIONS.clear();
    }

    private record Session(UUID token, UUID targetId, long expiresAt) {
    }

    public static enum Action {
        DRAW,
        DISCARD;

    }
}

