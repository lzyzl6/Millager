package org.lzyzl.millager.client.render.entity.millager;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.client.MillagerModelLayers;
import org.lzyzl.millager.entity.millager.AbstractMillager;
import org.lzyzl.millager.entity.millager.Swordmaster;

import static org.lzyzl.millager.Millager.MOD_ID;

public class SwordmasterRenderer extends MillagerRenderer<Swordmaster,SwordmasterRenderState>{

    private static final Identifier S_LOCATION = Identifier.fromNamespaceAndPath(MOD_ID,"textures/entity/millager/swordmaster.png");

    public SwordmasterRenderer(EntityRendererProvider.Context context) {
        super(context, new MillagerModel<>(context.bakeLayer(MillagerModelLayers.SWORDMASTER)), 0.5f);
    }

    @Override
    public @NonNull Identifier getTextureLocation(SwordmasterRenderState livingEntityRenderState) {
        return S_LOCATION;
    }

    @Override
    public SwordmasterRenderState createRenderState() {
        return new SwordmasterRenderState();
    }

    @Override
    public void extractRenderState(Swordmaster swordmaster, SwordmasterRenderState state, float f) {
        super.extractRenderState(swordmaster, state, f);
        if(state.armPose == AbstractMillager.MillagerPose.REGAINING_SWORD) {
            if(state.isLeftHanded) {
                state.leftHandItemStack = ItemStack.EMPTY;
                this.itemModelResolver.updateForLiving(
                        state.leftHandItemState,
                        ItemStack.EMPTY,
                        ItemDisplayContext.THIRD_PERSON_LEFT_HAND,
                        swordmaster
                );
            } else {
                state.rightHandItemStack = ItemStack.EMPTY;
                this.itemModelResolver.updateForLiving(
                        state.rightHandItemState,
                        ItemStack.EMPTY,
                        ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                        swordmaster
                );
            }

        } else if(state.armPose == AbstractMillager.MillagerPose.NEUTRAL) {
            state.leftHandItemStack = ItemStack.EMPTY;
            state.rightHandItemStack = ItemStack.EMPTY;

            this.itemModelResolver.updateForLiving(
                    state.leftHandItemState,
                    ItemStack.EMPTY,
                    ItemDisplayContext.THIRD_PERSON_LEFT_HAND,
                    swordmaster
            );
            this.itemModelResolver.updateForLiving(
                    state.rightHandItemState,
                    ItemStack.EMPTY,
                    ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                    swordmaster
            );

        }
    }
}
