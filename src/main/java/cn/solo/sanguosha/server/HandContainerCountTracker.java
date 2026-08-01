/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 */
package cn.solo.sanguosha.server;

import cn.solo.sanguosha.item.HandContainerItem;
import cn.solo.sanguosha.network.ModNetwork;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class HandContainerCountTracker {
    private static final double MAX_DISTANCE = 6.0;
    private static final double MAX_DISTANCE_SQR = 36.0;
    private static final Map<UUID, State> LAST_SENT = new HashMap<UUID, State>();

    private HandContainerCountTracker() {
    }

    public static void tick(ServerPlayer observer) {
        State previous;
        if (observer.f_19797_ % 2 != 0) {
            return;
        }
        State next = HandContainerCountTracker.find(observer);
        if (next.equals(previous = LAST_SENT.get(observer.m_20148_()))) {
            return;
        }
        LAST_SENT.put(observer.m_20148_(), next);
        if (previous == null && next.equals(State.CLEAR)) {
            return;
        }
        ModNetwork.sendHandContainerCount(observer, next.targetEntityId(), next.count());
    }

    public static void clear(ServerPlayer player) {
        State previous = LAST_SENT.remove(player.m_20148_());
        if (previous != null && previous.targetEntityId() >= 0) {
            ModNetwork.sendHandContainerCount(player, -1, 0);
        }
    }

    public static void clearAll() {
        LAST_SENT.clear();
    }

    private static State find(ServerPlayer observer) {
        if (!HandContainerCountTracker.holdsContainer((Player)observer) || !observer.m_6084_() || observer.m_9236_() == null) {
            return State.CLEAR;
        }
        Vec3 eye = observer.m_146892_();
        Vec3 end = eye.m_82549_(observer.m_20154_().m_82490_(6.0));
        BlockHitResult blockHit = observer.m_9236_().m_45547_(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)observer));
        double blockDistance = blockHit.m_6662_() == HitResult.Type.MISS ? 36.0 : eye.m_82557_(blockHit.m_82450_());
        AABB search = observer.m_20191_().m_82369_(end.m_82546_(eye)).m_82400_(1.0);
        EntityHitResult entityHit = null;
        double entityDistance = Double.MAX_VALUE;
        for (Entity entity : observer.m_9236_().m_6249_((Entity)observer, search, candidate -> {
            ServerPlayer target;
            return candidate instanceof ServerPlayer && (target = (ServerPlayer)candidate) != observer && target.m_6084_() && target.m_9236_() == observer.m_9236_() && HandContainerCountTracker.holdsContainer((Player)target);
        })) {
            double distance;
            AABB box = entity.m_20191_().m_82400_((double)entity.m_6143_());
            Optional hit = box.m_82371_(eye, end);
            if (!hit.isPresent() || !((distance = eye.m_82557_((Vec3)hit.get())) <= 36.0) || !(distance < entityDistance)) continue;
            entityDistance = distance;
            entityHit = new EntityHitResult(entity, (Vec3)hit.get());
        }
        if (entityHit == null || entityDistance >= blockDistance) {
            return State.CLEAR;
        }
        ServerPlayer target = (ServerPlayer)entityHit.m_82443_();
        return new State(target.m_19879_(), HandContainerItem.size(target.m_21205_()) + HandContainerItem.size(target.m_21206_()));
    }

    private static boolean holdsContainer(Player player) {
        return player.m_21205_().m_41720_() instanceof HandContainerItem || player.m_21206_().m_41720_() instanceof HandContainerItem;
    }

    private record State(int targetEntityId, int count) {
        private static final State CLEAR = new State(-1, 0);
    }
}

