package org.lzyzl.millager.entity.millager;
import net.minecraft.nbt.CompoundTag;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.providers.VanillaEnchantmentProviders;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lzyzl.millager.MillagerItems;
import org.lzyzl.millager.entity.ai.millager.ArcherBowAttackGoal;
import org.lzyzl.millager.entity.ai.millager.ArcherCraftArrowGoal;
import org.lzyzl.millager.entity.ai.millager.ArcherPickArrowGoal;

import java.util.function.Predicate;

public class Archer extends AbstractMillager implements RangedAttackMob {

    private static final EntityDataAccessor<Boolean> IS_CRAFTING = SynchedEntityData.defineId(Archer.class, EntityDataSerializers.BOOLEAN);

    private int craftingTicks = 0;
    private int craftCooldown = 0;

    public Archer(EntityType<? extends Archer> entityType, Level level) {
        super(entityType, level);
        this.inventory = new SimpleContainer(7);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractMillager.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.35F)
                .add(Attributes.ARMOR,7)
                .add(Attributes.ARMOR_TOUGHNESS, 3)
                .add(Attributes.FALL_DAMAGE_MULTIPLIER, 0.7D)
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE, 0.5D)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new ArcherPickArrowGoal(this));
        this.goalSelector.addGoal(2, new ArcherBowAttackGoal<>(this, 1.0, 20, 20.0F));
        this.goalSelector.addGoal(3, new ArcherCraftArrowGoal(this));
        super.registerGoals();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NonNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_CRAFTING, false);
    }

    @Override
    public void addAdditionalSaveData(@NonNull CompoundTag valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        ContainerHelper.saveAllItems(valueOutput, this.inventory.getItems(), this.level().registryAccess());
        valueOutput.putBoolean("isCrafting", this.isCrafting());
        valueOutput.putInt("craftingTicks", this.getCraftingTicks());
        valueOutput.putInt("craftCooldown", this.getCraftCooldown());
    }

    @Override
    public void readAdditionalSaveData(@NonNull CompoundTag valueInput) {
        super.readAdditionalSaveData(valueInput);
        ContainerHelper.loadAllItems(valueInput, this.inventory.getItems(), this.level().registryAccess());
        this.setCrafting(valueInput.getBoolean("isCrafting"));
        this.setCraftingTicks(valueInput.getInt("craftingTicks"));
        this.setCraftCooldown(valueInput.getInt("craftCooldown"));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        int craftingTicks = this.getCraftingTicks();
        int craftCooldown = this.getCraftCooldown();
        if (craftCooldown > 0) this.setCraftCooldown(craftCooldown - 1);
        if (craftingTicks > 0) this.setCraftingTicks(craftingTicks - 1);
    }

    @Override
    public float getWalkTargetValue(@NonNull BlockPos blockPos, @NonNull LevelReader levelReader) {
        return 0.0F;
    }

    @Override
    public MillagerPose getMillagerPose() {
        ItemStack itemstack = this.getItemInHand(InteractionHand.MAIN_HAND);
        if (itemstack.is(Items.BOW) && this.isAggressive()) {
            return MillagerPose.BOW_AND_ARROW;
        } else if(this.isCrafting()) {
            return MillagerPose.CRAFTING_ARROW;
        }
        return MillagerPose.NEUTRAL;
    }

    @Override
    public @NonNull ItemStack getProjectile(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ProjectileWeaponItem) {
            Predicate<ItemStack> predicate = ((ProjectileWeaponItem)itemStack.getItem()).getSupportedHeldProjectiles();
            if(this.getItemBySlot(EquipmentSlot.OFFHAND).isEmpty()) {
                for(int i = 0; i < this.inventory.getContainerSize() ;i++) {
                    ItemStack potentialArrow = this.inventory.getItem(i);
                    if(predicate.test(potentialArrow)) {
                        this.setItemSlot(EquipmentSlot.OFFHAND, potentialArrow.copyAndClear());
                        break;
                    }
                }
            }
            ItemStack itemStack2 = this.getItemBySlot(EquipmentSlot.OFFHAND);
            return itemStack2.isEmpty() ? new ItemStack(Items.ARROW) : itemStack2.split(1);
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    protected void dropCustomDeathLoot(@NonNull ServerLevel serverLevel, @NonNull DamageSource damageSource, boolean bl) {
        ItemStack itemStack = this.getItemBySlot(EquipmentSlot.OFFHAND);
        if (!itemStack.isEmpty()) {
            this.spawnAtLocation(itemStack);
            this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        }
        super.dropCustomDeathLoot(serverLevel, damageSource, bl);
    }

    @Override
    public void performRangedAttack(@NonNull LivingEntity target, float velocity) {
        ItemStack bow = this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, Items.BOW));
        ItemStack ammo = this.getProjectile(bow);

        AbstractArrow arrow = ProjectileUtil.getMobArrow(this, ammo, velocity, bow);
        double dist = this.distanceTo(target);

        double speed = 3.0;
        double predictionTicks = (dist / speed) + 2.0;

        Vec3 movement = target.getDeltaMovement();

        double predX = target.getX() + movement.x * predictionTicks;
        double predZ = target.getZ() + movement.z * predictionTicks;

        double predY;
        if (!target.onGround() && Math.abs(movement.y) > 0.01) {
            predY = target.getY() + (movement.y * predictionTicks);
        } else {
            predY = target.getY();
        }

        double dx = predX - this.getX();
        double dz = predZ - this.getZ();
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);

        double dy = (predY + target.getBbHeight() * 0.3333333333333333) - arrow.getY();

        float gravityCompensation = 0.045F;

        if (this.level() instanceof ServerLevel serverLevel) {
            arrow.shoot(dx, dy + horizontalDist * gravityCompensation, dz, (float)speed, (float)(14 - serverLevel.getDifficulty().getId() * 4));
            serverLevel.addFreshEntity(arrow);
        }

        this.playSound(SoundEvents.ARROW_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level, @NonNull DifficultyInstance difficulty, @NonNull MobSpawnType entitySpawnReason, @Nullable SpawnGroupData spawnGroupData
    ) {
        RandomSource randomSource = level.getRandom();
        this.populateDefaultEquipmentSlots(randomSource, difficulty);
        this.enchantSpawnedWeapon(level, level.getRandom(), difficulty);
        return super.finalizeSpawn(level, difficulty, entitySpawnReason, spawnGroupData);
    }

    @Override
    protected void populateDefaultEquipmentSlots(@NonNull RandomSource random, DifficultyInstance difficulty) {
        if (difficulty.isHarderThan(1) && random.nextInt(11 - difficulty.getDifficulty().getId()) == 0) {
            ItemStack itemStack;
            if(random.nextInt(12) < 6 + difficulty.getDifficulty().getId()) {
                itemStack = Items.TIPPED_ARROW.getDefaultInstance();
                PotionContents potionContents = random.nextBoolean() ? new PotionContents(Potions.STRONG_POISON) : new PotionContents(Potions.STRONG_SLOWNESS);
                itemStack.set(DataComponents.POTION_CONTENTS , potionContents);
            } else itemStack = MillagerItems.explosiveArrow.get().getDefaultInstance();
            this.setItemSlot(EquipmentSlot.OFFHAND, itemStack);
            if(random.nextBoolean()) {
                int i = random.nextInt(5);
                while(--i >0) this.inventory.addItem(itemStack);
            }
        }
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
    }

    @Override
    protected void enchantSpawnedWeapon(@NonNull ServerLevelAccessor serverLevelAccessor, @NonNull RandomSource randomSource, @NonNull DifficultyInstance difficultyInstance) {
        super.enchantSpawnedWeapon(serverLevelAccessor, randomSource, difficultyInstance);
        if (randomSource.nextInt(400 - difficultyInstance.getDifficulty().getId() * 50) == 0) {
            ItemStack itemStack = this.getMainHandItem();
            if (itemStack.is(Items.BOW)) {
                EnchantmentHelper.enchantItemFromProvider(
                        itemStack, serverLevelAccessor.registryAccess(), VanillaEnchantmentProviders.MOB_SPAWN_EQUIPMENT, difficultyInstance, randomSource
                );
            }
        }
    }

    @Override
    public void pickUpItem(@NonNull ItemEntity itemEntity) {
        ItemStack itemStack = itemEntity.getItem();

        if (this.wantsItem(itemStack)) {
            this.onItemPickup(itemEntity);
            ItemStack offHand = this.getItemBySlot(EquipmentSlot.OFFHAND);

            if (itemStack.getItem() instanceof ArrowItem && offHand.isEmpty()) {
                this.setItemSlot(EquipmentSlot.OFFHAND, itemStack.copyAndClear());
            } else if (ItemStack.isSameItemSameComponents(itemStack, offHand)) {
                int canAccept = offHand.getMaxStackSize() - offHand.getCount();
                int moveCount = Math.min(itemStack.getCount(), canAccept);

                if (moveCount > 0) {
                    offHand.grow(moveCount);
                    itemStack.shrink(moveCount);
                }
            }

            if (!itemStack.isEmpty()) {
                ItemStack remaining = this.inventory.addItem(itemStack);
                if (remaining.isEmpty()) {
                    itemEntity.discard();
                } else {
                    itemStack.setCount(remaining.getCount());
                }
            } else {
                itemEntity.discard();
            }
        }
    }

    @Override
    protected boolean wantsItem(ItemStack itemStack) {
        return itemStack.is(Items.SUNFLOWER) || (itemStack.getItem() instanceof ArrowItem arrow && arrow.asItem() != Items.ARROW);
    }

    public boolean isCrafting() {
        return this.entityData.get(IS_CRAFTING);
    }

    public void setCrafting(boolean crafting) {
        this.entityData.set(IS_CRAFTING, crafting);
    }

    public int getCraftingTicks() {
        return this.craftingTicks;
    }

    public void setCraftingTicks(int craftingTicks) {
        this.craftingTicks = craftingTicks;
    }

    public int getCraftCooldown() {
        return this.craftCooldown;
    }

    public void setCraftCooldown(int craftCooldown) {
        this.craftCooldown = craftCooldown;
    }

}