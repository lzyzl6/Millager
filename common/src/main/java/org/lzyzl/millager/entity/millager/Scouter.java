package org.lzyzl.millager.entity.millager;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.InstrumentItem;
import net.minecraft.world.item.Instruments;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
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


public class Scouter extends AbstractMillager implements CrossbowAttackMob, Rider {

    private static final EntityDataAccessor<Boolean> IS_CHARGING_CROSSBOW = SynchedEntityData.defineId(Scouter.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_TOOTING = SynchedEntityData.defineId(Scouter.class, EntityDataSerializers.BOOLEAN);

    private boolean hasTooted = false;
    private boolean pendingRaidToot = false;
    private int activityTicks = 0;

    public Scouter(EntityType<? extends AbstractMillager> entityType, Level level) {
        super(entityType, level);
        this.inventory = new SimpleContainer(5);
        this.setCanOpenDoors(false);
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
                .add(Attributes.MAX_HEALTH, 26.0)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected int calculateFallDamage(float fallDistance, float damageMultiplier) {
        return super.calculateFallDamage(fallDistance, damageMultiplier * 0.7F);
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
    public boolean hurt(@NonNull DamageSource damageSource, float amount) {
        if(
                this.isPassenger() && damageSource.is(DamageTypes.IN_WALL)
                        && this.getVehicle() instanceof Horse horse
                        && !horse.level().getBlockState(horse.blockPosition()).isSolidRender(horse.level(), horse.blockPosition())
        ) return false;
        return super.hurt(damageSource, amount);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_CHARGING_CROSSBOW, false);
        this.entityData.define(IS_TOOTING, false);
    }

    @Override
    public void addAdditionalSaveData(@NonNull CompoundTag valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        this.saveInventory(valueOutput);
        valueOutput.putBoolean("isTooting", this.isTooting());
        valueOutput.putBoolean("hasTooted", this.hasTooted());
        valueOutput.putBoolean("pendingRaidToot", this.isPendingRaidToot());
        valueOutput.putInt("activityTicks", this.getActivityTicks());
    }

    @Override
    public void readAdditionalSaveData(@NonNull CompoundTag valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.loadInventory(valueInput);
        this.setTooting(valueInput.getBoolean("isTooting"));
        this.setTooted(valueInput.getBoolean("hasTooted"));
        this.setPendingRaidToot(valueInput.getBoolean("pendingRaidToot"));
        this.setActivityTicks(valueInput.getInt("activityTicks"));
    }

    @Override
    public float getWalkTargetValue(@NonNull BlockPos blockPos, @NonNull LevelReader levelReader) {
        return 0.0F;
    }

    @Override
    public SpawnGroupData finalizeSpawn(@NonNull ServerLevelAccessor level, @NonNull DifficultyInstance difficulty,
                                        @NonNull MobSpawnType spawnReason, @Nullable SpawnGroupData spawnGroupData,
                                        @Nullable CompoundTag tag) {
        spawnGroupData = super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData, tag);
        this.populateDefaultEquipmentSlots(level.getRandom(), difficulty);
        this.createMount(level, spawnReason, spawnGroupData);
        this.enchantSpawnedWeapon(level.getRandom(), difficulty.getSpecialMultiplier());
        return spawnGroupData;
    }

    @Override
    protected void populateDefaultEquipmentSlots(@NonNull RandomSource random, @NonNull DifficultyInstance difficulty) {
        this.setItemSlot(EquipmentSlot.MAINHAND, Items.CROSSBOW.getDefaultInstance());
        this.setItemSlot(EquipmentSlot.OFFHAND, this.createHorn());
    }

    @Override
    protected void enchantSpawnedWeapon(@NonNull RandomSource randomSource, float specialMultiplier) {
        super.enchantSpawnedWeapon(randomSource, specialMultiplier);
        if (randomSource.nextInt(400 - this.level().getDifficulty().getId() * 50) == 0) {
            ItemStack itemStack = this.getMainHandItem();
            if (itemStack.is(Items.CROSSBOW)) {
                this.setItemSlot(EquipmentSlot.MAINHAND, EnchantmentHelper.enchantItem(
                        randomSource, itemStack, (int)(5.0F + specialMultiplier * randomSource.nextInt(18)), false));
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

        Vec3 predictedTarget = MiscHelper.predictProjectileTarget(target, predictionTicks);

        double dx = predictedTarget.x - this.getX();
        double dz = predictedTarget.z - this.getZ();
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);

        double dy = (predictedTarget.y + target.getBbHeight() * 0.3333333333333333) - rocket.getY();
        float gravityCompensation = 0.045F;

        if (this.level() instanceof ServerLevel serverLevel) {
            rocket.shoot(dx, dy + horizontalDist * gravityCompensation, dz, (float) speed, (float) (14 - serverLevel.getDifficulty().getId() * 4));
            serverLevel.addFreshEntity(rocket);
        }
        CrossbowItem.setCharged(crossBow, false);
        CompoundTag crossbowTag = crossBow.getTag();
        if (crossbowTag != null) {
            crossbowTag.remove("ChargedProjectiles");
        }
        this.playSound(SoundEvents.FIREWORK_ROCKET_LAUNCH, 1.0F,
                1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
    }

    @Override
    public void shootCrossbowProjectile(@NonNull LivingEntity target, @NonNull ItemStack crossbow,
                                        @NonNull Projectile projectile, float angle) {
        this.shootCrossbowProjectile(this, target, projectile, angle, 1.6F);
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

    private void createMount(ServerLevelAccessor level, MobSpawnType spawnReason, SpawnGroupData spawnGroupData) {
        Rider.createMount(this, level, spawnReason, spawnGroupData, 0.25D, 22.0D, 0, 2);
    }

    private ItemStack createFireworkRocket() {
        ItemStack rocket = new ItemStack(Items.FIREWORK_ROCKET);
        CompoundTag fireworks = rocket.getOrCreateTagElement("Fireworks");
        fireworks.putByte("Flight", (byte)1);
        ListTag explosions = new ListTag();
        explosions.add(this.createFireworkExplosion());
        for (int i = 0; i < 6; i++) {
            explosions.add(this.createFireworkExplosion());
        }
        fireworks.put("Explosions", explosions);
        return rocket;
    }

    private CompoundTag createFireworkExplosion() {
        CompoundTag explosion = new CompoundTag();
        explosion.putByte("Type", (byte)FireworkRocketItem.Shape.BURST.getId());
        explosion.putIntArray("Colors", new int[]{DyeColor.GRAY.getFireworkColor(), DyeColor.BLACK.getFireworkColor()});
        explosion.putBoolean("Trail", false);
        explosion.putBoolean("Flicker", false);
        return explosion;
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

    @Override
    public double getMyRidingOffset() {
        return -0.45D;
    }
}
