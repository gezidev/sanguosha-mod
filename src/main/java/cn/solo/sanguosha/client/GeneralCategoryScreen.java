package cn.solo.sanguosha.client;

import cn.solo.sanguosha.client.ClientGeneralCatalog;
import cn.solo.sanguosha.client.CustomGeneralScreen;
import cn.solo.sanguosha.client.GeneralSelectionScreen;
import cn.solo.sanguosha.config.GeneralAssetManager;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;

public final class GeneralCategoryScreen
extends Screen {
    private static final List<String> BUILTIN_CATEGORIES = GeneralAssetManager.CATEGORIES;
    private final InteractionHand hand;

    public GeneralCategoryScreen(InteractionHand hand) {
        super((Component)Component.m_237113_((String)"\u9009\u62e9\u6b66\u5c06\u5305"));
        this.hand = hand;
    }

    protected void m_7856_() {
        int buttonWidth = 120;
        int buttonHeight = 24;
        int gap = 8;
        List<String> categories = this.availableCategories();
        boolean hasCustomGenerals = !ClientGeneralCatalog.byCategory("\u81ea\u5b9a\u4e49").isEmpty();
        int buttonCount = categories.size() + (hasCustomGenerals ? 1 : 0) + 1;
        int totalHeight = buttonCount * buttonHeight + (buttonCount - 1) * gap;
        int y = Math.max(32, (this.f_96544_ - totalHeight) / 2);
        for (String category : categories) {
            int count = ClientGeneralCatalog.byCategory(category).size();
            String label = category + "\uff08" + count + "\uff09";
            this.m_142416_(Button.m_253074_((Component)Component.m_237113_((String)label), button -> this.f_96541_.m_91152_((Screen)new GeneralSelectionScreen(this.hand, category, this))).m_252987_((this.f_96543_ - buttonWidth) / 2, y, buttonWidth, buttonHeight).m_253136_());
            y += buttonHeight + gap;
        }
        if (hasCustomGenerals) {
            int count = ClientGeneralCatalog.byCategory("\u81ea\u5b9a\u4e49").size();
            String label = "\u81ea\u5b9a\u4e49\uff08" + count + "\uff09";
            this.m_142416_(Button.m_253074_((Component)Component.m_237113_((String)label), button -> this.f_96541_.m_91152_((Screen)new GeneralSelectionScreen(this.hand, "\u81ea\u5b9a\u4e49", this))).m_252987_((this.f_96543_ - buttonWidth) / 2, y, buttonWidth, buttonHeight).m_253136_());
            y += buttonHeight + gap;
        }
        this.m_142416_(Button.m_253074_((Component)Component.m_237113_((String)"+ \u521b\u5efa\u81ea\u5b9a\u4e49\u6b66\u5c06"), button -> this.f_96541_.m_91152_((Screen)new CustomGeneralScreen(this.hand, this))).m_252987_((this.f_96543_ - buttonWidth) / 2, y, buttonWidth, buttonHeight).m_253136_());
    }

    private List<String> availableCategories() {
        ArrayList<String> result = new ArrayList<String>();
        for (String category : BUILTIN_CATEGORIES) {
            if (ClientGeneralCatalog.byCategory(category).isEmpty()) continue;
            result.add(category);
        }
        return result;
    }

    public void m_88315_(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.m_280273_(graphics);
        graphics.m_280653_(this.f_96547_, this.f_96539_, this.f_96543_ / 2, 12, 0xFFFFFF);
        super.m_88315_(graphics, mouseX, mouseY, partialTick);
    }
}

