package org.lzyzl.millager.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.MillagerSounds;
import org.lzyzl.millager.entity.millager.AbstractMillager;
import org.lzyzl.millager.entity.millager.Lancer;
import org.lzyzl.millager.util.MillagerTargetingHelper;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class LancerSpearItem extends Item {
    private static final double MOB_MIN_REACH = 1.0D;
    private static final double MOB_MAX_REACH = 2.25D;
    private static final double PLAYER_MIN_REACH = 2.0D;
    private static final double PLAYER_MAX_REACH = 4.5D;
    private static final double CREATIVE_PLAYER_MAX_REACH = 6.5D;
    private static final double HITBOX_MARGIN = 0.125D;
    private static final Map<LivingEntity, Map<LivingEntity, Long>> RECENT_KINETIC_TARGETS = new WeakHashMap<>();

    public enum Material {
        IRON(Tiers.IRON, 0.95F, 0.95F, 0.6F, 2.5F, 8.0F, 6.75F, 11.25F),
        GOLD(Tiers.GOLD, 0.95F, 0.7F, 0.7F, 3.5F, 10.0F, 8.5F, 13.75F),
        DIAMOND(Tiers.DIAMOND, 1.05F, 1.075F, 0.5F, 3.0F, 7.5F, 6.5F, 10.0F);

        private final Tier tier;
        private final float attackDuration;
        private final KineticProfile kineticProfile;

        Material(Tier tier, float attackDuration, float damageMultiplier, float delaySeconds, float dismountSeconds, float dismountSpeed,
                 float knockbackSeconds, float damageSeconds) {
            this.tier = tier;
            this.attackDuration = attackDuration;
            this.kineticProfile = new KineticProfile(delaySeconds, dismountSeconds, dismountSpeed, knockbackSeconds,
                    (float) 5.1, damageSeconds, (float) 4.6, damageMultiplier);
        }

        public Tier tier() {
            return this.tier;
        }

        public float attackDuration() {
            return this.attackDuration;
        }

        public KineticProfile kineticProfile() {
            return this.kineticProfile;
        }
    }

    private final Material material;
    private final Multimap<Attribute, AttributeModifier> attributes;

    public LancerSpearItem(Material material, Properties properties) {
        super(properties);
        this.material = material;
        this.attributes = ImmutableMultimap.of(
                Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier",
                        material.tier().getAttackDamageBonus(), AttributeModifier.Operation.ADDITION),
                Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier",
                        1.0D / material.attackDuration() - 4.0D, AttributeModifier.Operation.ADDITION));
    }

    public int getDamageUseDuration() {
        return this.material.kineticProfile().damageUseDuration();
    }

    public KineticProfile getKineticProfile() {
        return this.material.kineticProfile();
    }

    @Override
    public int getEnchantmentValue() {
        return 0;
    }

    @Override
    public boolean isEnchantable(@NonNull ItemStack stack) {
        return false;
    }

    @Override
    public boolean isValidRepairItem(@NonNull ItemStack stack, @NonNull ItemStack repairCandidate) {
        return this.material.tier().getRepairIngredient().test(repairCandidate) || super.isValidRepairItem(stack, repairCandidate);
    }

    @Override
    public @NonNull Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(@NonNull EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? this.attributes : super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public int getUseDuration(@NonNull ItemStack stack) {
        return 72000;
    }

    @Override
    public void onUseTick(@NonNull Level level, @NonNull LivingEntity user, @NonNull ItemStack stack, int timeLeft) {
        if (level.isClientSide()) return;
        KineticProfile profile = this.material.kineticProfile();
        int useTicks = this.getUseDuration(stack) - timeLeft;
        if (useTicks < profile.delayTicks()) return;
        int kineticTicks = useTicks - profile.delayTicks();
        Vec3 look = user.getLookAngle();
        Vec3 attackerMotion = getKineticMotion(user);
        double attackerSpeed = look.dot(attackerMotion);
        double speedMultiplier = user instanceof Player ? 1.0D : 0.2D;
        for (LivingEntity target : this.getHitEntities(user, look)) {
            Vec3 targetMotion = getKineticMotion(target);
            double relativeSpeed = Math.max(0.0D, attackerSpeed - look.dot(targetMotion));
            boolean canDismount = profile.canDismount(kineticTicks, attackerSpeed, speedMultiplier);
            boolean canKnockback = profile.canKnockback(kineticTicks, attackerSpeed, speedMultiplier);
            boolean canDamage = profile.canDamage(kineticTicks, relativeSpeed, speedMultiplier);
            if (!canDismount && !canKnockback && !canDamage) continue;
            if (!rememberKineticTarget(user, target, level.getGameTime())) continue;
            float damage = (float) user.getAttributeBaseValue(Attributes.ATTACK_DAMAGE)
                    + Mth.floor((float) (relativeSpeed * profile.damageMultiplier()));
            boolean hit = canDamage && target.hurt(user.damageSources().mobAttack(user), damage);
            if (canKnockback) {
                hit = true;
                target.knockback((float) Math.max(0.25D, relativeSpeed * 0.1D), -look.x, -look.z);
            }
            if (canDismount && target.isPassenger()) {
                hit = true;
                target.stopRiding();
            }
            if (!hit) continue;
            target.playSound(MillagerSounds.LANCER_SPEAR_HIT, 1.0F, 1.0F);
            if (user instanceof Lancer lancer) lancer.onSpearHit();
        }
    }

    @Override
    public boolean hurtEnemy(@NonNull ItemStack stack, @NonNull LivingEntity target, @NonNull LivingEntity attacker) {
        target.playSound(MillagerSounds.LANCER_SPEAR_HIT, 1.0F, 1.0F);
        for (LivingEntity piercedTarget : this.getHitEntities(attacker, attacker.getLookAngle())) {
            if (piercedTarget != target) piercedTarget.hurt(attacker.damageSources().mobAttack(attacker),
                    (float) attacker.getAttributeValue(Attributes.ATTACK_DAMAGE));
        }
        return true;
    }

    private List<LivingEntity> getHitEntities(LivingEntity user, Vec3 look) {
        Vec3 eyePosition = user.getEyePosition();
        double forwardMovement = Math.max(0.0D, look.dot(getKineticMotion(user)) / 20.0D);
        double minReach = user instanceof Player ? PLAYER_MIN_REACH : MOB_MIN_REACH;
        double maxReach = user instanceof Player player && player.isCreative()
                ? CREATIVE_PLAYER_MAX_REACH : user instanceof Player ? PLAYER_MAX_REACH : MOB_MAX_REACH;
        Vec3 start = eyePosition.add(look.scale(minReach));
        Vec3 end = eyePosition.add(look.scale(maxReach + forwardMovement));
        AABB searchBox = new AABB(start, start).expandTowards(end.subtract(start)).inflate(1.0D);
        return user.level().getEntitiesOfClass(LivingEntity.class, searchBox,
                target -> this.canHit(user, target, start, end));
    }

    private boolean canHit(LivingEntity user, LivingEntity target, Vec3 start, Vec3 end) {
        return target != user && (user instanceof AbstractMillager millager ? MillagerTargetingHelper.canAttack(millager, target) : user.canAttack(target))
                && !user.isAlliedTo(target) && user.hasLineOfSight(target)
                && target.getBoundingBox().inflate(HITBOX_MARGIN).clip(start, end).isPresent();
    }

    private static Vec3 getKineticMotion(Entity entity) {
        if (!(entity instanceof Player) && entity.isPassenger()) entity = entity.getRootVehicle();
        return entity.getDeltaMovement().scale(20.0D);
    }

    private static boolean rememberKineticTarget(LivingEntity user, LivingEntity target, long gameTime) {
        Map<LivingEntity, Long> targets = RECENT_KINETIC_TARGETS.computeIfAbsent(user, key -> new WeakHashMap<>());
        Long lastContact = targets.get(target);
        if (lastContact != null && gameTime - lastContact < 10L) return false;
        targets.put(target, gameTime);
        return true;
    }

    @Override
    public @NonNull InteractionResultHolder<ItemStack> use(@NonNull Level level, @NonNull Player player,
                                                            @NonNull InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    public record KineticProfile(int delayTicks, int dismountTicks, float dismountSpeed, int knockbackTicks,
                                 float knockbackSpeed, int damageTicks, float relativeDamageSpeed,
                                 float forwardMovement, float damageMultiplier) {
        private KineticProfile(float delaySeconds, float dismountSeconds, float dismountSpeed, float knockbackSeconds,
                               float knockbackSpeed, float damageSeconds, float relativeDamageSpeed,
                               float damageMultiplier) {
            this((int) (delaySeconds * 20.0F), (int) (dismountSeconds * 20.0F), dismountSpeed,
                    (int) (knockbackSeconds * 20.0F), knockbackSpeed, (int) (damageSeconds * 20.0F),
                    relativeDamageSpeed, 0.38F, damageMultiplier);
        }

        public int damageUseDuration() {
            return this.delayTicks + this.damageTicks;
        }

        private boolean canDismount(int useTicks, double speed, double speedMultiplier) {
            return useTicks <= this.dismountTicks && speed >= this.dismountSpeed * speedMultiplier;
        }

        private boolean canKnockback(int useTicks, double speed, double speedMultiplier) {
            return useTicks <= this.knockbackTicks && speed >= this.knockbackSpeed * speedMultiplier;
        }

        private boolean canDamage(int useTicks, double relativeSpeed, double speedMultiplier) {
            return useTicks <= this.damageTicks && relativeSpeed >= this.relativeDamageSpeed * speedMultiplier;
        }
    }
}
