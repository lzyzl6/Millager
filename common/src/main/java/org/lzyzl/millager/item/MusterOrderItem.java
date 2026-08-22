package org.lzyzl.millager.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.MillagerSounds;
import org.lzyzl.millager.behavior.MillagerEntityPool;
import org.lzyzl.millager.behavior.raid.DefenderConfig;
import org.lzyzl.millager.entity.millager.AbstractMillager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MusterOrderItem extends Item {

    private static final int USE_DURATION = 60;
    private static final int START_SOUND_DELAY_TICKS = 20;
    private static final int COOLDOWN_TICKS = 10;
    private static final int MIN_SPACING_SQ = 9;
    private static final int MAX_SEARCH_RADIUS = 8;
    private static final double MIN_PLAYER_DISTANCE_SQ = 4.0;

    private final Variant variant;

    public MusterOrderItem(Variant variant, Properties properties) {
        super(properties);
        this.variant = variant;
    }

    @Override
    public @NonNull InteractionResult use(@NonNull Level level, @NonNull Player player, @NonNull InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUseTick(@NonNull Level level, @NonNull LivingEntity livingEntity, @NonNull ItemStack stack,
                          int remainingUseDuration) {
        if (!level.isClientSide() && remainingUseDuration == USE_DURATION - START_SOUND_DELAY_TICKS) {
            level.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(),
                    MillagerSounds.MUSTER_ORDER_START, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    @Override
    public @NonNull ItemStack finishUsingItem(@NonNull ItemStack stack, @NonNull Level level,
                                               @NonNull LivingEntity livingEntity) {
        if (livingEntity instanceof Player player && level instanceof ServerLevel serverLevel) {
            summonSquad(serverLevel, player);
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    MillagerSounds.MUSTER_ORDER_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);
            player.awardStat(Stats.ITEM_USED.get(this));
            player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
            stack.consume(1, player);
        }
        return stack;
    }

    @Override
    public int getUseDuration(@NonNull ItemStack stack, @NonNull LivingEntity livingEntity) {
        return USE_DURATION;
    }

    @Override
    public @NonNull ItemUseAnimation getUseAnimation(@NonNull ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    private void summonSquad(ServerLevel level, Player player) {
        boolean cavalry = variant == Variant.CAVALRY
                || variant == Variant.RANDOM && level.getRandom().nextBoolean();
        List<MillagerEntityPool.Entry> pool = cavalry ? MillagerEntityPool.CAVALRY : MillagerEntityPool.INFANTRY;
        int count = DefenderConfig.SQUAD_MIN_SIZE + level.getRandom().nextInt(
                DefenderConfig.SQUAD_MAX_SIZE - DefenderConfig.SQUAD_MIN_SIZE + 1);
        List<BlockPos> usedPositions = new ArrayList<>();
        UUID squadId = UUID.randomUUID();

        for (int i = 0; i < count; i++) {
            MillagerEntityPool.Entry entry = MillagerEntityPool.weightedPick(pool, level.getRandom());
            if (entry == null) break;
            BlockPos pos = findSpawnPos(level, player, usedPositions, cavalry);
            AbstractMillager entity = entry.type().create(level, EntitySpawnReason.EVENT);
            if (entity == null) continue;

            entity.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            entity.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.EVENT, null);
            boolean leader = usedPositions.isEmpty();
            entity.setSquad(squadId, false);
            if (leader) {
                entity.promoteToSquadLeader();
                entity.equipSquadLeaderBanner();
            }
            level.addFreshEntityWithPassengers(entity);
            usedPositions.add(pos);
            level.sendParticles(ParticleTypes.POOF, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5,
                    12, 0.35, 0.6, 0.35, 0.03);
        }
    }

    private BlockPos findSpawnPos(ServerLevel level, Player player, List<BlockPos> usedPositions, boolean cavalry) {
        BlockPos pos = findNearbySpawnPos(level, player, usedPositions, MIN_SPACING_SQ, cavalry);
        if (pos == null) pos = findNearbySpawnPos(level, player, usedPositions, 4, cavalry);
        if (pos == null) pos = findNearbySpawnPos(level, player, usedPositions, 1, cavalry);
        return pos != null ? pos : player.blockPosition();
    }

    private BlockPos findNearbySpawnPos(ServerLevel level, Player player, List<BlockPos> usedPositions,
                                         int spacingSq, boolean cavalry) {
        BlockPos playerPos = player.blockPosition();
        int minimumY = Mth.ceil(player.getY() - 1.5);

        for (int radius = 1; radius <= MAX_SEARCH_RADIUS; radius++) {
            int perimeter = radius * 8;
            int start = level.getRandom().nextInt(perimeter);
            for (int step = 0; step < perimeter; step++) {
                int index = (start + step) % perimeter;
                int sideLength = radius * 2;
                int side = index / sideLength;
                int sideOffset = index % sideLength;
                int dx = side == 0 ? -radius + sideOffset
                        : side == 1 ? radius
                        : side == 2 ? radius - sideOffset : -radius;
                int dz = side == 0 ? -radius
                        : side == 1 ? -radius + sideOffset
                        : side == 2 ? radius : radius - sideOffset;
                int x = playerPos.getX() + dx;
                int z = playerPos.getZ() + dz;
                double playerDx = x + 0.5 - player.getX();
                double playerDz = z + 0.5 - player.getZ();
                if (playerDx * playerDx + playerDz * playerDz < MIN_PLAYER_DISTANCE_SQ) continue;
                int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);

                for (int y = Math.min(surfaceY, playerPos.getY()); y >= minimumY; y--) {
                    BlockPos candidate = new BlockPos(x, y, z);
                    if (!isValidGround(level, candidate)) continue;
                    if (usedPositions.stream().anyMatch(used -> horizontalDistSqr(used, candidate) < spacingSq)) continue;
                    if (cavalry && hasCavalrySpawnObstruction(level, candidate)) continue;
                    return candidate;
                }
            }
        }
        return null;
    }

    private boolean hasCavalrySpawnObstruction(ServerLevel level, BlockPos pos) {
        AABB box = EntityTypes.HORSE.getDimensions().makeBoundingBox(
                pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D).inflate(
                DefenderConfig.CAVALRY_SPAWN_CLEARANCE, 0.0D, DefenderConfig.CAVALRY_SPAWN_CLEARANCE);
        return !level.noCollision(box);
    }

    private int horizontalDistSqr(BlockPos first, BlockPos second) {
        int dx = first.getX() - second.getX();
        int dz = first.getZ() - second.getZ();
        return dx * dx + dz * dz;
    }

    private boolean isValidGround(ServerLevel level, BlockPos pos) {
        BlockPos floorPos = pos.below();
        BlockState floor = level.getBlockState(floorPos);
        if (floor.is(BlockTags.LEAVES) || floor.is(BlockTags.LOGS)) return false;
        if (!floor.getFluidState().isEmpty() || !floor.isFaceSturdy(level, floorPos, Direction.UP)) return false;

        for (int dy = 0; dy < 3; dy++) {
            BlockPos openPos = pos.above(dy);
            BlockState state = level.getBlockState(openPos);
            if (!state.getFluidState().isEmpty() || !state.getCollisionShape(level, openPos).isEmpty()) return false;
        }
        return true;
    }

    public enum Variant {
        INFANTRY,
        CAVALRY,
        RANDOM
    }
}
