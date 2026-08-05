package org.lzyzl.millager.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.lzyzl.millager.entity.ai.golem.IronGolemFollowDoctorGoal;
import org.lzyzl.millager.entity.golem.IronGolemAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IronGolem.class)
public abstract class IronGolemMixin extends AbstractGolem implements IronGolemAccessor {

    protected IronGolemMixin(EntityType<? extends AbstractGolem> entityType, Level level) {
        super(entityType, level);
    }

    @Unique
    private boolean millager$isDoctorCreated = false;

    @Override
    public boolean millager$isDoctorCreated() {
        return this.millager$isDoctorCreated;
    }

    @Override
    public void millager$setDoctorCreated(boolean isDoctorCreated) {
        this.millager$isDoctorCreated = isDoctorCreated;
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void addFollowDoctorGoal(CallbackInfo ci) {
        this.goalSelector.addGoal(4, new IronGolemFollowDoctorGoal((IronGolem) (Object) this,
                1.1D, 8.0F, 4.0F));
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void millager$addAdditionalSaveData(ValueOutput output, CallbackInfo ci) {
        output.putBoolean("millager$isDoctorCreated", this.millager$isDoctorCreated);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void millager$readAdditionalSaveData(ValueInput input, CallbackInfo ci) {
        this.millager$isDoctorCreated = input.getBooleanOr("millager$isDoctorCreated", false);
    }
}

