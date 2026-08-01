package cn.solo.sanguosha.registry;

import cn.solo.sanguosha.entity.GroundCardEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create((IForgeRegistry)ForgeRegistries.ENTITY_TYPES, (String)"sanguosha");
    public static final RegistryObject<EntityType<GroundCardEntity>> GROUND_CARD = ENTITY_TYPES.register("ground_card", () -> EntityType.Builder.<GroundCardEntity>m_20704_((type, level) -> new GroundCardEntity(type, level), MobCategory.MISC).m_20699_(0.6f, 0.1f).m_20702_(10).m_20717_(20).m_20712_("sanguosha:ground_card"));

    private ModEntities() {
    }
}

