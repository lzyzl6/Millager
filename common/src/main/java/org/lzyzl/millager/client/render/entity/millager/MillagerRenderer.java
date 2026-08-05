package org.lzyzl.millager.client.render.entity.millager;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.world.item.CrossbowItem;
import org.lzyzl.millager.entity.millager.AbstractMillager;

public abstract class MillagerRenderer<T extends AbstractMillager, S extends MillagerRenderState> extends HumanoidMobRenderer<T, S, MillagerModel<S>> {

    public MillagerRenderer(EntityRendererProvider.Context context, MillagerModel<S> entityModel, float f) {
        super(context, entityModel, f);
        this.addLayer(new ItemInHandLayer<>(this));
    }

    public void extractRenderState(T abstractMillager, S millagerRenderState, float f) {
        super.extractRenderState(abstractMillager, millagerRenderState, f);
        ArmedEntityRenderState.extractArmedEntityRenderState(abstractMillager, millagerRenderState, this.itemModelResolver, f);
        millagerRenderState.isRiding = abstractMillager.isPassenger();
        millagerRenderState.armPose = abstractMillager.getMillagerPose();
        millagerRenderState.isLeftHanded = abstractMillager.isLeftHanded();

        millagerRenderState.maxCrossbowChargeDuration = millagerRenderState.armPose == AbstractMillager.MillagerPose.CROSSBOW_CHARGE ? CrossbowItem.getChargeDuration(abstractMillager.getUseItem(), abstractMillager) : 0;
    }

}
