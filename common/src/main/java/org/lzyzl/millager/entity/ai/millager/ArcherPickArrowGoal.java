package org.lzyzl.millager.entity.ai.millager;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lzyzl.millager.MillagerSounds;
import org.lzyzl.millager.entity.millager.Archer;

import java.util.EnumSet;
import java.util.List;

public class ArcherPickArrowGoal extends Goal {
    private final Archer archer;
    private ItemEntity targetArrow;
    private LivingEntity previousTarget = null;
    private int scanCooldown;

    public ArcherPickArrowGoal(Archer archer) {
        this.archer = archer;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if(this.archer.isCrafting()) return false;
        if (!this.archer.getItemBySlot(EquipmentSlot.OFFHAND).isEmpty()) return false;
        if (scanCooldown-- > 0) return false;

        this.scanCooldown = 10 + this.archer.getRandom().nextInt(10);

        List<ItemEntity> list = this.archer.level().getEntitiesOfClass(
                ItemEntity.class,
                this.archer.getBoundingBox().inflate(8.0D, 1.0D, 8.0D),
                item -> {
                    ItemStack stack = item.getItem();
                    return stack.getItem() instanceof ArrowItem && !stack.is(Items.ARROW) && item.isAlive();
                });

        if (list.isEmpty()) return false;

        this.targetArrow = list.getFirst();
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.targetArrow != null && this.targetArrow.isAlive()
                && this.archer.getItemBySlot(EquipmentSlot.OFFHAND).isEmpty()
                && this.archer.getInventory().canAddItem(this.targetArrow.getItem());
    }

    @Override
    public void start() {
        this.previousTarget = this.archer.getTarget();
        this.archer.setTarget(null);

        this.archer.level().playSound(null, this.archer.getX(), this.archer.getY(), this.archer.getZ(),
                MillagerSounds.ARCHER_DRINKING_POTION, SoundSource.NEUTRAL, 1.0F, 1.0F);
        this.archer.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,30, 0, false, false));
        this.archer.getNavigation().moveTo(this.targetArrow, 1.2D);
    }

    @Override
    public void stop() {
        this.targetArrow = null;

        this.archer.removeEffect(MobEffects.INVISIBILITY);

        if (this.previousTarget != null && this.previousTarget.isAlive()) {
            this.archer.setTarget(this.previousTarget);
        }
        this.previousTarget = null;
    }

    @Override
    public void tick() {
        if (this.targetArrow == null || !this.targetArrow.isAlive()) return;

        if (this.archer.tickCount % 10 == 0) {
            this.archer.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40, 0, false, false));
        }

        // 盯着箭看
        this.archer.getLookControl().setLookAt(this.targetArrow, 30.0F, 30.0F);

        if (this.archer.getNavigation().isStuck() || this.archer.getNavigation().isDone()) {
            this.archer.getNavigation().moveTo(this.targetArrow, 1.2D);
        }

        if (this.archer.distanceToSqr(this.targetArrow) < 2.25D) {
            this.pickUp();
        }
    }

    // 优化后的 pickUp 逻辑
    private void pickUp() {
        if (this.targetArrow == null || !this.targetArrow.isAlive()) return;

        if (this.archer.level() instanceof ServerLevel serverLevel) {
            this.archer.pickUpItem(serverLevel, this.targetArrow);
        }
        this.targetArrow = null;
    }
}