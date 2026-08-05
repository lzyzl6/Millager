package org.lzyzl.millager.client.render.entity.millager;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.InstrumentItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.client.MillagerModelLayers;
import org.lzyzl.millager.entity.millager.AbstractMillager;
import org.lzyzl.millager.entity.millager.Scouter;

import static org.lzyzl.millager.Millager.MOD_ID;

public class ScouterRenderer extends MillagerRenderer<Scouter, ScouterRenderState> {

    private static final Identifier SC_LOCATION = Identifier.fromNamespaceAndPath(MOD_ID,"textures/entity/millager/scouter.png");

    public ScouterRenderer(EntityRendererProvider.Context context) {
        super(context, new MillagerModel<>(context.bakeLayer(MillagerModelLayers.SCOUTER)), 0.5f);
        this.addLayer(new ScouterHornHeldLayer<>(this));
    }

    @Override
    public @NonNull Identifier getTextureLocation(ScouterRenderState state) {
        return SC_LOCATION;
    }

    @Override
    public ScouterRenderState createRenderState() {
        return new ScouterRenderState();
    }

    @Override
    public void extractRenderState(Scouter scouter, ScouterRenderState state, float f) {
        super.extractRenderState(scouter, state, f);
        state.offhandStack = scouter.getOffhandItem();
        if(state.armPose != AbstractMillager.MillagerPose.TOOT_HORN) {
            if(state.offhandStack.getItem() instanceof InstrumentItem) {
                this.itemModelResolver.updateForTopItem(
                        state.hornRenderState,
                        state.offhandStack,
                        ItemDisplayContext.FIXED,
                        scouter.level(),
                        scouter,
                        scouter.getId()
                );
            }
            if(!state.isLeftHanded) {
                state.leftHandItemStack = ItemStack.EMPTY;

                this.itemModelResolver.updateForLiving(
                        state.leftHandItemState,
                        state.leftHandItemStack,
                        ItemDisplayContext.THIRD_PERSON_LEFT_HAND,
                        scouter
                );
            } else {
                state.rightHandItemStack = ItemStack.EMPTY;

                this.itemModelResolver.updateForLiving(
                        state.rightHandItemState,
                        state.rightHandItemStack,
                        ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                        scouter
                );
            }
        }
    }
}
