package org.lzyzl.millager.entity.millager;
import net.minecraft.nbt.CompoundTag;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BannerPatternTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.MillagerItems;
import org.lzyzl.millager.entity.ai.millager.RioterShieldGoal;
import org.lzyzl.millager.entity.ai.millager.RioterTauntGoal;
import org.lzyzl.millager.entity.ai.millager.RioterThrowGoal;
import org.lzyzl.millager.entity.projectile.MolotovCocktail;
import org.lzyzl.millager.entity.projectile.TNTOnAStick;
import org.lzyzl.millager.util.MiscHelper;
import org.lzyzl.millager.util.ResourceVariantHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.lzyzl.millager.util.MiscHelper.isAllyCaused;

public class Rioter extends AbstractMillager implements RangedAttackMob {

    private static final EntityDataAccessor<Boolean> IS_TAUNTING = SynchedEntityData.defineId(Rioter.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_THROWING = SynchedEntityData.defineId(Rioter.class, EntityDataSerializers.BOOLEAN);

    private int strafeTick = 0;

    private int activityTicks = 0;
    private int throwCooldown = 0;
    private int tauntCooldown = 0;
    private int disableCooldown = 0;

    private static final List<DyeColor> SHIELD_COLORS = Arrays.stream(DyeColor.values())
            .filter(color -> color != DyeColor.YELLOW)
            .toList();

    public Rioter(EntityType<? extends AbstractMillager> entityType, Level level) {
        super(entityType, level);
        this.inventory = new SimpleContainer(7);
    }

    @Override
    public MillagerPose getMillagerPose() {
        if(this.isTaunting()) return MillagerPose.TAUNTING;
        if(this.isThrowing()) return MillagerPose.THROWING;
        if(this.isAggressive()) return MillagerPose.SHIELD;
        return MillagerPose.NEUTRAL;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractMillager.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.35F)
                .add(Attributes.ARMOR,5)
                .add(Attributes.ARMOR_TOUGHNESS, 12)
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected int calculateFallDamage(float fallDistance, float damageMultiplier) {
        return super.calculateFallDamage(fallDistance, damageMultiplier * 0.7F);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new RioterTauntGoal(this));
        this.goalSelector.addGoal(1, new RioterThrowGoal(this));
        this.goalSelector.addGoal(2, new RioterShieldGoal(this));
        super.registerGoals();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        int activityTicks = this.getActivityTicks();
        int tauntCooldown = this.getTauntCooldown();
        int throwCooldown = this.getThrowCooldown();
        int disableCooldown = this.getDisableCooldown();
        if (tauntCooldown > 0) this.setTauntCooldown(tauntCooldown - 1);
        if (activityTicks > 0) this.setActivityTicks(activityTicks - 1);
        if (throwCooldown > 0) this.setThrowCooldown(throwCooldown - 1);
        if (disableCooldown > 0) this.setDisableCooldown(disableCooldown - 1);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_TAUNTING, false);
        this.entityData.define(IS_THROWING, false);
    }

    @Override
    public void addAdditionalSaveData(@NonNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        this.saveInventory(tag);
        tag.putBoolean("isTaunting", this.isTaunting());
        tag.putBoolean("isThrowing", this.isThrowing());
        tag.putInt("activityTicks", this.getActivityTicks());
        tag.putInt("throwCooldown", this.getThrowCooldown());
        tag.putInt("tauntCooldown", this.getTauntCooldown());
        tag.putInt("disableCooldown", this.getDisableCooldown());
    }

    @Override
    public void readAdditionalSaveData(@NonNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.loadInventory(tag);
        this.setTaunting(tag.getBoolean("isTaunting"));
        this.setThrowing(tag.getBoolean("isThrowing"));
        this.setActivityTicks(tag.getInt("activityTicks"));
        this.setThrowCooldown(tag.getInt("throwCooldown"));
        this.setTauntCooldown(tag.getInt("tauntCooldown"));
        this.setDisableCooldown(tag.getInt("disableCooldown"));
    }

    @Override
    public SpawnGroupData finalizeSpawn(@NonNull ServerLevelAccessor level, @NonNull DifficultyInstance difficulty,
                                        @NonNull MobSpawnType spawnReason, @Nullable SpawnGroupData spawnGroupData,
                                        @Nullable CompoundTag tag) {
        spawnGroupData = super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData, tag);
        this.setItemSlot(EquipmentSlot.OFFHAND, getRandomDecoratedShield(random, level.registryAccess()));
        this.enchantSpawnedWeapon(level.getRandom(), difficulty.getSpecialMultiplier());
        return spawnGroupData;
    }

    @Override
    protected void populateDefaultEquipmentSlots(@NonNull RandomSource random, @NonNull DifficultyInstance difficulty) {
    }

    @Override
    protected void enchantSpawnedWeapon(@NonNull RandomSource randomSource, float specialMultiplier) {
        super.enchantSpawnedWeapon(randomSource, specialMultiplier);
        if (randomSource.nextInt(400 - this.level().getDifficulty().getId() * 50) == 0) {
            ItemStack itemStack = this.getItemBySlot(EquipmentSlot.OFFHAND);
            if (itemStack.is(Items.SHIELD)) {
                this.setItemSlot(EquipmentSlot.OFFHAND, EnchantmentHelper.enchantItem(
                        randomSource, itemStack, (int)(5.0F + specialMultiplier * randomSource.nextInt(18)), false));
            }
        }
    }

    @Override
    public void performRangedAttack(@NonNull LivingEntity target, float f) {
        ItemStack itemStack = this.getItemBySlot(EquipmentSlot.MAINHAND);

        if (this.level() instanceof ServerLevel serverLevel) {

            double speed = 0.75;
            double dist = this.distanceTo(target);

            double predictionTicks = (dist / speed) + 1.5;

            Vec3 predictedTarget = MiscHelper.predictProjectileTarget(target, predictionTicks);

            double dx = predictedTarget.x - this.getX();
            double dz = predictedTarget.z - this.getZ();
            double horizontalDist = Math.sqrt(dx * dx + dz * dz);

            double dy = (predictedTarget.y + target.getBbHeight() * 0.3333) - (this.getY() + 1.5);

            float gravityCompensation = 0.15F;

            float inaccuracy = (float)(14 - serverLevel.getDifficulty().getId() * 3);

            Projectile projectile;
            if (itemStack.is(MillagerItems.tntOnAStick.asItem())) {
                projectile = new TNTOnAStick(serverLevel, this, itemStack);
            } else {
                projectile = new MolotovCocktail(serverLevel, this, itemStack);
            }
            projectile.shoot(dx, dy + horizontalDist * gravityCompensation, dz, (float)speed, inaccuracy);
            serverLevel.addFreshEntity(projectile);
        }
        if (!this.isSilent()) {
            this.level().playSound(null,
                    this.getX(), this.getY(), this.getZ(),
                    itemStack.is(MillagerItems.tntOnAStick.asItem()) ? SoundEvents.TNT_PRIMED : SoundEvents.SPLASH_POTION_THROW,
                    this.getSoundSource(), 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
        }
    }

    @Override
    public boolean hurt(@NonNull DamageSource damageSource, float f) {
        if(isAllyCaused(damageSource)) return false;
        if (!this.level().isClientSide() && f > 0.0F && this.isDamageSourceBlocked(damageSource)) {
            this.hurtCurrentlyUsedShield(f);
            if (damageSource.getDirectEntity() instanceof LivingEntity attacker) {
                this.blockUsingShield(attacker);
            }
            this.level().playSound(null, this, SoundEvents.SHIELD_BLOCK, this.getSoundSource(), 1.0F,
                    0.8F + this.getRandom().nextFloat() * 0.4F);
            if (f > 8.0F + this.level().getDifficulty().getId()) {
                this.setDisableCooldown(Math.min(200, (int)(80 + f * 10 - this.level().getDifficulty().getId() * 20)));
                this.stopUsingItem();
            }
            return false;
        }
        return super.hurt(damageSource, f);
    }

    @Override
    protected boolean wantsItem(ItemStack itemStack) {
        return itemStack.is(Items.SPORE_BLOSSOM) || itemStack.is(MillagerItems.liquor.asItem());
    }

    public void updateTargetingAndDistance() {
        LivingEntity target = this.getTarget();

        if (target == null || !target.isAlive() || target.isRemoved()) {
            this.getNavigation().stop();
            return;
        }

        double distSqr = this.distanceToSqr(target);
        boolean hasLOS = this.getSensing().hasLineOfSight(target);

        double throwSpeed = 0.75;
        double predictionTicks = (Math.sqrt(distSqr) / throwSpeed) + 2.0;
        Vec3 targetMotion = target.getDeltaMovement();

        double predX = target.getX() + targetMotion.x * predictionTicks;
        double predZ = target.getZ() + targetMotion.z * predictionTicks;
        double predY = target.onGround() ? target.getEyeY() : target.getEyeY() + targetMotion.y * predictionTicks;

        this.getLookControl().setLookAt(predX, predY, predZ, 40.0F, 40.0F);

        double dx = predX - this.getX();
        double dz = predZ - this.getZ();
        float targetYaw = (float) (Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;

        this.setYRot(MiscHelper.rotateTowards(this.getYRot(), targetYaw, 30.0F));
        this.yBodyRot = this.getYRot();
        this.yHeadRot = this.getYRot();

        double idealSqr = 64.0;
        double toleranceSqr = 25.0;
        double attackRadiusSqr = 225.0;

        if (distSqr <= attackRadiusSqr && hasLOS) {
            this.getNavigation().stop();

            float forward = 0.0F;
            if (distSqr < idealSqr - toleranceSqr) {
                forward = -0.5F;
            } else if (distSqr > idealSqr + toleranceSqr) {
                forward = 0.5F;
            }

            float sideways = ((this.strafeTick / 20) % 2 == 0) ? 0.4F : -0.4F;
            this.strafeTick++;

            this.getMoveControl().strafe(forward, sideways);

        } else {
            double speedModifier = 1.0;
            this.getNavigation().moveTo(target, speedModifier);
            this.strafeTick = 0;
        }
    }

    private ItemStack getRandomDecoratedShield(RandomSource random, RegistryAccess registryAccess) {
        ItemStack shield = new ItemStack(Items.SHIELD);
        Registry<BannerPattern> patternRegistry = registryAccess.registryOrThrow(Registries.BANNER_PATTERN);

        Optional<List<Holder<BannerPattern>>> patternHolders = patternRegistry
                .getTag(BannerPatternTags.NO_ITEM_REQUIRED)
                .map(named -> new ArrayList<>(named.stream().toList()));

        if (patternHolders.isEmpty() || patternHolders.get().isEmpty()) {
            return shield;
        }

        List<Holder<BannerPattern>> pool = patternHolders.get();
        Collections.shuffle(pool, new Random(random.nextLong()));

        BannerPattern.Builder builder = new BannerPattern.Builder();

        int randomLayerCount = Math.min(3 + random.nextInt(3), pool.size());

        for (int i = 0; i < randomLayerCount; i++) {
            DyeColor randomColor = SHIELD_COLORS.get(random.nextInt(SHIELD_COLORS.size()));
            builder.addPattern(pool.get(i), randomColor);
        }

        patternRegistry.getHolder(ResourceVariantHelper.VILLAGER).ifPresent(holder -> {
            DyeColor villagerLayerColor = SHIELD_COLORS.get(random.nextInt(SHIELD_COLORS.size()));
            builder.addPattern(holder, villagerLayerColor);
        });

        CompoundTag blockEntityTag = shield.getOrCreateTagElement("BlockEntityTag");
        blockEntityTag.putInt("Base", DyeColor.YELLOW.getId());
        blockEntityTag.put("Patterns", builder.toListTag());

        return shield;
    }

    public int getDisableCooldown() {
        return this.disableCooldown;
    }

    public void setDisableCooldown(int disableCooldown) {
        this.disableCooldown = disableCooldown;
    }

    public int getTauntCooldown() {
        return this.tauntCooldown;
    }

    public void setTauntCooldown(int tauntCooldown) {
        this.tauntCooldown = tauntCooldown;
    }

    public int getThrowCooldown() {
        return this.throwCooldown;
    }

    public void setThrowCooldown(int throwCooldown) {
        this.throwCooldown = throwCooldown;
    }

    public int getActivityTicks() {
        return this.activityTicks;
    }

    public void setActivityTicks(int activityTicks) {
        this.activityTicks = activityTicks;
    }

    public boolean isThrowing() {
        return this.entityData.get(IS_THROWING);
    }

    public void setThrowing(boolean isThrowing) {
        this.entityData.set(IS_THROWING, isThrowing);
    }

    public boolean isTaunting() {
        return this.entityData.get(IS_TAUNTING);
    }

    public void setTaunting(boolean isTaunting) {this.entityData.set(IS_TAUNTING, isTaunting);}
}
