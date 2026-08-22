package org.lzyzl.millager.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.MillagerEntityTypes;
import org.lzyzl.millager.MillagerItems;
import org.lzyzl.millager.entity.millager.Rioter;
import org.lzyzl.millager.util.MiscHelper;

public class MolotovCocktail extends ThrowableItemProjectile implements RioterProjectile {

    private static final int COLOR = 14589720;
    private static final float RADIUS = 5F;
    private boolean isRioterProjectile;

    public MolotovCocktail(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
        this.isRioterProjectile = this.getOwner() instanceof Rioter;
    }

    public MolotovCocktail(Level level, LivingEntity livingEntity, ItemStack itemStack) {
        super(MillagerEntityTypes.Cocktail_Projectile.get(), livingEntity, level);
        this.setItem(itemStack);
        this.isRioterProjectile = this.getOwner() instanceof Rioter;

    }

    public MolotovCocktail(Level level, double x, double y, double z, ItemStack itemStack) {
        super(MillagerEntityTypes.Cocktail_Projectile.get() , x, y, z, level);
        this.setItem(itemStack);
        this.isRioterProjectile = this.getOwner() instanceof Rioter;

    }

    @Override
    public void addAdditionalSaveData(@NonNull CompoundTag valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.putBoolean("isRioterProjectile", this.isRioterProjectile);
    }

    @Override
    public void readAdditionalSaveData(@NonNull CompoundTag valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.isRioterProjectile = valueInput.getBoolean("isRioterProjectile");
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
            MiscHelper.performFireExplosion(this, (ServerLevel)this.level(), this.position(), this.isRioterProjectile, RADIUS, COLOR, 2.0F, 0.6F);
            this.discard();
        }
        super.tick();
    }

    @Override
    protected void onHit(@NonNull HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level().isClientSide()) {
            MiscHelper.performFireExplosion(this, (ServerLevel)this.level(), this.position(), this.isRioterProjectile, RADIUS, COLOR, 2.0F, 0.6F);
            this.level().broadcastEntityEvent(this, (byte)3);
            this.discard();
        }
    }
}
