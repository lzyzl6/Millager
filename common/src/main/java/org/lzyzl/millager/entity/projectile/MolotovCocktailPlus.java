package org.lzyzl.millager.entity.projectile;

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
import org.lzyzl.millager.util.MiscHelper;

public class MolotovCocktailPlus extends ThrowableItemProjectile {

    private static final int COLOR = 14589720;
    private static final float RADIUS = 9F;

    public MolotovCocktailPlus(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public MolotovCocktailPlus(Level level, LivingEntity livingEntity, ItemStack itemStack) {
        super(MillagerEntityTypes.Cocktail_Plus_Projectile.get(), livingEntity, level);
        this.setItem(itemStack);
    }

    public MolotovCocktailPlus(Level level, double x, double y, double z, ItemStack itemStack) {
        super(MillagerEntityTypes.Cocktail_Plus_Projectile.get() , x, y, z, level);
        this.setItem(itemStack);
    }

    @Override
    protected @NonNull Item getDefaultItem() {
        return MillagerItems.molotovCocktailPlus.get();
    }

    @Override
    public void tick() {
        if(this.isOnFire() && !this.level().isClientSide()) {
            MiscHelper.performFireExplosion(this, (ServerLevel)this.level(), this.position(), RADIUS, COLOR, 4.0F, 0.8F);
            this.discard();
        }
        super.tick();
    }

    @Override
    protected void onHit(@NonNull HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level().isClientSide()) {
            MiscHelper.performFireExplosion(this, (ServerLevel)this.level(), this.position(), RADIUS, COLOR, 4.0F, 0.8F);
            this.level().broadcastEntityEvent(this, (byte)3);
            this.discard();
        }
    }
}
