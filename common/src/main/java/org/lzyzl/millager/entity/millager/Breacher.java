package org.lzyzl.millager.entity.millager;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lzyzl.millager.MillagerItems;
import org.lzyzl.millager.entity.ai.millager.BreacherAxeAttackGoal;
import org.lzyzl.millager.entity.ai.millager.BreacherChargeGoal;
import org.lzyzl.millager.entity.ai.millager.BreacherShieldBashGoal;
import org.lzyzl.millager.entity.ai.millager.RiderAvoidAllyGoal;
import org.lzyzl.millager.entity.ai.millager.RiderHorseHurtByTargetGoal;
import org.lzyzl.millager.entity.ai.millager.RiderRemountGoal;
import org.lzyzl.millager.entity.golem.BeeGolem;

public class Breacher extends AbstractMillager implements Rider {

    private static final EntityDataAccessor<Boolean> IS_CHARGING = SynchedEntityData.defineId(Breacher.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_BASHING = SynchedEntityData.defineId(Breacher.class, EntityDataSerializers.BOOLEAN);

    private int axeCooldown = 0;
    private int bashCooldown = 0;
    private int shieldCooldown = 0;

    public Breacher(EntityType<? extends Breacher> entityType, Level level) {
        super(entityType, level);
        this.inventory = new SimpleContainer(3);
        this.setCanOpenDoors(false);
    }

    @Override
    public MillagerPose getMillagerPose() {
        if (this.isBashing()) return MillagerPose.SHIELD_BASH;
        if (this.isCharging()) return MillagerPose.SHIELD;
        if (this.isAggressive()) return MillagerPose.ATTACKING;
        return this.getMainHandItem().isEmpty() && this.getOffhandItem().isEmpty() ? MillagerPose.NEUTRAL : MillagerPose.HOLDING_ITEM;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractMillager.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.3F)
                .add(Attributes.MAX_HEALTH, 34.0)
                .add(Attributes.ARMOR, 8)
                .add(Attributes.ARMOR_TOUGHNESS, 4)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new RiderRemountGoal<>(this, 1.2D));
        this.goalSelector.addGoal(1, new BreacherShieldBashGoal(this));
        this.goalSelector.addGoal(2, new BreacherChargeGoal(this));
        this.goalSelector.addGoal(3, new BreacherAxeAttackGoal(this));
        this.goalSelector.addGoal(4, new AvoidEntityGoal<>(this, Horse.class, 4.0F, 1.0D, 1.2D));

        this.targetSelector.addGoal(1, new RiderAvoidAllyGoal<>(this, true));
        this.targetSelector.addGoal(1, new RiderHorseHurtByTargetGoal<>(this, AbstractMillager.class, BeeGolem.class, IronGolem.class));
        super.registerGoals();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_CHARGING, false);
        this.entityData.define(IS_BASHING, false);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.axeCooldown > 0) this.axeCooldown--;
        if (this.bashCooldown > 0) this.bashCooldown--;
        if (this.shieldCooldown > 0) this.shieldCooldown--;
    }

    @Override
    public boolean hurt(@NonNull DamageSource damageSource, float amount) {
        if(
                this.isPassenger() && damageSource.is(DamageTypes.IN_WALL)
                        && this.getVehicle() instanceof Horse horse
                        && !horse.level().getBlockState(horse.blockPosition()).isSolidRender(horse.level(), horse.blockPosition())
        ) return false;
        if (!this.level().isClientSide() && amount > 0.0F && this.isDamageSourceBlocked(damageSource)) {
            this.hurtCurrentlyUsedShield(amount);
            if (damageSource.getDirectEntity() instanceof LivingEntity attacker) {
                this.blockUsingShield(attacker);
            }
            this.level().playSound(null, this, SoundEvents.SHIELD_BLOCK, this.getSoundSource(), 1.0F,
                    0.8F + this.getRandom().nextFloat() * 0.4F);
            return false;
        }
        return super.hurt(damageSource, amount);
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
        ItemStack axe = new ItemStack(random.nextFloat() < 0.33F ? Items.DIAMOND_AXE : Items.IRON_AXE);
        this.setItemSlot(EquipmentSlot.MAINHAND, axe);
        this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(MillagerItems.buckler.get()));
    }

    @Override
    protected void enchantSpawnedWeapon(@NonNull RandomSource randomSource, float specialMultiplier) {
        super.enchantSpawnedWeapon(randomSource, specialMultiplier);
        if (randomSource.nextInt(400 - this.level().getDifficulty().getId() * 50) == 0) {
            ItemStack itemStack = this.getMainHandItem();
            if (itemStack.is(Items.IRON_AXE) || itemStack.is(Items.DIAMOND_AXE)) {
                this.setItemSlot(EquipmentSlot.MAINHAND, EnchantmentHelper.enchantItem(
                        randomSource, itemStack, (int)(5.0F + specialMultiplier * randomSource.nextInt(18)), false));
            }
        }
    }

    @Override
    protected boolean wantsItem(ItemStack itemStack) {
        return itemStack.is(MillagerItems.rose.get());
    }

    @Override
    public void createMount(ServerLevelAccessor level, MobSpawnType spawnReason, SpawnGroupData spawnGroupData) {
        Horse horse = EntityType.HORSE.create(level.getLevel());
        if (horse != null) {
            horse.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
            horse.finalizeSpawn(level, level.getCurrentDifficultyAt(horse.blockPosition()), spawnReason, spawnGroupData, null);

            var speedAttr = horse.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speedAttr != null && speedAttr.getBaseValue() < 0.3) speedAttr.setBaseValue(0.3);

            var healthAttr = horse.getAttribute(Attributes.MAX_HEALTH);
            if (healthAttr != null && healthAttr.getBaseValue() < 25) healthAttr.setBaseValue(25);

            horse.setHealth(horse.getMaxHealth());
            horse.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Rider.getRandomHorseArmor(level.getRandom(), 1, 3)));
            horse.setTamed(true);
            horse.addTag("millager_mount");

            this.startRiding(horse);
            level.addFreshEntity(horse);
        }
    }

    @Override
    public void addAdditionalSaveData(@NonNull CompoundTag output) {
        super.addAdditionalSaveData(output);
        this.saveInventory(output);
        output.putBoolean("isCharging", this.isCharging());
        output.putBoolean("isBashing", this.isBashing());
        output.putInt("axeCooldown", this.axeCooldown);
        output.putInt("bashCooldown", this.bashCooldown);
        output.putInt("shieldCooldown", this.shieldCooldown);
    }

    @Override
    public void readAdditionalSaveData(@NonNull CompoundTag input) {
        super.readAdditionalSaveData(input);
        this.loadInventory(input);
        this.setCharging(input.getBoolean("isCharging"));
        this.setBashing(input.getBoolean("isBashing"));
        this.axeCooldown = input.getInt("axeCooldown");
        this.bashCooldown = input.getInt("bashCooldown");
        this.shieldCooldown = input.getInt("shieldCooldown");
    }

    public boolean isCharging() { return this.entityData.get(IS_CHARGING); }
    public void setCharging(boolean charging) { this.entityData.set(IS_CHARGING, charging); }
    public boolean isBashing() { return this.entityData.get(IS_BASHING); }
    public void setBashing(boolean bashing) { this.entityData.set(IS_BASHING, bashing); }
    public int getAxeCooldown() { return this.axeCooldown; }
    public void setAxeCooldown(int cooldown) { this.axeCooldown = cooldown; }
    public int getBashCooldown() { return this.bashCooldown; }
    public void setBashCooldown(int cooldown) { this.bashCooldown = cooldown; }
    public int getShieldCooldown() { return this.shieldCooldown; }
    public void setShieldCooldown(int cooldown) { this.shieldCooldown = cooldown; }
    public boolean canUseShield() { return this.shieldCooldown <= 0 && this.getOffhandItem().is(MillagerItems.buckler.get()); }

    @Override
    public double getMyRidingOffset() {
        return -0.45D;
    }
}
