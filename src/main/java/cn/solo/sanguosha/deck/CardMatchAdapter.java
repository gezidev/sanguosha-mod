package cn.solo.sanguosha.deck;

import cn.solo.sanguosha.deck.DeckDrawConfig;
import cn.solo.sanguosha.item.StandardCardItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class CardMatchAdapter {
    private CardMatchAdapter() {
    }

    public static boolean matchCard(ItemStack stack, DeckDrawConfig config) {
        if (stack.m_41619_()) {
            return false;
        }
        int selected = config.selectedSuits();
        if (selected == 0) {
            return true;
        }
        Item item = stack.m_41720_();
        if (!(item instanceof StandardCardItem)) {
            return false;
        }
        StandardCardItem card = (StandardCardItem)item;
        int suitBit = switch (card.suit()) {
            case "h" -> 2;
            case "d" -> 4;
            case "c" -> 8;
            case "s" -> 16;
            default -> 0;
        };
        return (selected & suitBit) != 0;
    }
}

