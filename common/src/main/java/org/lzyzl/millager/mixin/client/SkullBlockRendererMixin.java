package org.lzyzl.millager.mixin.client;

import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.SkullBlock;
import org.lzyzl.millager.block.AbstractHeadBlock;
import org.lzyzl.millager.client.MillagerModelLayers;
import org.lzyzl.millager.client.render.block.HeadBlockRenderer;
import org.lzyzl.millager.client.render.block.VillagerHeadModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SkullBlockRenderer.class)
public class SkullBlockRendererMixin {

    @Inject(method = "createModel", at = @At("HEAD"), cancellable = true)
    private static void millager$onCreateModel(EntityModelSet entityModelSet, SkullBlock.Type type, CallbackInfoReturnable<SkullModelBase> cir) {
        if (type instanceof AbstractHeadBlock.MillagerTypes millagerType) {
            if (millagerType == AbstractHeadBlock.MillagerTypes.VILLAGER) {
                cir.setReturnValue(new VillagerHeadModel(entityModelSet.bakeLayer(MillagerModelLayers.VILLAGER_HEAD)));
            } else if (millagerType == AbstractHeadBlock.MillagerTypes.ILLAGER) {
                cir.setReturnValue(new VillagerHeadModel(entityModelSet.bakeLayer(MillagerModelLayers.ILLAGER_HEAD)));
            }
        }
    }

    @Inject(method = "getSkullRenderType", at = @At("HEAD"), cancellable = true)
    private static void millager$onGetRenderType(SkullBlock.Type type, Identifier identifier, CallbackInfoReturnable<RenderType> cir) {
        if (type instanceof AbstractHeadBlock.MillagerTypes) {
            cir.setReturnValue(RenderTypes.entityCutoutNoCullZOffset(HeadBlockRenderer.SKIN_BY_TYPE.get(type)));
        }
    }
}