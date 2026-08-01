/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraftforge.common.extensions.IForgeMenuType
 *  net.minecraftforge.registries.DeferredRegister
 *  net.minecraftforge.registries.RegistryObject
 */
package cn.solo.sanguosha.registry;

import cn.solo.sanguosha.menu.GameTable2Menu;
import cn.solo.sanguosha.menu.HandContainerMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create((ResourceKey)Registries.f_256798_, (String)"sanguosha");
    public static final RegistryObject<MenuType<HandContainerMenu>> HAND_CONTAINER = MENUS.register("hand_container", () -> IForgeMenuType.create(HandContainerMenu::new));
    public static final RegistryObject<MenuType<GameTable2Menu>> GAME_TABLE_2 = MENUS.register("game_table2", () -> IForgeMenuType.create(GameTable2Menu::new));

    private ModMenus() {
    }
}

