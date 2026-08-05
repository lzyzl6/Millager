package org.lzyzl.millager.entity.ai.millager;

import net.minecraft.world.entity.Entity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.lzyzl.millager.entity.millager.AbstractMillager;
import org.lzyzl.millager.entity.millager.Rider;
import org.lzyzl.millager.util.MiscHelper;

public class RiderAvoidAllyGoal<R extends AbstractMillager & Rider> extends TargetGoal {

    private static final double COLLISION_MARGIN = 0.05D;
    private static final double SEPARATION_SPEED = 0.03D;
    private static final double MAX_SEPARATION_SPEED = 0.05D;
    private static final double VELOCITY_DAMPING = 0.35D;

    private final R rider;

    public RiderAvoidAllyGoal(R rider, boolean bl) {
        super(rider, bl, false);
        this.rider = rider;
    }

    @Override
    public boolean canUse() {
        return this.rider.shouldAvoidAllies() && getOverlappingAlly() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.rider.shouldAvoidAllies() && getOverlappingAlly() != null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        AbstractMillager ally = getOverlappingAlly();
        if (ally == null) return;

        Entity mover = this.rider.getRootVehicle();
        Vec3 direction = mover.position().subtract(ally.getRootVehicle().position()).multiply(1.0D, 0.0D, 1.0D);
        if (direction.horizontalDistanceSqr() < 1.0E-4D) {
            direction = new Vec3((this.rider.getId() & 1) == 0 ? 1.0D : -1.0D, 0.0D,
                    (ally.getId() & 1) == 0 ? 1.0D : -1.0D);
        }

        Vec3 velocity = mover.getDeltaMovement();
        Vec3 separation = direction.normalize().scale(SEPARATION_SPEED);
        mover.setDeltaMovement(
                Mth.clamp(velocity.x * VELOCITY_DAMPING + separation.x, -MAX_SEPARATION_SPEED, MAX_SEPARATION_SPEED),
                velocity.y,
                Mth.clamp(velocity.z * VELOCITY_DAMPING + separation.z, -MAX_SEPARATION_SPEED, MAX_SEPARATION_SPEED)
        );
    }

    private AbstractMillager getOverlappingAlly() {
        AABB ownBox = MiscHelper.getMillagerCollisionBox(this.rider);
        return this.rider.level().getEntitiesOfClass(
                AbstractMillager.class,
                ownBox.inflate(COLLISION_MARGIN),
                ally -> ally != this.rider && this.rider.isAlliedTo(ally)
        ).stream().filter(ally -> ownBox.inflate(COLLISION_MARGIN).intersects(MiscHelper.getMillagerCollisionBox(ally))).findFirst().orElse(null);
    }
}
