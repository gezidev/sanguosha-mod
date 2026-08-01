/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.blockentity.BlockEntityRenderer
 *  net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider$Context
 *  net.minecraft.client.renderer.entity.ItemRenderer
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.core.Direction
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 */
package cn.solo.sanguosha.client;

import cn.solo.sanguosha.block.GameTable2Block;
import cn.solo.sanguosha.block.TableBlockEntity;
import cn.solo.sanguosha.client.CardItemBakedModel;
import cn.solo.sanguosha.client.GeneralCardClientExtension;
import cn.solo.sanguosha.item.IdentityCardItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public final class TableBlockEntityRenderer<T extends TableBlockEntity>
implements BlockEntityRenderer<T> {
    private static final ResourceLocation IDENTITY_BACK = new ResourceLocation("sanguosha", "textures/item/identity_card_back.png");

    public TableBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    public void m_6922_(T table, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        BlockState state = table.m_58900_();
        float yaw = ((Direction)state.m_61143_((Property)GameTable2Block.FACING)).m_122435_();
        pose.m_85836_();
        pose.m_85837_(0.5, 1.0125, 0.5);
        pose.m_252781_(Axis.f_252436_.m_252977_(-yaw));
        int nonEmpty = 0;
        for (int slot = 0; slot < 7; ++slot) {
            if (table.items().getStackInSlot(slot).m_41619_()) continue;
            ++nonEmpty;
        }
        int columns = nonEmpty >= 7 ? 4 : 3;
        int index = 0;
        for (int slot = 0; slot < 7; ++slot) {
            Item item;
            ItemStack stack = table.items().getStackInSlot(slot);
            if (stack.m_41619_()) continue;
            int row = index / columns;
            int column = index % columns;
            int rowSize = Math.min(columns, nonEmpty - row * columns);
            float spacing = columns == 4 ? 0.255f : 0.31f;
            float x = ((float)column - (float)(rowSize - 1) / 2.0f) * spacing;
            float z = nonEmpty <= columns ? 0.0f : (row == 0 ? -0.19f : 0.19f);
            pose.m_85836_();
            pose.m_252880_(x, (float)index * 8.0E-4f, z);
            pose.m_252781_(Axis.f_252436_.m_252977_(180.0f));
            pose.m_252781_(Axis.f_252436_.m_252977_((float)(-table.getRotation(slot))));
            float scale = 1.17f;
            pose.m_85841_(scale, scale, scale);
            if (slot == 0 && (item = stack.m_41720_()) instanceof IdentityCardItem) {
                IdentityCardItem identity = (IdentityCardItem)item;
                TableBlockEntityRenderer.renderIdentity(table, stack, identity, pose, buffers, light);
            } else {
                Minecraft.m_91087_().m_91291_().m_269128_(stack, ItemDisplayContext.GROUND, light, OverlayTexture.f_118083_, pose, buffers, table.m_58904_(), slot);
            }
            pose.m_85849_();
            ++index;
        }
        pose.m_85849_();
    }

    private static void renderIdentity(TableBlockEntity table, ItemStack stack, IdentityCardItem identity, PoseStack pose, MultiBufferSource buffers, int light) {
        ItemRenderer renderer = Minecraft.m_91087_().m_91291_();
        BakedModel model = renderer.m_174264_(stack, table.m_58904_(), null, 0);
        if (model instanceof CardItemBakedModel) {
            CardItemBakedModel wrapped = (CardItemBakedModel)model;
            model = wrapped.originalModel();
        }
        pose.m_85836_();
        model.m_7442_().m_269404_(ItemDisplayContext.GROUND).m_111763_(false, pose);
        pose.m_252880_(-0.5f, -0.5f, -0.5f);
        ResourceLocation front = new ResourceLocation("sanguosha", "textures/item/" + identity.identity() + ".png");
        GeneralCardClientExtension.renderTwoSided(front, IDENTITY_BACK, !table.isFaceUp(), pose, buffers, light, OverlayTexture.f_118083_);
        pose.m_85849_();
    }
}

