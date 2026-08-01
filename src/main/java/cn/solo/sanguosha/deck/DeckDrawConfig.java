/*
 * Decompiled with CFR 0.152.
 */
package cn.solo.sanguosha.deck;

public record DeckDrawConfig(int mask) {
    public static final int FROM_BOTTOM = 1;
    public static final int HEARTS = 2;
    public static final int DIAMONDS = 4;
    public static final int CLUBS = 8;
    public static final int SPADES = 16;
    public static final int SUIT_MASK = 30;
    public static final int VALID_MASK = 31;
    public static final DeckDrawConfig DEFAULT = new DeckDrawConfig(0);

    public DeckDrawConfig {
        if ((mask & 0xFFFFFFE0) != 0) {
            throw new IllegalArgumentException("Invalid deck draw mask: " + mask);
        }
    }

    public boolean fromBottom() {
        return (this.mask & 1) != 0;
    }

    public int selectedSuits() {
        return this.mask & 0x1E;
    }

    public boolean isVanillaTopDraw() {
        return this.mask == 0;
    }

    public static DeckDrawConfig fromNetwork(int mask) {
        return new DeckDrawConfig(mask);
    }
}

