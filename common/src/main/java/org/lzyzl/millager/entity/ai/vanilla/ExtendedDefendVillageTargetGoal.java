package org.lzyzl.millager.entity.ai.vanilla;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.lzyzl.millager.entity.millager.AbstractMillager;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class ExtendedDefendVillageTargetGoal extends TargetGoal {
    private final TargetingConditions friendTargeting = TargetingConditions.forNonCombat().range(64.0D);
    private final TargetingConditions enemyTargeting = TargetingConditions.forCombat().range(64.0D);

    public ExtendedDefendVillageTargetGoal(AbstractMillager millager) {
        super(millager, false, true);
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        AABB aABB = this.mob.getBoundingBox().inflate(10.0D, 8.0D, 10.0D);
        ServerLevel serverLevel = (ServerLevel) this.mob.level();
        List<Villager> villagers = serverLevel.getNearbyEntities(Villager.class, this.friendTargeting, this.mob, aABB);

        if (villagers.isEmpty()) return false;

        for (Villager villager : villagers) {
            Optional<LivingEntity> attacker = villager.getBrain().getMemory(MemoryModuleType.HURT_BY_ENTITY);
            if (attacker.isPresent()) {
                LivingEntity target = attacker.get();
                if (target.isAlive() && this.mob.canAttack(target) && target.distanceToSqr(this.mob) < 256.0D) {
                    this.targetMob = target;
                    return true;
                }
            }
        }

        List<Player> players = serverLevel.getNearbyPlayers(this.enemyTargeting, this.mob, aABB);

        for (Player player : players) {
            if (player.isSpectator() || player.isCreative() || !player.isAlive()) continue;

            for (Villager villager : villagers) {
                if (villager.getPlayerReputation(player) <= -100) {
                    this.targetMob = player;
                    return true;
                }
            }
        }

        return false;
    }
}
