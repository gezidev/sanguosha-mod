/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.InteractionHand
 */
package cn.solo.sanguosha.client;

import cn.solo.sanguosha.client.ClientGeneralCatalog;
import cn.solo.sanguosha.client.CustomGeneralImageManager;
import cn.solo.sanguosha.config.GeneralDefinition;
import cn.solo.sanguosha.network.ModNetwork;
import java.util.ArrayList;
import java.util.UUID;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;

public final class CustomGeneralScreen
extends Screen {
    private final InteractionHand hand;
    private final Screen parent;
    private EditBox name;
    private EditBox kingdom;
    private EditBox hp;
    private EditBox skillOneName;
    private EditBox skillOneText;
    private EditBox skillTwoName;
    private EditBox skillTwoText;
    private Button saveButton;
    private String selectedFileName = "";
    private String status = "";
    private volatile boolean loading;
    private CustomGeneralImageManager.SelectedImage selected;

    public CustomGeneralScreen(InteractionHand hand, Screen parent) {
        super((Component)Component.m_237113_((String)"\u81ea\u5b9a\u4e49\u6b66\u5c06"));
        this.hand = hand;
        this.parent = parent;
    }

    protected void m_7856_() {
        int fieldWidth = Math.min(310, this.f_96543_ - 24);
        int left = (this.f_96543_ - fieldWidth) / 2;
        int top = 12;
        int step = 25;
        this.name = this.addField(left, top, fieldWidth, "\u6b66\u5c06\u59d3\u540d", "");
        this.kingdom = this.addField(left, top + step, fieldWidth, "\u52bf\u529b", "");
        this.hp = this.addField(left, top + step * 2, fieldWidth, "\u4f53\u529b", "4");
        this.skillOneName = this.addField(left, top + step * 3, fieldWidth, "\u6280\u80fd\u4e00\u540d\u79f0", "");
        this.skillOneText = this.addField(left, top + step * 4, fieldWidth, "\u6280\u80fd\u4e00\u63cf\u8ff0", "");
        this.skillTwoName = this.addField(left, top + step * 5, fieldWidth, "\u6280\u80fd\u4e8c\u540d\u79f0", "");
        this.skillTwoText = this.addField(left, top + step * 6, fieldWidth, "\u6280\u80fd\u4e8c\u63cf\u8ff0", "");
        this.m_142416_(Button.m_253074_((Component)Component.m_237113_((String)"\u9009\u62e9\u56fe\u7247\uff08PNG/GIF\uff09"), button -> this.chooseImage()).m_252987_(this.f_96543_ / 2 - 155, top + step * 7 + 8, 310, 20).m_253136_());
        this.m_142416_(Button.m_253074_((Component)Component.m_237113_((String)"\u53d6\u6d88"), button -> this.m_7379_()).m_252987_(this.f_96543_ / 2 - 105, this.f_96544_ - 24, 100, 20).m_253136_());
        this.saveButton = (Button)this.m_142416_(Button.m_253074_((Component)Component.m_237113_((String)"\u4fdd\u5b58\u5230\u670d\u52a1\u5668"), button -> this.save()).m_252987_(this.f_96543_ / 2 + 5, this.f_96544_ - 24, 100, 20).m_253136_());
        this.saveButton.f_93623_ = this.selected != null && this.selected.success() && !this.loading;
    }

    private EditBox addField(int x, int y, int fieldWidth, String hint, String value) {
        EditBox field = new EditBox(this.f_96547_, x, y + 8, fieldWidth, 17, (Component)Component.m_237113_((String)hint));
        field.m_257771_((Component)Component.m_237113_((String)hint));
        field.m_94144_(value);
        this.m_142416_(field);
        return field;
    }

    private void chooseImage() {
        if (this.loading) {
            return;
        }
        this.loading = true;
        this.selected = null;
        this.selectedFileName = "";
        this.saveButton.f_93623_ = false;
        this.status = "\u6b63\u5728\u6253\u5f00\u7cfb\u7edf\u6587\u4ef6\u9009\u62e9\u5668\u2026\u2026";
        CustomGeneralImageManager.chooseAsync().whenComplete((result, error) -> {
            if (this.f_96541_ == null) {
                return;
            }
            this.f_96541_.execute(() -> {
                this.loading = false;
                if (error != null || result == null || !result.success()) {
                    this.status = result == null ? "\u56fe\u7247\u9009\u62e9\u5931\u8d25" : result.message();
                    return;
                }
                this.selected = result;
                this.selectedFileName = result.fileName();
                this.status = result.message();
                this.saveButton.f_93623_ = true;
            });
        });
    }

    private void save() {
        int health;
        String valueName = CustomGeneralScreen.limit(this.name.m_94155_(), 32);
        String valueKingdom = CustomGeneralScreen.limit(this.kingdom.m_94155_(), 16);
        String valueHp = CustomGeneralScreen.limit(this.hp.m_94155_(), 8);
        if (valueName.isBlank()) {
            this.status = "\u8bf7\u586b\u5199\u6b66\u5c06\u59d3\u540d";
            return;
        }
        if (this.selected == null || !this.selected.success()) {
            this.status = "\u8bf7\u5148\u9009\u62e9\u6709\u6548\u56fe\u7247";
            return;
        }
        try {
            health = Integer.parseInt(valueHp.isBlank() ? "4" : valueHp);
        }
        catch (NumberFormatException exception) {
            this.status = "\u4f53\u529b\u5fc5\u987b\u662f\u6570\u5b57";
            return;
        }
        ArrayList<GeneralDefinition.Skill> skills = new ArrayList<GeneralDefinition.Skill>();
        String skill1 = CustomGeneralScreen.limit(this.skillOneName.m_94155_(), 32);
        String text1 = CustomGeneralScreen.limit(this.skillOneText.m_94155_(), 160);
        String skill2 = CustomGeneralScreen.limit(this.skillTwoName.m_94155_(), 32);
        String text2 = CustomGeneralScreen.limit(this.skillTwoText.m_94155_(), 160);
        if (!skill1.isBlank() || !text1.isBlank()) {
            skills.add(new GeneralDefinition.Skill(skill1, text1));
        }
        if (!skill2.isBlank() || !text2.isBlank()) {
            skills.add(new GeneralDefinition.Skill(skill2, text2));
        }
        String id = "custom_" + UUID.randomUUID().toString().replace("-", "");
        GeneralDefinition def = new GeneralDefinition(id, valueName, valueKingdom, health, this.selected.contentId(), this.selected.format(), skills);
        this.saveButton.f_93623_ = false;
        this.status = "\u6b63\u5728\u4e0a\u4f20\u56fe\u7247\u2026\u2026";
        ModNetwork.uploadAndCreateCustomGeneral(this.hand, def, this.selected.data());
        ClientGeneralCatalog.registerCustomGeneral(def, this.selected.data());
        this.m_7379_();
    }

    private static String limit(String value, int max) {
        String trimmed = value == null ? "" : value.trim();
        return trimmed.length() <= max ? trimmed : trimmed.substring(0, max);
    }

    public void m_88315_(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.m_280273_(graphics);
        graphics.m_280653_(this.f_96547_, this.f_96539_, this.f_96543_ / 2, 3, 0xFFFFFF);
        this.drawLabel(graphics, "\u6b66\u5c06\u59d3\u540d", this.name);
        this.drawLabel(graphics, "\u52bf\u529b", this.kingdom);
        this.drawLabel(graphics, "\u4f53\u529b", this.hp);
        this.drawLabel(graphics, "\u6280\u80fd\u4e00\u540d\u79f0", this.skillOneName);
        this.drawLabel(graphics, "\u6280\u80fd\u4e00\u63cf\u8ff0", this.skillOneText);
        this.drawLabel(graphics, "\u6280\u80fd\u4e8c\u540d\u79f0", this.skillTwoName);
        this.drawLabel(graphics, "\u6280\u80fd\u4e8c\u63cf\u8ff0", this.skillTwoText);
        if (!this.selectedFileName.isBlank()) {
            graphics.m_280137_(this.f_96547_, "\u5df2\u9009\u62e9\uff1a" + this.selectedFileName, this.f_96543_ / 2, this.f_96544_ - 58, 0xAAAAAA);
        }
        if (!this.status.isBlank()) {
            graphics.m_280137_(this.f_96547_, this.status, this.f_96543_ / 2, this.f_96544_ - 46, this.status.contains("\u5931\u8d25") || this.status.contains("\u65e0\u6548") || this.status.contains("\u8bf7") ? 0xFF5555 : 0x55FF55);
        }
        super.m_88315_(graphics, mouseX, mouseY, partialTick);
    }

    private void drawLabel(GuiGraphics graphics, String label, EditBox field) {
        graphics.m_280488_(this.f_96547_, label, field.m_252754_(), field.m_252907_() - 10, 0xCFCFCF);
    }

    public void m_7379_() {
        if (this.f_96541_ != null) {
            this.f_96541_.m_91152_(this.parent);
        }
    }

    public boolean m_7043_() {
        return false;
    }
}

