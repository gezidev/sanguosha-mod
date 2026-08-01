package cn.solo.sanguosha.client;

import cn.solo.sanguosha.client.ClientGeneralCatalog;
import cn.solo.sanguosha.config.GeneralDefinition;
import cn.solo.sanguosha.network.ModNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;

public final class GeneralConfirmScreen
extends Screen {
    private static final ResourceLocation FALLBACK = new ResourceLocation("sanguosha", "textures/item/general_card.png");
    private final InteractionHand hand;
    private final GeneralDefinition definition;
    private final Screen parent;

    public GeneralConfirmScreen(InteractionHand hand, GeneralDefinition definition, Screen parent) {
        super((Component)Component.m_237113_((String)definition.name()));
        this.hand = hand;
        this.definition = definition;
        this.parent = parent;
    }

    protected void m_7856_() {
        int y = this.f_96544_ - 30;
        this.m_142416_(Button.m_253074_((Component)Component.m_237113_((String)"\u53d6\u6d88"), button -> this.f_96541_.m_91152_(this.parent)).m_252987_(this.f_96543_ / 2 - 105, y, 100, 20).m_253136_());
        this.m_142416_(Button.m_253074_((Component)Component.m_237113_((String)"\u786e\u8ba4"), button -> {
            ModNetwork.selectGeneral(this.hand, this.definition.id());
            this.m_7379_();
        }).m_252987_(this.f_96543_ / 2 + 5, y, 100, 20).m_253136_());
    }

    public void m_88315_(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.m_280273_(graphics);
        int availableWidth = Math.max(1, this.f_96543_ - 20);
        int availableHeight = Math.max(1, this.f_96544_ - 54);
        float scale = (float)Math.min((double)availableWidth / 512.0, (double)availableHeight / 720.0);
        int imageWidth = Math.max(1, Math.round(512.0f * scale));
        int imageHeight = Math.max(1, Math.round(720.0f * scale));
        int x = (this.f_96543_ - imageWidth) / 2;
        int y = Math.max(4, (this.f_96544_ - 34 - imageHeight) / 2);
        graphics.m_280168_().m_85836_();
        graphics.m_280168_().m_252880_((float)x, (float)y, 0.0f);
        graphics.m_280168_().m_85841_(scale, scale, 1.0f);
        ResourceLocation texture = ClientGeneralCatalog.texture(this.definition.id());
        graphics.m_280163_(texture, 0, 0, 0.0f, 0.0f, 512, 720, 512, 720);
        graphics.m_280168_().m_85849_();
        super.m_88315_(graphics, mouseX, mouseY, partialTick);
    }

    public void m_7379_() {
        if (this.f_96541_ != null) {
            this.f_96541_.m_91152_(null);
        }
    }
}

