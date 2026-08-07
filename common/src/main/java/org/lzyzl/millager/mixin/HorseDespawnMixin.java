package org.lzyzl.millager.mixin;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.player.Player;
import org.lzyzl.millager.behavior.MiscConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractHorse.class)
public abstract class HorseDespawnMixin {

    @Unique
    private int millager$despawnTicks = 0;
    @Inject(method = "aiStep", at = @At("HEAD"))
    private void millager$tickFastDespawn(CallbackInfo ci) {
        if ((Object) this instanceof Horse horse) {
            if (horse.entityTags().contains("millager_fast_despawn")) {
                if (millager$hasLivingPassenger(horse)) {
                    horse.entityTags().remove("millager_fast_despawn");
                    millager$despawnTicks = 0;
                } else {
                    if (millager$despawnTicks <= 0 || millager$despawnTicks > MiscConfig.FAST_HORSE_DESPAWN_TICKS) {
                        millager$despawnTicks = MiscConfig.FAST_HORSE_DESPAWN_TICKS;
                    }
                    millager$despawnTicks--;
                    if (millager$despawnTicks <= 0) {
                        millager$despawnHorse(horse);
                    }
                }
            } else if (horse.entityTags().contains("millager_mount") && !millager$hasLivingPassenger(horse)) {
                if (millager$despawnTicks <= 0 || millager$despawnTicks > MiscConfig.MOUNT_HORSE_DESPAWN_TICKS) {
                    millager$despawnTicks = MiscConfig.MOUNT_HORSE_DESPAWN_TICKS;
                }
                millager$despawnTicks--;
                if (millager$despawnTicks <= 0) {
                    millager$despawnHorse(horse);
                }
            }
        }
    }

    @Unique
    private static boolean millager$hasLivingPassenger(Horse horse) {
        return horse.isVehicle() && horse.getPassengers().stream()
                .anyMatch(p -> p instanceof LivingEntity le && !le.isDeadOrDying());
    }

    @Unique
    private static void millager$despawnHorse(Horse horse) {
        if (horse.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.POOF,
                    horse.getX(), horse.getY() + 0.5, horse.getZ(),
                    20, 0.5, 0.5, 0.5, 0.02);
        }
        horse.discard();
    }

    @Inject(method = "mobInteract", at = @At("HEAD"))
    private void millager$cancelFastDespawnOnInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if ((Object) this instanceof Horse horse) {
            horse.entityTags().remove("millager_fast_despawn");
            millager$despawnTicks = 0;
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void millager$saveDespawnTicks(ValueOutput output, CallbackInfo ci) {
        if ((Object) this instanceof Horse) {
            output.putInt("millager_fast_despawn", millager$despawnTicks);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void millager$loadDespawnTicks(ValueInput input, CallbackInfo ci) {
        if ((Object) this instanceof Horse) {
            millager$despawnTicks = input.getIntOr("millager_fast_despawn", 0);
        }
    }
}
