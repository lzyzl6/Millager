package org.lzyzl.millager.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.warden.Warden;
import org.lzyzl.millager.entity.golem.BeeGolem;
import org.lzyzl.millager.entity.millager.AbstractMillager;

public class EnemyAttackHelper {

    public static void onEntityJoinLevel(Entity entity) {
        if (!(entity instanceof PathfinderMob mob)) return;
        if (!(mob instanceof Enemy)) return;

        if (isSpecial(mob)) return;

        if (!(mob instanceof Creeper) && !(mob instanceof NeutralMob)) {
            mob.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(mob, AbstractMillager.class, true));
        }

        if (isRangedEnemy(mob)) {
            mob.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(mob, BeeGolem.class, true));
        } else if (isMeleeEnemy(mob)) {
            mob.goalSelector.addGoal(2, new AvoidEntityGoal<>(mob, BeeGolem.class, 6.0F, 1.0, 1.1));
        }
    }

    private static boolean isSpecial(Mob mob) {
        return mob instanceof EnderDragon
                || mob instanceof WitherBoss
                || mob instanceof Warden
                || mob instanceof Ravager
                || mob instanceof Phantom
                || !mob.canChangeDimensions();
    }

    private static boolean isRangedEnemy(Mob mob) {
        return mob instanceof RangedAttackMob
                || mob instanceof Blaze
                || mob instanceof Guardian
                || mob instanceof Vex
                || mob instanceof Ghast;
    }

    private static boolean isMeleeEnemy(Mob mob) {
        return (mob instanceof Enemy && !isRangedEnemy(mob)) || mob instanceof Piglin;
    }
}
