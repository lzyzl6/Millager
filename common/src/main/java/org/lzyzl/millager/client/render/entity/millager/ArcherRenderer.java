package org.lzyzl.millager.client.render.entity.millager;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.client.MillagerModelLayers;
import org.lzyzl.millager.entity.millager.AbstractMillager;
import org.lzyzl.millager.entity.millager.Archer;

import static org.lzyzl.millager.Millager.MOD_ID;

public class ArcherRenderer extends MillagerRenderer<Archer, ArcherRenderState> {

    private static final Identifier A_LOCATION = Identifier.fromNamespaceAndPath(MOD_ID,"textures/entity/millager/archer.png");

    public ArcherRenderer(EntityRendererProvider.Context context) {
        super(context, new MillagerModel<>(context.bakeLayer(MillagerModelLayers.ARCHER)), 0.5f);
    }

    @Override
    public @NonNull Identifier getTextureLocation(ArcherRenderState livingEntityRenderState) {
        return A_LOCATION;
    }

    @Override
    public ArcherRenderState createRenderState() {
        return new ArcherRenderState();
    }

    @Override
    public void extractRenderState(Archer archer, ArcherRenderState state, float f) {
        super.extractRenderState(archer, state, f);
        if(state.armPose == AbstractMillager.MillagerPose.NEUTRAL) {
            if(state.isLeftHanded) {
                state.rightHandItemStack = ItemStack.EMPTY;
                this.itemModelResolver.updateForLiving(
                        state.rightHandItemState,
                        state.rightHandItemStack,
                        ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                        archer
                );
            } else {
                state.leftHandItemStack = ItemStack.EMPTY;
                this.itemModelResolver.updateForLiving(
                        state.leftHandItemState,
                        state.leftHandItemStack,
                        ItemDisplayContext.THIRD_PERSON_LEFT_HAND,
                        archer
                );
            }
        }
        if(state.armPose == AbstractMillager.MillagerPose.CRAFTING_ARROW) {
            if(state.isLeftHanded) {
                state.leftHandItemStack = ItemStack.EMPTY;
                this.itemModelResolver.updateForLiving(
                        state.leftHandItemState,
                        ItemStack.EMPTY,
                        ItemDisplayContext.THIRD_PERSON_LEFT_HAND,
                        archer
                );

                state.rightHandItemStack = Items.ARROW.getDefaultInstance();
                this.itemModelResolver.updateForLiving(
                        state.rightHandItemState,
                        state.rightHandItemStack,
                        ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                        archer
                );
            } else {
                state.rightHandItemStack = ItemStack.EMPTY;
                this.itemModelResolver.updateForLiving(
                        state.rightHandItemState,
                        ItemStack.EMPTY,
                        ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                        archer
                );

                state.leftHandItemStack = Items.ARROW.getDefaultInstance();
                this.itemModelResolver.updateForLiving(
                        state.leftHandItemState,
                        state.leftHandItemStack,
                        ItemDisplayContext.THIRD_PERSON_LEFT_HAND,
                        archer
                );
            }

        }
    }
}
