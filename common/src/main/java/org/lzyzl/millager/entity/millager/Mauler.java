package org.lzyzl.millager.entity.millager;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.MillagerItems;
import org.lzyzl.millager.MillagerSounds;
import org.lzyzl.millager.entity.ai.millager.MaulerFixArmorGoal;
import org.lzyzl.millager.entity.ai.millager.MaulerMaceSmashGoal;

import java.util.Objects;

public class Mauler extends AbstractMillager {

    private static final EntityDataAccessor<Boolean> IS_FIXING = SynchedEntityData.defineId(Mauler.class, EntityDataSerializers.BOOLEAN);

    private int fixingTicks;
    private int fixCooldown;

    public Mauler(EntityType<? extends AbstractMillager> entityType, Level level) {
        super(entityType, level);
        this.inventory = new SimpleContainer(3);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(MillagerItems.IRON_MACE.get()));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractMillager.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.3F)
                .add(Attributes.ARMOR, 20)
                .add(Attributes.ARMOR_TOUGHNESS, 6)
                .add(Attributes.MAX_HEALTH, 32.0)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    public MillagerPose getMillagerPose() {
        if (this.isAggressive()) return MillagerPose.ATTACKING;
        if (this.isFixing()) return MillagerPose.FIXING;
        return MillagerPose.NEUTRAL;
    }

    @Override
    public boolean hurt(@NonNull DamageSource damageSource, float amount) {
        boolean hurt = super.hurt(damageSource, amount);
        if (hurt && Objects.requireNonNull(this.getAttribute(Attributes.ARMOR)).getBaseValue() > 10) {
            if (amount > 25 + 2 * this.level().getDifficulty().getId()) {
                Objects.requireNonNull(this.getAttribute(Attributes.ARMOR)).setBaseValue(10);
                this.playSound(SoundEvents.AMETHYST_CLUSTER_BREAK);
            } else if (amount >= 3.5 + this.level().getDifficulty().getId() * 0.5 && this.random.nextBoolean()) {
                Objects.requireNonNull(this.getAttribute(Attributes.ARMOR)).setBaseValue(Objects.requireNonNull(this.getAttribute(Attributes.ARMOR)).getBaseValue() - 1);
                this.playSound(SoundEvents.AMETHYST_BLOCK_BREAK);
            }
        }
        return hurt;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float damageMultiplier, @NonNull DamageSource damageSource) {
        return super.causeFallDamage(fallDistance, damageMultiplier * 0.5F, damageSource);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MaulerFixArmorGoal(this));
        this.goalSelector.addGoal(2, new MaulerMaceSmashGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2D, false));
        super.registerGoals();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.fixCooldown > 0) this.fixCooldown--;
        if (this.fixingTicks > 0) this.fixingTicks--;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_FIXING, false);
    }

    @Override
    public void addAdditionalSaveData(@NonNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        this.saveInventory(tag);
        tag.putBoolean("isFixing", this.isFixing());
        tag.putInt("fixingTicks", this.fixingTicks);
        tag.putInt("fixCooldown", this.fixCooldown);
    }

    @Override
    public void readAdditionalSaveData(@NonNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.loadInventory(tag);
        this.setFixing(tag.getBoolean("isFixing"));
        this.fixingTicks = tag.getInt("fixingTicks");
        this.fixCooldown = tag.getInt("fixCooldown");
    }

    @Override
    public SpawnGroupData finalizeSpawn(@NonNull ServerLevelAccessor level, @NonNull DifficultyInstance difficulty, @NonNull MobSpawnType spawnReason, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag tag) {
        spawnGroupData = super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData, tag);
        this.populateDefaultEquipmentSlots(level.getRandom(), difficulty);
        return spawnGroupData;
    }

    @Override
    protected void populateDefaultEquipmentSlots(@NonNull RandomSource random, @NonNull DifficultyInstance difficulty) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(MillagerItems.IRON_MACE.get()));
    }

    @Override
    public boolean doHurtTarget(@NonNull Entity entity) {
        boolean hurt = super.doHurtTarget(entity);
        if (hurt) this.playSound(MillagerSounds.MAULER_SMASH, 1.5F, 1.0F);
        return hurt;
    }

    @Override
    protected boolean wantsItem(ItemStack itemStack) {
        return itemStack.is(Items.AMETHYST_CLUSTER) || itemStack.is(ItemTags.LECTERN_BOOKS);
    }

    public Crackiness getCrackiness() {
        if (this.getArmorValue() <= 10.0F) return Crackiness.HIGH;
        if (this.getArmorValue() <= 14.0F) return Crackiness.MEDIUM;
        if (this.getArmorValue() <= 18.0F) return Crackiness.LOW;
        return Crackiness.NONE;
    }

    public boolean isFixing() {
        return this.entityData.get(IS_FIXING);
    }

    public void setFixing(boolean fixing) {
        this.entityData.set(IS_FIXING, fixing);
    }

    public int getFixingTicks() {
        return this.fixingTicks;
    }

    public void setFixingTicks(int fixingTicks) {
        this.fixingTicks = fixingTicks;
    }

    public int getFixCooldown() {
        return this.fixCooldown;
    }

    public void setFixCooldown(int fixCooldown) {
        this.fixCooldown = fixCooldown;
    }

    public enum Crackiness {
        NONE,
        LOW,
        MEDIUM,
        HIGH
    }
}
