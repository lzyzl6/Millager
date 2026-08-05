package org.lzyzl.millager.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
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
            ServerLevel level, LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (!(entity instanceof AbstractMillager target)) return;
        if (level.getDifficulty() != Difficulty.HARD) return;
        if (level.getRandom().nextBoolean()) return;
        Zombie self = (Zombie) (Object) this;
        ZombieVillager converted = target.convertTo(EntityType.ZOMBIE_VILLAGER, true);
        if (converted != null) {
            converted.finalizeSpawn(level, level.getCurrentDifficultyAt(converted.blockPosition()), MobSpawnType.CONVERSION, new Zombie.ZombieGroupData(false, true), null);
            converted.setVillagerData(new VillagerData(
                    VillagerType.byBiome(level.getBiome(converted.blockPosition())),
                    VillagerProfession.NITWIT,
                    1
            ));
            converted.setTradeOffers(new MerchantOffers().createTag());
            converted.setVillagerXp(0);
            if (!self.isSilent()) {
                level.levelEvent(null, 1026, self.blockPosition(), 0);
            }
            cir.setReturnValue(false);
        }
    }
}
