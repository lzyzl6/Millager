package org.lzyzl.millager.entity.ai.millager;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import org.lzyzl.millager.entity.millager.Swordmaster;

import java.util.EnumSet;
import java.util.List;

public class SwordmasterRespawnGoal extends Goal {

    private final Swordmaster swordmaster;
    private final ItemStack oldSword = createOldSword();

    public SwordmasterRespawnGoal(Swordmaster swordmaster) {
        this.swordmaster = swordmaster;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.swordmaster.isRespawned();
    }

    @Override
    public boolean canContinueToUse() {
        return this.swordmaster.getInvulnerableTicks() > 49;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public void start() {
        this.swordmaster.getNavigation().stop();
        if (this.swordmaster.getInvulnerableTicks() <= 0) {
            this.swordmaster.setInvulnerableTicks(80);
            this.swordmaster.setHealth(15.0f);
        }
    }

    @Override
    public void tick() {
        if (this.swordmaster.getHealth() < this.swordmaster.getMaxHealth() * 0.8f && this.swordmaster.getInvulnerableTicks() % 8 == 0) {
            this.swordmaster.setHealth(this.swordmaster.getHealth() + 1);
        }
        int ticks = this.swordmaster.getInvulnerableTicks();
        this.swordmaster.performRespawnKnockback(ticks);

        if (ticks == 60) {
            this.swordmaster.setItemSlot(EquipmentSlot.OFFHAND, oldSword.copy());
            this.swordmaster.playSound(SoundEvents.ITEM_PICKUP);
        }

        if (ticks == 50) {
            if(this.swordmaster.getLastHurtByMob() instanceof LivingEntity living) {
                this.swordmaster.getLookControl().setLookAt(living, 30.0F, 30.0F);
                this.swordmaster.getNavigation().moveTo(living,0.6f);
                this.swordmaster.playSound(SoundEvents.VILLAGER_NO, 3.0f, 1.0f);
            }
        }

        if (ticks > 50) {
            this.swordmaster.getNavigation().stop();
        }
    }

    @Override
    public void stop() {
        this.swordmaster.setRespawned(false);
        this.swordmaster.setInvulnerableTicks(49);
    }

    private ItemStack createOldSword() {
        ItemStack oldSword = new ItemStack(Items.COPPER_SWORD);
        oldSword.set(DataComponents.ITEM_NAME, Component.translatable("item.millager.training_sword"));
        List<Component> lines = List.of(
                Component.translatable("tooltip.millager.training_sword.lore1")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
                Component.translatable("tooltip.millager.training_sword.lore2")
                        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
        );
        oldSword.set(DataComponents.LORE, new ItemLore(lines));
        return oldSword;
    }

}