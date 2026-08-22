package org.lzyzl.millager.entity.millager;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.providers.VanillaEnchantmentProviders;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.entity.ai.millager.RiderAvoidAllyGoal;
import org.lzyzl.millager.entity.ai.millager.RiderHorseHurtByTargetGoal;
import org.lzyzl.millager.entity.ai.millager.RiderRemountGoal;
import org.lzyzl.millager.entity.ai.millager.ScouterTootHornGoal;
import org.lzyzl.millager.entity.ai.vanilla.VanillaRangedCrossbowAttackGoal;
import org.lzyzl.millager.entity.golem.BeeGolem;
import org.lzyzl.millager.util.MiscHelper;

import java.util.List;

public class Scouter extends AbstractMillager implements CrossbowAttackMob, Rider {

    private static final EntityDataAccessor<Boolean> IS_CHARGING_CROSSBOW = SynchedEntityData.defineId(Scouter.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_TOOTING = SynchedEntityData.defineId(Scouter.class, EntityDataSerializers.BOOLEAN);

    private boolean hasTooted = false;
    private boolean pendingRaidToot = false;
    private int activityTicks = 0;

    public Scouter(EntityType<? extends AbstractMillager> entityType, Level level) {
        super(entityType, level);
        this.inventory = new SimpleContainer(5);
        this.getNavigation().setCanOpenDoors(false);
    }

    @Override
    public MillagerPose getMillagerPose() {
        if(this.isTooting()) return MillagerPose.TOOT_HORN;
        if(this.isChargingCrossbow()) return MillagerPose.CROSSBOW_CHARGE;
        if(this.getItemBySlot(EquipmentSlot.MAINHAND).is(Items.CROSSBOW)) return MillagerPose.CROSSBOW_HOLD;
        return MillagerPose.NEUTRAL;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractMillager.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.365F)
                .add(Attributes.ARMOR,8)
                .add(Attributes.ARMOR_TOUGHNESS, 2)
                .add(Attributes.FALL_DAMAGE_MULTIPLIER, 0.7D)
                .add(Attributes.MAX_HEALTH, 26.0)
                .add(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE, 0.5D)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new RiderRemountGoal<>(this, 1.2D));
        this.goalSelector.addGoal(1, new ScouterTootHornGoal(this));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Horse.class, 4.0F, 1.0D, 1.2D));
        this.goalSelector.addGoal(2, new VanillaRangedCrossbowAttackGoal<>(this, 1.5F, 15.0F));

        this.targetSelector.addGoal(1, new RiderAvoidAllyGoal<>(this,true));
        this.targetSelector.addGoal(1, new RiderHorseHurtByTargetGoal<>(this,AbstractMillager.class, BeeGolem.class, IronGolem.class));
        super.registerGoals();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        int activityTicks = this.getActivityTicks();
        if(activityTicks > 0) this.setActivityTicks(activityTicks - 1);
    }

    @Override
    public boolean hurtServer(@NonNull ServerLevel serverLevel, @NonNull DamageSource damageSource, float amount) {
        if(
                this.isPassenger() && damageSource.is(DamageTypes.IN_WALL)
                        && this.getVehicle() instanceof Horse horse && !horse.getInBlockState().isSolidRender()
        ) return false;
        return super.hurtServer(serverLevel, damageSource, amount);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NonNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_CHARGING_CROSSBOW, false);
        builder.define(IS_TOOTING, false);
    }

    @Override
    protected void addAdditionalSaveData(@NonNull ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        this.writeInventoryToTag(valueOutput);
        valueOutput.putBoolean("isTooting", this.isTooting());
        valueOutput.putBoolean("hasTooted", this.hasTooted());
        valueOutput.putBoolean("pendingRaidToot", this.isPendingRaidToot());
        valueOutput.putInt("activityTicks", this.getActivityTicks());
    }

    @Override
    protected void readAdditionalSaveData(@NonNull ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.readInventoryFromTag(valueInput);
        this.setTooting(valueInput.getBooleanOr("isTooting",false));
        this.setTooted(valueInput.getBooleanOr("hasTooted",false));
        this.setPendingRaidToot(valueInput.getBooleanOr("pendingRaidToot",false));
        this.setActivityTicks(valueInput.getIntOr("activityTicks",0));
    }

    @Override
    public boolean canUseNonMeleeWeapon(ItemStack itemStack) {
        return itemStack.getItem() == Items.CROSSBOW;
    }

    @Override
    public float getWalkTargetValue(@NonNull BlockPos blockPos, @NonNull LevelReader levelReader) {
        return 0.0F;
    }

    @Override
    public SpawnGroupData finalizeSpawn(@NonNull ServerLevelAccessor level, @NonNull DifficultyInstance difficulty, @NonNull EntitySpawnReason spawnReason, @Nullable SpawnGroupData spawnGroupData) {
        spawnGroupData = super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData);
        this.populateDefaultEquipmentSlots(level.getRandom(), difficulty);
        this.createMount(level, spawnReason, spawnGroupData);
        this.enchantSpawnedWeapon(level, level.getRandom(), difficulty);
        return spawnGroupData;
    }

    @Override
    protected void populateDefaultEquipmentSlots(@NonNull RandomSource random, @NonNull DifficultyInstance difficulty) {
        this.setItemSlot(EquipmentSlot.MAINHAND, Items.CROSSBOW.getDefaultInstance());
        this.setItemSlot(EquipmentSlot.OFFHAND, this.createHorn());
    }

    @Override
    protected void enchantSpawnedWeapon(@NonNull ServerLevelAccessor serverLevelAccessor, @NonNull RandomSource randomSource, @NonNull DifficultyInstance difficultyInstance) {
        super.enchantSpawnedWeapon(serverLevelAccessor, randomSource, difficultyInstance);
        if (randomSource.nextInt(400 - difficultyInstance.getDifficulty().getId() * 50) == 0) {
            ItemStack itemStack = this.getMainHandItem();
            if (itemStack.is(Items.CROSSBOW)) {
                EnchantmentHelper.enchantItemFromProvider(
                        itemStack, serverLevelAccessor.registryAccess(), VanillaEnchantmentProviders.MOB_SPAWN_EQUIPMENT, difficultyInstance, randomSource
                );
            }
        }
    }

    @Override
    public @NonNull ItemStack getProjectile(ItemStack itemStack) {
        if (itemStack.getItem() instanceof CrossbowItem) {
            return this.createFireworkRocket();
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public void performRangedAttack(@NonNull LivingEntity target, float velocity) {
        ItemStack crossBow = this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, Items.CROSSBOW));
        ItemStack ammo = this.getProjectile(crossBow);
        FireworkRocketEntity rocket = new FireworkRocketEntity(this.level(), ammo, this,
                this.getX(), this.getY() + this.getEyeHeight(), this.getZ(), true);

        double dist = this.distanceTo(target);
        double speed = 1.5;
        double predictionTicks = (dist / speed) + 2.0;

        Vec3 movement = target.getDeltaMovement();
        double predX = target.getX() + movement.x * predictionTicks;
        double predZ = target.getZ() + movement.z * predictionTicks;
        double predY = (!target.onGround() && Math.abs(movement.y) > 0.01)
                ? target.getY() + (movement.y * predictionTicks)
                : target.getY();

        double dx = predX - this.getX();
        double dz = predZ - this.getZ();
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);

        double dy = (predY + target.getBbHeight() * 0.3333333333333333) - rocket.getY();
        float gravityCompensation = 0.045F;

        if (this.level() instanceof ServerLevel serverLevel) {
            Projectile.spawnProjectileUsingShoot(
                    rocket,
                    serverLevel,
                    ammo,
                    dx,
                    dy + horizontalDist * gravityCompensation,
                    dz,
                    (float) speed,
                    (float) (14 - serverLevel.getDifficulty().getId() * 4)
            );
        }
        crossBow.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
        this.playSound(SoundEvents.FIREWORK_ROCKET_LAUNCH, 1.0F,
                1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
    }

    @Override
    protected boolean wantsItem(ItemStack itemStack) {
        return itemStack.is(Items.BRAIN_CORAL) || itemStack.is(Items.FEATHER);
    }

    public void faceTowardsTarget(LivingEntity target) {
        if (target == null || !target.isAlive() || target.isRemoved()) {
            this.getNavigation().stop();
            return;
        }
        this.getLookControl().setLookAt(target, 40.0F, 40.0F);

        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        float targetYaw = (float) (Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;

        this.setYRot(MiscHelper.rotateTowards(this.getYRot(), targetYaw, 30.0F));
        this.yBodyRot = this.getYRot();
        this.yHeadRot = this.getYRot();
    }

    @Override
    public void createMount(ServerLevelAccessor level, EntitySpawnReason spawnReason, SpawnGroupData spawnGroupData) {
        Horse horse = EntityType.HORSE.create(level.getLevel(), spawnReason);
        if (horse != null) {
            horse.setPos(this.getX(), this.getY(), this.getZ());
            horse.setYRot(this.getYRot());
            horse.setYBodyRot(this.getYRot());
            horse.setYHeadRot(this.getYRot());
            horse.finalizeSpawn(level, level.getCurrentDifficultyAt(horse.blockPosition()), spawnReason, spawnGroupData);
            double minSpeed = 0.25;// 正常范围大约是 0.1125 到 0.3375
            var speedAttribute = horse.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speedAttribute != null && speedAttribute.getBaseValue() < minSpeed) {
                speedAttribute.setBaseValue(minSpeed);
            }
            double minHealth = 22; // 正常范围大约是 15 到 30
            var healthAttribute = horse.getAttribute(Attributes.MAX_HEALTH);
            if (healthAttribute != null && healthAttribute.getBaseValue() < minHealth) {
                healthAttribute.setBaseValue(minHealth);
            }
            var explosionKnockbackAttribute = horse.getAttribute(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE);
            if (explosionKnockbackAttribute != null) {
                explosionKnockbackAttribute.setBaseValue(0.5);
            }
            horse.setHealth(horse.getMaxHealth());
            horse.setItemSlot(EquipmentSlot.BODY, new ItemStack(Rider.getRandomHorseArmor(level.getRandom(), 3)));
            horse.setTamed(true);
            horse.addTag("millager_mount");

            this.startRiding(horse);
            level.addFreshEntity(horse);
        }
    }

    private ItemStack createFireworkRocket() {
        ItemStack rocket = new ItemStack(Items.FIREWORK_ROCKET);
        FireworkExplosion major_explosion = new FireworkExplosion(
                FireworkExplosion.Shape.BURST,
                IntList.of(DyeColor.GRAY.getFireworkColor(), DyeColor.BLACK.getFireworkColor()),
                IntList.of(),
                false,
                false
        );
        FireworkExplosion sub_explosion = new FireworkExplosion(
                FireworkExplosion.Shape.BURST,
                IntList.of(DyeColor.GRAY.getFireworkColor(), DyeColor.BLACK.getFireworkColor()),
                IntList.of(),
                false,
                false
        );
        Fireworks fireworks = new Fireworks(
                1,
                List.of(major_explosion, sub_explosion, sub_explosion, sub_explosion, sub_explosion, sub_explosion, sub_explosion)
        );
        rocket.set(DataComponents.FIREWORKS, fireworks);
        return rocket;
    }

    private ItemStack createHorn() {
        return this.level().registryAccess().lookup(Registries.INSTRUMENT)
                .flatMap(registry -> registry.get(Instruments.FEEL_GOAT_HORN))
                .map(instrument -> InstrumentItem.create(Items.GOAT_HORN, instrument))
                .orElse(ItemStack.EMPTY);
    }

    public boolean hasTooted() {
        return this.hasTooted;
    }

    public void setTooted(boolean hasTooted) {
        this.hasTooted = hasTooted;
    }

    public boolean isTooting() {
        return this.entityData.get(IS_TOOTING);
    }

    public void setTooting(boolean tooting) {
        this.entityData.set(IS_TOOTING, tooting);
    }

    public int getActivityTicks() {
        return this.activityTicks;
    }

    public void setActivityTicks(int activityTicks) {
        this.activityTicks = activityTicks;
    }

    public boolean isChargingCrossbow() {
        return this.entityData.get(IS_CHARGING_CROSSBOW);
    }

    public void setChargingCrossbow(boolean bl) {
        this.entityData.set(IS_CHARGING_CROSSBOW, bl);
    }

    public void onCrossbowAttackPerformed() {
        this.noActionTime = 0;
    }

    public boolean isPendingRaidToot() {
        return this.pendingRaidToot;
    }

    public void setPendingRaidToot(boolean pending) {
        this.pendingRaidToot = pending;
    }
}
