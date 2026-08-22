package org.lzyzl.millager.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.GuiEntityRenderer;
import net.minecraft.client.gui.render.state.pip.GuiEntityRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.CameraRenderState;
import org.lzyzl.millager.client.gui.screens.MillagerTargetConfigScreen;
import org.lzyzl.millager.client.gui.screens.PreviewEntityLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GuiEntityRenderer.class)
public abstract class GuiEntityRendererMixin {

    @WrapOperation(method = "renderToTexture(Lnet/minecraft/client/gui/render/state/pip/GuiEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lnet/minecraft/client/renderer/state/CameraRenderState;DDDLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V"))
    private void millager$submitPreviewSafely(EntityRenderDispatcher dispatcher, EntityRenderState renderState,
                                               CameraRenderState cameraState, double x, double y, double z,
                                               PoseStack poseStack, SubmitNodeCollector collector,
                                               Operation<Void> original) throws Throwable {
        boolean targetScreen = Minecraft.getInstance().screen instanceof MillagerTargetConfigScreen;
        if (targetScreen && PreviewEntityLevel.renderFailed(renderState.entityType)) return;
        try {
            original.call(dispatcher, renderState, cameraState, x, y, z, poseStack, collector);
        } catch (Throwable exception) {
            if (!targetScreen) throw exception;
            PreviewEntityLevel.failRender(renderState.entityType, exception);
        }
    }

    @WrapOperation(method = "renderToTexture(Lnet/minecraft/client/gui/render/state/pip/GuiEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher;renderAllFeatures()V"))
    private void millager$renderPreviewFeaturesSafely(FeatureRenderDispatcher dispatcher, Operation<Void> original,
                                                       GuiEntityRenderState state, PoseStack poseStack) throws Throwable {
        boolean targetScreen = Minecraft.getInstance().screen instanceof MillagerTargetConfigScreen;
        if (targetScreen && PreviewEntityLevel.renderFailed(state.renderState().entityType)) return;
        try {
            original.call(dispatcher);
        } catch (Throwable exception) {
            if (!targetScreen) throw exception;
            PreviewEntityLevel.failRender(state.renderState().entityType, exception);
        }
    }

}
