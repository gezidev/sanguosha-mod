/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.block.model.ItemOverrides
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraftforge.client.model.BakedModelWrapper
 */
package cn.solo.sanguosha.client;

import cn.solo.sanguosha.client.GeneralCardItemOverrides;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraftforge.client.model.BakedModelWrapper;

public final class CardItemBakedModel
extends BakedModelWrapper<BakedModel> {
    private final ItemOverrides overrides;

    public CardItemBakedModel(BakedModel originalModel) {
        this(originalModel, originalModel.m_7343_());
    }

    public CardItemBakedModel(BakedModel originalModel, ItemOverrides overrides) {
        super(originalModel);
        this.overrides = overrides;
    }

    public static CardItemBakedModel generalCard(BakedModel originalModel) {
        GeneralCardItemOverrides overrides = new GeneralCardItemOverrides();
        CardItemBakedModel wrapped = new CardItemBakedModel(originalModel, overrides);
        overrides.bind((BakedModel)wrapped);
        return wrapped;
    }

    public boolean m_7521_() {
        return true;
    }

    public ItemOverrides m_7343_() {
        return this.overrides;
    }

    public BakedModel originalModel() {
        return this.originalModel;
    }
}

