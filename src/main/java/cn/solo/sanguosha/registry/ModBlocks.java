/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.SoundType
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.material.MapColor
 *  net.minecraftforge.registries.DeferredRegister
 *  net.minecraftforge.registries.RegistryObject
 */
package cn.solo.sanguosha.registry;

import cn.solo.sanguosha.block.GameTable2Block;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create((ResourceKey)Registries.f_256747_, (String)"sanguosha");
    public static final RegistryObject<Block> GAME_TABLE_2 = BLOCKS.register("game_table2", () -> new GameTable2Block(BlockBehaviour.Properties.m_284310_().m_284180_(MapColor.f_283825_).m_60913_(2.0f, 3.0f).m_60918_(SoundType.f_56736_).m_60955_()));

    private ModBlocks() {
    }
}

