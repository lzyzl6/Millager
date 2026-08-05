package org.lzyzl.millager.entity.millager;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.providers.VanillaEnchantmentProviders;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.entity.ai.millager.MaulerFixArmorGoal;
import org.lzyzl.millager.entity.ai.millager.MaulerMaceSmashGoal;

import java.util.Objects;

public class Mauler extends AbstractMillager {

    private static final EntityDataAccessor<Boolean> IS_FIXING = SynchedEntityData.defineId(Mauler.class, EntityDataSerializers.BOOLEAN);

    private int fixingTicks = 0;
    private int fixCooldown = 0;

    public Mauler(EntityType<? extends AbstractMillager> entityType, Level level) {
        super(entityType, level);
        this.inventory = new SimpleContainer(3);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractMillager.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.3F)
                .add(Attributes.ARMOR,20)
                .add(Attributes.ARMOR_TOUGHNESS, 6)
                .add(Attributes.FALL_DAMAGE_MULTIPLIER, 0.5D)
                .add(Attributes.MAX_HEALTH, 32.0)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE, 0.5D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    public MillagerPose getMillagerPose() {
        if(this.isAggressive()) return MillagerPose.ATTACKING;
        else if(this.isFixing()) return MillagerPose.FIXING;
        return MillagerPose.NEUTRAL;
    }

    @Override
    public boolean hurtServer(@NonNull ServerLevel serverLevel, @NonNull DamageSource damageSource, float f) {
        boolean isHurt = super.hurtServer(serverLevel, damageSource, f);
        if(isHurt && Objects.requireNonNull(this.getAttribute(Attributes.ARMOR)).getBaseValue() > 10) {
            if(f > 25 + 2 * serverLevel.getDifficulty().getId()) {
                Objects.requireNonNull(this.getAttribute(Attributes.ARMOR)).setBaseValue(10);
                this.playSound(SoundEvents.AMETHYST_CLUSTER_BREAK);
            }
            else if(f >= 3.5 + serverLevel.getDifficulty().getId() * 0.5) {
                if(serverLevel.getRandom().nextBoolean()) {
                    Objects.requireNonNull(this.getAttribute(Attributes.ARMOR)).setBaseValue(Objects.requireNonNull(this.getAttribute(Attributes.ARMOR)).getBaseValue() - 1);
                    this.playSound(SoundEvents.AMETHYST_BLOCK_BREAK);
                }
            }
        }
        return isHurt;
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
        if(this.getFixCooldown() > 0) {
            this.setFixCooldown(this.getFixCooldown() - 1);
        }
        if(this.getFixingTicks() > 0) {
            this.setFixingTicks(this.getFixingTicks() - 1);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NonNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_FIXING, false);
    }

    @Override
    protected void addAdditionalSaveData(@NonNull ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        this.writeInventoryToTag(valueOutput);
        valueOutput.putBoolean("isFixing", this.isFixing());
        valueOutput.putInt("fixingTicks", this.getFixingTicks());
        valueOutput.putInt("fixCooldown", this.getFixCooldown());
    }

    @Override
    protected void readAdditionalSaveData(@NonNull ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.readInventoryFromTag(valueInput);
        this.setFixing(valueInput.getBooleanOr("isFixing",false));
        this.setFixingTicks(valueInput.getIntOr("fixingTicks",0));
        this.setFixCooldown(valueInput.getIntOr("fixCooldown",0));
    }

    @Override
    public SpawnGroupData finalizeSpawn(@NonNull ServerLevelAccessor level, @NonNull DifficultyInstance difficulty, @NonNull EntitySpawnReason spawnReason, @Nullable SpawnGroupData spawnGroupData) {
        spawnGroupData = super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData);
        this.populateDefaultEquipmentSlots(level.getRandom(), difficulty);
        this.enchantSpawnedWeapon(level, level.getRandom(), difficulty);
        return spawnGroupData;
    }

    @Override
    protected void populateDefaultEquipmentSlots(@NonNull RandomSource random, @NonNull DifficultyInstance difficulty) {
        this.setItemSlot(EquipmentSlot.MAINHAND, Items.MACE.getDefaultInstance());
    }

    @Override
    protected void enchantSpawnedWeapon(@NonNull ServerLevelAccessor serverLevelAccessor, @NonNull RandomSource randomSource, @NonNull DifficultyInstance difficultyInstance) {
        super.enchantSpawnedWeapon(serverLevelAccessor, randomSource, difficultyInstance);
        if (randomSource.nextInt(400 - difficultyInstance.getDifficulty().getId() * 50) == 0) {
            ItemStack itemStack = this.getMainHandItem();
            if (itemStack.is(Items.MACE)) {
                EnchantmentHelper.enchantItemFromProvider(
                        itemStack, serverLevelAccessor.registryAccess(), VanillaEnchantmentProviders.MOB_SPAWN_EQUIPMENT, difficultyInstance, randomSource
                );
            }
        }
    }

    @Override
    protected boolean wantsItem(ItemStack itemStack) {
        return itemStack.is(Items.AMETHYST_CLUSTER) || itemStack.is(ItemTags.LECTERN_BOOKS);
    }

    public Crackiness.Level getCrackiness() {
        if(this.getArmorValue() <= 10f) return Crackiness.Level.HIGH;
        else if(this.getArmorValue() <= 14f) return Crackiness.Level.MEDIUM;
        else if(this.getArmorValue() <= 18f) return Crackiness.Level.LOW;
        else return Crackiness.Level.NONE;
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
}