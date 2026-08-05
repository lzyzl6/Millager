package org.lzyzl.millager.client.render.entity.millager;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Crackiness;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.client.MillagerModelLayers;
import org.lzyzl.millager.entity.millager.AbstractMillager;
import org.lzyzl.millager.entity.millager.Mauler;

import static org.lzyzl.millager.Millager.MOD_ID;

public class MaulerRenderer extends MillagerRenderer<Mauler,MaulerRenderState> {

    private static final Identifier M_LOCATION = Identifier.fromNamespaceAndPath(MOD_ID,"textures/entity/millager/mauler.png");
    private static final Identifier CRACKINESS_LOW_TEXTURE = Identifier.fromNamespaceAndPath(MOD_ID,"textures/entity/millager/mauler_cracked_low.png");
    private static final Identifier CRACKINESS_MEDIUM_TEXTURE = Identifier.fromNamespaceAndPath(MOD_ID,"textures/entity/millager/mauler_cracked_medium.png");
    private static final Identifier CRACKINESS_HIGH_TEXTURE = Identifier.fromNamespaceAndPath(MOD_ID,"textures/entity/millager/mauler_cracked_high.png");

    public MaulerRenderer(EntityRendererProvider.Context context) {
        super(context, new MillagerModel<>(context.bakeLayer(MillagerModelLayers.MAULER)), 0.5f);
    }

    @Override
    public @NonNull Identifier getTextureLocation(MaulerRenderState state) {
        if (state.isInvisible) return M_LOCATION;
        if (state.crackiness == Crackiness.Level.LOW) return CRACKINESS_LOW_TEXTURE;
        if (state.crackiness == Crackiness.Level.MEDIUM) return CRACKINESS_MEDIUM_TEXTURE;
        if (state.crackiness == Crackiness.Level.HIGH) return CRACKINESS_HIGH_TEXTURE;
        return M_LOCATION;
    }

    @Override
    public MaulerRenderState createRenderState() {
        return new MaulerRenderState();
    }

    @Override
    public void extractRenderState(Mauler mauler, MaulerRenderState state, float f) {
        super.extractRenderState(mauler, state, f);
        state.crackiness = mauler.getCrackiness();
        state.attackArm = state.mainArm;
        if(state.armPose == AbstractMillager.MillagerPose.NEUTRAL) {
            state.leftHandItemStack = ItemStack.EMPTY;
            state.rightHandItemStack = ItemStack.EMPTY;

            this.itemModelResolver.updateForLiving(
                    state.leftHandItemState,
                    ItemStack.EMPTY,
                    ItemDisplayContext.THIRD_PERSON_LEFT_HAND,
                    mauler
            );
            this.itemModelResolver.updateForLiving(
                    state.rightHandItemState,
                    ItemStack.EMPTY,
                    ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                    mauler
            );
        }
        if(state.armPose == AbstractMillager.MillagerPose.FIXING) {
            if(!state.isLeftHanded) {
                state.leftHandItemStack = Items.AMETHYST_BLOCK.getDefaultInstance();

                this.itemModelResolver.updateForLiving(
                        state.leftHandItemState,
                        state.leftHandItemStack,
                        ItemDisplayContext.THIRD_PERSON_LEFT_HAND,
                        mauler
                );
            } else {
                state.rightHandItemStack = Items.AMETHYST_BLOCK.getDefaultInstance();

                this.itemModelResolver.updateForLiving(
                        state.rightHandItemState,
                        state.rightHandItemStack,
                        ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                        mauler
                );
            }
        }
    }
}
