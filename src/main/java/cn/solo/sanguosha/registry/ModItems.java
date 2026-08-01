/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.CreativeModeTab
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.block.Block
 *  net.minecraftforge.registries.DeferredRegister
 *  net.minecraftforge.registries.RegistryObject
 */
package cn.solo.sanguosha.registry;

import cn.solo.sanguosha.chessboard.ModChessboards;
import cn.solo.sanguosha.item.CardDeckItem;
import cn.solo.sanguosha.item.DeckCollectorItem;
import cn.solo.sanguosha.item.GeneralCardItem;
import cn.solo.sanguosha.item.GenericCardItem;
import cn.solo.sanguosha.item.HandContainerItem;
import cn.solo.sanguosha.item.HealthCardItem;
import cn.solo.sanguosha.item.IdentityCardItem;
import cn.solo.sanguosha.item.PlaceableCardItem;
import cn.solo.sanguosha.item.StandardCardItem;
import cn.solo.sanguosha.registry.ModBlocks;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create((ResourceKey)Registries.f_256913_, (String)"sanguosha");
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create((ResourceKey)Registries.f_279569_, (String)"sanguosha");
    public static final RegistryObject<Item> LORD = ModItems.role("lord", "lord");
    public static final RegistryObject<Item> LOYALIST = ModItems.role("loyalist", "loyalist");
    public static final RegistryObject<Item> REBEL = ModItems.role("rebel", "rebel");
    public static final RegistryObject<Item> RENEGADE = ModItems.role("renegade", "renegade");
    public static final RegistryObject<Item> GENERAL = ITEMS.register("general_card", () -> new GeneralCardItem(new Item.Properties().m_41487_(16)));
    public static final RegistryObject<Item> HEALTH = ITEMS.register("health_card", () -> new HealthCardItem(new Item.Properties().m_41487_(16)));
    public static final RegistryObject<Item> STANDARD_CARD = ITEMS.register("standard_card", () -> new GenericCardItem(new Item.Properties().m_41487_(64)));
    public static final RegistryObject<Item> CARD_BACK = ITEMS.register("card_back", () -> new PlaceableCardItem(new Item.Properties()));
    public static final RegistryObject<Item> HAND_CONTAINER = ITEMS.register("hand_container", () -> new HandContainerItem(new Item.Properties().m_41487_(1)));
    public static final RegistryObject<Item> DECK_COLLECTOR = ITEMS.register("deck_collector", () -> new DeckCollectorItem(new Item.Properties().m_41487_(1)));
    public static final RegistryObject<Item> GAME_TABLE_2 = ITEMS.register("game_table2", () -> new BlockItem((Block)ModBlocks.GAME_TABLE_2.get(), new Item.Properties()));
    public static final RegistryObject<Item> IDENTITY_DECK = ITEMS.register("identity_deck", () -> new CardDeckItem(CardDeckItem.DeckType.IDENTITY, new Item.Properties().m_41487_(16)));
    public static final RegistryObject<Item> NON_IDENTITY_DECK = ITEMS.register("non_identity_deck", () -> new CardDeckItem(CardDeckItem.DeckType.NON_IDENTITY, new Item.Properties().m_41487_(16)));
    public static final RegistryObject<Item> GENERAL_DECK = ITEMS.register("general_deck", () -> new CardDeckItem(CardDeckItem.DeckType.GENERAL, new Item.Properties().m_41487_(16)));
    private static final List<RegistryObject<? extends Item>> MUTABLE_STANDARD_CARDS = new ArrayList<>();
    private static final List<CardDefinition> MUTABLE_CARD_DEFINITIONS = new ArrayList<CardDefinition>();
    public static final List<RegistryObject<? extends Item>> STANDARD_CARDS;
    public static final List<CardDefinition> CARD_DEFINITIONS;
    public static final RegistryObject<CreativeModeTab> TAB;
    public static final RegistryObject<CreativeModeTab> BOARDGAMES_TAB;

    private static void card(String id, String suit, String rank, String type, boolean ex) {
        MUTABLE_CARD_DEFINITIONS.add(new CardDefinition(id, suit, rank, type, ex, MUTABLE_CARD_DEFINITIONS.size() + 1));
        MUTABLE_STANDARD_CARDS.add(ITEMS.register(id, () -> new StandardCardItem(id, suit, rank, type, ex, new Item.Properties().m_41487_(64))));
    }

    public static ItemStack createStandardCard(String id) {
        for (int i = 0; i < CARD_DEFINITIONS.size(); ++i) {
            if (!CARD_DEFINITIONS.get(i).id().equals(id)) continue;
            return new ItemStack((ItemLike)STANDARD_CARDS.get(i).get());
        }
        return ItemStack.f_41583_;
    }

    private static RegistryObject<Item> role(String id, String key) {
        return ITEMS.register(id, () -> new IdentityCardItem(key, new Item.Properties().m_41487_(16)));
    }

    private ModItems() {
    }

    static {
        ModItems.card("h_a_1_god_salvation", "h", "A", "\u9526\u56ca\u724c", false);
        ModItems.card("h_a_2_archery_attack", "h", "A", "\u9526\u56ca\u724c", false);
        ModItems.card("h_2_1_jink", "h", "2", "\u57fa\u672c\u724c", false);
        ModItems.card("h_2_2_jink", "h", "2", "\u57fa\u672c\u724c", false);
        ModItems.card("h_3_1_peach", "h", "3", "\u57fa\u672c\u724c", false);
        ModItems.card("h_3_2_amazing_grace", "h", "3", "\u9526\u56ca\u724c", false);
        ModItems.card("h_4_1_peach", "h", "4", "\u57fa\u672c\u724c", false);
        ModItems.card("h_4_2_amazing_grace", "h", "4", "\u9526\u56ca\u724c", false);
        ModItems.card("h_5_1_kylin_bow", "h", "5", "\u88c5\u5907\u724c", false);
        ModItems.card("h_5_2_chitu", "h", "5", "\u88c5\u5907\u724c", false);
        ModItems.card("h_6_1_peach", "h", "6", "\u57fa\u672c\u724c", false);
        ModItems.card("h_6_2_indulgence", "h", "6", "\u9526\u56ca\u724c", false);
        ModItems.card("h_7_1_peach", "h", "7", "\u57fa\u672c\u724c", false);
        ModItems.card("h_7_2_ex_nihilo", "h", "7", "\u9526\u56ca\u724c", false);
        ModItems.card("h_8_1_peach", "h", "8", "\u57fa\u672c\u724c", false);
        ModItems.card("h_8_2_ex_nihilo", "h", "8", "\u9526\u56ca\u724c", false);
        ModItems.card("h_9_1_peach", "h", "9", "\u57fa\u672c\u724c", false);
        ModItems.card("h_9_2_ex_nihilo", "h", "9", "\u9526\u56ca\u724c", false);
        ModItems.card("h_10_1_slash", "h", "10", "\u57fa\u672c\u724c", false);
        ModItems.card("h_10_2_slash", "h", "10", "\u57fa\u672c\u724c", false);
        ModItems.card("h_j_1_slash", "h", "J", "\u57fa\u672c\u724c", false);
        ModItems.card("h_j_2_ex_nihilo", "h", "J", "\u9526\u56ca\u724c", false);
        ModItems.card("h_q_1_peach", "h", "Q", "\u57fa\u672c\u724c", false);
        ModItems.card("h_q_2_dismantlement", "h", "Q", "\u9526\u56ca\u724c", false);
        ModItems.card("h_q_ex_lightning", "h", "Q", "\u9526\u56ca\u724c", true);
        ModItems.card("h_k_1_jink", "h", "K", "\u57fa\u672c\u724c", false);
        ModItems.card("h_k_2_zhuahuangfeidian", "h", "K", "\u88c5\u5907\u724c", false);
        ModItems.card("s_a_1_duel", "s", "A", "\u9526\u56ca\u724c", false);
        ModItems.card("s_a_2_lightning", "s", "A", "\u9526\u56ca\u724c", false);
        ModItems.card("s_2_1_double_sword", "s", "2", "\u88c5\u5907\u724c", false);
        ModItems.card("s_2_2_eight_diagram", "s", "2", "\u88c5\u5907\u724c", false);
        ModItems.card("s_2_ex_ice_sword", "s", "2", "\u88c5\u5907\u724c", true);
        ModItems.card("s_3_1_dismantlement", "s", "3", "\u9526\u56ca\u724c", false);
        ModItems.card("s_3_2_snatch", "s", "3", "\u9526\u56ca\u724c", false);
        ModItems.card("s_4_1_dismantlement", "s", "4", "\u9526\u56ca\u724c", false);
        ModItems.card("s_4_2_snatch", "s", "4", "\u9526\u56ca\u724c", false);
        ModItems.card("s_5_1_green_dragon_blade", "s", "5", "\u88c5\u5907\u724c", false);
        ModItems.card("s_5_2_jueying", "s", "5", "\u88c5\u5907\u724c", false);
        ModItems.card("s_6_1_indulgence", "s", "6", "\u9526\u56ca\u724c", false);
        ModItems.card("s_6_2_qinggang_sword", "s", "6", "\u88c5\u5907\u724c", false);
        ModItems.card("s_7_1_slash", "s", "7", "\u57fa\u672c\u724c", false);
        ModItems.card("s_7_2_savage_assault", "s", "7", "\u9526\u56ca\u724c", false);
        ModItems.card("s_8_1_slash", "s", "8", "\u57fa\u672c\u724c", false);
        ModItems.card("s_8_2_slash", "s", "8", "\u57fa\u672c\u724c", false);
        ModItems.card("s_9_1_slash", "s", "9", "\u57fa\u672c\u724c", false);
        ModItems.card("s_9_2_slash", "s", "9", "\u57fa\u672c\u724c", false);
        ModItems.card("s_10_1_slash", "s", "10", "\u57fa\u672c\u724c", false);
        ModItems.card("s_10_2_slash", "s", "10", "\u57fa\u672c\u724c", false);
        ModItems.card("s_j_1_snatch", "s", "J", "\u9526\u56ca\u724c", false);
        ModItems.card("s_j_2_nullification", "s", "J", "\u9526\u56ca\u724c", false);
        ModItems.card("s_q_1_dismantlement", "s", "Q", "\u9526\u56ca\u724c", false);
        ModItems.card("s_q_2_spear", "s", "Q", "\u88c5\u5907\u724c", false);
        ModItems.card("s_k_1_savage_assault", "s", "K", "\u9526\u56ca\u724c", false);
        ModItems.card("s_k_2_dayuan", "s", "K", "\u88c5\u5907\u724c", false);
        ModItems.card("d_a_1_crossbow", "d", "A", "\u88c5\u5907\u724c", false);
        ModItems.card("d_a_2_duel", "d", "A", "\u9526\u56ca\u724c", false);
        ModItems.card("d_2_1_jink", "d", "2", "\u57fa\u672c\u724c", false);
        ModItems.card("d_2_2_jink", "d", "2", "\u57fa\u672c\u724c", false);
        ModItems.card("d_3_1_jink", "d", "3", "\u57fa\u672c\u724c", false);
        ModItems.card("d_3_2_snatch", "d", "3", "\u9526\u56ca\u724c", false);
        ModItems.card("d_4_1_jink", "d", "4", "\u57fa\u672c\u724c", false);
        ModItems.card("d_4_2_snatch", "d", "4", "\u9526\u56ca\u724c", false);
        ModItems.card("d_5_1_jink", "d", "5", "\u57fa\u672c\u724c", false);
        ModItems.card("d_5_2_axe", "d", "5", "\u88c5\u5907\u724c", false);
        ModItems.card("d_6_1_slash", "d", "6", "\u57fa\u672c\u724c", false);
        ModItems.card("d_6_2_jink", "d", "6", "\u57fa\u672c\u724c", false);
        ModItems.card("d_7_1_slash", "d", "7", "\u57fa\u672c\u724c", false);
        ModItems.card("d_7_2_jink", "d", "7", "\u57fa\u672c\u724c", false);
        ModItems.card("d_8_1_slash", "d", "8", "\u57fa\u672c\u724c", false);
        ModItems.card("d_8_2_jink", "d", "8", "\u57fa\u672c\u724c", false);
        ModItems.card("d_9_1_slash", "d", "9", "\u57fa\u672c\u724c", false);
        ModItems.card("d_9_2_jink", "d", "9", "\u57fa\u672c\u724c", false);
        ModItems.card("d_10_1_slash", "d", "10", "\u57fa\u672c\u724c", false);
        ModItems.card("d_10_2_jink", "d", "10", "\u57fa\u672c\u724c", false);
        ModItems.card("d_j_1_jink", "d", "J", "\u57fa\u672c\u724c", false);
        ModItems.card("d_j_2_jink", "d", "J", "\u57fa\u672c\u724c", false);
        ModItems.card("d_q_1_peach", "d", "Q", "\u57fa\u672c\u724c", false);
        ModItems.card("d_q_2_halberd", "d", "Q", "\u88c5\u5907\u724c", false);
        ModItems.card("d_q_ex_nullification", "d", "Q", "\u9526\u56ca\u724c", true);
        ModItems.card("d_k_1_slash", "d", "K", "\u57fa\u672c\u724c", false);
        ModItems.card("d_k_2_zixing", "d", "K", "\u88c5\u5907\u724c", false);
        ModItems.card("c_a_1_duel", "c", "A", "\u9526\u56ca\u724c", false);
        ModItems.card("c_a_2_crossbow", "c", "A", "\u88c5\u5907\u724c", false);
        ModItems.card("c_2_1_slash", "c", "2", "\u57fa\u672c\u724c", false);
        ModItems.card("c_2_2_eight_diagram", "c", "2", "\u88c5\u5907\u724c", false);
        ModItems.card("c_2_ex_renwang_shield", "c", "2", "\u88c5\u5907\u724c", true);
        ModItems.card("c_3_1_slash", "c", "3", "\u57fa\u672c\u724c", false);
        ModItems.card("c_3_2_dismantlement", "c", "3", "\u9526\u56ca\u724c", false);
        ModItems.card("c_4_1_slash", "c", "4", "\u57fa\u672c\u724c", false);
        ModItems.card("c_4_2_dismantlement", "c", "4", "\u9526\u56ca\u724c", false);
        ModItems.card("c_5_1_slash", "c", "5", "\u57fa\u672c\u724c", false);
        ModItems.card("c_5_2_dilu", "c", "5", "\u88c5\u5907\u724c", false);
        ModItems.card("c_6_1_slash", "c", "6", "\u57fa\u672c\u724c", false);
        ModItems.card("c_6_2_indulgence", "c", "6", "\u9526\u56ca\u724c", false);
        ModItems.card("c_7_1_slash", "c", "7", "\u57fa\u672c\u724c", false);
        ModItems.card("c_7_2_savage_assault", "c", "7", "\u9526\u56ca\u724c", false);
        ModItems.card("c_8_1_slash", "c", "8", "\u57fa\u672c\u724c", false);
        ModItems.card("c_8_2_slash", "c", "8", "\u57fa\u672c\u724c", false);
        ModItems.card("c_9_1_slash", "c", "9", "\u57fa\u672c\u724c", false);
        ModItems.card("c_9_2_slash", "c", "9", "\u57fa\u672c\u724c", false);
        ModItems.card("c_10_1_slash", "c", "10", "\u57fa\u672c\u724c", false);
        ModItems.card("c_10_2_slash", "c", "10", "\u57fa\u672c\u724c", false);
        ModItems.card("c_j_1_slash", "c", "J", "\u57fa\u672c\u724c", false);
        ModItems.card("c_j_2_slash", "c", "J", "\u57fa\u672c\u724c", false);
        ModItems.card("c_q_1_collateral", "c", "Q", "\u9526\u56ca\u724c", false);
        ModItems.card("c_q_2_nullification", "c", "Q", "\u9526\u56ca\u724c", false);
        ModItems.card("c_k_1_collateral", "c", "K", "\u9526\u56ca\u724c", false);
        ModItems.card("c_k_2_nullification", "c", "K", "\u9526\u56ca\u724c", false);
        ModItems.card("h_a_3_nullification", "h", "A", "\u9526\u56ca\u724c", false);
        ModItems.card("h_2_3_fire_attack", "h", "2", "\u9526\u56ca\u724c", false);
        ModItems.card("h_3_3_fire_attack", "h", "3", "\u9526\u56ca\u724c", false);
        ModItems.card("h_4_3_fire_slash", "h", "4", "\u57fa\u672c\u724c", false);
        ModItems.card("h_5_3_peach", "h", "5", "\u57fa\u672c\u724c", false);
        ModItems.card("h_6_3_peach", "h", "6", "\u57fa\u672c\u724c", false);
        ModItems.card("h_7_3_fire_slash", "h", "7", "\u57fa\u672c\u724c", false);
        ModItems.card("h_8_3_jink", "h", "8", "\u57fa\u672c\u724c", false);
        ModItems.card("h_9_3_jink", "h", "9", "\u57fa\u672c\u724c", false);
        ModItems.card("h_10_3_fire_slash", "h", "10", "\u57fa\u672c\u724c", false);
        ModItems.card("h_j_3_jink", "h", "J", "\u57fa\u672c\u724c", false);
        ModItems.card("h_q_3_jink", "h", "Q", "\u57fa\u672c\u724c", false);
        ModItems.card("h_k_3_nullification", "h", "K", "\u9526\u56ca\u724c", false);
        ModItems.card("c_a_3_silver_lion", "c", "A", "\u88c5\u5907\u724c", false);
        ModItems.card("c_2_3_vine", "c", "2", "\u88c5\u5907\u724c", false);
        ModItems.card("c_3_3_analeptic", "c", "3", "\u57fa\u672c\u724c", false);
        ModItems.card("c_4_3_supply_shortage", "c", "4", "\u9526\u56ca\u724c", false);
        ModItems.card("c_5_3_thunder_slash", "c", "5", "\u57fa\u672c\u724c", false);
        ModItems.card("c_6_3_thunder_slash", "c", "6", "\u57fa\u672c\u724c", false);
        ModItems.card("c_7_3_thunder_slash", "c", "7", "\u57fa\u672c\u724c", false);
        ModItems.card("c_8_3_thunder_slash", "c", "8", "\u57fa\u672c\u724c", false);
        ModItems.card("c_9_3_analeptic", "c", "9", "\u57fa\u672c\u724c", false);
        ModItems.card("c_10_3_iron_chain", "c", "10", "\u9526\u56ca\u724c", false);
        ModItems.card("c_j_3_iron_chain", "c", "J", "\u9526\u56ca\u724c", false);
        ModItems.card("c_q_3_iron_chain", "c", "Q", "\u9526\u56ca\u724c", false);
        ModItems.card("c_k_3_iron_chain", "c", "K", "\u9526\u56ca\u724c", false);
        ModItems.card("s_a_3_guding_blade", "s", "A", "\u88c5\u5907\u724c", false);
        ModItems.card("s_2_3_vine", "s", "2", "\u88c5\u5907\u724c", false);
        ModItems.card("s_3_3_analeptic", "s", "3", "\u57fa\u672c\u724c", false);
        ModItems.card("s_4_3_thunder_slash", "s", "4", "\u57fa\u672c\u724c", false);
        ModItems.card("s_5_3_thunder_slash", "s", "5", "\u57fa\u672c\u724c", false);
        ModItems.card("s_6_3_thunder_slash", "s", "6", "\u57fa\u672c\u724c", false);
        ModItems.card("s_7_3_thunder_slash", "s", "7", "\u57fa\u672c\u724c", false);
        ModItems.card("s_8_3_thunder_slash", "s", "8", "\u57fa\u672c\u724c", false);
        ModItems.card("s_9_3_analeptic", "s", "9", "\u57fa\u672c\u724c", false);
        ModItems.card("s_10_3_supply_shortage", "s", "10", "\u9526\u56ca\u724c", false);
        ModItems.card("s_j_3_iron_chain", "s", "J", "\u9526\u56ca\u724c", false);
        ModItems.card("s_q_3_iron_chain", "s", "Q", "\u9526\u56ca\u724c", false);
        ModItems.card("s_k_3_nullification", "s", "K", "\u9526\u56ca\u724c", false);
        ModItems.card("d_a_3_fan", "d", "A", "\u88c5\u5907\u724c", false);
        ModItems.card("d_2_3_peach", "d", "2", "\u57fa\u672c\u724c", false);
        ModItems.card("d_3_3_peach", "d", "3", "\u57fa\u672c\u724c", false);
        ModItems.card("d_4_3_fire_slash", "d", "4", "\u57fa\u672c\u724c", false);
        ModItems.card("d_5_3_fire_slash", "d", "5", "\u57fa\u672c\u724c", false);
        ModItems.card("d_6_3_jink", "d", "6", "\u57fa\u672c\u724c", false);
        ModItems.card("d_7_3_jink", "d", "7", "\u57fa\u672c\u724c", false);
        ModItems.card("d_8_3_jink", "d", "8", "\u57fa\u672c\u724c", false);
        ModItems.card("d_9_3_analeptic", "d", "9", "\u57fa\u672c\u724c", false);
        ModItems.card("d_10_3_jink", "d", "10", "\u57fa\u672c\u724c", false);
        ModItems.card("d_j_3_jink", "d", "J", "\u57fa\u672c\u724c", false);
        ModItems.card("d_q_3_fire_attack", "d", "Q", "\u9526\u56ca\u724c", false);
        ModItems.card("d_k_3_hualiu", "d", "K", "\u88c5\u5907\u724c", false);
        STANDARD_CARDS = Collections.unmodifiableList(MUTABLE_STANDARD_CARDS);
        CARD_DEFINITIONS = Collections.unmodifiableList(MUTABLE_CARD_DEFINITIONS);
        TAB = TABS.register("cards", () -> CreativeModeTab.builder().m_257941_((Component)Component.m_237115_((String)"itemGroup.sanguosha")).m_257737_(() -> ModItems.createStandardCard(CARD_DEFINITIONS.get(0).id())).m_257501_((params, out) -> {
            out.m_246326_((ItemLike)LORD.get());
            out.m_246326_((ItemLike)LOYALIST.get());
            out.m_246326_((ItemLike)REBEL.get());
            out.m_246326_((ItemLike)RENEGADE.get());
            CARD_DEFINITIONS.forEach(card -> out.m_246342_(ModItems.createStandardCard(card.id())));
        }).m_257652_());
        BOARDGAMES_TAB = TABS.register("boardgames", () -> CreativeModeTab.builder().m_257941_((Component)Component.m_237115_((String)"itemGroup.sanguosha.boardgames")).m_257737_(() -> new ItemStack((ItemLike)IDENTITY_DECK.get())).m_257501_((params, out) -> {
            out.m_246326_((ItemLike)IDENTITY_DECK.get());
            out.m_246326_((ItemLike)NON_IDENTITY_DECK.get());
            out.m_246326_((ItemLike)GENERAL_DECK.get());
            out.m_246326_((ItemLike)GENERAL.get());
            out.m_246326_((ItemLike)HEALTH.get());
            out.m_246326_((ItemLike)HAND_CONTAINER.get());
            out.m_246326_((ItemLike)DECK_COLLECTOR.get());
            out.m_246326_((ItemLike)GAME_TABLE_2.get());
            out.m_246326_((ItemLike)ModChessboards.CHINESE_CHESSBOARD_ITEM.get());
            out.m_246326_((ItemLike)ModChessboards.GOMOKU_BOARD_ITEM.get());
            out.m_246326_((ItemLike)ModChessboards.TICTACTOE_BOARD_ITEM.get());
        }).m_257652_());
    }

    public record CardDefinition(String id, String suit, String rank, String type, boolean ex, int modelData) {
    }
}

