/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.entity.BlockEntityType$Builder
 *  net.minecraftforge.registries.DeferredRegister
 *  net.minecraftforge.registries.RegistryObject
 */
package cn.solo.sanguosha.registry;

import cn.solo.sanguosha.block.GameTable2BlockEntity;
import cn.solo.sanguosha.registry.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create((ResourceKey)Registries.f_256922_, (String)"sanguosha");
    public static final RegistryObject<BlockEntityType<GameTable2BlockEntity>> GAME_TABLE_2 = BLOCK_ENTITIES.register("game_table2", () -> BlockEntityType.Builder.m_155273_(GameTable2BlockEntity::new, (Block[])new Block[]{(Block)ModBlocks.GAME_TABLE_2.get()}).m_58966_(null));

    private ModBlockEntities() {
    }
}

