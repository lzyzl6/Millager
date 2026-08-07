package org.lzyzl.millager.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.NotNull;
import org.lzyzl.millager.MillagerItems;
import org.lzyzl.millager.MillagerSounds;
import org.lzyzl.millager.entity.millager.AbstractMillager;

import java.util.function.Supplier;

public class ProfessionOrderItem extends Item {

    private final Supplier<? extends EntityType<? extends AbstractMillager>> entityType;
    private final Equipment equipment;

    public ProfessionOrderItem(Supplier<? extends EntityType<? extends AbstractMillager>> entityType,
                               Equipment equipment, Properties properties) {
        super(properties);
        this.entityType = entityType;
        this.equipment = equipment;
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack stack, @NotNull Player player,
                                                            @NotNull LivingEntity target,
                                                            @NotNull InteractionHand hand) {
        if (target instanceof Villager villager) {
            if (villager.isBaby()) return InteractionResult.PASS;
            if (villager.getVillagerData().getProfession() != VillagerProfession.NONE) {
                villager.setUnhappyCounter(40);
                if (!villager.level().isClientSide()) villager.makeSound(SoundEvents.VILLAGER_NO);
                return InteractionResult.SUCCESS;
            }
        } else if (!(target instanceof AbstractMillager millager) || millager.getType() == this.entityType.get()) {
            return InteractionResult.PASS;
        }
        if (target.level().isClientSide()) return InteractionResult.SUCCESS;
        if (!(target.level() instanceof ServerLevel level)) return InteractionResult.PASS;

        double x = target.getX();
        double y = target.getY();
        double z = target.getZ();
        float yRot = target.getYRot();
        float xRot = target.getXRot();
        AbstractMillager millager = this.entityType.get().create(level);
        if (millager == null) return InteractionResult.PASS;
        millager.setPos(x, y, z);
        millager.setYRot(yRot);
        millager.setXRot(xRot);
        millager.setCustomName(target.getCustomName());
        millager.setCustomNameVisible(target.isCustomNameVisible());
        millager.setPersistenceRequired();
        millager.finalizeSpawn(level, level.getCurrentDifficultyAt(target.blockPosition()),
                MobSpawnType.CONVERSION, null);
        millager.setProfessionOrderOwner(player.getUUID());
        this.equipment.apply(millager);
        Entity rootVehicle = millager.getRootVehicle();
        rootVehicle.moveTo(x, y, z, yRot, xRot);
        Entity oldVehicle = target instanceof AbstractMillager ? target.getVehicle() : null;
        target.discard();
        if (oldVehicle instanceof Horse) oldVehicle.discard();
        level.addFreshEntityWithPassengers(millager);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, millager.getX(), millager.getY() + 1.0D,
                millager.getZ(), 30, 0.5D, 0.75D, 0.5D, 0.1D);
        level.playSound(null, millager.getX(), millager.getY(), millager.getZ(),
                MillagerSounds.PROFESSION_ORDER_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
        stack.consume(1, player);
        return InteractionResult.SUCCESS;
    }

    public enum Equipment {
        DEFAULT {
            @Override
            void apply(AbstractMillager millager) {
            }
        },
        IRON_SWORDS {
            @Override
            void apply(AbstractMillager millager) {
                millager.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
                ItemStack offhand = new ItemStack(Items.IRON_SWORD);
                CustomData.update(DataComponents.CUSTOM_DATA, offhand,
                        tag -> tag.putBoolean("SwordmasterDeathProtection", true));
                millager.setItemSlot(EquipmentSlot.OFFHAND, offhand);
            }
        },
        IRON_HORSE_ARMOR {
            @Override
            void apply(AbstractMillager millager) {
                setHorseArmor(millager);
            }
        },
        IRON_SPEAR_AND_HORSE_ARMOR {
            @Override
            void apply(AbstractMillager millager) {
                millager.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(MillagerItems.ironLancerSpear.get()));
                setHorseArmor(millager);
            }
        },
        IRON_AXE_AND_HORSE_ARMOR {
            @Override
            void apply(AbstractMillager millager) {
                millager.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
                setHorseArmor(millager);
            }
        };

        abstract void apply(AbstractMillager millager);

        static void setHorseArmor(AbstractMillager millager) {
            if (millager.getVehicle() instanceof Horse horse) {
                horse.setItemSlot(EquipmentSlot.BODY, new ItemStack(Items.IRON_HORSE_ARMOR));
            }
        }
    }
}
