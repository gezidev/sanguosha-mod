/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.math.Axis
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.resources.ResourceLocation
 */
package cn.solo.sanguosha.client;

import cn.solo.sanguosha.client.ClientGeneralCatalog;
import cn.solo.sanguosha.client.GeneralSingleSelection;
import cn.solo.sanguosha.config.GeneralDefinition;
import cn.solo.sanguosha.network.ModNetwork;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public final class FiveGeneralSelectionScreen
extends Screen {
    private final BlockPos anchor;
    private final List<GeneralDefinition> generals = new ArrayList<GeneralDefinition>();
    private final GeneralSingleSelection selection = new GeneralSingleSelection();
    private Button confirmButton;
    private String status = "";

    public FiveGeneralSelectionScreen(BlockPos anchor, List<String> ids) {
        super((Component)Component.m_237115_((String)"screen.sanguosha.military.select"));
        this.anchor = anchor;
        this.updateOffers(ids);
    }

    public boolean matches(BlockPos other) {
        return this.anchor.equals((Object)other);
    }

    public void updateOffers(List<String> ids) {
        if (this.selection.confirmed()) {
            return;
        }
        this.generals.clear();
        ids.forEach(id -> ClientGeneralCatalog.get(id).ifPresent(this.generals::add));
        this.selection.retainOnly(this.generals.stream().map(GeneralDefinition::id).collect(Collectors.toSet()));
        this.refreshConfirmButton();
    }

    public void applyResult(boolean success, boolean complete, String message) {
        this.status = message == null ? "" : message;
        this.selection.applyResult(success);
        this.refreshConfirmButton();
    }

    protected void m_7856_() {
        this.confirmButton = (Button)this.m_142416_(Button.m_253074_((Component)Component.m_237113_((String)"\u786e\u8ba4\u9009\u62e9"), button -> this.confirmSelection()).m_252987_(this.f_96543_ / 2 - 50, this.f_96544_ - 34, 100, 20).m_253136_());
        this.refreshConfirmButton();
    }

    private void confirmSelection() {
        String selected = this.selection.beginConfirmation();
        if (selected == null) {
            return;
        }
        this.status = "";
        this.refreshConfirmButton();
        ModNetwork.selectOfferedGeneral(this.anchor, selected);
    }

    private void refreshConfirmButton() {
        if (this.confirmButton != null) {
            this.confirmButton.f_93623_ = this.selection.canConfirm();
        }
    }

    private Layout layout(int i) {
        double center = (double)(this.generals.size() - 1) / 2.0;
        double d = (double)i - center;
        int w = Math.min(100, Math.max(58, (this.f_96543_ - 60) / 5));
        int h = w * 720 / 512;
        int x = (int)((double)this.f_96543_ / 2.0 + d * (double)w * 0.72 - (double)w / 2.0);
        int y = (int)((double)this.f_96544_ * 0.56 - Math.cos(d * 0.38) * 28.0 - (double)h / 2.0);
        return new Layout(x, y, w, h, (float)(d * 5.5));
    }

    public void m_88315_(GuiGraphics g, int mx, int my, float pt) {
        this.m_280273_(g);
        g.m_280653_(this.f_96547_, this.f_96539_, this.f_96543_ / 2, 16, 0xFFFFFF);
        MutableComponent hint = this.selection.confirmed() ? Component.m_237113_((String)"\u5df2\u786e\u8ba4\uff0c\u7b49\u5f85\u5176\u4ed6\u53c2\u6218\u8005") : (this.selection.pending() ? Component.m_237113_((String)"\u6b63\u5728\u786e\u8ba4\uff0c\u8bf7\u7a0d\u5019") : Component.m_237113_((String)"\u9009\u62e9\u4e00\u540d\u6b66\u5c06\uff0c\u518d\u70b9\u51fb\u786e\u8ba4"));
        g.m_280653_(this.f_96547_, (Component)hint, this.f_96543_ / 2, 30, this.selection.confirmed() ? 0x66FF66 : 0xCCCCCC);
        if (!this.status.isBlank()) {
            g.m_280137_(this.f_96547_, this.status, this.f_96543_ / 2, 44, this.selection.confirmed() ? 0x66FF66 : 0xFF6666);
        }
        for (int i = 0; i < this.generals.size(); ++i) {
            GeneralDefinition def = this.generals.get(i);
            Layout l = this.layout(i);
            boolean selected = def.id().equals(this.selection.selectedGeneral());
            boolean hover = !this.selection.confirmed() && !this.selection.pending() && l.contains(mx, my);
            g.m_280168_().m_85836_();
            g.m_280168_().m_85837_((double)l.x + (double)l.w / 2.0, (double)(l.y + l.h), (double)(100 + i));
            g.m_280168_().m_252781_(Axis.f_252403_.m_252977_(l.angle));
            float s = hover ? 1.12f : 1.0f;
            g.m_280168_().m_85841_(s, s, 1.0f);
            g.m_280168_().m_85837_((double)(-l.w) / 2.0, (double)(-l.h), 0.0);
            g.m_280509_(-2, -2, l.w + 2, l.h + 2, selected ? -13227 : -8947849);
            ResourceLocation texture = ClientGeneralCatalog.texture(def.id());
            g.m_280168_().m_85841_((float)l.w / 512.0f, (float)l.h / 720.0f, 1.0f);
            g.m_280163_(texture, 0, 0, 0.0f, 0.0f, 512, 720, 512, 720);
            g.m_280168_().m_85849_();
            g.m_280137_(this.f_96547_, def.name(), l.x + l.w / 2, l.y + l.h + 4, 0xFFFFFF);
        }
        super.m_88315_(g, mx, my, pt);
    }

    public boolean m_6375_(double x, double y, int button) {
        if (button == 0 && !this.selection.pending() && !this.selection.confirmed()) {
            for (int i = this.generals.size() - 1; i >= 0; --i) {
                Layout l = this.layout(i);
                if (!l.contains(x, y)) continue;
                this.selection.click(this.generals.get(i).id());
                this.status = "";
                this.refreshConfirmButton();
                return true;
            }
        }
        return super.m_6375_(x, y, button);
    }

    public boolean m_6913_() {
        return false;
    }

    public boolean m_7043_() {
        return false;
    }

    private record Layout(int x, int y, int w, int h, float angle) {
        boolean contains(double px, double py) {
            return px >= (double)this.x && px < (double)(this.x + this.w) && py >= (double)this.y && py < (double)(this.y + this.h);
        }
    }
}

