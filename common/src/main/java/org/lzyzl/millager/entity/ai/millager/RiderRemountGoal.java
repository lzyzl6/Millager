package org.lzyzl.millager.entity.ai.millager;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.item.ItemStack;
import org.lzyzl.millager.entity.millager.AbstractMillager;
import org.lzyzl.millager.entity.millager.Rider;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class RiderRemountGoal<T extends AbstractMillager & Rider> extends Goal {
    private final T rider;
    private final double speedModifier;
    private Horse targetHorse;

    public RiderRemountGoal(T rider, double speedModifier) {
        this.rider = rider;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.rider.isPassenger()) return false;
        if (this.rider.getTarget() != null) return false;

        List<Horse> nearbyHorses = this.rider.level().getEntitiesOfClass(
                Horse.class,
                this.rider.getBoundingBox().inflate(32.0D),
                horse -> !horse.isVehicle() && horse.isAlive()
        );

        if (nearbyHorses.isEmpty()) return false;
        Collections.shuffle(nearbyHorses);

        this.targetHorse = nearbyHorses.stream()
                .filter(Horse::isTamed)
                .findAny()
                .orElse(nearbyHorses.getFirst());

        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return !this.rider.isPassenger()
                && this.targetHorse != null
                && this.targetHorse.isAlive()
                && !this.targetHorse.isVehicle();
    }

    @Override
    public void start() {
        this.rider.getNavigation().moveTo(this.targetHorse, this.speedModifier);
    }

    @Override
    public void stop() {
        this.targetHorse = null;
        this.rider.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.targetHorse != null) {
            this.rider.getNavigation().moveTo(this.targetHorse, this.speedModifier);

            if (this.rider.distanceToSqr(this.targetHorse) < 4.0D) {
                if (!this.targetHorse.isTamed()) this.targetHorse.setTamed(true);

                if (!this.targetHorse.entityTags().contains("millager_mount")) {
                    double minSpeed = 0.25;
                    var speedAtt = this.targetHorse.getAttribute(Attributes.MOVEMENT_SPEED);
                    if (speedAtt != null && speedAtt.getBaseValue() < minSpeed)
                        speedAtt.setBaseValue(minSpeed);
                    double minHealth = 20;
                    var healthAtt = this.targetHorse.getAttribute(Attributes.MAX_HEALTH);
                    if (healthAtt != null && healthAtt.getBaseValue() < minHealth)
                        healthAtt.setBaseValue(minHealth);
                    var kbAtt = this.targetHorse.getAttribute(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE);
                    if (kbAtt != null) kbAtt.setBaseValue(0.5);
                    this.targetHorse.setHealth(this.targetHorse.getMaxHealth());
                    this.targetHorse.setItemSlot(EquipmentSlot.BODY,
                            new ItemStack(Rider.getRandomHorseArmor(this.rider.getRandom(), 3)));
                    this.targetHorse.addTag("millager_mount");
                }

                float yRot = this.rider.getYRot();
                this.targetHorse.setYRot(yRot);
                this.targetHorse.setYBodyRot(yRot);
                this.targetHorse.setYHeadRot(yRot);
                this.rider.setYBodyRot(yRot);
                this.rider.setYHeadRot(yRot);
                this.rider.startRiding(this.targetHorse);
            }
        }
    }
}
