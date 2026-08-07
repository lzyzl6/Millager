package org.lzyzl.millager.entity.millager;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.Filterable;
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
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.providers.VanillaEnchantmentProviders;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.entity.ai.millager.LancerHealAuraGoal;
import org.lzyzl.millager.entity.ai.millager.RiderAvoidAllyGoal;
import org.lzyzl.millager.entity.ai.millager.RiderHorseHurtByTargetGoal;
import org.lzyzl.millager.entity.ai.millager.RiderRemountGoal;
import org.lzyzl.millager.entity.ai.vanilla.VanillaSpearUseGoal;
import org.lzyzl.millager.entity.golem.BeeGolem;

import java.util.ArrayList;
import java.util.List;

public class Lancer extends AbstractMillager implements Rider {

    private static final EntityDataAccessor<Boolean> IS_HEALING = SynchedEntityData.defineId(Lancer.class, EntityDataSerializers.BOOLEAN);

    private long nextHealTime = 0L;
    private int castTicks = 0;
    private final ItemStack book = createLancerBook();

    public Lancer(EntityType<? extends Lancer> entityType, Level level) {
        super(entityType, level);
        this.inventory = new SimpleContainer(3);
        this.getNavigation().setCanOpenDoors(false);
    }

    @Override
    public MillagerPose getMillagerPose() {
        if(this.isHealing()) return MillagerPose.SPELLCASTING;
        else if(this.getItemBySlot(EquipmentSlot.MAINHAND).is(ItemTags.SPEARS)) return MillagerPose.SPEAR;
        return MillagerPose.NEUTRAL;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractMillager.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.35F)
                .add(Attributes.ARMOR,10)
                .add(Attributes.ARMOR_TOUGHNESS, 3)
                .add(Attributes.FALL_DAMAGE_MULTIPLIER, 0.7D)
                .add(Attributes.MAX_HEALTH, 28.0)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE, 0.5D)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new RiderRemountGoal<>(this, 1.2D));
        this.goalSelector.addGoal(1, new LancerHealAuraGoal(this));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Horse.class, 4.0F, 1.0D, 1.2D));
        this.goalSelector.addGoal(2, new VanillaSpearUseGoal<>(this, 1.5, 1.3, 10.0F, 2.0F));

        this.targetSelector.addGoal(1, new RiderAvoidAllyGoal<>(this,true));
        this.targetSelector.addGoal(1, new RiderHorseHurtByTargetGoal<>(this,AbstractMillager.class, BeeGolem.class, IronGolem.class));
        super.registerGoals();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NonNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_HEALING, false);
    }

    @Override
    protected void addAdditionalSaveData(@NonNull ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        this.writeInventoryToTag(valueOutput);
        valueOutput.putBoolean("isHealing", this.isHealing());
        valueOutput.putLong("nextHealTime", this.getNextHealTime());
        valueOutput.putInt("castTicks", this.getCastTicks());
    }

    @Override
    protected void readAdditionalSaveData(@NonNull ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.readInventoryFromTag(valueInput);
        this.setHealing(valueInput.getBooleanOr("isHealing",false));
        this.setNextHealTime(valueInput.getLongOr("nextHealTime",0L));
        this.setCastTicks(valueInput.getIntOr("castTicks",0));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        int castTicks = this.getCastTicks();
        if(castTicks > 0) this.setCastTicks(castTicks - 1);
    }

    @Override
    public boolean hurtServer(@NonNull ServerLevel serverLevel, @NonNull DamageSource damageSource, float amount) {
        if(
                this.isPassenger() && damageSource.is(DamageTypes.IN_WALL)
                        && this.getVehicle() instanceof Horse horse && !horse.getInBlockState().isSolidRender()
        ) return false;
        if (this.isHealing() && this.getVehicle() instanceof AbstractHorse mount) {
            float halfDamage = amount / 2.0F;
            mount.hurtServer(serverLevel, damageSource, halfDamage);
            return super.hurtServer(serverLevel, damageSource, halfDamage);
        }
        return super.hurtServer(serverLevel, damageSource, amount);
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
        ItemStack spear = new ItemStack(getRandomSpear(random));
        this.setItemSlot(EquipmentSlot.MAINHAND, spear);
        this.setItemSlot(EquipmentSlot.OFFHAND, book.copy());
    }

    @Override
    protected void enchantSpawnedWeapon(@NonNull ServerLevelAccessor serverLevelAccessor, @NonNull RandomSource randomSource, @NonNull DifficultyInstance difficultyInstance) {
        super.enchantSpawnedWeapon(serverLevelAccessor, randomSource, difficultyInstance);
        if (randomSource.nextInt(400 - difficultyInstance.getDifficulty().getId() * 50) == 0) {
            ItemStack itemStack = this.getMainHandItem();
            if (itemStack.is(ItemTags.SPEARS)) {
                EnchantmentHelper.enchantItemFromProvider(
                        itemStack, serverLevelAccessor.registryAccess(), VanillaEnchantmentProviders.MOB_SPAWN_EQUIPMENT, difficultyInstance, randomSource
                );
            }
        }
    }

    @Override
    protected boolean wantsItem(ItemStack itemStack) {
        return itemStack.is(Items.CACTUS_FLOWER) || itemStack.is(Items.LEAD);
    }

    @Override
    public void createMount(ServerLevelAccessor level, EntitySpawnReason spawnReason, SpawnGroupData spawnGroupData) {
        Horse horse = EntityType.HORSE.create(level.getLevel(), spawnReason);
        if (horse != null) {
            horse.setPos(this.getX(), this.getY(), this.getZ());
            horse.finalizeSpawn(level, level.getCurrentDifficultyAt(horse.blockPosition()), spawnReason, spawnGroupData);
            double minSpeed = 0.3;// 正常范围大约是 0.1125 到 0.3375
            var speedAttribute = horse.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speedAttribute != null && speedAttribute.getBaseValue() < minSpeed) {
                speedAttribute.setBaseValue(minSpeed);
            }
            double minHealth = 25; // 正常范围大约是 15 到 30
            var healthAttribute = horse.getAttribute(Attributes.MAX_HEALTH);
            if (healthAttribute != null && healthAttribute.getBaseValue() < minHealth) {
                healthAttribute.setBaseValue(minHealth);
            }
            var explosionKnockbackAttribute = horse.getAttribute(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE);
            if (explosionKnockbackAttribute != null) {
                explosionKnockbackAttribute.setBaseValue(0.5);
            }
            horse.setHealth(horse.getMaxHealth());
            horse.setItemSlot(EquipmentSlot.BODY, new ItemStack(Rider.getRandomHorseArmor(level.getRandom(), 1,4)));
            horse.setTamed(true);
            horse.addTag("millager_mount");

            this.startRiding(horse);
            level.addFreshEntity(horse);
        }
    }

    private ItemStack createLancerBook() {
        ItemStack book = Items.WRITTEN_BOOK.getDefaultInstance();
        List<Filterable<Component>> pages = new ArrayList<>();

        for (int i = 1; i <= 13; i++) {
            pages.add(Filterable.passThrough(Component.translatable("item.millager.written_book.lancer.page" + i)));
        }

        WrittenBookContent content = new WrittenBookContent(
                Filterable.passThrough(Component.translatable("item.millager.written_book.lancer.title").getString()),
                Component.translatable("item.millager.written_book.lancer.author").getString(),
                1,
                pages,
                true
        );

        book.set(DataComponents.WRITTEN_BOOK_CONTENT, content);
        return book;
    }

    private Item getRandomSpear(RandomSource random) {
        int i = random.nextInt(4);
        return switch (i) {
            case 0 -> Items.COPPER_SPEAR;
            case 1 -> Items.GOLDEN_SPEAR;
            case 2 -> Items.DIAMOND_SPEAR;
            default -> Items.IRON_SPEAR;
        };
    }

    public boolean isHealing() {
        return this.entityData.get(IS_HEALING);
    }

    public void setHealing(boolean crafting) {
        this.entityData.set(IS_HEALING, crafting);
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

}
