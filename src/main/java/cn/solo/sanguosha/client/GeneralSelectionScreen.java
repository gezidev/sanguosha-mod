package cn.solo.sanguosha.client;

import cn.solo.sanguosha.client.ClientGeneralCatalog;
import cn.solo.sanguosha.client.GeneralConfirmScreen;
import cn.solo.sanguosha.config.GeneralDefinition;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;

public final class GeneralSelectionScreen
extends Screen {
    private static final int COLUMNS = 3;
    private static final int CELL_WIDTH = 94;
    private static final int CELL_HEIGHT = 126;
    private static final int IMAGE_WIDTH = 72;
    private static final int IMAGE_HEIGHT = 101;
    private final InteractionHand hand;
    private final String category;
    private final Screen parent;
    private List<GeneralDefinition> generals = List.of();
    private int panelLeft;
    private int panelTop;
    private int panelWidth;
    private int panelHeight;
    private int scroll;

    public GeneralSelectionScreen(InteractionHand hand, String category, Screen parent) {
        super((Component)Component.m_237113_((String)(category + "\u5305\u6b66\u5c06")));
        this.hand = hand;
        this.category = category;
        this.parent = parent;
    }

    protected void m_7856_() {
        this.generals = ClientGeneralCatalog.byCategory(this.category);
        this.panelWidth = Math.min(this.f_96543_ - 24, 294);
        this.panelHeight = Math.min(this.f_96544_ - 52, 378);
        this.panelLeft = (this.f_96543_ - this.panelWidth) / 2;
        this.panelTop = 30;
    }

    public void m_88315_(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.m_280273_(graphics);
        graphics.m_280653_(this.f_96547_, this.f_96539_, this.f_96543_ / 2, 11, 0xFFFFFF);
        graphics.m_280509_(this.panelLeft - 4, this.panelTop - 4, this.panelLeft + this.panelWidth + 4, this.panelTop + this.panelHeight + 4, -585820907);
        graphics.m_280588_(this.panelLeft, this.panelTop, this.panelLeft + this.panelWidth, this.panelTop + this.panelHeight);
        for (int index = 0; index < this.generals.size(); ++index) {
            this.drawGeneral(graphics, index, mouseX, mouseY);
        }
        graphics.m_280618_();
        super.m_88315_(graphics, mouseX, mouseY, partialTick);
    }

    private void drawGeneral(GuiGraphics graphics, int index, int mouseX, int mouseY) {
        GeneralDefinition def = this.generals.get(index);
        int x = this.panelLeft + 11 + index % 3 * 94;
        int y = this.panelTop + 4 + index / 3 * 126 - this.scroll;
        if (y + 126 < this.panelTop || y > this.panelTop + this.panelHeight) {
            return;
        }
        boolean hovered = mouseX >= x && mouseX < x + 72 && mouseY >= y && mouseY < y + 101;
        graphics.m_280509_(x - 2, y - 2, x + 72 + 2, y + 101 + 2, hovered ? -13227 : -8947849);
        GeneralSelectionScreen.drawGeneralImage(graphics, def, x, y, 72, 101);
        graphics.m_280137_(this.f_96547_, def.name(), x + 36, y + 101 + 4, 0xFFFFFF);
        graphics.m_280137_(this.f_96547_, def.kingdom(), x + 36, y + 101 + 14, 0xAAAAAA);
    }

    private static void drawGeneralImage(GuiGraphics graphics, GeneralDefinition def, int x, int y, int targetWidth, int targetHeight) {
        ResourceLocation texture = ClientGeneralCatalog.texture(def.id());
        float scaleX = (float)targetWidth / 512.0f;
        float scaleY = (float)targetHeight / 720.0f;
        graphics.m_280168_().m_85836_();
        graphics.m_280168_().m_252880_((float)x, (float)y, 0.0f);
        graphics.m_280168_().m_85841_(scaleX, scaleY, 1.0f);
        graphics.m_280163_(texture, 0, 0, 0.0f, 0.0f, 512, 720, 512, 720);
        graphics.m_280168_().m_85849_();
    }

    public boolean m_6375_(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseX >= (double)this.panelLeft && mouseX < (double)(this.panelLeft + this.panelWidth) && mouseY >= (double)this.panelTop && mouseY < (double)(this.panelTop + this.panelHeight)) {
            int relativeX = (int)mouseX - this.panelLeft - 11;
            int relativeY = (int)mouseY - this.panelTop - 4 + this.scroll;
            if (relativeX >= 0 && relativeY >= 0) {
                int column = relativeX / 94;
                int row = relativeY / 126;
                int withinX = relativeX % 94;
                int withinY = relativeY % 126;
                int index = row * 3 + column;
                if (column < 3 && withinX < 72 && withinY < 101 && index >= 0 && index < this.generals.size()) {
                    this.f_96541_.m_91152_((Screen)new GeneralConfirmScreen(this.hand, this.generals.get(index), this));
                    return true;
                }
            }
        }
        return super.m_6375_(mouseX, mouseY, button);
    }

    public boolean m_6050_(double mouseX, double mouseY, double delta) {
        this.scroll = Math.max(0, Math.min(this.maxScroll(), this.scroll - (int)Math.signum(delta) * 42));
        return true;
    }

    private int maxScroll() {
        int rows = (this.generals.size() + 3 - 1) / 3;
        return Math.max(0, rows * 126 + 8 - this.panelHeight);
    }

    public void m_7379_() {
        this.f_96541_.m_91152_(this.parent);
    }
}

