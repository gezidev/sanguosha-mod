package cn.solo.sanguosha.item;

import cn.solo.sanguosha.client.ClientScreens;
import cn.solo.sanguosha.client.GeneralCardClientExtension;
import cn.solo.sanguosha.config.GeneralAssetManager;
import cn.solo.sanguosha.config.GeneralDefinition;
import cn.solo.sanguosha.config.GeneralManager;
import cn.solo.sanguosha.item.PlaceableCardItem;
import cn.solo.sanguosha.registry.ModItems;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

public final class GeneralCardItem
extends PlaceableCardItem {
    public static final String GENERAL_ID_TAG = "GeneralId";
    public static final String CUSTOM_IMAGE_TAG = "CustomImagePng";

    public GeneralCardItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(GeneralCardClientExtension.INSTANCE);
    }

    @Override
    public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
        if (player.m_6047_()) {
            return super.m_7203_(level, player, hand);
        }
        ItemStack stack = player.m_21120_(hand);
        if (level.f_46443_) {
            String generalId = GeneralCardItem.id(stack);
            if (generalId.isBlank()) {
                ClientScreens.openGeneralCard(hand);
            } else {
                ClientScreens.openGeneralPreview(stack);
            }
        }
        return InteractionResultHolder.m_19092_(stack, (boolean)level.f_46443_);
    }

    public static ItemStack create(String id, int count) {
        ItemStack stack = new ItemStack((ItemLike)ModItems.GENERAL.get(), count);
        stack.m_41784_().m_128359_(GENERAL_ID_TAG, id);
        return stack;
    }

    public static void setGeneral(ItemStack stack, String generalId) {
        stack.m_41784_().m_128359_(GENERAL_ID_TAG, generalId);
        stack.m_41784_().m_128379_("SanguoshaFaceDown", false);
        stack.m_41784_().m_128473_(CUSTOM_IMAGE_TAG);
        stack.m_41787_();
    }

    public static String id(ItemStack stack) {
        return stack.m_41782_() ? stack.m_41783_().m_128461_(GENERAL_ID_TAG) : "";
    }

    public static boolean isCustomId(String generalId) {
        return generalId != null && generalId.startsWith("custom_");
    }

    private static GeneralDefinition getDefinition(String generalId) {
        if (generalId == null || generalId.isBlank()) {
            return null;
        }
        Optional<GeneralAssetManager.GeneralAsset> asset = GeneralAssetManager.get(generalId);
        if (asset.isPresent()) {
            GeneralAssetManager.GeneralAsset a = asset.get();
            return new GeneralDefinition(a.id(), a.chineseName(), a.kingdom(), 0, "", List.of());
        }
        return GeneralManager.get(generalId).orElse(null);
    }

    public Component m_7626_(ItemStack stack) {
        GeneralDefinition def;
        String generalId = GeneralCardItem.id(stack);
        if (GeneralCardItem.isCustomId(generalId) && (def = GeneralCardItem.getDefinition(generalId)) != null) {
            return Component.m_237113_((String)def.name());
        }
        return GeneralAssetManager.get(generalId).map(asset -> (Component)Component.m_237113_((String)asset.chineseName())).orElseGet(() -> super.m_7626_(stack));
    }

    public void m_7373_(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        String generalId = GeneralCardItem.id(stack);
        if (GeneralCardItem.isCustomId(generalId)) {
            GeneralDefinition def = GeneralCardItem.getDefinition(generalId);
            if (def != null) {
                lines.add((Component)Component.m_237113_((String)("\u52bf\u529b\uff1a" + def.kingdom() + "  \u4f53\u529b\uff1a" + def.health())).m_130940_(ChatFormatting.GRAY));
                for (GeneralDefinition.Skill skill : def.skills()) {
                    if (skill.name().isBlank() && skill.description().isBlank()) continue;
                    String prefix = skill.name().isBlank() ? "\u6280\u80fd" : skill.name();
                    lines.add((Component)Component.m_237113_((String)(prefix + "\uff1a" + skill.description())).m_130940_(ChatFormatting.GOLD));
                }
            } else {
                lines.add((Component)Component.m_237113_((String)"\u81ea\u5b9a\u4e49\u6b66\u5c06\uff08\u6570\u636e\u672a\u540c\u6b65\uff09").m_130940_(ChatFormatting.YELLOW));
            }
        } else {
            GeneralAssetManager.get(generalId).ifPresentOrElse(asset -> lines.add((Component)Component.m_237113_((String)("\u52bf\u529b\uff1a" + asset.kingdom())).m_130940_(ChatFormatting.GRAY)), () -> lines.add((Component)Component.m_237115_((String)"tooltip.sanguosha.select_general").m_130940_(ChatFormatting.YELLOW)));
        }
        lines.add((Component)Component.m_237115_((String)"tooltip.sanguosha.general_controls").m_130940_(ChatFormatting.DARK_GRAY));
    }
}

