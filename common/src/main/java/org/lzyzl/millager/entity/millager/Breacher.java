package org.lzyzl.millager.entity.millager;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.providers.VanillaEnchantmentProviders;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
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

    private int axeCooldown;
    private int bashCooldown;
    private int shieldCooldown;

    public Breacher(EntityType<? extends Breacher> entityType, Level level) {
        super(entityType, level);
        this.inventory = new SimpleContainer(3);
        this.getNavigation().setCanOpenDoors(false);
    }

    @Override
    public MillagerPose getMillagerPose() {
        if (this.isBashing()) return MillagerPose.BASHING;
        if (this.isCharging()) return MillagerPose.SHIELD;
        if (this.isAggressive()) return MillagerPose.ATTACKING;
        return this.getMainHandItem().isEmpty() && this.getOffhandItem().isEmpty() ? MillagerPose.NEUTRAL : MillagerPose.HOLDING_ITEM;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractMillager.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.3F).add(Attributes.MAX_HEALTH, 34.0).add(Attributes.ARMOR, 8).add(Attributes.ARMOR_TOUGHNESS, 4).add(Attributes.ATTACK_DAMAGE, 4.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.3D).add(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE, 0.5D).add(Attributes.FOLLOW_RANGE, 32.0);
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
    protected void defineSynchedData(SynchedEntityData.@NonNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_CHARGING, false);
        builder.define(IS_BASHING, false);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.axeCooldown > 0) this.axeCooldown--;
        if (this.bashCooldown > 0) this.bashCooldown--;
        if (this.shieldCooldown > 0) this.shieldCooldown--;
    }

    @Override
    public boolean hurtServer(@NonNull ServerLevel serverLevel, @NonNull DamageSource damageSource, float amount) {
        if (this.isPassenger() && damageSource.is(DamageTypes.IN_WALL)
                && this.getVehicle() instanceof Horse horse && !horse.getInBlockState().isSolidRender()) {
            return false;
        }
        ItemStack blockingWith = this.getItemBlockingWith();
        if (blockingWith != null && blockingWith.is(MillagerItems.buckler.asItem()) && !this.isLowBucklerHit(damageSource)) {
            BlocksAttacks blocksAttacks = blockingWith.get(DataComponents.BLOCKS_ATTACKS);
            if (blocksAttacks != null && !blocksAttacks.bypassedBy().map(damageSource::is).orElse(false)
                    && !(damageSource.getDirectEntity() instanceof AbstractArrow arrow && arrow.getPierceLevel() > 0)) {
                Vec3 sourcePos = damageSource.getSourcePosition();
                double angle = sourcePos == null ? Math.PI : Math.acos(new Vec3(sourcePos.x - this.getX(), 0.0D, sourcePos.z - this.getZ()).normalize().dot(this.calculateViewVector(0.0F, this.getYHeadRot())));
                if (amount - blocksAttacks.resolveBlockedDamage(damageSource, amount, angle) <= 0.0F) {
                    blocksAttacks.onBlocked(serverLevel, this);
                    if (amount > 7.0F + serverLevel.getDifficulty().getId() || this.damageSourceUsesAxe(damageSource)) {
                        this.shieldCooldown = 60;
                        blocksAttacks.disable(serverLevel, this, 3.0F, blockingWith);
                    }
                    return false;
                }
            }
        }
        boolean wasBlocking = this.isUsingItem() && this.getUseItem().is(MillagerItems.buckler.asItem());
        boolean hurt = super.hurtServer(serverLevel, damageSource, amount);
        if (wasBlocking && !this.isUsingItem()) this.shieldCooldown = 60;
        return hurt;
    }

    private boolean isLowBucklerHit(DamageSource damageSource) {
        return damageSource.getDirectEntity() instanceof Projectile projectile
                && projectile.getY() < this.getY() + this.getBbHeight() / 3.0D;
    }

    private boolean damageSourceUsesAxe(DamageSource damageSource) {
        ItemStack weapon = damageSource.getWeaponItem();
        return weapon != null && weapon.is(ItemTags.AXES);
    }

    @Override
    protected void addAdditionalSaveData(@NonNull ValueOutput output) {
        super.addAdditionalSaveData(output);
        this.writeInventoryToTag(output);
        output.putBoolean("isCharging", this.isCharging());
        output.putBoolean("isBashing", this.isBashing());
        output.putInt("axeCooldown", this.axeCooldown);
        output.putInt("bashCooldown", this.bashCooldown);
        output.putInt("shieldCooldown", this.shieldCooldown);
    }

    @Override
    protected void readAdditionalSaveData(@NonNull ValueInput input) {
        super.readAdditionalSaveData(input);
        this.readInventoryFromTag(input);
        this.setCharging(input.getBooleanOr("isCharging", false));
        this.setBashing(input.getBooleanOr("isBashing", false));
        this.axeCooldown = input.getIntOr("axeCooldown", 0);
        this.bashCooldown = input.getIntOr("bashCooldown", 0);
        this.shieldCooldown = input.getIntOr("shieldCooldown", 0);
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
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(random.nextFloat() < 0.33F ? Items.DIAMOND_AXE : Items.IRON_AXE));
        this.setItemSlot(EquipmentSlot.OFFHAND, MillagerItems.buckler.get().getDefaultInstance());
    }

    @Override
    protected void enchantSpawnedWeapon(@NonNull ServerLevelAccessor level, @NonNull RandomSource random, @NonNull DifficultyInstance difficulty) {
        super.enchantSpawnedWeapon(level, random, difficulty);
        if (random.nextInt(400 - difficulty.getDifficulty().getId() * 50) == 0) {
            EnchantmentHelper.enchantItemFromProvider(this.getMainHandItem(), level.registryAccess(), VanillaEnchantmentProviders.MOB_SPAWN_EQUIPMENT, difficulty, random);
        }
    }

    @Override
    protected boolean wantsItem(ItemStack itemStack) {
        return itemStack.is(MillagerItems.rose.asItem());
    }

    @Override
    public void createMount(ServerLevelAccessor level, EntitySpawnReason spawnReason, SpawnGroupData spawnGroupData) {
        Horse horse = EntityType.HORSE.create(level.getLevel(), spawnReason);
        if (horse == null) return;
        horse.setPos(this.getX(), this.getY(), this.getZ());
        horse.finalizeSpawn(level, level.getCurrentDifficultyAt(horse.blockPosition()), spawnReason, spawnGroupData);
        var speed = horse.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null && speed.getBaseValue() < 0.3D) speed.setBaseValue(0.3D);
        var health = horse.getAttribute(Attributes.MAX_HEALTH);
        if (health != null && health.getBaseValue() < 25.0D) health.setBaseValue(25.0D);
        var explosionKnockback = horse.getAttribute(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE);
        if (explosionKnockback != null) explosionKnockback.setBaseValue(0.5D);
        horse.setHealth(horse.getMaxHealth());
        horse.setItemSlot(EquipmentSlot.BODY, new ItemStack(Rider.getRandomHorseArmor(level.getRandom(), 1, 4)));
        horse.setTamed(true);
        horse.addTag("millager_mount");
        this.startRiding(horse);
        level.addFreshEntity(horse);
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
    public boolean canUseShield() { return this.shieldCooldown <= 0 && this.getOffhandItem().is(MillagerItems.buckler.asItem()); }
}
