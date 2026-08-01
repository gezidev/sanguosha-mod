package cn.solo.sanguosha.network;

import cn.solo.sanguosha.entity.GroundCardEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

final class DeckTargetValidator {
    private static final double RANGE = 5.0;

    private DeckTargetValidator() {
    }

    static GroundCardEntity validate(ServerPlayer player, int entityId) {
        AABB search;
        Vec3 end;
        GroundCardEntity deck;
        if (player == null || !player.m_6084_() || player.m_213877_() || player.m_5833_()) {
            return null;
        }
        Entity candidate = player.m_9236_().m_6815_(entityId);
        if (!(candidate instanceof GroundCardEntity) || !(deck = (GroundCardEntity)candidate).m_6084_() || deck.m_213877_()) {
            return null;
        }
        if (player.m_9236_() != deck.m_9236_() || player.m_20280_((Entity)deck) > 25.0 || !player.m_142582_((Entity)deck)) {
            return null;
        }
        Vec3 start = player.m_146892_();
        EntityHitResult entityHit = ProjectileUtil.m_37287_((Entity)player, (Vec3)start, (Vec3)(end = start.m_82549_(player.m_20252_(1.0f).m_82490_(5.0))), (AABB)(search = player.m_20191_().m_82369_(player.m_20252_(1.0f).m_82490_(5.0)).m_82400_(1.0)), entity -> entity instanceof GroundCardEntity && entity.m_6087_() && !entity.m_5833_(), (double)25.0);
        if (entityHit == null || entityHit.m_82443_() != deck) {
            return null;
        }
        BlockHitResult blockHit = player.m_9236_().m_45547_(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)player));
        return blockHit.m_6662_() == HitResult.Type.MISS || start.m_82557_(entityHit.m_82450_()) <= start.m_82557_(blockHit.m_82450_()) + 0.01 ? deck : null;
    }
}

