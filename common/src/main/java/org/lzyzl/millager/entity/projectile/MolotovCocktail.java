package org.lzyzl.millager.entity.projectile;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.MillagerEntityTypes;
import org.lzyzl.millager.MillagerItems;
import org.lzyzl.millager.entity.millager.Rioter;
import org.lzyzl.millager.util.MiscHelper;

public class MolotovCocktail extends ThrowableItemProjectile implements RioterProjectile {

    final int color = 14589720;
    final float radius = 5f;
    private boolean isRioterProjectile;

    public MolotovCocktail(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
        this.isRioterProjectile = this.getOwner() instanceof Rioter;
    }

    public MolotovCocktail(Level level, LivingEntity livingEntity, ItemStack itemStack) {
        super(MillagerEntityTypes.Cocktail_Projectile.get(), livingEntity, level, itemStack);
        this.isRioterProjectile = this.getOwner() instanceof Rioter;

    }

    public MolotovCocktail(Level level, double x, double y, double z, ItemStack itemStack) {
        super(MillagerEntityTypes.Cocktail_Projectile.get() , x, y, z, level, itemStack);
        this.isRioterProjectile = this.getOwner() instanceof Rioter;

    }

    @Override
    protected void addAdditionalSaveData(@NonNull ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.putBoolean("isRioterProjectile", this.isRioterProjectile);
    }

    @Override
    protected void readAdditionalSaveData(@NonNull ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.isRioterProjectile = valueInput.getBooleanOr("isRioterProjectile", false);
    }

    @Override
    protected @NonNull Item getDefaultItem() {
        return MillagerItems.molotovCocktail.get();
    }

    @Override
    public boolean isRioterProjectile() {
        return this.isRioterProjectile;
    }

    @Override
    public void tick() {
        if(this.isOnFire() && !this.level().isClientSide()) {
            MiscHelper.performFireExplosion(this,(ServerLevel)this.level(), this.position(), this.isRioterProjectile, radius, color,2.0f,0.6f);
            this.discard();
        }
        super.tick();
    }

    @Override
    protected void onHit(@NonNull HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level().isClientSide()) {
            MiscHelper.performFireExplosion(this, (ServerLevel)this.level(), this.position(), this.isRioterProjectile, radius, color,2.0f,0.6f);
            this.level().broadcastEntityEvent(this, (byte)3);
            this.discard();
        }
    }
}
