package org.lzyzl.millager.entity.millager;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.network.Filterable;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
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
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lzyzl.millager.MillagerItems;
import org.lzyzl.millager.entity.ai.millager.LancerHealAuraGoal;
import org.lzyzl.millager.entity.ai.millager.RiderAvoidAllyGoal;
import org.lzyzl.millager.entity.ai.millager.RiderHorseHurtByTargetGoal;
import org.lzyzl.millager.entity.ai.millager.RiderRemountGoal;
import org.lzyzl.millager.entity.ai.vanilla.LancerSpearUseGoal;
import org.lzyzl.millager.entity.golem.BeeGolem;

import java.util.ArrayList;
import java.util.List;

public class Lancer extends AbstractMillager implements Rider {
    private static final EntityDataAccessor<Boolean> IS_HEALING = SynchedEntityData.defineId(Lancer.class, EntityDataSerializers.BOOLEAN);
    private long nextHealTime;
    private int castTicks;
    public Lancer(EntityType<? extends Lancer> entityType, Level level) {
        super(entityType, level);
        this.inventory = new SimpleContainer(3);
        this.setCanOpenDoors(false);
    }

    @Override
    public MillagerPose getMillagerPose() {
        if (this.isHealing()) return MillagerPose.SPELLCASTING;
        if (this.isUsingItem() && LancerSpearUseGoal.isLancerSpear(this.getUseItem())) return MillagerPose.SPEAR;
        return this.getMainHandItem().isEmpty() ? MillagerPose.NEUTRAL : MillagerPose.HOLDING_ITEM;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractMillager.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.35F)
                .add(Attributes.ARMOR, 10)
                .add(Attributes.ARMOR_TOUGHNESS, 3)
                .add(Attributes.MAX_HEALTH, 28.0)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new RiderRemountGoal<>(this, 1.2D));
        this.goalSelector.addGoal(1, new LancerHealAuraGoal(this));
        this.goalSelector.addGoal(2, new LancerSpearUseGoal<>(this, 1.5D, 1.3D, 10.0F, 2.0F));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Horse.class, 4.0F, 1.0D, 1.2D));
        this.targetSelector.addGoal(1, new RiderAvoidAllyGoal<>(this, true));
        this.targetSelector.addGoal(1, new RiderHorseHurtByTargetGoal<>(this, AbstractMillager.class, BeeGolem.class, IronGolem.class));
        super.registerGoals();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NonNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_HEALING, false);
    }

    @Override
    public void addAdditionalSaveData(@NonNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("isHealing", this.isHealing());
        tag.putLong("nextHealTime", this.getNextHealTime());
        tag.putInt("castTicks", this.getCastTicks());
    }

    @Override
    public void readAdditionalSaveData(@NonNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setHealing(tag.getBoolean("isHealing"));
        this.setNextHealTime(tag.getLong("nextHealTime"));
        this.setCastTicks(tag.getInt("castTicks"));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.getCastTicks() > 0) this.setCastTicks(this.getCastTicks() - 1);
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        if(
                this.isPassenger() && damageSource.is(DamageTypes.IN_WALL)
                        && this.getVehicle() instanceof Horse horse && !horse.getInBlockState().isSolidRender(this.level(), horse.blockPosition())
        ) return false;
        if (this.isHealing() && this.getVehicle() instanceof Horse horse) {
            float halfDamage = amount / 2.0F;
            horse.hurt(damageSource, halfDamage);
            return super.hurt(damageSource, halfDamage);
        }
        return super.hurt(damageSource, amount);
    }

    public boolean isHealing() {
        return this.entityData.get(IS_HEALING);
    }

    public void setHealing(boolean healing) {
        this.entityData.set(IS_HEALING, healing);
    }

    public long getNextHealTime() {
        return this.nextHealTime;
    }

    public void setNextHealTime(long nextHealTime) {
        this.nextHealTime = nextHealTime;
    }

    public int getCastTicks() {
        return this.castTicks;
    }

    public void setCastTicks(int castTicks) {
        this.castTicks = castTicks;
    }

    @Override
    public SpawnGroupData finalizeSpawn(@NonNull ServerLevelAccessor level, @NonNull DifficultyInstance difficulty,
                                        @NonNull MobSpawnType spawnReason, @Nullable SpawnGroupData spawnGroupData) {
        spawnGroupData = super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData);
        this.populateDefaultEquipmentSlots(level.getRandom(), difficulty);
        this.createMount(level, spawnReason, spawnGroupData);
        this.enchantSpawnedWeapon(level, level.getRandom(), difficulty);
        return spawnGroupData;
    }

    @Override
    protected void populateDefaultEquipmentSlots(@NonNull RandomSource random, @NonNull DifficultyInstance difficulty) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(switch (random.nextInt(3)) {
            case 0 -> MillagerItems.ironLancerSpear.get();
            case 1 -> MillagerItems.goldenLancerSpear.get();
            default -> MillagerItems.diamondLancerSpear.get();
        }));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        this.setItemSlot(EquipmentSlot.OFFHAND, this.createLancerBook());
    }

    private ItemStack createLancerBook() {
        ItemStack book = Items.WRITTEN_BOOK.getDefaultInstance();
        List<Filterable<Component>> pages = new ArrayList<>();
        for (int i = 1; i <= 13; i++) {
            pages.add(Filterable.passThrough(Component.translatable("item.millager.written_book.lancer.page" + i)));
        }
        book.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(
                Filterable.passThrough(Component.translatable("item.millager.written_book.lancer.title").getString()),
                Component.translatable("item.millager.written_book.lancer.author").getString(), 1, pages, true));
        return book;
    }

    @Override
    public void createMount(ServerLevelAccessor level, MobSpawnType spawnReason, SpawnGroupData spawnGroupData) {
        Horse horse = EntityType.HORSE.create(level.getLevel());
        if (horse == null) return;
        horse.setPos(this.getX(), this.getY(), this.getZ());
        horse.finalizeSpawn(level, level.getCurrentDifficultyAt(horse.blockPosition()), spawnReason, spawnGroupData);
        var speedAttr = horse.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null && speedAttr.getBaseValue() < 0.3) speedAttr.setBaseValue(0.3);
        var healthAttr = horse.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null && healthAttr.getBaseValue() < 25) healthAttr.setBaseValue(25);
        var kbAttr = horse.getAttribute(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE);
        if (kbAttr != null) kbAttr.setBaseValue(0.5);
        horse.setHealth(horse.getMaxHealth());
        horse.setItemSlot(EquipmentSlot.BODY, new ItemStack(Rider.getRandomHorseArmor(level.getRandom(), 1, 3)));
        horse.setTamed(true);
        horse.addTag("millager_mount");
        this.startRiding(horse);
        level.addFreshEntity(horse);
    }

    @Override
    protected boolean wantsItem(ItemStack itemStack) {
        return itemStack.is(Items.CACTUS) || itemStack.is(Items.LEAD);
    }
}
