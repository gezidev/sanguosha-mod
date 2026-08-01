package cn.solo.sanguosha.deck;

import cn.solo.sanguosha.deck.DeckDrawConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

public final class PlayerDeckDrawConfig {
    private static final String ROOT = "SanguoshaDeckDraw";
    private static final String MASK = "Mask";

    private PlayerDeckDrawConfig() {
    }

    public static DeckDrawConfig get(Player player) {
        CompoundTag persistent = player.getPersistentData().m_128469_("PlayerPersisted");
        CompoundTag root = persistent.m_128469_(ROOT);
        int mask = root.m_128441_(MASK) ? root.m_128451_(MASK) : 0;
        return (mask & 0xFFFFFFE0) == 0 ? new DeckDrawConfig(mask) : DeckDrawConfig.DEFAULT;
    }

    public static void set(Player player, DeckDrawConfig config) {
        CompoundTag forgeData = player.getPersistentData();
        CompoundTag persistent = forgeData.m_128469_("PlayerPersisted");
        CompoundTag root = persistent.m_128469_(ROOT);
        root.m_128405_(MASK, config.mask());
        persistent.m_128365_(ROOT, (Tag)root);
        forgeData.m_128365_("PlayerPersisted", (Tag)persistent);
    }

    public static void copyOnClone(Player original, Player clone) {
        PlayerDeckDrawConfig.set(clone, PlayerDeckDrawConfig.get(original));
    }
}

