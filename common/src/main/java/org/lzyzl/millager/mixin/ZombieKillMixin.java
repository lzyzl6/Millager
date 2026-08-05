package org.lzyzl.millager.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.gossip.GossipContainer;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerType;
import net.minecraft.world.item.trading.MerchantOffers;
import org.lzyzl.millager.entity.millager.AbstractMillager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Zombie.class)
public class ZombieKillMixin {

    @Inject(
            method = "killedEntity",
            at = @At("TAIL"),
            cancellable = true
    )
    private void millager$handleCustomInfectable(
            ServerLevel level, LivingEntity livingEntity, DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        if (!(livingEntity instanceof AbstractMillager target)) return;
        if (level.getDifficulty() != Difficulty.HARD) return;
        if (level.getRandom().nextBoolean()) return;
        Zombie self = (Zombie) (Object) this;
        ZombieVillager converted = target.convertTo(
                EntityTypes.ZOMBIE_VILLAGER,
                ConversionParams.single(target, true, true),
                (zombieVillager) -> {
                    zombieVillager.finalizeSpawn(level, level.getCurrentDifficultyAt(zombieVillager.blockPosition()), EntitySpawnReason.CONVERSION, new Zombie.ZombieGroupData(false, true));
                    zombieVillager.setVillagerData(Villager.createDefaultVillagerData().withProfession(level.registryAccess(), VillagerProfession.NITWIT).withType(level.registryAccess(), VillagerType.byBiome(level.getBiome(self.blockPosition()))));
                    zombieVillager.setGossips(new GossipContainer());
                    zombieVillager.setTradeOffers(new MerchantOffers());
                    zombieVillager.setVillagerXp(0);
                    if (!self.isSilent()) {
                        level.levelEvent(null, 1026, self.blockPosition(), 0);
                    }
                }
        );
        if (converted != null) cir.setReturnValue(false);
    }
}