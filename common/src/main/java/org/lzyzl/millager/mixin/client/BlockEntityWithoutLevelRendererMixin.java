package org.lzyzl.millager.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.lzyzl.millager.MillagerItems;
import org.lzyzl.millager.client.MillagerModelLayers;
import org.lzyzl.millager.client.render.entity.projectile.TNTOnAStickModel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityWithoutLevelRenderer.class)
public class BlockEntityWithoutLevelRendererMixin {

    @Shadow @Final private EntityModelSet entityModelSet;

    @Unique
    private TNTOnAStickModel millager$tntOnAStickModel;

    @Inject(method = "onResourceManagerReload", at = @At("TAIL"))
    private void millager$reloadTntOnAStickModel(ResourceManager resourceManager, CallbackInfo ci) {
        this.millager$tntOnAStickModel = new TNTOnAStickModel(this.entityModelSet.bakeLayer(MillagerModelLayers.TNT_ON_A_STICK));
    }

    @Inject(method = "renderByItem", at = @At("HEAD"), cancellable = true)
    private void millager$renderTntOnAStick(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                                            MultiBufferSource bufferSource, int packedLight, int packedOverlay,
                                            CallbackInfo ci) {
        if (!stack.is(MillagerItems.tntOnAStick.get())) return;
        poseStack.pushPose();
        poseStack.scale(0.4F, -0.4F, -0.4F);
        VertexConsumer vertexConsumer = ItemRenderer.getFoilBufferDirect(bufferSource,
                RenderType.entityCutout(ResourceLocation.fromNamespaceAndPath("millager", "textures/projectile/tnt_on_a_stick_cude.png")), false, stack.hasFoil());
        this.millager$tntOnAStickModel.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, -1);
        poseStack.popPose();
        ci.cancel();
    }
}
