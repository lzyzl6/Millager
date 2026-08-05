package org.lzyzl.millager.client.render.entity.millager;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.client.MillagerModelLayers;
import org.lzyzl.millager.entity.millager.AbstractMillager;
import org.lzyzl.millager.entity.millager.Lancer;

import static org.lzyzl.millager.Millager.MOD_ID;

public class LancerRenderer extends MillagerRenderer<Lancer, LancerRenderState> {

    private static final Identifier L_LOCATION = Identifier.fromNamespaceAndPath(MOD_ID,"textures/entity/millager/lancer.png");

    public LancerRenderer(EntityRendererProvider.Context context) {
        super(context, new MillagerModel<>(context.bakeLayer(MillagerModelLayers.LANCER)), 0.5f);
    }

    @Override
    public @NonNull Identifier getTextureLocation(LancerRenderState livingEntityRenderState) {
        return L_LOCATION;
    }

    @Override
    public LancerRenderState createRenderState() {
        return new LancerRenderState();
    }

    @Override
    public void extractRenderState(Lancer lancer, LancerRenderState state, float f) {
        super.extractRenderState(lancer, state, f);
        if(state.armPose == AbstractMillager.MillagerPose.SPELLCASTING) {
            state.leftHandItemStack = ItemStack.EMPTY;
            state.rightHandItemStack = ItemStack.EMPTY;

            this.itemModelResolver.updateForLiving(
                    state.leftHandItemState,
                    ItemStack.EMPTY,
                    ItemDisplayContext.THIRD_PERSON_LEFT_HAND,
                    lancer
            );
            this.itemModelResolver.updateForLiving(
                    state.rightHandItemState,
                    ItemStack.EMPTY,
                    ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                    lancer
            );

        }
    }
}
