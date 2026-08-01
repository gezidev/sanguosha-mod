package cn.solo.sanguosha.client;

import cn.solo.sanguosha.network.ModNetwork;
import java.util.UUID;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class HandPouchCardBackScreen
extends Screen {
    private static final ResourceLocation CARD_BACK = new ResourceLocation("sanguosha", "textures/item/card_back.png");
    private static final int CARD_W = 35;
    private static final int CARD_H = 51;
    private static final int GAP = 7;
    private final UUID token;
    private final String targetName;
    private final int count;
    private final boolean draw;
    private boolean sent;

    public HandPouchCardBackScreen(UUID token, String targetName, int count, boolean draw) {
        super((Component)Component.m_237113_((String)(draw ? "\u9009\u62e9\u8981\u987a\u8d70\u7684\u724c" : "\u9009\u62e9\u8981\u62c6\u6389\u7684\u724c")));
        this.token = token;
        this.targetName = targetName;
        this.count = Math.max(0, count);
        this.draw = draw;
    }

    public void m_88315_(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.m_280273_(graphics);
        super.m_88315_(graphics, mouseX, mouseY, partialTick);
        graphics.m_280653_(this.f_96547_, this.f_96539_, this.f_96543_ / 2, 14, 0xFFFFFF);
        graphics.m_280137_(this.f_96547_, this.targetName + " \u00b7 " + this.count + " \u5f20", this.f_96543_ / 2, 28, 12568533);
        for (int i = 0; i < this.count; ++i) {
            int x = this.cardX(i);
            int y = this.cardY(i);
            graphics.m_280163_(CARD_BACK, x, y, 0.0f, 0.0f, 35, 51, 35, 51);
            graphics.m_280137_(this.f_96547_, Integer.toString(i + 1), x + 17, y + 51 - 11, 0xFFFFFF);
        }
    }

    public boolean m_6375_(double mouseX, double mouseY, int button) {
        if (button == 0 && !this.sent) {
            for (int i = 0; i < this.count; ++i) {
                int x = this.cardX(i);
                int y = this.cardY(i);
                if (!(mouseX >= (double)x) || !(mouseX < (double)(x + 35)) || !(mouseY >= (double)y) || !(mouseY < (double)(y + 51))) continue;
                this.sent = true;
                ModNetwork.chooseHandPouchCard(this.token, this.draw, i);
                this.m_7379_();
                return true;
            }
        }
        return super.m_6375_(mouseX, mouseY, button);
    }

    private int columns() {
        return Math.max(1, Math.min(10, (this.f_96543_ - 20) / 42));
    }

    private int cardX(int index) {
        int columns = this.columns();
        int rowCount = Math.min(columns, this.count);
        int row = index / columns;
        int cardsInRow = Math.min(columns, this.count - row * columns);
        int rowWidth = cardsInRow * 35 + Math.max(0, cardsInRow - 1) * 7;
        return (this.f_96543_ - rowWidth) / 2 + index % columns * 42;
    }

    private int cardY(int index) {
        return 48 + index / this.columns() * 58;
    }
}

