package org.lzyzl.millager.client.render.entity.golem;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.entity.golem.BeeGolem;
import org.lzyzl.millager.util.ResourceLocationHelper;

import static org.lzyzl.millager.Millager.MOD_ID;

public class BeeGolemFlashLayer extends RenderLayer<BeeGolem, BeeGolemModel> {

    private static final ResourceLocation FLASH_TEXTURE = ResourceLocationHelper.create(MOD_ID, "textures/entity/golem/bee_golem_flash.png");
    private final BeeGolemFlashModel flashModel;

    public BeeGolemFlashLayer(RenderLayerParent<BeeGolem, BeeGolemModel> parent, BeeGolemFlashModel flashModel) {
        super(parent);
        this.flashModel = flashModel;
    }

    @Override
    public void render(@NonNull PoseStack poseStack, @NonNull MultiBufferSource bufferSource, int packedLight, BeeGolem entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        int life = entity.getLifeTicks();
        float time = (float) entity.tickCount + partialTick;

        float danger = Mth.clamp((float) life / 2400.0F, 0.0F, 1.0F);
        float freq = Mth.lerp(danger, 0.08F, 0.6F);
        float s = (Mth.sin(time * freq) + 1.0F) / 2.0F;

        int r, g, b;
        if (entity.isSummoned()) {
            r = 0; g = 0; b = (int) (s * 255);
        } else if (entity.isAttacking()) {
            r = (int) (s * 255); g = 0; b = 0;
        } else {
            r = (int) (s * Mth.lerp(danger, 0.0F, 255.0F));
            g = (int) (s * 255.0F);
            b = 0;
        }
        int a = Math.max(10, (int) (s * 255));

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(FLASH_TEXTURE));
        this.flashModel.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        this.flashModel.renderToBuffer(poseStack, consumer, 0xF000F0, OverlayTexture.NO_OVERLAY,
                r / 255.0F, g / 255.0F, b / 255.0F, a / 255.0F);
    }
}
