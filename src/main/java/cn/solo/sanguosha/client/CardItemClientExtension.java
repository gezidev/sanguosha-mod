/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.client.extensions.common.IClientItemExtensions
 *  net.minecraftforge.registries.ForgeRegistries
 */
package cn.solo.sanguosha.client;

import cn.solo.sanguosha.client.GeneralCardClientExtension;
import cn.solo.sanguosha.item.GenericCardItem;
import cn.solo.sanguosha.item.PlaceableCardItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.registries.ForgeRegistries;

public final class CardItemClientExtension
implements IClientItemExtensions {
    public static final CardItemClientExtension INSTANCE = new CardItemClientExtension();
    private static final ResourceLocation CARD_BACK = new ResourceLocation("sanguosha", "textures/item/card_back.png");
    private final BlockEntityWithoutLevelRenderer renderer = new BlockEntityWithoutLevelRenderer(Minecraft.m_91087_().m_167982_(), Minecraft.m_91087_().m_167973_()){

        public void m_108829_(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
            ItemStack frontStack = stack.m_41720_() instanceof GenericCardItem ? GenericCardItem.legacyRenderStack(stack) : stack;
            ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(frontStack.m_41720_());
            if (itemId == null) {
                return;
            }
            ResourceLocation front = new ResourceLocation(itemId.m_135827_(), "textures/item/" + itemId.m_135815_() + ".png");
            if (CardItemClientExtension.isFirstPerson(context)) {
                GeneralCardClientExtension.applyFirstPersonFaceCorrection(context, pose);
            }
            if (front.equals((Object)CARD_BACK)) {
                GeneralCardClientExtension.renderFlatTexture(CARD_BACK, pose, buffers, light, overlay);
                return;
            }
            GeneralCardClientExtension.renderTwoSided(front, CARD_BACK, PlaceableCardItem.isFaceDown(stack), pose, buffers, light, overlay);
        }
    };

    static boolean isFirstPerson(ItemDisplayContext context) {
        return context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
    }

    public static boolean isHeld(ItemDisplayContext context) {
        return context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
    }

    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
        return this.renderer;
    }

    private CardItemClientExtension() {
    }
}

