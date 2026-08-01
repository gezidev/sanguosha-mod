package cn.solo.sanguosha.client;

import cn.solo.sanguosha.entity.GroundCardEntity;
import cn.solo.sanguosha.item.HandContainerItem;
import cn.solo.sanguosha.network.ModNetwork;
import cn.solo.sanguosha.registry.ModBlocks;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

@Mod.EventBusSubscriber(modid="sanguosha", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public final class HandContainerHud {
    private static final int UI_SCALE = 5;
    private static final float CARD_SCALE = 6.75f;
    private static final float FOCUSED_SCALE = 1.15f;
    private static final float MAX_ANGLE_STEP = 1.8f;
    private static final float MAX_FAN_ANGLE = 16.0f;
    private static final float MIN_CENTER_STEP = 42.0f;
    private static final int MAX_VISIBLE = 31;
    private static final int SIDE_MARGIN = 18;
    private static final int BOTTOM_MARGIN = 12;

    private HandContainerHud() {
    }

    @SubscribeEvent
    public static void onMouseButton(InputEvent.MouseButton.Pre event) {
        BlockHitResult hit;
        HitResult hitResult;
        Minecraft minecraft = Minecraft.m_91087_();
        if (minecraft.f_91074_ == null || minecraft.f_91080_ != null || event.getAction() != 1) {
            return;
        }
        if (event.getButton() == 1 && HandContainerHud.controlDown(minecraft)) {
            hitResult = minecraft.f_91077_;
            if (hitResult instanceof BlockHitResult) {
                hit = (BlockHitResult)hitResult;
                if (minecraft.f_91073_ != null && minecraft.f_91073_.m_8055_(hit.m_82425_()).m_60713_((Block)ModBlocks.GAME_TABLE_2.get())) {
                    ModNetwork.openGameTable2(hit.m_82425_());
                    event.setCanceled(true);
                    return;
                }
            }
            if ((hitResult = minecraft.f_91077_) instanceof EntityHitResult && ((EntityHitResult)hitResult).m_82443_() instanceof GroundCardEntity) {
                ModNetwork.requestDeckDrawConfig(((EntityHitResult)hitResult).m_82443_().m_19879_());
                event.setCanceled(true);
                return;
            }
        }
        if (event.getButton() != 2 || !minecraft.f_91074_.m_6047_()) {
            return;
        }
        if (!(minecraft.f_91074_.m_21205_().m_41720_() instanceof HandContainerItem)) {
            return;
        }
        hitResult = minecraft.f_91077_;
        if (hitResult instanceof EntityHitResult && ((EntityHitResult)hitResult).m_82443_() instanceof Player) {
            ModNetwork.requestHandPouchSession(((EntityHitResult)hitResult).m_82443_().m_19879_());
            event.setCanceled(true);
        }
    }

    private static boolean controlDown(Minecraft minecraft) {
        long window = minecraft.m_91268_().m_85439_();
        return InputConstants.m_84830_((long)window, (int)341) || InputConstants.m_84830_((long)window, (int)345);
    }

    @SubscribeEvent
    public static void onScroll(InputEvent.MouseScrollingEvent event) {
        int direction;
        boolean ctrl;
        Minecraft minecraft = Minecraft.m_91087_();
        if (minecraft.f_91074_ == null || minecraft.f_91080_ != null || event.getScrollDelta() == 0.0) {
            return;
        }
        long window = minecraft.m_91268_().m_85439_();
        boolean bl = ctrl = InputConstants.m_84830_((long)window, (int)341) || InputConstants.m_84830_((long)window, (int)345);
        if (!ctrl) {
            return;
        }
        HeldContainer held = HandContainerHud.findHeld(minecraft);
        if (held == null || HandContainerItem.size(held.stack) == 0) {
            return;
        }
        int n = direction = event.getScrollDelta() > 0.0 ? -1 : 1;
        if (!HandContainerItem.cycle(held.stack, direction)) {
            return;
        }
        ModNetwork.selectHandCard(held.hand, HandContainerItem.selected(held.stack));
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onHud(RenderGuiOverlayEvent.Post event) {
        CardLayout layout;
        Minecraft minecraft = Minecraft.m_91087_();
        if (minecraft.f_91074_ == null || minecraft.f_91066_.f_92062_ || minecraft.f_91080_ != null) {
            return;
        }
        HeldContainer held = HandContainerHud.findHeld(minecraft);
        if (held == null) {
            return;
        }
        List<ItemStack> cards = HandContainerItem.cards(held.stack);
        if (cards.isEmpty()) {
            return;
        }
        GuiGraphics graphics = event.getGuiGraphics();
        int selected = HandContainerItem.selected(held.stack);
        int visibleLimit = HandContainerHud.visibleCardLimit(graphics.m_280182_());
        int from = Math.max(0, Math.min(selected - visibleLimit / 2, Math.max(0, cards.size() - visibleLimit)));
        int to = Math.min(cards.size(), from + visibleLimit);
        List<CardLayout> layouts = HandContainerHud.createFanLayout(graphics, from, to);
        double mouseX = minecraft.f_91067_.m_91589_() * (double)graphics.m_280182_() / (double)minecraft.m_91268_().m_85443_();
        double mouseY = minecraft.f_91067_.m_91594_() * (double)graphics.m_280206_() / (double)minecraft.m_91268_().m_85444_();
        int hovered = HandContainerHud.findHovered(layouts, selected, mouseX, mouseY);
        for (CardLayout layout2 : layouts) {
            if (layout2.index == selected || layout2.index == hovered) continue;
            HandContainerHud.renderCard(graphics, cards.get(layout2.index), layout2, false, layout2.visual);
        }
        if (selected >= from && selected < to) {
            layout = layouts.get(selected - from);
            HandContainerHud.renderCard(graphics, cards.get(selected), layout, true, 1000);
        }
        if (hovered >= from && hovered < to && hovered != selected) {
            layout = layouts.get(hovered - from);
            HandContainerHud.renderCard(graphics, cards.get(hovered), layout, true, 1001);
        }
    }

    private static int visibleCardLimit(int guiWidth) {
        float focusedWidth = 124.2f;
        int fitting = 1 + Math.max(0, (int)(((float)guiWidth - 36.0f - focusedWidth) / 42.0f));
        int limit = Math.min(31, fitting);
        if (limit > 1 && limit % 2 == 0) {
            --limit;
        }
        return Math.max(1, limit);
    }

    private static List<CardLayout> createFanLayout(GuiGraphics graphics, int from, int to) {
        int shown = to - from;
        ArrayList<CardLayout> result = new ArrayList<CardLayout>(shown);
        float angleStep = shown <= 1 ? 0.0f : Math.min(1.8f, 16.0f / (float)(shown - 1));
        float halfAngle = angleStep * (float)(shown - 1) * 0.5f;
        double stepRadians = Math.toRadians(angleStep);
        double halfRadians = Math.toRadians(halfAngle);
        float focusedHalf = 62.1f;
        double usableHalfWidth = Math.max(1.0, (double)graphics.m_280182_() * 0.5 - 18.0 - (double)focusedHalf);
        double desiredRadius = angleStep == 0.0f ? 0.0 : 42.0 / (2.0 * Math.sin(stepRadians * 0.5));
        double fittingRadius = halfAngle == 0.0f ? desiredRadius : usableHalfWidth / Math.sin(halfRadians);
        double radius = Math.min(desiredRadius, fittingRadius);
        double sag = halfAngle == 0.0f ? 0.0 : radius * (1.0 - Math.cos(halfRadians));
        float centerX = (float)graphics.m_280182_() * 0.5f;
        float arcTopY = (float)((double)((float)(graphics.m_280206_() - 12) - focusedHalf) - sag);
        for (int visual = 0; visual < shown; ++visual) {
            float centered = (float)visual - (float)(shown - 1) * 0.5f;
            float degrees = centered * angleStep;
            double radians = Math.toRadians(degrees);
            float x = (float)((double)centerX + radius * Math.sin(radians));
            float y = (float)((double)arcTopY + radius * (1.0 - Math.cos(radians)));
            result.add(new CardLayout(from + visual, visual, x, y, degrees));
        }
        return result;
    }

    private static int findHovered(List<CardLayout> layouts, int selected, double mouseX, double mouseY) {
        for (CardLayout layout : layouts) {
            if (layout.index != selected || !HandContainerHud.contains(layout, mouseX, mouseY, 7.7625f)) continue;
            return selected;
        }
        for (int i = layouts.size() - 1; i >= 0; --i) {
            CardLayout layout;
            layout = layouts.get(i);
            if (layout.index == selected || !HandContainerHud.contains(layout, mouseX, mouseY, 6.75f)) continue;
            return layout.index;
        }
        return -1;
    }

    private static boolean contains(CardLayout layout, double mouseX, double mouseY, float scale) {
        double radians = Math.toRadians(layout.degrees);
        double dx = mouseX - (double)layout.x;
        double dy = mouseY - (double)layout.y;
        double localX = Math.cos(radians) * dx + Math.sin(radians) * dy;
        double localY = -Math.sin(radians) * dx + Math.cos(radians) * dy;
        float half = 8.0f * scale;
        return Math.abs(localX) <= (double)half && Math.abs(localY) <= (double)half;
    }

    private static void renderCard(GuiGraphics graphics, ItemStack card, CardLayout layout, boolean focused, int depth) {
        PoseStack pose = graphics.m_280168_();
        pose.m_85836_();
        pose.m_252880_(layout.x, layout.y, (float)depth);
        pose.m_252781_(new Quaternionf(new AxisAngle4f((float)Math.toRadians(layout.degrees), 0.0f, 0.0f, 1.0f)));
        float scale = 6.75f * (focused ? 1.15f : 1.0f);
        pose.m_85841_(scale, scale, 1.0f);
        graphics.m_280480_(card, -8, -8);
        pose.m_85849_();
    }

    private static HeldContainer findHeld(Minecraft minecraft) {
        ItemStack main = minecraft.f_91074_.m_21205_();
        if (main.m_41720_() instanceof HandContainerItem) {
            return new HeldContainer(InteractionHand.MAIN_HAND, main);
        }
        ItemStack off = minecraft.f_91074_.m_21206_();
        if (off.m_41720_() instanceof HandContainerItem) {
            return new HeldContainer(InteractionHand.OFF_HAND, off);
        }
        return null;
    }

    private record HeldContainer(InteractionHand hand, ItemStack stack) {
    }

    private record CardLayout(int index, int visual, float x, float y, float degrees) {
    }
}

