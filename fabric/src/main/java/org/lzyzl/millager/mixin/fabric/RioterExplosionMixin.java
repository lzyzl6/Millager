package org.lzyzl.millager.mixin.fabric;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Explosion;
import org.lzyzl.millager.entity.projectile.RioterProjectile;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

@Mixin(Explosion.class)
public class RioterExplosionMixin {

    @Shadow
    @Final
    private Entity source;

    @ModifyVariable(method = "explode", at = @At(value = "STORE"), ordinal = 0)
    private List<Entity> millager$filterExplosionTargets(List<Entity> entities) {
        if (this.source instanceof RioterProjectile rioterProjectile && rioterProjectile.isRioterProjectile()) {
            entities.removeIf(entity -> !(entity instanceof LivingEntity) || entity instanceof ArmorStand);
        }
        return entities;
    }
}
