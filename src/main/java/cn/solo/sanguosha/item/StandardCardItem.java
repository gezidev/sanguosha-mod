package cn.solo.sanguosha.item;

import cn.solo.sanguosha.client.CardItemClientExtension;
import cn.solo.sanguosha.item.PlaceableCardItem;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

public final class StandardCardItem
extends PlaceableCardItem {
    private static final Set<String> PLUS_ONE_MOUNTS = Set.of("jueying", "dilu", "zhuahuangfeidian", "hualiu");
    private static final Set<String> MINUS_ONE_MOUNTS = Set.of("chitu", "dayuan", "zixing");
    private static final Set<String> ARMORS = Set.of("eight_diagram", "renwang_shield", "silver_lion", "vine");
    private final String suit;
    private final String rank;
    private final String type;
    private final boolean ex;
    private final String legacyId;
    private final String cardName;
    private final EquipmentType equipmentType;

    public StandardCardItem(String legacyId, String suit, String rank, String type, boolean ex, Item.Properties properties) {
        super(properties);
        this.legacyId = legacyId;
        this.suit = suit;
        this.rank = rank;
        this.type = type;
        this.ex = ex;
        this.cardName = StandardCardItem.extractCardName(legacyId);
        this.equipmentType = StandardCardItem.classifyEquipment(type, this.cardName);
    }

    public String legacyId() {
        return this.legacyId;
    }

    public String suit() {
        return this.suit;
    }

    public String rank() {
        return this.rank;
    }

    public String cardName() {
        return this.cardName;
    }

    public String cardType() {
        return this.type;
    }

    public boolean isEx() {
        return this.ex;
    }

    public EquipmentType equipmentType() {
        return this.equipmentType;
    }

    public boolean isWeapon() {
        return this.equipmentType == EquipmentType.WEAPON;
    }

    static String extractCardName(String id) {
        String[] parts = id.split("_", 4);
        return parts.length == 4 ? parts[3] : id;
    }

    static EquipmentType classifyEquipment(String cardType, String name) {
        if (!"\u88c5\u5907\u724c".equals(cardType)) {
            return EquipmentType.NONE;
        }
        if (PLUS_ONE_MOUNTS.contains(name)) {
            return EquipmentType.PLUS_ONE_MOUNT;
        }
        if (MINUS_ONE_MOUNTS.contains(name)) {
            return EquipmentType.MINUS_ONE_MOUNT;
        }
        if (ARMORS.contains(name)) {
            return EquipmentType.ARMOR;
        }
        return EquipmentType.WEAPON;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(CardItemClientExtension.INSTANCE);
    }

    public void m_7373_(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        String symbol = switch (this.suit) {
            case "h" -> "\u2665";
            case "d" -> "\u2666";
            case "s" -> "\u2660";
            default -> "\u2663";
        };
        ChatFormatting color = this.suit.equals("h") || this.suit.equals("d") ? ChatFormatting.RED : ChatFormatting.DARK_GRAY;
        lines.add((Component)Component.m_237113_((String)(symbol + this.rank + " \u00b7 " + this.type + (this.ex ? " \u00b7 EX" : ""))).m_130940_(color));
        lines.add((Component)Component.m_237115_((String)"tooltip.sanguosha.ground_controls").m_130940_(ChatFormatting.DARK_GRAY));
    }

    public static enum EquipmentType {
        NONE,
        PLUS_ONE_MOUNT,
        MINUS_ONE_MOUNT,
        WEAPON,
        ARMOR;

    }
}

