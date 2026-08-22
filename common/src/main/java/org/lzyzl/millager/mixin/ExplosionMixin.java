package org.lzyzl.millager.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;
import org.lzyzl.millager.entity.millager.AbstractMillager;
import org.lzyzl.millager.entity.millager.Rioter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Explosion.class)
public abstract class ExplosionMixin {

    @Redirect(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"))
    private void millager$applyExplosionKnockbackResistance(Entity entity, Vec3 movement) {
        double resistance = entity instanceof Rioter ? 0.8D
                : entity instanceof AbstractMillager || entity instanceof Horse horse && horse.getTags().contains("millager_mount") ? 0.5D : 0.0D;
        Vec3 currentMovement = entity.getDeltaMovement();
        entity.setDeltaMovement(currentMovement.add(movement.subtract(currentMovement).scale(1.0D - resistance)));
    }
}
