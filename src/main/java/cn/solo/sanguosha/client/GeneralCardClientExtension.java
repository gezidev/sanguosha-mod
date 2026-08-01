/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.client.extensions.common.IClientItemExtensions
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 */
package cn.solo.sanguosha.client;

import cn.solo.sanguosha.client.ClientGeneralCatalog;
import cn.solo.sanguosha.config.GeneralDefinition;
import cn.solo.sanguosha.item.GeneralCardItem;
import cn.solo.sanguosha.item.PlaceableCardItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.EnumMap;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public final class GeneralCardClientExtension
implements IClientItemExtensions {
    public static final GeneralCardClientExtension INSTANCE = new GeneralCardClientExtension();
    private static final ResourceLocation GENERAL_CARD_BACK = new ResourceLocation("sanguosha", "textures/item/general_card_back.png");
    private static final Logger LOGGER = LogManager.getLogger((String)"SanguoshaGeneralCardRender");
    private static final String DEBUG_PROPERTY = "sanguosha.debugGeneralCardRender";
    private static final long DEBUG_INTERVAL_MILLIS = 1000L;
    private static final Map<ItemStack, EnumMap<ItemDisplayContext, Long>> DEBUG_LAST = new WeakHashMap<ItemStack, EnumMap<ItemDisplayContext, Long>>();
    private final BlockEntityWithoutLevelRenderer renderer = new BlockEntityWithoutLevelRenderer(Minecraft.m_91087_().m_167982_(), Minecraft.m_91087_().m_167973_()){

        public void m_108829_(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
            ResourceLocation front = ClientGeneralCatalog.texture(stack);
            boolean faceDown = PlaceableCardItem.isFaceDown(stack);
            GeneralCardClientExtension.debugRender(stack, context, faceDown, front);
            GeneralCardClientExtension.renderTwoSided(front, GENERAL_CARD_BACK, faceDown, pose, buffers, light, overlay);
        }
    };

    static boolean isHandContext(ItemDisplayContext context) {
        return context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND || context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void debugRender(ItemStack stack, ItemDisplayContext context, boolean faceDown, ResourceLocation resolved) {
        if (!Boolean.getBoolean(DEBUG_PROPERTY)) {
            return;
        }
        long now = System.currentTimeMillis();
        Map<ItemStack, EnumMap<ItemDisplayContext, Long>> map = DEBUG_LAST;
        synchronized (map) {
            EnumMap<ItemDisplayContext, Long> contexts = DEBUG_LAST.computeIfAbsent(stack, ignored -> new EnumMap<>(ItemDisplayContext.class));
            long previous = contexts.getOrDefault(context, 0L);
            if (now - previous < 1000L) {
                return;
            }
            contexts.put(context, now);
        }
        String generalId = GeneralCardItem.id(stack);
        GeneralDefinition definition = ClientGeneralCatalog.get(generalId).orElse(null);
        String imageId = definition == null || definition.imageId().isBlank() ? "none" : definition.imageId();
        String format = definition == null || definition.imageFormat().isBlank() ? "none" : definition.imageFormat();
        LOGGER.info("GeneralCardRender context={} GeneralId={} imageId={} format={} FaceDown={} resolved={} cache={}", (Object)context, (Object)generalId, (Object)imageId, (Object)format, (Object)faceDown, (Object)resolved, (Object)ClientGeneralCatalog.cacheStatus(generalId));
    }

    public static void applyFirstPersonFaceCorrection(ItemDisplayContext context, PoseStack pose) {
        if (context != ItemDisplayContext.FIRST_PERSON_RIGHT_HAND && context != ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
            return;
        }
        pose.m_252880_(0.5f, 0.5f, 0.5f);
        pose.m_252781_(Axis.f_252403_.m_252977_(180.0f));
        pose.m_252880_(-0.5f, -0.5f, -0.5f);
    }

    public static void renderTwoSided(ResourceLocation front, ResourceLocation back, boolean faceDown, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (front.equals((Object)back)) {
            throw new IllegalArgumentException("Card front and back textures must be different");
        }
        ResourceLocation top = faceDown ? back : front;
        ResourceLocation bottom = faceDown ? front : back;
        GeneralCardClientExtension.renderOneSide(top, true, pose, buffers, light, overlay);
        GeneralCardClientExtension.renderOneSide(bottom, false, pose, buffers, light, overlay);
    }

    public static void renderFlatTexture(ResourceLocation texture, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        GeneralCardClientExtension.renderOneSide(texture, true, pose, buffers, light, overlay);
    }

    private static void renderOneSide(ResourceLocation texture, boolean top, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        float x0 = 0.21875f;
        float x1 = 0.78125f;
        float y = (top ? 8.251f : 7.749f) / 16.0f;
        float z0 = 0.05f;
        float z1 = 0.95f;
        VertexConsumer consumer = buffers.m_6299_(RenderType.m_110470_((ResourceLocation)texture));
        Matrix4f matrix = pose.m_85850_().m_252922_();
        Matrix3f normal = pose.m_85850_().m_252943_();
        if (top) {
            consumer.m_252986_(matrix, x0, y, z0).m_6122_(255, 255, 255, 255).m_7421_(0.0f, 0.0f).m_86008_(overlay).m_85969_(light).m_252939_(normal, 0.0f, 1.0f, 0.0f).m_5752_();
            consumer.m_252986_(matrix, x0, y, z1).m_6122_(255, 255, 255, 255).m_7421_(0.0f, 1.0f).m_86008_(overlay).m_85969_(light).m_252939_(normal, 0.0f, 1.0f, 0.0f).m_5752_();
            consumer.m_252986_(matrix, x1, y, z1).m_6122_(255, 255, 255, 255).m_7421_(1.0f, 1.0f).m_86008_(overlay).m_85969_(light).m_252939_(normal, 0.0f, 1.0f, 0.0f).m_5752_();
            consumer.m_252986_(matrix, x1, y, z0).m_6122_(255, 255, 255, 255).m_7421_(1.0f, 0.0f).m_86008_(overlay).m_85969_(light).m_252939_(normal, 0.0f, 1.0f, 0.0f).m_5752_();
        } else {
            consumer.m_252986_(matrix, x1, y, z0).m_6122_(255, 255, 255, 255).m_7421_(0.0f, 0.0f).m_86008_(overlay).m_85969_(light).m_252939_(normal, 0.0f, -1.0f, 0.0f).m_5752_();
            consumer.m_252986_(matrix, x1, y, z1).m_6122_(255, 255, 255, 255).m_7421_(0.0f, 1.0f).m_86008_(overlay).m_85969_(light).m_252939_(normal, 0.0f, -1.0f, 0.0f).m_5752_();
            consumer.m_252986_(matrix, x0, y, z1).m_6122_(255, 255, 255, 255).m_7421_(1.0f, 1.0f).m_86008_(overlay).m_85969_(light).m_252939_(normal, 0.0f, -1.0f, 0.0f).m_5752_();
            consumer.m_252986_(matrix, x0, y, z0).m_6122_(255, 255, 255, 255).m_7421_(1.0f, 0.0f).m_86008_(overlay).m_85969_(light).m_252939_(normal, 0.0f, -1.0f, 0.0f).m_5752_();
        }
    }

    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
        return this.renderer;
    }

    private GeneralCardClientExtension() {
    }
}

