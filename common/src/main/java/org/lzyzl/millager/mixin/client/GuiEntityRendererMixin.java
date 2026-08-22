package org.lzyzl.millager.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.GuiEntityRenderer;
import net.minecraft.client.renderer.state.gui.pip.GuiEntityRenderState;
import org.lzyzl.millager.client.gui.screens.MillagerTargetConfigScreen;
import org.lzyzl.millager.client.gui.screens.PreviewEntityLevel;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GuiEntityRenderer.class)
public abstract class GuiEntityRendererMixin {

    @WrapMethod(method = "renderToTexture(Lnet/minecraft/client/renderer/state/gui/pip/GuiEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V")
    private void millager$renderPreviewSafely(GuiEntityRenderState state, PoseStack poseStack, Operation<Void> original) throws Throwable {
        boolean targetScreen = Minecraft.getInstance().screen instanceof MillagerTargetConfigScreen;
        if (targetScreen && PreviewEntityLevel.renderFailed(state.renderState().entityType)) return;
        try {
            original.call(state, poseStack);
        } catch (Throwable exception) {
            if (!targetScreen) throw exception;
            PreviewEntityLevel.failRender(state.renderState().entityType, exception);
        }
    }

}
