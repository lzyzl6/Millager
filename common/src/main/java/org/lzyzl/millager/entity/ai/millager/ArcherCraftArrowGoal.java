package org.lzyzl.millager.entity.ai.millager;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.phys.Vec3;
import org.lzyzl.millager.MillagerItems;
import org.lzyzl.millager.MillagerSounds;
import org.lzyzl.millager.entity.millager.Archer;

public class ArcherCraftArrowGoal extends Goal {

    private final Archer archer;
    private boolean finished = false;

    public ArcherCraftArrowGoal(Archer archer) {
        this.archer = archer;
    }

    @Override
    public boolean canUse() {
        if(this.archer.isCrafting()) return true;
        if (this.archer.getCraftCooldown() > 0) return false;
        return !finished && this.archer.getTarget() == null
                && this.archer.getItemBySlot(EquipmentSlot.OFFHAND).isEmpty()
                && this.archer.getRandom().nextInt(100) == 0;
    }

    @Override
    public boolean canContinueToUse() {
        return !finished && this.archer.getTarget() == null;
    }

    @Override
    public void start() {
        this.archer.setCraftingTicks(80 + this.archer.getRandom().nextInt(81));
        this.archer.playSound(MillagerSounds.ARCHER_CRAFTING_ARROW);
        this.archer.setCrafting(true);
    }

    @Override
    public void stop() {
        if (this.finished && this.archer.getRandom().nextInt(9 + this.archer.level().getDifficulty().getId()) != 0) {
            ItemStack result;
            int amount = 1 + this.archer.getRandom().nextInt(3);

            result = getArrowResult();
            result.setCount(amount);

            this.archer.setItemSlot(EquipmentSlot.OFFHAND, result);
            if(this.archer.level() instanceof ServerLevel level) {
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.archer.getX(), this.archer.getY(), this.archer.getZ(), 60,
                        0.5, 1, 0.5,
                        0.1);
            }
            this.archer.playSound(SoundEvents.VILLAGER_YES);
            this.archer.setCraftCooldown(200 + this.archer.getRandom().nextInt(100));
        } else {
            this.archer.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
            if(this.archer.level() instanceof ServerLevel level) {
                level.sendParticles(ParticleTypes.ANGRY_VILLAGER, this.archer.getX(), this.archer.getY(), this.archer.getZ(), 20,
                        0.5, 1, 0.5,
                        0.1);
            }
            this.archer.level().playSound(null,this.archer.getOnPos(), SoundEvents.ITEM_BREAK, SoundSource.NEUTRAL,0.8f,1.0f);
            this.archer.playSound(SoundEvents.VILLAGER_NO,1.2f,1.0f);
            this.archer.setCraftCooldown(100);
        }
        this.archer.setCraftingTicks(0);
        this.archer.setCrafting(false);
        finished = false;
    }

    @Override
    public void tick() {
        int ticks = this.archer.getCraftingTicks();
        if(ticks < 1) this.finished = true;

        if(ticks % 40 == 0 && this.archer.getRandom().nextBoolean())  {
            this.archer.playSound(MillagerSounds.ARCHER_CRAFTING_ARROW);
            if(this.archer.level() instanceof ServerLevel level) {
                Vec3 vec3 =this.archer.getHandHoldingItemAngle(Items.BOW);
                level.sendParticles(ParticleTypes.DUST_PLUME,
                        this.archer.getX() + vec3.x, this.archer.getY() + vec3.y, this.archer.getZ() + vec3.z,
                        50,
                        0.5, 0.5, 0.5,
                        0.1);
            }
        }
    }

    private ItemStack getArrowResult() {
        ItemStack result;
        PotionContents[] potionContents = {
                new PotionContents(Potions.STRONG_SLOWNESS),
                new PotionContents(Potions.SLOWNESS),
                new PotionContents(Potions.LONG_SLOWNESS),
                new PotionContents(Potions.STRONG_POISON),
                new PotionContents(Potions.LONG_POISON),
                new PotionContents(Potions.POISON),
                new PotionContents(Potions.HARMING),
                new PotionContents(Potions.HEALING),
                new PotionContents(Potions.WEAKNESS)
        };
        ItemStack tippedArrow = new ItemStack(Items.TIPPED_ARROW);
        tippedArrow.set(DataComponents.POTION_CONTENTS, potionContents[this.archer.getRandom().nextInt(potionContents.length)]);
        ItemStack[] specialArrows = {
                new ItemStack(Items.SPECTRAL_ARROW),
                tippedArrow,
                new ItemStack(MillagerItems.explosiveArrow.get())
        };
        result = specialArrows[this.archer.getRandom().nextInt(specialArrows.length)];
        return result;
    }
}
