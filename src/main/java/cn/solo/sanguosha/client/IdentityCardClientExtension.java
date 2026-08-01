/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.client.extensions.common.IClientItemExtensions
 */
package cn.solo.sanguosha.client;

import cn.solo.sanguosha.client.CardItemClientExtension;
import cn.solo.sanguosha.client.GeneralCardClientExtension;
import cn.solo.sanguosha.item.IdentityCardItem;
import cn.solo.sanguosha.item.PlaceableCardItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public final class IdentityCardClientExtension
implements IClientItemExtensions {
    public static final IdentityCardClientExtension INSTANCE = new IdentityCardClientExtension();
    private static final ResourceLocation IDENTITY_BACK = new ResourceLocation("sanguosha", "textures/item/identity_card_back.png");
    private final BlockEntityWithoutLevelRenderer renderer = new BlockEntityWithoutLevelRenderer(Minecraft.m_91087_().m_167982_(), Minecraft.m_91087_().m_167973_()){

        public void m_108829_(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
            Item item = stack.m_41720_();
            if (!(item instanceof IdentityCardItem)) {
                return;
            }
            IdentityCardItem identityCard = (IdentityCardItem)item;
            ResourceLocation front = new ResourceLocation("sanguosha", "textures/item/" + identityCard.identity() + ".png");
            if (CardItemClientExtension.isFirstPerson(context)) {
                GeneralCardClientExtension.applyFirstPersonFaceCorrection(context, pose);
            }
            GeneralCardClientExtension.renderTwoSided(front, IDENTITY_BACK, PlaceableCardItem.isFaceDown(stack), pose, buffers, light, overlay);
        }
    };

    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
        return this.renderer;
    }

    private IdentityCardClientExtension() {
    }
}

