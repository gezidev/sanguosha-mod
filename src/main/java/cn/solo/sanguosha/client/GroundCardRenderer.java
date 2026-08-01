/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.client.renderer.entity.ItemRenderer
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 */
package cn.solo.sanguosha.client;

import cn.solo.sanguosha.client.CardItemBakedModel;
import cn.solo.sanguosha.client.ClientGeneralCatalog;
import cn.solo.sanguosha.client.GeneralCardClientExtension;
import cn.solo.sanguosha.entity.GroundCardEntity;
import cn.solo.sanguosha.item.GeneralCardItem;
import cn.solo.sanguosha.item.GenericCardItem;
import cn.solo.sanguosha.item.IdentityCardItem;
import cn.solo.sanguosha.registry.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class GroundCardRenderer
extends EntityRenderer<GroundCardEntity> {
    private static final ResourceLocation CARD_BACK = new ResourceLocation("sanguosha", "textures/item/card_back.png");
    private static final ItemStack CARD_BACK_STACK = new ItemStack((ItemLike)ModItems.CARD_BACK.get());

    public GroundCardRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.f_114477_ = 0.0f;
    }

    public void m_7392_(GroundCardEntity entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        boolean specialTopCard;
        ItemStack topCard = entity.getCard();
        int cardCount = entity.getCardCount();
        if (cardCount <= 0 || topCard.m_41619_()) {
            return;
        }
        boolean faceDown = entity.isFaceDown();
        boolean bl = specialTopCard = topCard.m_41720_() instanceof GeneralCardItem || topCard.m_41720_() instanceof IdentityCardItem;
        ItemStack renderStack = faceDown ? (specialTopCard ? topCard : CARD_BACK_STACK) : (topCard.m_41720_() instanceof GenericCardItem ? GenericCardItem.legacyRenderStack(topCard) : topCard);
        double layerHeight = Math.max(3.5E-4, 0.018 / Math.pow(Math.max(1.0, (double)cardCount / 8.0), 0.72));
        for (int i = 0; i < cardCount; ++i) {
            boolean topLayer;
            pose.m_85836_();
            pose.m_85837_(0.0, (double)i * layerHeight, 0.0);
            pose.m_252781_(Axis.f_252436_.m_252977_(180.0f - entity.getCardRotation()));
            pose.m_85841_(2.16f, 2.16f, 2.16f);
            boolean bl2 = topLayer = i == cardCount - 1;
            if (topLayer && specialTopCard) {
                GroundCardRenderer.renderSpecialTopCard(topCard, faceDown, entity, pose, buffers, light);
            } else {
                ItemStack layerStack = topLayer ? renderStack : CARD_BACK_STACK;
                Minecraft.m_91087_().m_91291_().m_269128_(layerStack, ItemDisplayContext.GROUND, light, OverlayTexture.f_118083_, pose, buffers, entity.m_9236_(), entity.m_19879_() + i);
            }
            pose.m_85849_();
        }
        super.m_7392_(entity, yaw, partialTick, pose, buffers, light);
    }

    private static void renderSpecialTopCard(ItemStack stack, boolean faceDown, GroundCardEntity entity, PoseStack pose, MultiBufferSource buffers, int light) {
        ResourceLocation back;
        ResourceLocation front;
        ItemRenderer itemRenderer = Minecraft.m_91087_().m_91291_();
        BakedModel model = itemRenderer.m_174264_(stack, entity.m_9236_(), null, entity.m_19879_());
        if (model instanceof CardItemBakedModel) {
            CardItemBakedModel wrapped = (CardItemBakedModel)model;
            model = wrapped.originalModel();
        }
        pose.m_85836_();
        model.m_7442_().m_269404_(ItemDisplayContext.GROUND).m_111763_(false, pose);
        pose.m_252880_(-0.5f, -0.5f, -0.5f);
        Item item = stack.m_41720_();
        if (item instanceof IdentityCardItem) {
            IdentityCardItem identity = (IdentityCardItem)item;
            front = new ResourceLocation("sanguosha", "textures/item/" + identity.identity() + ".png");
            back = new ResourceLocation("sanguosha", "textures/item/identity_card_back.png");
        } else {
            front = ClientGeneralCatalog.texture(stack);
            back = new ResourceLocation("sanguosha", "textures/item/general_card_back.png");
        }
        GeneralCardClientExtension.renderTwoSided(front, back, faceDown, pose, buffers, light, OverlayTexture.f_118083_);
        pose.m_85849_();
    }

    public ResourceLocation m_5478_(GroundCardEntity entity) {
        return CARD_BACK;
    }
}

