package org.lzyzl.millager.entity.millager;
import net.minecraft.nbt.CompoundTag;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.MillagerItems;
import org.lzyzl.millager.entity.ai.millager.DoctorBeeSummonGoal;
import org.lzyzl.millager.entity.ai.millager.DoctorFlyEscapeGoal;
import org.lzyzl.millager.entity.ai.millager.DoctorSpawnIronGoal;

import java.util.List;

public class Doctor extends AbstractMillager {

    private static final EntityDataAccessor<Boolean> IS_BLOCKING = SynchedEntityData.defineId(Doctor.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_SUMMONING = SynchedEntityData.defineId(Doctor.class, EntityDataSerializers.BOOLEAN);

    private int ironBuildCooldown = 0;
    private int ironGolemsBuilt = 0;
    private int beeSpawnCooldown = 0;

    private DoctorSpawnIronGoal spawnIronGoal;

    private boolean needsBuildCleanup = false;
    private BlockPos pendingCleanupPos = null;

    private static final List<Item> wantItems = List.of(Items.TORCHFLOWER, MillagerItems.golemAmber.get(), MillagerItems.liquor.get(),
            MillagerItems.molotovCocktail.get(), MillagerItems.molotovCocktailPlus.get(), MillagerItems.tntOnAStick.get(),
            Items.TNT, Items.APPLE, Items.IRON_BLOCK, Items.CARVED_PUMPKIN, Items.GUNPOWDER);

    public Doctor(EntityType<? extends Doctor> entityType, Level level) {
        super(entityType, level);
        this.inventory = new SimpleContainer(15);
    }

    @Override
    public MillagerPose getMillagerPose() {
        if(this.isSummoning()) return MillagerPose.SUMMONING;
        else if(this.isBlockingGolem()) {
            if(this.swinging) return MillagerPose.SWINGING_ARM;
            return MillagerPose.HOLDING_ITEM;
        }
        return MillagerPose.NEUTRAL;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractMillager.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.3F)
                .add(Attributes.ARMOR,3)
                .add(Attributes.ARMOR_TOUGHNESS, 1)
                .add(Attributes.FALL_DAMAGE_MULTIPLIER, 0.7D)
                .add(Attributes.MAX_HEALTH, 22.0)
                .add(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE, 0.5D)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void registerGoals() {
        this.spawnIronGoal = new DoctorSpawnIronGoal(this);
        this.goalSelector.addGoal(1, new DoctorFlyEscapeGoal(this));
        this.goalSelector.addGoal(1, new DoctorBeeSummonGoal(this));
        this.goalSelector.addGoal(2, this.spawnIronGoal);
        super.registerGoals();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NonNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_BLOCKING, false);
        builder.define(IS_SUMMONING, false);
    }

    @Override
    public void addAdditionalSaveData(@NonNull CompoundTag valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        ContainerHelper.saveAllItems(valueOutput, this.inventory.getItems(), this.level().registryAccess());
        if (this.spawnIronGoal != null) {
            valueOutput.putInt("BuildStage", this.spawnIronGoal.getBuildStage());
            if (this.spawnIronGoal.getStructureOrigin() != null) {
                valueOutput.putLong("StructureOrigin", this.spawnIronGoal.getStructureOrigin().asLong());
            }
        }
        valueOutput.putBoolean("isBlocking", this.isBlockingGolem());
        valueOutput.putBoolean("isSummoning", this.isSummoning());
        valueOutput.putInt("ironBuildCooldown",this.getIronBuildCooldown());
        valueOutput.putInt("ironGolemsBuilt",this.getIronGolemsBuilt());
        valueOutput.putInt("beeSpawnCooldown",this.getBeeSpawnCooldown());
    }

    @Override
    public void readAdditionalSaveData(@NonNull CompoundTag valueInput) {
        super.readAdditionalSaveData(valueInput);
        ContainerHelper.loadAllItems(valueInput, this.inventory.getItems(), this.level().registryAccess());
        int stage = valueInput.contains("BuildStage") ? valueInput.getInt("BuildStage") : 0;
        long posLong = valueInput.contains("StructureOrigin") ? valueInput.getLong("StructureOrigin") : 0L;
        if (stage > 0 && posLong != 0L) {
            this.needsBuildCleanup = true;
            this.pendingCleanupPos = BlockPos.of(posLong);
        }
        this.setBlockingGolem(valueInput.getBoolean("isBlocking"));
        this.setSummoning(valueInput.getBoolean("isSummoning"));
        this.setIronBuildCooldown(valueInput.getInt("ironBuildCooldown"));
        this.setIronGolemsBuilt(valueInput.getInt("ironGolemsBuilt"));
        this.setBeeSpawnCooldown(valueInput.getInt("beeSpawnCooldown"));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        int ironBuildCooldown = this.getIronBuildCooldown();
        int beeSpawnCooldown = this.getBeeSpawnCooldown();
        if (ironBuildCooldown > 0) this.setIronBuildCooldown(ironBuildCooldown -1);
        if (beeSpawnCooldown > 0) this.setBeeSpawnCooldown(beeSpawnCooldown - 1);
    }

    @Override
    protected boolean wantsItem(ItemStack itemStack) {
        return wantItems.contains(itemStack.getItem());
    }

    public boolean consumeCleanupRequest() {
        if (this.needsBuildCleanup) {
            this.needsBuildCleanup = false;
            return true;
        }
        return false;
    }

    public BlockPos getPendingCleanupPos() {
        return this.pendingCleanupPos;
    }

    public boolean isBlockingGolem() {
        return this.entityData.get(IS_BLOCKING);
    }

    public void setBlockingGolem(boolean blockingGolem) {
        this.entityData.set(IS_BLOCKING, blockingGolem);
    }

    public boolean isSummoning() {
        return this.entityData.get(IS_SUMMONING);
    }

    public void setSummoning(boolean summoning) {
        this.entityData.set(IS_SUMMONING, summoning);
    }

    public int getIronBuildCooldown() {
        return this.ironBuildCooldown;
    }

    public void setIronBuildCooldown(int ironBuildCooldown) {
        this.ironBuildCooldown = ironBuildCooldown;
    }

    public int getIronGolemsBuilt() {
        return this.ironGolemsBuilt;
    }

    public void setIronGolemsBuilt(int ironGolemsBuilt) {
        this.ironGolemsBuilt = ironGolemsBuilt;
    }

    public int getBeeSpawnCooldown() {
        return this.beeSpawnCooldown;
    }

    public void setBeeSpawnCooldown(int beeSpawnCooldown) {
        this.beeSpawnCooldown = beeSpawnCooldown;
    }
}
