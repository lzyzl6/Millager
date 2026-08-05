package org.lzyzl.millager.client.render.entity.millager;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.client.MillagerModelLayers;
import org.lzyzl.millager.entity.millager.AbstractMillager;
import org.lzyzl.millager.entity.millager.Rioter;

import static org.lzyzl.millager.Millager.MOD_ID;

public class RioterRenderer extends MillagerRenderer<Rioter,RioterRenderState>{

    private static final Identifier R_LOCATION = Identifier.fromNamespaceAndPath(MOD_ID,"textures/entity/millager/rioter.png");

    public RioterRenderer(EntityRendererProvider.Context context) {
        super(context, new MillagerModel<>(context.bakeLayer(MillagerModelLayers.RIOTER)), 0.5f);
        this.addLayer(new RioterShieldBackLayer<>(this));
    }

    @Override
    public @NonNull Identifier getTextureLocation(RioterRenderState livingEntityRenderState) {
        return R_LOCATION;
    }

    @Override
    public RioterRenderState createRenderState() {
        return new RioterRenderState();
    }

    @Override
    public void extractRenderState(Rioter rioter, RioterRenderState state, float f) {
        super.extractRenderState(rioter, state, f);
        state.offhandStack = rioter.getOffhandItem();
        if(state.armPose == AbstractMillager.MillagerPose.NEUTRAL) {

            if(state.offhandStack.getItem() instanceof ShieldItem) {
                this.itemModelResolver.updateForTopItem(
                        state.shieldRenderState,
                        state.offhandStack,
                        ItemDisplayContext.FIXED,
                        rioter.level(),
                        rioter,
                        rioter.getId()
                );
            } else {
                state.shieldRenderState.clear();
            }

            state.leftHandItemStack = ItemStack.EMPTY;
            state.rightHandItemStack = ItemStack.EMPTY;

            this.itemModelResolver.updateForLiving(
                    state.leftHandItemState,
                    ItemStack.EMPTY,
                    ItemDisplayContext.THIRD_PERSON_LEFT_HAND,
                    rioter
            );
            this.itemModelResolver.updateForLiving(
                    state.rightHandItemState,
                    ItemStack.EMPTY,
                    ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                    rioter
            );
        }
    }
}
