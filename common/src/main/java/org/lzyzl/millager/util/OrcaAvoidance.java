package org.lzyzl.millager.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.lzyzl.millager.entity.millager.AbstractMillager;

import java.util.Comparator;
import java.util.List;

public final class OrcaAvoidance {

    private static final double TIME_HORIZON = 20.0D;
    private static final double SAFETY_MARGIN = 0.1D;
    private static final double MAX_CORRECTION = 0.035D;

    private OrcaAvoidance() {
    }

    public static void apply(AbstractMillager mob, List<AbstractMillager> squad) {
        if (!mob.getNavigation().isInProgress()) return;

        Entity mover = mob.getRootVehicle();
        Vec3 velocity = mover.getDeltaMovement();
        double preferredSpeed = velocity.horizontalDistance();
        if (preferredSpeed < 0.01D) return;

        AABB ownBox = MiscHelper.getMillagerCollisionBox(mob);
        double ownRadius = getRadius(ownBox);
        List<AbstractMillager> neighbors = squad.stream()
                .filter(neighbor -> neighbor != mob && neighbor.isAlive())
                .sorted(Comparator.comparing(AbstractMillager::getUUID))
                .toList();

        Vec3 totalCorrection = Vec3.ZERO;
        for (AbstractMillager neighbor : neighbors) {
            Entity otherMover = neighbor.getRootVehicle();
            if (otherMover == mover) continue;
            AABB otherBox = MiscHelper.getMillagerCollisionBox(neighbor);
            double combinedRadius = ownRadius + getRadius(otherBox) + SAFETY_MARGIN;
            Vec3 relativePosition = otherMover.position().subtract(mover.position()).multiply(1.0D, 0.0D, 1.0D);
            Vec3 relativeVelocity = otherMover.getDeltaMovement().subtract(velocity).multiply(1.0D, 0.0D, 1.0D);
            Vec3 predictedPosition = relativePosition.add(relativeVelocity.scale(TIME_HORIZON));
            double predictedDistance = predictedPosition.horizontalDistance();
            if (predictedDistance >= combinedRadius) continue;

            Vec3 separationDirection = predictedDistance > 1.0E-4D
                    ? predictedPosition.normalize().scale(-1.0D)
                    : getFallbackDirection(mob, neighbor);
            double correction = Math.min(MAX_CORRECTION,
                    (combinedRadius - predictedDistance) / TIME_HORIZON * 0.5D);
            totalCorrection = totalCorrection.add(separationDirection.scale(correction));
        }

        double correctionLength = totalCorrection.horizontalDistance();
        if (correctionLength > MAX_CORRECTION) {
            totalCorrection = totalCorrection.normalize().scale(MAX_CORRECTION);
        }
        Vec3 adjustedVelocity = velocity.add(totalCorrection);
        double adjustedSpeed = adjustedVelocity.horizontalDistance();
        double maxSpeed = preferredSpeed + MAX_CORRECTION;
        if (adjustedSpeed > maxSpeed) {
            Vec3 normalized = adjustedVelocity.normalize().scale(maxSpeed);
            adjustedVelocity = new Vec3(normalized.x, velocity.y, normalized.z);
        }
        mover.setDeltaMovement(adjustedVelocity.x, velocity.y, adjustedVelocity.z);
    }

    private static double getRadius(AABB box) {
        return Math.max(box.getXsize(), box.getZsize()) * 0.5D;
    }

    private static Vec3 getFallbackDirection(AbstractMillager mob, AbstractMillager neighbor) {
        return new Vec3((mob.getId() & 1) == 0 ? -1.0D : 1.0D, 0.0D,
                (neighbor.getId() & 1) == 0 ? -1.0D : 1.0D).normalize();
    }
}
