package org.lzyzl.millager.mixin.client;

import com.google.common.collect.ImmutableMap;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.world.level.block.SkullBlock;
import org.lzyzl.millager.block.AbstractHeadBlock;
import org.lzyzl.millager.client.MillagerModelLayers;
import org.lzyzl.millager.client.render.block.HeadBlockRenderer;
import org.lzyzl.millager.client.render.block.VillagerHeadModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(SkullBlockRenderer.class)
public class SkullBlockRendererMixin {

    @Inject(method = "createSkullRenderers", at = @At("RETURN"), cancellable = true)
    private static void millager$onCreateSkullRenderers(EntityModelSet entityModelSet, CallbackInfoReturnable<Map<SkullBlock.Type, SkullModelBase>> cir) {
        cir.setReturnValue(ImmutableMap.<SkullBlock.Type, SkullModelBase>builder()
                .putAll(cir.getReturnValue())
                .put(AbstractHeadBlock.MillagerTypes.VILLAGER,
                        new VillagerHeadModel(entityModelSet.bakeLayer(MillagerModelLayers.VILLAGER_HEAD)))
                .put(AbstractHeadBlock.MillagerTypes.ILLAGER,
                        new VillagerHeadModel(entityModelSet.bakeLayer(MillagerModelLayers.ILLAGER_HEAD)))
                .build());
    }

    @Inject(method = "getRenderType", at = @At("HEAD"), cancellable = true)
    private static void millager$onGetRenderType(SkullBlock.Type type, GameProfile profile, CallbackInfoReturnable<RenderType> cir) {
        if (type instanceof AbstractHeadBlock.MillagerTypes) {
            cir.setReturnValue(HeadBlockRenderer.getSkullRenderType(type));
        }
    }
}
