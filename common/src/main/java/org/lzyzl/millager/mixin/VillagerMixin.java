package org.lzyzl.millager.mixin;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.gossip.GossipContainer;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lzyzl.millager.MillagerItems;
import org.lzyzl.millager.advancement.MillagerCriteria;
import org.lzyzl.millager.item.ProfessionOrderItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Villager.class)
public class VillagerMixin {

    @Unique
    private boolean millager$hasNearbyElixirHolder = false;

    @Inject(method = "customServerAiStep", at = @At("TAIL"))
    private void millager$onCustomServerAiStep(CallbackInfo ci) {
        Villager self = (Villager) (Object) this;
        if (self.getVillagerData().getProfession() != VillagerProfession.NITWIT) return;

        if (self.level().getGameTime() % 20 == 0) {
            List<Player> nearby = self.level().getEntitiesOfClass(Player.class,
                    self.getBoundingBox().inflate(8.0));
            millager$hasNearbyElixirHolder = false;
            nearby.stream().filter(p -> p.getMainHandItem().is(MillagerItems.elixir.asItem())
                            || p.getOffhandItem().is(MillagerItems.elixir.asItem()))
                    .findFirst().ifPresent(p -> {
                        millager$hasNearbyElixirHolder = true;
                        self.getLookControl().setLookAt(p);
                    });
        }

        ItemStack currentHeld = self.getMainHandItem();
        if (millager$hasNearbyElixirHolder) {
            if (!currentHeld.is(Items.EMERALD_BLOCK)) {
                self.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.EMERALD_BLOCK));
                self.setDropChance(EquipmentSlot.MAINHAND, 0f);
            }
        } else {
            if (currentHeld.is(Items.EMERALD_BLOCK)) {
                self.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            }
        }
    }

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void millager$onMobInteract(Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {
        Villager self = (Villager) (Object) this;
        ItemStack heldItem = player.getItemInHand(interactionHand);
        if (heldItem.getItem() instanceof ProfessionOrderItem professionOrder) {
            professionOrder.interactLivingEntity(heldItem, player, self, interactionHand);
            cir.setReturnValue(InteractionResult.SUCCESS);
            return;
        }
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);

        if (head.is(MillagerItems.VILLAGER_HEAD.asItem()) || head.is(MillagerItems.ILLAGER_HEAD.asItem())) {
            if (self.level() instanceof ServerLevel serverLevel) {
                if (head.is(MillagerItems.VILLAGER_HEAD.asItem())) serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER,
                        self.getX(), self.getY() + (self.isBaby() ? 0.5D : 1.5D), self.getZ(), 5, 0.5D, 0.5D, 0.5D, 0.05D);
                self.playSound(SoundEvents.VILLAGER_NO);
            }
            if (!player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE)) {
                self.setUnhappyCounter(40);
                cir.setReturnValue(InteractionResult.SUCCESS);
            }
        }

        if (self.getVillagerData().getProfession() == VillagerProfession.NITWIT) {
            ItemStack itemStack = player.getItemInHand(interactionHand);
            if (itemStack.is(MillagerItems.elixir.asItem())) {
                player.swing(interactionHand);
                if (!player.getAbilities().instabuild) itemStack.shrink(1);
                if (self.level() instanceof ServerLevel serverLevel) {
                    self.setVillagerData(new VillagerData(
                            self.getVillagerData().getType(),
                            VillagerProfession.NONE,
                            1
                    ));
                    serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, self.getX(), self.getY(), self.getZ(), 60, 0.5, 0.5, 0.5, 0.1);
                    serverLevel.sendParticles(ParticleTypes.GLOW, self.getX(), self.getY(), self.getZ(), 60, 0.5, 0.5, 0.5, 0.1);
                    self.playSound(SoundEvents.VILLAGER_YES);
                    GossipContainer gossipContainer = self.getGossips();
                    gossipContainer.add(player.getUUID(), GossipType.MAJOR_POSITIVE, 20);
                    gossipContainer.add(player.getUUID(), GossipType.MINOR_POSITIVE, 25);
                    millager$hasNearbyElixirHolder = false;
                    self.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                    self.refreshBrain(serverLevel);
                    int count = 12 + serverLevel.getRandom().nextInt(9);
                    BehaviorUtils.throwItem(self, new ItemStack(Items.EMERALD_BLOCK, count), player.position());
                    if (player instanceof ServerPlayer serverPlayer) {
                        MillagerCriteria.HEAL_NITWIT.get().trigger(serverPlayer);
                    }
                }
                cir.setReturnValue(InteractionResult.SUCCESS);
            }
        }
    }

}
