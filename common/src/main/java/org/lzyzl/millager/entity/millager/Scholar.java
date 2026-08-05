package org.lzyzl.millager.entity.millager;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.client.util.BookAnimationController;
import org.lzyzl.millager.entity.ai.millager.ScholarReadBookGoal;

public class Scholar extends AbstractMillager {

    private static final EntityDataAccessor<Boolean> IS_READING = SynchedEntityData.defineId(Scholar.class, EntityDataSerializers.BOOLEAN);
    public final BookAnimationController bookController = new BookAnimationController();

    public Scholar(EntityType<? extends AbstractMillager> entityType, Level level) {
        super(entityType, level);
        this.inventory = new SimpleContainer(8);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NonNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_READING, false);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(4, new ScholarReadBookGoal(this, 160));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            updateBookBehavior();
            bookController.tick();
        }
    }

    @Override
    public MillagerPose getMillagerPose() {
        return MillagerPose.NEUTRAL;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractMillager.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.4F)
                .add(Attributes.ARMOR,6)
                .add(Attributes.ARMOR_TOUGHNESS, 3)
                .add(Attributes.FALL_DAMAGE_MULTIPLIER, 0.7D)
                .add(Attributes.MAX_HEALTH, 21.0)
                .add(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE, 0.5D)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected boolean wantsItem(ItemStack itemStack) {
        return itemStack.is(Items.BLUE_ORCHID) || itemStack.is(ItemTags.LECTERN_BOOKS);
    }

    private void updateBookBehavior() {
        if (this.isReading()) {
            // 收到服务端的正在阅读信号 -> 调整书本位置、开启自动翻页
            bookController.setMode(BookAnimationController.PathMode.SINE_WAVE);
            bookController.moveTo(0.0f, 1.4f, 0.6f); // 脸前的位置
            bookController.rotateTo(0, 30, 0);       // 稍微倾斜像在手里
            bookController.setOpen(true);
            bookController.setAutoFlip(true);        // 开启自动翻页
        } else {
            // 没有阅读 -> 书本收回侧边挂着
            bookController.setMode(BookAnimationController.PathMode.STATIONARY);
            bookController.moveTo(0.4f, 0.8f, -0.2f); // 腰间位置
            bookController.rotateTo(0, 90, 0);
            bookController.setOpen(false);
            bookController.setAutoFlip(false);
        }
    }

    public boolean isReading() {
        return this.entityData.get(IS_READING);
    }

    public void setReading(boolean reading) {
        this.entityData.set(IS_READING, reading);
    }
}
