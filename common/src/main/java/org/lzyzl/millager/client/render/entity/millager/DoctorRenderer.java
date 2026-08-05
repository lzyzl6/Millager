package org.lzyzl.millager.client.render.entity.millager;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.client.MillagerModelLayers;
import org.lzyzl.millager.entity.millager.AbstractMillager;
import org.lzyzl.millager.entity.millager.Doctor;

import static org.lzyzl.millager.Millager.MOD_ID;

public class DoctorRenderer extends MillagerRenderer<Doctor, DoctorRenderState> {

    private static final Identifier D_LOCATION = Identifier.fromNamespaceAndPath(MOD_ID,"textures/entity/millager/doctor.png");

    public DoctorRenderer(EntityRendererProvider.Context context) {
        super(context, new MillagerModel<>(context.bakeLayer(MillagerModelLayers.DOCTOR)), 0.5f);
    }

    @Override
    public @NonNull Identifier getTextureLocation(DoctorRenderState livingEntityRenderState) {
        return D_LOCATION;
    }

    @Override
    public DoctorRenderState createRenderState() {
        return new DoctorRenderState();
    }

    @Override
    public void extractRenderState(Doctor doctor, DoctorRenderState state, float f) {
        super.extractRenderState(doctor, state, f);
        if(state.armPose == AbstractMillager.MillagerPose.SWINGING_ARM || state.armPose == AbstractMillager.MillagerPose.HOLDING_ITEM) {

            state.leftHandItemStack = state.isLeftHanded ? new ItemStack(Items.IRON_BLOCK) : new ItemStack(Items.CARVED_PUMPKIN);
            state.rightHandItemStack = state.isLeftHanded ? new ItemStack(Items.CARVED_PUMPKIN) : new ItemStack(Items.IRON_BLOCK);

            this.itemModelResolver.updateForLiving(
                    state.leftHandItemState,
                    state.leftHandItemStack,
                    ItemDisplayContext.THIRD_PERSON_LEFT_HAND,
                    doctor
            );
            this.itemModelResolver.updateForLiving(
                    state.rightHandItemState,
                    state.rightHandItemStack,
                    ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                    doctor
            );
        }
    }
}
