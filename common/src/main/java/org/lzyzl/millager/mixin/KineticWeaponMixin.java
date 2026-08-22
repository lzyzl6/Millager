package org.lzyzl.millager.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.component.KineticWeapon;
import org.lzyzl.millager.entity.millager.Lancer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.function.Predicate;

@Mixin(KineticWeapon.class)
public class KineticWeaponMixin {

    @ModifyArgs(method = "damageEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getHitEntitiesAlong(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/component/AttackRange;Ljava/util/function/Predicate;Lnet/minecraft/world/level/ClipContext$Block;)Lcom/mojang/datafixers/util/Either;"))
    private void millager$excludeLancerAllies(Args args) {
        if (!(args.<Entity>get(0) instanceof Lancer lancer)) return;
        Predicate<Entity> predicate = args.get(2);
        args.set(2, predicate.and(entity -> {
            if (lancer.isAlliedTo(entity)) return false;
            Entity controllingPassenger = entity.getControllingPassenger();
            return controllingPassenger == null || !lancer.isAlliedTo(controllingPassenger);
        }));
    }
}
