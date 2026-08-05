package org.lzyzl.millager.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PlayerRideable;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.lzyzl.millager.MillagerBlocks;
import org.lzyzl.millager.MillagerGameRules;
import org.lzyzl.millager.entity.golem.BeeGolem;
import org.lzyzl.millager.entity.millager.AbstractMillager;
import org.lzyzl.millager.entity.millager.Rioter;
import org.jspecify.annotations.Nullable;
import org.joml.Vector3f;

import java.util.function.IntFunction;
import java.util.List;
import java.util.UUID;

import static org.lzyzl.millager.Millager.MOD_ID;

public class MiscHelper {

    public static AABB getMillagerCollisionBox(AbstractMillager entity) {
        Entity root = entity.getRootVehicle();
        AABB body = entity.getBoundingBox();
        AABB mount = root.getBoundingBox();
        return new AABB(
                Math.min(body.minX, mount.minX), Math.min(body.minY, mount.minY), Math.min(body.minZ, mount.minZ),
                Math.max(body.maxX, mount.maxX), Math.max(body.maxY, mount.maxY), Math.max(body.maxZ, mount.maxZ)
        );
    }

    public static Mob getMillagerMover(AbstractMillager millager) {
        return millager.getVehicle() instanceof Horse horse && horse.isAlive() ? horse : millager;
    }

    public static PathNavigation getMillagerNavigation(AbstractMillager millager) {
        return getMillagerMover(millager).getNavigation();
    }

    public static @Nullable LivingEntity updateMillagerSpeedModifier(AbstractMillager millager, @Nullable LivingEntity modifiedEntity, UUID modifierId, String modifierName) {
        LivingEntity current = getMillagerMover(millager);
        if (current == modifiedEntity) return modifiedEntity;
        removeMillagerSpeedModifier(modifiedEntity, modifierId);
        AttributeInstance speed = current.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) return null;
        speed.removeModifier(modifierId);
        speed.addTransientModifier(new AttributeModifier(modifierId, modifierName, 0.15D, AttributeModifier.Operation.MULTIPLY_BASE));
        return current;
    }

    public static void removeMillagerSpeedModifier(@Nullable LivingEntity modifiedEntity, UUID modifierId) {
        if (modifiedEntity == null) return;
        AttributeInstance speed = modifiedEntity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) speed.removeModifier(modifierId);
    }

    //Custom Explorer Map
    //Requires pre-build tag JSON
    public static TagKey<Structure> createStructureKey(String name) {
        return TagKey.create(Registries.STRUCTURE, ResourceLocationHelper.create(MOD_ID, name));
    }

    public static ItemStack createExplorerMap(String path, TagKey<Structure> key , MapDecoration.Type decorationType, ServerLevel world, Entity entity) {
        BlockPos aimPos = world.findNearestMapStructure(key, entity.blockPosition(), 100, true);
        if (aimPos == null) {
            ItemStack itemStack = MapItem.create(world,entity.getBlockX(),entity.getBlockZ(), (byte)2,true,true);
            MapItem.renderBiomePreviewMap(world, itemStack);
            MapItemSavedData.addTargetDecoration(itemStack, entity.getOnPos(), "+", MapDecoration.Type.TARGET_POINT);
            itemStack.setHoverName(Component.translatable("filled_map.near"));
            return itemStack;
        }
        ItemStack itemStack = MapItem.create(world, aimPos.getX(), aimPos.getZ(), (byte)2, true, true);
        MapItem.renderBiomePreviewMap(world, itemStack);
        MapItemSavedData.addTargetDecoration(itemStack, aimPos, "+", decorationType);
        itemStack.setHoverName(Component.translatable("filled_map." + path));
        return itemStack;
    }

    public static ItemStack getVariantPainting(ResourceKey<PaintingVariant> key) {
        ItemStack stack = new ItemStack(Items.PAINTING);
        CompoundTag entityData = stack.getOrCreateTagElement("EntityTag");
        entityData.putString("id", "minecraft:painting");
        entityData.putString("variant", key.location().toString());
        return stack;
    }

    /**
     * 将实体的角度平滑地转向目标角度
     * @param current 当前角度
     * @param target 目标角度
     * @param maxDelta 本次更新允许的最大旋转步长 (度)
     */
    public static float rotateTowards(float current, float target, float maxDelta) {
        float f = Mth.wrapDegrees(target - current);
        if (f > maxDelta) f = maxDelta;
        if (f < -maxDelta) f = -maxDelta;
        return current + f;
    }

    public static boolean isMillagerFaction(Object entity) {
        if (entity == null) return false;

        if(entity instanceof IronGolem golem) return !MultigolemDetector.isZombieGolem(golem);

        if (entity instanceof AbstractMillager || entity instanceof Villager ||
                 (entity instanceof BeeGolem bee && bee.isSummoned())) {
            return true;
        }

        if (entity.getClass().getCanonicalName() != null &&
                entity.getClass().getCanonicalName().contains("guardvillagers")) {
            return true;
        }

        if (entity instanceof Horse horse) {
            return horse.getTags().contains("millager_mount");
        }

        return false;
    }

    public static boolean isAllyCaused(DamageSource damageSource) {
        Entity source = damageSource.getEntity();
        if(source == null) source = damageSource.getDirectEntity();
        return isMillagerFaction(source);
    }

    public static void performFireExplosion(ThrowableItemProjectile projectile, ServerLevel level, Vec3 pos, float radius, int color, float instantDamage, float spawnChance) {
        performFireExplosion(projectile, level, pos, false, radius, color, instantDamage, spawnChance);
    }

    public static void performFireExplosion(ThrowableItemProjectile projectile, ServerLevel level, Vec3 pos, boolean isRioterProjectile, float radius, int color, float instantDamage, float spawnChance) {

        IntFunction<Vector3f> colorToVec = c -> new Vector3f(
                (float)(c >> 16 & 0xFF) / 255.0F,
                (float)(c >> 8 & 0xFF) / 255.0F,
                (float)(c & 0xFF) / 255.0F
        );
        DustParticleOptions PARTICLE = new DustParticleOptions(colorToVec.apply(color), instantDamage - 1);
        level.sendParticles(PARTICLE, pos.x, pos.y, pos.z, (int) (instantDamage * 50), radius/2, radius/2, radius/2, instantDamage/2.5);
        level.sendParticles(new DustParticleOptions(new Vector3f(
                        (float)(4605510 >> 16 & 0xFF) / 255.0F,
                        (float)(4605510 >> 8 & 0xFF) / 255.0F,
                        (float)(4605510 & 0xFF) / 255.0F
                ), instantDamage/2), pos.x, pos.y, pos.z, (int) (instantDamage * 50), radius/2, radius/2, radius/2, instantDamage/2.5);

        level.playSound(null, projectile.blockPosition(), SoundEvents.GLASS_BREAK, projectile.getSoundSource(), radius/2, 1f);//G:speed
        level.playSound(null, projectile.blockPosition(), SoundEvents.FIRECHARGE_USE, projectile.getSoundSource(), radius, 1.65f);

        AABB area = new AABB(pos.x - radius, pos.y - radius, pos.z - radius, pos.x + radius, pos.y + radius, pos.z + radius);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area, entity -> !(entity instanceof ArmorStand));

        for (LivingEntity entity : targets) {
            BlockHitResult hit = level.clip(new ClipContext(pos, entity.getEyePosition(),
                    ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, entity));

            if (hit.getType() != HitResult.Type.BLOCK) {
                entity.hurt(level.damageSources().explosion(projectile, projectile.getOwner()), instantDamage);
                if(isRioterProjectile) {
                    boolean canIgnite = !isMillagerFaction(entity) || level.getGameRules().getRule(MillagerGameRules.FRIENDLY_FIRE).get();
                    if(projectile.getOwner() instanceof Rioter rioter) {
                        LivingEntity target = rioter.getTarget();
                        if(target != null && entity.getUUID().equals(target.getUUID()) && canIgnite) entity.setSecondsOnFire(Mth.ceil(instantDamage * 2 + 2));
                        else if(!(entity instanceof Player) && canIgnite) entity.setSecondsOnFire(Mth.ceil(instantDamage * 2 + 2));
                    }
                    else if(!(entity instanceof Player) && canIgnite) entity.setSecondsOnFire(Mth.ceil(instantDamage * 2 + 2));
                }
                else entity.setSecondsOnFire(Mth.ceil(instantDamage * 2 + 1));
            } else {
                if(!isRioterProjectile && level.getBlockState(hit.getBlockPos()).getBlock().getExplosionResistance() < instantDamage) {
                    level.setBlockAndUpdate(hit.getBlockPos(), MillagerBlocks.TIMED_FIRE.get().defaultBlockState());
                }
            }
        }

        if(isRioterProjectile) return;

        BlockPos centerPos = BlockPos.containing(pos);

        double verticalRadius = radius * 0.5;

        double hRadiusSq = (double) radius * (double) radius;
        double vRadiusSq = verticalRadius * verticalRadius;

        int hR = (int) Math.ceil(radius);
        int vR = (int) Math.ceil(verticalRadius);

        for (BlockPos currentPos : BlockPos.betweenClosed(
                centerPos.offset(-hR, -vR, -hR),
                centerPos.offset(hR, vR, hR))) {

            double dx = currentPos.getX() - centerPos.getX();
            double dy = currentPos.getY() - centerPos.getY();
            double dz = currentPos.getZ() - centerPos.getZ();

            double ellipseValue = (dx * dx / hRadiusSq) + (dy * dy / vRadiusSq) + (dz * dz / hRadiusSq);

            if (ellipseValue <= 1.0) {
                BlockState currentState = level.getBlockState(currentPos);

                if (currentState.isAir()) {
                    float chance = level.getRandom().nextFloat();

                    if (chance < spawnChance) {
                        level.setBlockAndUpdate(currentPos, MillagerBlocks.TIMED_FIRE.get().defaultBlockState());
                    }
                    else if (chance < (spawnChance - 0.4f)) {
                        level.setBlockAndUpdate(currentPos, Blocks.FIRE.defaultBlockState());
                    }
                }
            }
        }
    }

}
