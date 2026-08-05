package org.lzyzl.millager.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.lzyzl.millager.MillagerItems;
import org.lzyzl.millager.util.ResourceLocationHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static org.lzyzl.millager.Millager.MOD_ID;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {

    @Unique
    private static final ModelResourceLocation TNT_ON_A_STICK_INVENTORY = new ModelResourceLocation(
            ResourceLocationHelper.create(MOD_ID, "tnt_on_a_stick"), "inventory");

    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true)
    private BakedModel millager$selectTntOnAStickModel(BakedModel model, ItemStack stack,
                                                       ItemDisplayContext displayContext, boolean leftHand,
                                                       PoseStack poseStack, MultiBufferSource bufferSource,
                                                       int packedLight, int packedOverlay) {
        if (!stack.is(MillagerItems.tntOnAStick.get())) return model;
        BakedModel inventoryModel = Minecraft.getInstance().getModelManager().getModel(TNT_ON_A_STICK_INVENTORY);
        if (displayContext == ItemDisplayContext.GUI || displayContext == ItemDisplayContext.GROUND
                || displayContext == ItemDisplayContext.FIXED) {
            return inventoryModel;
        }
        BakedModel handModel = inventoryModel.getOverrides().resolve(inventoryModel, stack,
                Minecraft.getInstance().level, Minecraft.getInstance().player, 0);
        if (displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
                || displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {
            return handModel;
        }
        return handModel != null ? handModel.getOverrides().resolve(handModel, stack,
                Minecraft.getInstance().level, Minecraft.getInstance().player, 0) : null;
    }
}
