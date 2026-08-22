package org.lzyzl.millager.entity.millager;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.entity.ai.millager.SwordmasterMeleeAttackGoal;
import org.lzyzl.millager.entity.ai.millager.SwordmasterRespawnGoal;
import org.lzyzl.millager.entity.ai.millager.SwordmasterSwordShieldGoal;
import org.lzyzl.millager.util.MillagerTargetingHelper;

import java.util.List;

import static org.lzyzl.millager.util.MiscHelper.isAllyCaused;

public class Swordmaster extends AbstractMillager {

    private static final EntityDataAccessor<Boolean> RESPAWNED = SynchedEntityData.defineId(Swordmaster.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_SHIELDING = SynchedEntityData.defineId(Swordmaster.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> INVULNERABLE_TICKS = SynchedEntityData.defineId(Swordmaster.class, EntityDataSerializers.INT);

    private SwordmasterSwordShieldGoal shieldGoal;

    private int shieldedCooldown = 0;
    private int shieldedTimes = 0;

    public Swordmaster(EntityType<? extends Swordmaster> entityType, Level level) {
        super(entityType, level);
        this.inventory = new SimpleContainer(3);
    }

    @Override
    public MillagerPose getMillagerPose() {
        if(this.isRespawned()) return MillagerPose.REGAINING_SWORD;
        else if(this.getInvulnerableTicks() <= 49 && this.getInvulnerableTicks() > 0) return MillagerPose.APPROACHING;
        else if(this.isShielding()) return MillagerPose.SWORD_SHIELDING;
        else if (this.isAggressive()) return MillagerPose.ATTACKING;
        return MillagerPose.NEUTRAL;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractMillager.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.365F)
                .add(Attributes.ARMOR,6)
                .add(Attributes.ARMOR_TOUGHNESS, 6)
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.ATTACK_DAMAGE, 1.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected int calculateFallDamage(float fallDistance, float damageMultiplier) {
        return super.calculateFallDamage(fallDistance, damageMultiplier * 0.65F);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SwordmasterRespawnGoal(this));
        this.shieldGoal = new SwordmasterSwordShieldGoal(this);
        this.goalSelector.addGoal(1, this.shieldGoal);
        this.goalSelector.addGoal(1, new SwordmasterMeleeAttackGoal(this, 1.1D, false));
        super.registerGoals();
    }

    @Override
    public void tick() {
        super.tick();
        int ticks =this.getInvulnerableTicks();
        if(ticks > 0) {
            this.setInvulnerable(true);
            this.invulnerableTime = 40;
            performRespawnKnockback(ticks);
            if(ticks == 1) {
                this.setInvulnerable(false);
                this.invulnerableTime = 0;
            }
        }
        if(!this.isRespawned() && this.getItemBySlot(EquipmentSlot.OFFHAND).isEmpty()) {
            this.setRespawned(true);
        }
    }

    @Override
    public boolean hurt(@NonNull DamageSource damageSource, float amount) {
        if(isAllyCaused(damageSource)) return false;
        if(this.getInvulnerableTicks() > 0 && !damageSource.isCreativePlayer()) return false;
        if (this.shieldGoal != null && this.shieldGoal.attemptBlock(damageSource, amount)) return false;
        return super.hurt(damageSource, amount);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(RESPAWNED, false);
        this.entityData.define(IS_SHIELDING, false);
        this.entityData.define(INVULNERABLE_TICKS, 0);
    }

    @Override
    public void addAdditionalSaveData(@NonNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        this.saveInventory(tag);
        tag.putBoolean("respawned",this.isRespawned());
        tag.putBoolean("Invulnerable",this.isRespawned() || this.isInvulnerable());
        tag.putBoolean("isShielding",this.isShielding());
        tag.putInt("invulnerableTicks",this.getInvulnerableTicks());
        tag.putInt("shieldedTimes",this.getShieldedTimes());
        tag.putInt("shieldedCooldown",this.getShieldedCooldown());
    }

    @Override
    public void readAdditionalSaveData(@NonNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.loadInventory(tag);
        this.setRespawned(tag.getBoolean("respawned"));
        this.setShielding(tag.getBoolean("isShielding"));
        this.setInvulnerableTicks(tag.getInt("invulnerableTicks"));
        this.setShieldedTimes(tag.getInt("shieldedTimes"));
        this.setShieldedCooldown(tag.getInt("shieldedCooldown"));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        int cooldown = this.getShieldedCooldown();
        if (cooldown > 0) {
            this.setShieldedCooldown(cooldown - 1);
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(@NonNull ServerLevelAccessor level, @NonNull DifficultyInstance difficulty,
                                        @NonNull MobSpawnType spawnReason, @Nullable SpawnGroupData spawnGroupData,
                                        @Nullable CompoundTag tag) {
        spawnGroupData = super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData, tag);
        this.populateDefaultEquipmentSlots(level.getRandom(), difficulty);
        this.enchantSpawnedWeapon(level.getRandom(), difficulty.getSpecialMultiplier());
        return spawnGroupData;
    }

    @Override
    protected void populateDefaultEquipmentSlots(@NonNull RandomSource random, @NonNull DifficultyInstance difficulty) {
        Item swordItem = random.nextInt(7 - difficulty.getDifficulty().getId()) == 0 ? Items.DIAMOND_SWORD : Items.IRON_SWORD;
        ItemStack sword = new ItemStack(swordItem);
        this.setItemSlot(EquipmentSlot.MAINHAND, sword);
        ItemStack offHand = new ItemStack(swordItem);
        offHand.getOrCreateTag().putBoolean("SwordmasterDeathProtection", true);
        this.setItemSlot(EquipmentSlot.OFFHAND, offHand);
    }

    @Override
    protected void enchantSpawnedWeapon(@NonNull RandomSource randomSource, float specialMultiplier) {
        super.enchantSpawnedWeapon(randomSource, specialMultiplier);
        if (randomSource.nextInt(400 - this.level().getDifficulty().getId() * 50) == 0) {
            ItemStack itemStack = this.getMainHandItem();
            if (itemStack.is(ItemTags.SWORDS)) {
                this.setItemSlot(EquipmentSlot.MAINHAND, EnchantmentHelper.enchantItem(
                        randomSource, itemStack, (int)(5.0F + specialMultiplier * randomSource.nextInt(18)), false));
            }
        }
        if (randomSource.nextInt(400 - this.level().getDifficulty().getId() * 50) == 0) {
            ItemStack offHand = this.getOffhandItem();
            if (offHand.is(ItemTags.SWORDS)) {
                this.setItemSlot(EquipmentSlot.OFFHAND, EnchantmentHelper.enchantItem(
                        randomSource, offHand, (int)(5.0F + specialMultiplier * randomSource.nextInt(18)), false));
            }
        }

    }

    @Override
    protected boolean wantsItem(ItemStack itemStack) {
        return itemStack.is(Items.PITCHER_PLANT) || itemStack.is(Items.TOTEM_OF_UNDYING);
    }

    public void performRespawnKnockback(int ticks) {
        if (ticks > 0) {
            this.setInvulnerableTicks(ticks - 1);
            List<LivingEntity> targets = this.level().getEntitiesOfClass(
                    LivingEntity.class,
                    this.getBoundingBox().inflate(4.0),
                    entity -> !MillagerTargetingHelper.isFriendlyToMillager(entity) && entity.isAlive()
            );

            for (LivingEntity target : targets) {
                if (!(target instanceof Player player && (player.isSpectator() || player.isCreative()))) {
                    double dx = target.getX() - this.getX();
                    double dz = target.getZ() - this.getZ();
                    target.knockback(0.5, -dx, -dz);
                    target.hurtMarked = true;
                }
            }
        }
    }

    public boolean isRespawned() {
        return this.entityData.get(RESPAWNED);
    }

    public void setRespawned(boolean respawned) {
        this.entityData.set(RESPAWNED, respawned);
    }

    public int getInvulnerableTicks() {
        return this.entityData.get(INVULNERABLE_TICKS);
    }

    public void setInvulnerableTicks(int ticks) {
        this.entityData.set(INVULNERABLE_TICKS, ticks);
    }

    public boolean isShielding() {
        return this.entityData.get(IS_SHIELDING);
    }

    public void setShielding(boolean shielding) {
        this.entityData.set(IS_SHIELDING, shielding);
    }

    public int getShieldedCooldown() {
        return this.shieldedCooldown;
    }

    public void setShieldedCooldown(int shieldedCooldown) {
        this.shieldedCooldown = shieldedCooldown;
    }

    public int getShieldedTimes() {
        return this.shieldedTimes;
    }

    public void setShieldedTimes(int shieldedTimes) {
        this.shieldedTimes = shieldedTimes;
    }
}
