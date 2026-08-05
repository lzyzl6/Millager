package org.lzyzl.millager.entity.millager;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.lzyzl.millager.MillagerItems;

public class Conjurer extends AbstractMillager {

    public Conjurer(EntityType<? extends AbstractMillager> entityType, Level level) {
        super(entityType, level);
        this.inventory = new SimpleContainer(3);
    }

    @Override
    public MillagerPose getMillagerPose() {
        return MillagerPose.NEUTRAL;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractMillager.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.35F)
                .add(Attributes.ARMOR,2)
                .add(Attributes.ARMOR_TOUGHNESS, 12)
                .add(Attributes.FALL_DAMAGE_MULTIPLIER, 0.0D)
                .add(Attributes.MAX_HEALTH, 28.0)
                .add(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE, 0.5D)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected boolean wantsItem(ItemStack itemStack) {
        return itemStack.is(Items.WITHER_ROSE) || itemStack.is(MillagerItems.ILLAGER_HEAD.asItem());
    }
}