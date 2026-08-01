package cn.solo.sanguosha.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class GeneralCardItemOverrides
extends ItemOverrides {
    private BakedModel customRenderedModel;

    void bind(BakedModel customRenderedModel) {
        if (this.customRenderedModel != null) {
            throw new IllegalStateException("General card model already bound");
        }
        this.customRenderedModel = customRenderedModel;
    }

    public BakedModel m_173464_(BakedModel model, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        if (this.customRenderedModel == null) {
            throw new IllegalStateException("General card model not bound");
        }
        return this.customRenderedModel;
    }
}

