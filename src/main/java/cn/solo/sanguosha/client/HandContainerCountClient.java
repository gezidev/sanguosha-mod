/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.Font$DisplayMode
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.ClientPlayerNetworkEvent$LoggingOut
 *  net.minecraftforge.client.event.RenderLivingEvent$Post
 *  net.minecraftforge.event.level.LevelEvent$Unload
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 */
package cn.solo.sanguosha.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="sanguosha", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public final class HandContainerCountClient {
    private static int targetEntityId = -1;
    private static int count;

    private HandContainerCountClient() {
    }

    public static void accept(int entityId, int newCount) {
        if (entityId < 0) {
            HandContainerCountClient.clear();
            return;
        }
        targetEntityId = entityId;
        count = Math.max(0, newCount);
    }

    public static void clear() {
        targetEntityId = -1;
        count = 0;
    }

    static int targetEntityId() {
        return targetEntityId;
    }

    static int count() {
        return count;
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        HandContainerCountClient.clear();
    }

    @SubscribeEvent
    public static void onWorldUnload(LevelEvent.Unload event) {
        if (event.getLevel().m_5776_()) {
            HandContainerCountClient.clear();
        }
    }

    @SubscribeEvent
    public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
        Player target;
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof Player) || (target = (Player)livingEntity).m_19879_() != targetEntityId) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        LocalPlayer observer = minecraft.f_91074_;
        if (observer == null || target == observer || minecraft.f_91073_ == null) {
            return;
        }
        MutableComponent text = Component.m_237113_((String)("\u624b\u724c\u6570\u91cf\uff1a" + count));
        Font font = minecraft.f_91062_;
        float x = (float)font.m_92852_((FormattedText)target.m_5446_()) / 2.0f + 4.0f;
        PoseStack pose = event.getPoseStack();
        pose.m_85836_();
        pose.m_85837_(0.0, (double)target.m_20206_() + 0.5, 0.0);
        pose.m_252781_(minecraft.m_91290_().m_253208_());
        pose.m_85841_(-0.025f, -0.025f, 0.025f);
        RenderSystem.enableDepthTest();
        font.m_272077_((Component)text, x, 0.0f, -10929, false, pose.m_85850_().m_252922_(), event.getMultiBufferSource(), Font.DisplayMode.NORMAL, Integer.MIN_VALUE, event.getPackedLight());
        pose.m_85849_();
    }
}

