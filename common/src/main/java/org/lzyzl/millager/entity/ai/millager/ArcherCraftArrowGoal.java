package org.lzyzl.millager.entity.ai.millager;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
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
        }
    }

    private ItemStack getArrowResult() {
        ItemStack result;
        Potion[] potions = {
                Potions.STRONG_SLOWNESS,
                Potions.SLOWNESS,
                Potions.LONG_SLOWNESS,
                Potions.STRONG_POISON,
                Potions.LONG_POISON,
                Potions.POISON,
                Potions.HARMING,
                Potions.HEALING,
                Potions.WEAKNESS
        };
        ItemStack tippedArrow = new ItemStack(Items.TIPPED_ARROW);
        PotionUtils.setPotion(tippedArrow, potions[this.archer.getRandom().nextInt(potions.length)]);
        ItemStack[] specialArrows = {
                new ItemStack(Items.SPECTRAL_ARROW),
                tippedArrow,
                new ItemStack(MillagerItems.explosiveArrow.get())
        };
        result = specialArrows[this.archer.getRandom().nextInt(specialArrows.length)];
        return result;
    }
}
