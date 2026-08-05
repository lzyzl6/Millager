package org.lzyzl.millager.entity.ai.millager;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.lzyzl.millager.MillagerItems;
import org.lzyzl.millager.entity.millager.Rioter;

import java.util.EnumSet;

public class RioterThrowGoal extends Goal {

    private final Rioter rioter;

    private Item itemToThrow;

    public RioterThrowGoal(Rioter rioter) {
        this.rioter = rioter;
        this.setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if(this.rioter.isThrowing()) return true;
        if(this.rioter.isTaunting()) return false;
        if(this.rioter.getThrowCooldown() > 0) return false;
        return this.rioter.getTarget() != null && this.rioter.getTarget().isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return this.rioter.getTarget() != null && this.rioter.getTarget().isAlive() && this.rioter.getActivityTicks() > 0;
    }

    @Override
    public void start() {
        this.rioter.setAggressive(true);
        this.rioter.setThrowing(true);
        this.rioter.setActivityTicks(10 + this.rioter.getRandom().nextIntBetweenInclusive(6,14));
        this.itemToThrow = this.shouldThrowFire(this.rioter) ? MillagerItems.molotovCocktail.get() : MillagerItems.tntOnAStick.get();
        this.rioter.setItemSlot(EquipmentSlot.MAINHAND, this.itemToThrow.getDefaultInstance());
    }

    @Override
    public void tick() {
        this.rioter.updateTargetingAndDistance();
    }

    @Override
    public void stop() {
        LivingEntity livingEntity  = this.rioter.getTarget();
        if(livingEntity != null && livingEntity.isAlive()) this.rioter.performRangedAttack(livingEntity, 1.0F);
        this.rioter.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        this.rioter.setAggressive(false);
        this.rioter.setThrowing(false);
        if(this.rioter.getActivityTicks() <= 0) this.rioter.setThrowCooldown(this.itemToThrow == MillagerItems.tntOnAStick.get() ? 60 : 40 + this.rioter.getRandom().nextInt(50));
        this.rioter.setActivityTicks(0);
    }

    private boolean shouldThrowFire(Rioter rioter) {
        LivingEntity target = rioter.getTarget();
        if(target == null || !target.isAlive()) return false;
        if(target.isInWater() || target.fireImmune() || target.isOnFire()) return false;
        if(rioter.level() instanceof ServerLevel serverLevel) {
            if(serverLevel.isRainingAt(target.blockPosition())) return false;
            if(target.distanceToSqr(rioter) < 16) return serverLevel.getRandom().nextInt(3) != 0;
            if(rioter.getHealth() < rioter.getMaxHealth() * 0.25) return serverLevel.getRandom().nextBoolean();
            return serverLevel.getRandom().nextInt(5) == 0;
        }
        return false;
    }
}

