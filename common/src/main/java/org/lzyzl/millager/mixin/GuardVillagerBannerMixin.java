package org.lzyzl.millager.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jspecify.annotations.Nullable;
import org.lzyzl.millager.behavior.MiscConfig;
import org.lzyzl.millager.util.VillageBannerHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class GuardVillagerBannerMixin {

    @Unique
    private boolean millager$pendingVillageBanner;

    @Inject(method = "finalizeSpawn", at = @At("TAIL"))
    private void millager$rollVillageBanner(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnReason,
                                            @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag tag,
                                            CallbackInfoReturnable<SpawnGroupData> cir) {
        Mob guard = (Mob) (Object) this;
        if (millager$isGuardVillager(guard)) guard.setCanPickUpLoot(true);
        this.millager$pendingVillageBanner = millager$isGuardVillager(guard)
                && guard.getRandom().nextInt(100) < MiscConfig.GUARD_VILLAGER_BANNER_SPAWN_CHANCE;
    }

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void millager$equipVillageBanner(CallbackInfo ci) {
        if (!this.millager$pendingVillageBanner) return;
        this.millager$pendingVillageBanner = false;
        Mob guard = (Mob) (Object) this;
        if (millager$isGuardVillager(guard)) guard.setCanPickUpLoot(true);
        if (guard.getMainHandItem().is(Items.SHIELD)) {
            guard.setItemSlot(EquipmentSlot.MAINHAND, VillageBannerHelper.createShield());
        } else if (guard.getOffhandItem().is(Items.SHIELD)) {
            guard.setItemSlot(EquipmentSlot.OFFHAND, VillageBannerHelper.createShield());
        } else if (guard.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
            guard.setItemSlot(EquipmentSlot.HEAD, VillageBannerHelper.create());
        }
    }

    @Unique
    private static boolean millager$isGuardVillager(Mob mob) {
        return "guardvillagers:guard".equals(BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).toString());
    }
}
