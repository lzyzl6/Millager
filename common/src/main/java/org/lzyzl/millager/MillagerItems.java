package org.lzyzl.millager;

import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.UseEffects;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.waypoints.Waypoint;
import org.lzyzl.millager.item.*;
import org.lzyzl.millager.registry.DeferredHolder;
import org.lzyzl.millager.registry.DeferredItem;
import org.lzyzl.millager.registry.DeferredRegister;
import org.lzyzl.millager.util.ResourceVariantHelper;

import java.util.List;
import java.util.Optional;

import static net.minecraft.world.item.component.Consumables.defaultDrink;

public class MillagerItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Millager.MOD_ID);
    public static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(BuiltInRegistries.POTION, Millager.MOD_ID);

    private static final FoodProperties LIQUOR = new FoodProperties.Builder()
            .nutrition(3).saturationModifier(0.1F).alwaysEdible().build();
    private static final Consumable LIQUOR_C = Consumable.builder()
            .consumeSeconds(2.5F).animation(ItemUseAnimation.DRINK)
            .sound(MillagerSounds.QUAFFING).hasConsumeParticles(false).build();

    private static final FoodProperties ELIXIR = new FoodProperties.Builder()
            .nutrition(4).saturationModifier(1.2F).alwaysEdible().build();
    private static final Consumable ELIXIR_C = defaultDrink().onConsume(new ApplyStatusEffectsConsumeEffect(
            List.of(new MobEffectInstance(MobEffects.STRENGTH, 36000, 0),
                    new MobEffectInstance(MobEffects.SATURATION, 36000, 0),
                    new MobEffectInstance(MobEffects.JUMP_BOOST, 36000, 0),
                    new MobEffectInstance(MobEffects.SPEED, 36000, 0),
                    new MobEffectInstance(MobEffects.HASTE, 36000, 0),
                    new MobEffectInstance(MobEffects.WATER_BREATHING, 36000, 0),
                    new MobEffectInstance(MobEffects.NIGHT_VISION, 36000, 0),
                    new MobEffectInstance(MobEffects.LUCK, 72000, 4)))).build();

    public static final DeferredHolder<Potion, Potion> ASCENSION = POTIONS.register("ascension",
            () -> new Potion("ascension",
                    new MobEffectInstance(MobEffects.LEVITATION, 400),
                    new MobEffectInstance(MobEffects.SLOW_FALLING, 660),
                    new MobEffectInstance(MobEffects.INVISIBILITY, 250),
                    new MobEffectInstance(MobEffects.REGENERATION, 200,1)
            )
    );

    public static final DeferredHolder<Potion, Potion> LONG_ASCENSION = POTIONS.register("long_ascension",
            () -> new Potion("ascension",
                    new MobEffectInstance(MobEffects.LEVITATION, 800),
                    new MobEffectInstance(MobEffects.SLOW_FALLING, 1320),
                    new MobEffectInstance(MobEffects.INVISIBILITY, 450),
                    new MobEffectInstance(MobEffects.REGENERATION, 400,1)
            )
    );

    public static final DeferredItem<GolemAmber> golemAmber = ITEMS.registerItem(
            "golem_amber", GolemAmber::new, () -> new Item.Properties().rarity(Rarity.UNCOMMON));

    public static final DeferredItem<ProfessionOrderItem> archerProfessionOrder = ITEMS.registerItem(
            "archer_profession_order", props -> new ProfessionOrderItem(MillagerEntityTypes.Archers,
                    ProfessionOrderItem.Equipment.DEFAULT, props));

    public static final DeferredItem<ProfessionOrderItem> doctorProfessionOrder = ITEMS.registerItem(
            "doctor_profession_order", props -> new ProfessionOrderItem(MillagerEntityTypes.Doctors,
                    ProfessionOrderItem.Equipment.DEFAULT, props));

    public static final DeferredItem<ProfessionOrderItem> swordmasterProfessionOrder = ITEMS.registerItem(
            "swordmaster_profession_order", props -> new ProfessionOrderItem(MillagerEntityTypes.Swordmasters,
                    ProfessionOrderItem.Equipment.IRON_SWORDS, props));

    public static final DeferredItem<ProfessionOrderItem> scouterProfessionOrder = ITEMS.registerItem(
            "scouter_profession_order", props -> new ProfessionOrderItem(MillagerEntityTypes.Scouters,
                    ProfessionOrderItem.Equipment.IRON_HORSE_ARMOR, props));

    public static final DeferredItem<ProfessionOrderItem> lancerProfessionOrder = ITEMS.registerItem(
            "lancer_profession_order", props -> new ProfessionOrderItem(MillagerEntityTypes.Lancers,
                    ProfessionOrderItem.Equipment.IRON_SPEAR_AND_HORSE_ARMOR, props));

    public static final DeferredItem<ProfessionOrderItem> maulerProfessionOrder = ITEMS.registerItem(
            "mauler_profession_order", props -> new ProfessionOrderItem(MillagerEntityTypes.Maulers,
                    ProfessionOrderItem.Equipment.DEFAULT, props));

    public static final DeferredItem<ProfessionOrderItem> rioterProfessionOrder = ITEMS.registerItem(
            "rioter_profession_order", props -> new ProfessionOrderItem(MillagerEntityTypes.Rioters,
                    ProfessionOrderItem.Equipment.DEFAULT, props));

    public static final DeferredItem<ProfessionOrderItem> breacherProfessionOrder = ITEMS.registerItem(
            "breacher_profession_order", props -> new ProfessionOrderItem(MillagerEntityTypes.Breachers,
                    ProfessionOrderItem.Equipment.IRON_AXE_AND_HORSE_ARMOR, props));

    public static final DeferredItem<MusterOrderItem> infantryMusterOrder = ITEMS.registerItem(
            "infantry_muster_order", props -> new MusterOrderItem(MusterOrderItem.Variant.INFANTRY, props),
            () -> new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<MusterOrderItem> cavalryMusterOrder = ITEMS.registerItem(
            "cavalry_muster_order", props -> new MusterOrderItem(MusterOrderItem.Variant.CAVALRY, props),
            () -> new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<MusterOrderItem> randomMusterOrder = ITEMS.registerItem(
            "random_muster_order", props -> new MusterOrderItem(MusterOrderItem.Variant.RANDOM, props),
            () -> new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<ExplosiveArrowItem> explosiveArrow = ITEMS.registerItem(
            "explosive_arrow", ExplosiveArrowItem::new, Item.Properties::new);

    public static final DeferredItem<TNTOnAStickItem> tntOnAStick = ITEMS.registerItem(
            "tnt_on_a_stick", TNTOnAStickItem::new, Item.Properties::new);

    public static final DeferredItem<Liquor> liquor = ITEMS.registerItem(
            "liquor", Liquor::new,
            () -> new Item.Properties().food(LIQUOR, LIQUOR_C).usingConvertsTo(Items.GLASS_BOTTLE).stacksTo(16));

    public static final DeferredItem<MolotovCocktailItem> molotovCocktail = ITEMS.registerItem(
            "molotov_cocktail", MolotovCocktailItem::new,
            () -> new Item.Properties().stacksTo(16).useCooldown(1.0f));

    public static final DeferredItem<MolotovCocktailPlusItem> molotovCocktailPlus = ITEMS.registerItem(
            "molotov_cocktail_plus", MolotovCocktailPlusItem::new,
            () -> new Item.Properties().stacksTo(16).useCooldown(1.0f).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<Elixir> elixir = ITEMS.registerItem(
            "elixir", Elixir::new, () -> new Item.Properties().food(ELIXIR, ELIXIR_C).rarity(Rarity.RARE));

    public static final DeferredItem<HealingTotem> totemOfHealing = ITEMS.registerItem(
            "totem_of_healing", HealingTotem::new, () -> new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<CrackedHealingTotem> crackedTotemOfHealing = ITEMS.registerItem(
            "cracked_totem_of_healing", CrackedHealingTotem::new, () -> new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<DamagedHealingTotem> damagedTotemOfHealing = ITEMS.registerItem(
            "damaged_totem_of_healing", DamagedHealingTotem::new, () -> new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<ShieldItem> buckler = ITEMS.registerItem(
            "buckler", ShieldItem::new, () -> new Item.Properties().durability(255)
                    .repairable(ItemTags.WOODEN_TOOL_MATERIALS).equippableUnswappable(EquipmentSlot.OFFHAND)
                    .component(DataComponents.BLOCKS_ATTACKS, new BlocksAttacks(
                            0.15F,
                            0.6F,
                            List.of(new BlocksAttacks.DamageReduction(90.0F, Optional.empty(), 0.0F, 1.0F)),
                            new BlocksAttacks.ItemDamageFunction(2.5F, 1.0F, 1.0F),
                            Optional.of(DamageTypeTags.BYPASSES_SHIELD),
                            Optional.of(SoundEvents.SHIELD_BLOCK),
                            Optional.of(SoundEvents.SHIELD_BREAK)
                    ))
                    .component(DataComponents.USE_EFFECTS, new UseEffects(true, true, 1.0F))
                    .component(DataComponents.BREAK_SOUND, SoundEvents.SHIELD_BREAK));

    public static final DeferredItem<BlockItem> rose = ITEMS.registerItem(
            "rose", props -> new BlockItem(MillagerBlocks.ROSE.get(), props));

    public static final DeferredItem<Item> VILLAGER_BANNER_PATTERN = ITEMS.registerItem(
            "villager_banner_pattern", Item::new,
            () -> new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)
                    .component(DataComponents.PROVIDES_BANNER_PATTERNS, ResourceVariantHelper.PATTERN_ITEM_VILLAGER));

    // 头颅方块物品
    public static final DeferredItem<StandingAndWallBlockItem> VILLAGER_HEAD = ITEMS.registerItem(
            "villager_head",
            props -> new StandingAndWallBlockItem(
                    MillagerBlocks.VILLAGER_HEAD.get(), MillagerBlocks.VILLAGER_WALL_HEAD.get(),
                    Direction.DOWN, Waypoint.addHideAttribute(props)),
            () -> new Item.Properties().rarity(Rarity.UNCOMMON).equippableUnswappable(EquipmentSlot.HEAD));

    public static final DeferredItem<StandingAndWallBlockItem> ILLAGER_HEAD = ITEMS.registerItem(
            "illager_head",
            props -> new StandingAndWallBlockItem(
                    MillagerBlocks.ILLAGER_HEAD.get(), MillagerBlocks.ILLAGER_WALL_HEAD.get(),
                    Direction.DOWN, Waypoint.addHideAttribute(props)),
            () -> new Item.Properties().rarity(Rarity.UNCOMMON).equippableUnswappable(EquipmentSlot.HEAD));

    public static final DeferredItem<BlockItem> TOTEM_INFUSER = ITEMS.registerItem(
            "totem_infuser",
            props -> new BlockItem(MillagerBlocks.TOTEM_INFUSER.get(), props.useBlockDescriptionPrefix()),
            Item.Properties::new);

    public static final DeferredItem<BlockItem> SAND_TABLE = ITEMS.registerItem(
            "sand_table",
            props -> new BlockItem(MillagerBlocks.SAND_TABLE.get(), props.useBlockDescriptionPrefix()),
            Item.Properties::new);

    // 刷怪蛋
    public static final DeferredItem<SpawnEggItem> BEE_GOLEM_SPAWN_EGG =
            registerSpawnEgg(MillagerEntityTypes.Bee_Golem);
    public static final DeferredItem<SpawnEggItem> DOCTOR_SPAWN_EGG =
            registerSpawnEgg(MillagerEntityTypes.Doctors);
    public static final DeferredItem<SpawnEggItem> ARCHER_SPAWN_EGG =
            registerSpawnEgg(MillagerEntityTypes.Archers);
    public static final DeferredItem<SpawnEggItem> BREACHER_SPAWN_EGG =
            registerSpawnEgg(MillagerEntityTypes.Breachers);
    public static final DeferredItem<SpawnEggItem> LANCER_SPAWN_EGG =
            registerSpawnEgg(MillagerEntityTypes.Lancers);
    public static final DeferredItem<SpawnEggItem> SWORDMASTER_SPAWN_EGG =
            registerSpawnEgg(MillagerEntityTypes.Swordmasters);
    public static final DeferredItem<SpawnEggItem> MAULER_SPAWN_EGG =
            registerSpawnEgg(MillagerEntityTypes.Maulers);
    public static final DeferredItem<SpawnEggItem> RIOTER_SPAWN_EGG =
            registerSpawnEgg(MillagerEntityTypes.Rioters);
    public static final DeferredItem<SpawnEggItem> SCOUTER_SPAWN_EGG =
            registerSpawnEgg(MillagerEntityTypes.Scouters);

    private static DeferredItem<SpawnEggItem> registerSpawnEgg(DeferredHolder<EntityType<?>, ? extends EntityType<?>> holder) {
        String name = holder.getKey().identifier().getPath() + "_spawn_egg";
        // properties.spawnEgg() sets the DataComponent that binds the entity type to this spawn egg.
        // Called inside the lambda so the entity type isn't resolved before registration completes.
        return ITEMS.registerItem(name, props -> new SpawnEggItem(props.spawnEgg(holder.get())));
    }

    public static void initialize() {
        ITEMS.register();
        POTIONS.register();
    }
}
