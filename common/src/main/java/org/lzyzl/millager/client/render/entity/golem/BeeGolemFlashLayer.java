package org.lzyzl.millager.client.render.entity.golem;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import static org.lzyzl.millager.Millager.MOD_ID;

public class BeeGolemFlashLayer extends RenderLayer<BeeGolemRenderState, BeeGolemModel> {

    private static final Identifier FLASH_TEXTURE = Identifier.fromNamespaceAndPath(MOD_ID, "textures/entity/golem/bee_golem_flash.png");
    private final BeeGolemFlashModel flashModel;

    public BeeGolemFlashLayer(RenderLayerParent<BeeGolemRenderState, BeeGolemModel> parent, BeeGolemFlashModel flashModel) {
        super(parent);
        this.flashModel = flashModel;
    }

    @Override
    public void submit(@NonNull PoseStack poseStack, @NonNull SubmitNodeCollector collector, int light, BeeGolemRenderState state, float yaw, float pitch) {
        int alpha = (state.flashColor >> 24) & 0xFF;
        if (alpha <= 10) return;
        collector.order(1).submitModel(
                this.flashModel,
                state,
                poseStack,
                RenderTypes.entityCutoutNoCull(FLASH_TEXTURE),
                0xF000F0,
                LivingEntityRenderer.getOverlayCoords(state, 0.0F),
                state.flashColor,
                null,
                state.outlineColor,
                null
        );
    }


}
