package org.lzyzl.millager;

import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import org.lzyzl.millager.item.*;
import org.lzyzl.millager.registry.DeferredHolder;
import org.lzyzl.millager.registry.DeferredItem;
import org.lzyzl.millager.registry.DeferredRegister;
import org.lzyzl.millager.util.ResourceVariantHelper;

import static net.minecraft.core.registries.BuiltInRegistries.POTION;

public class MillagerItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Millager.MOD_ID);
    public static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(POTION.key(), Millager.MOD_ID);

    private static final FoodProperties LIQUOR = new FoodProperties.Builder()
            .nutrition(3).saturationModifier(0.1F).alwaysEdible().build();

    private static final FoodProperties ELIXIR = new FoodProperties.Builder()
            .nutrition(4).saturationModifier(1.2F).alwaysEdible()
            .effect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 36000, 0), 1.0F)
            .effect(new MobEffectInstance(MobEffects.SATURATION, 36000, 0), 1.0F)
            .effect(new MobEffectInstance(MobEffects.JUMP, 36000, 0), 1.0F)
            .effect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 36000, 0), 1.0F)
            .effect(new MobEffectInstance(MobEffects.DIG_SPEED, 36000, 0), 1.0F)
            .effect(new MobEffectInstance(MobEffects.WATER_BREATHING, 36000, 0), 1.0F)
            .effect(new MobEffectInstance(MobEffects.NIGHT_VISION, 36000, 0), 1.0F)
            .effect(new MobEffectInstance(MobEffects.LUCK, 72000, 4), 1.0F).build();

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

    public static final DeferredItem<LancerSpearItem> ironLancerSpear = ITEMS.registerItem(
            "iron_lancer_spear", props -> new LancerSpearItem(LancerSpearItem.Material.IRON, props));

    public static final DeferredItem<LancerSpearItem> goldenLancerSpear = ITEMS.registerItem(
            "golden_lancer_spear", props -> new LancerSpearItem(LancerSpearItem.Material.GOLD, props));

    public static final DeferredItem<LancerSpearItem> diamondLancerSpear = ITEMS.registerItem(
            "diamond_lancer_spear", props -> new LancerSpearItem(LancerSpearItem.Material.DIAMOND, props));

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
            () -> new Item.Properties().food(LIQUOR).stacksTo(16));

    public static final DeferredItem<MolotovCocktailItem> molotovCocktail = ITEMS.registerItem(
            "molotov_cocktail", MolotovCocktailItem::new,
            () -> new Item.Properties().stacksTo(16));

    public static final DeferredItem<MolotovCocktailPlusItem> molotovCocktailPlus = ITEMS.registerItem(
            "molotov_cocktail_plus", MolotovCocktailPlusItem::new,
            () -> new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<Elixir> elixir = ITEMS.registerItem(
            "elixir", Elixir::new, () -> new Item.Properties().food(ELIXIR).rarity(Rarity.RARE));

    public static final DeferredItem<HealingTotem> totemOfHealing = ITEMS.registerItem(
            "totem_of_healing", HealingTotem::new, () -> new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<CrackedHealingTotem> crackedTotemOfHealing = ITEMS.registerItem(
            "cracked_totem_of_healing", CrackedHealingTotem::new, () -> new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<DamagedHealingTotem> damagedTotemOfHealing = ITEMS.registerItem(
            "damaged_totem_of_healing", DamagedHealingTotem::new, () -> new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<BucklerItem> buckler = ITEMS.registerItem(
            "buckler", BucklerItem::new, () -> new Item.Properties().durability(255));

    public static final DeferredItem<BlockItem> rose = ITEMS.registerItem(
            "rose", props -> new BlockItem(MillagerBlocks.ROSE.get(), props),
            Item.Properties::new);

    public static final DeferredItem<BannerPatternItem> VILLAGER_BANNER_PATTERN = ITEMS.registerItem(
            "banner_pattern", props -> new BannerPatternItem(ResourceVariantHelper.PATTERN_ITEM_VILLAGER, (new Item.Properties()).stacksTo(1).rarity(Rarity.UNCOMMON)))
            ;

    public static final DeferredItem<StandingAndWallBlockItem> VILLAGER_HEAD = ITEMS.registerItem(
            "villager_head",
            props -> new StandingAndWallBlockItem(
                    MillagerBlocks.VILLAGER_HEAD.get(), MillagerBlocks.VILLAGER_WALL_HEAD.get(),
                    props, Direction.DOWN),
            () -> new Item.Properties().rarity(Rarity.UNCOMMON));

    public static final DeferredItem<StandingAndWallBlockItem> ILLAGER_HEAD = ITEMS.registerItem(
            "illager_head",
            props -> new StandingAndWallBlockItem(
                    MillagerBlocks.ILLAGER_HEAD.get(), MillagerBlocks.ILLAGER_WALL_HEAD.get(),
                    props, Direction.DOWN),
            () -> new Item.Properties().rarity(Rarity.UNCOMMON));

    public static final DeferredItem<BlockItem> TOTEM_INFUSER = ITEMS.registerItem(
            "totem_infuser",
            props -> new BlockItem(MillagerBlocks.TOTEM_INFUSER.get(), props),
            Item.Properties::new);

    public static final DeferredItem<BlockItem> SAND_TABLE = ITEMS.registerItem(
            "sand_table",
            props -> new BlockItem(MillagerBlocks.SAND_TABLE.get(), props),
            Item.Properties::new);

    public static final DeferredItem<SpawnEggItem> BEE_GOLEM_SPAWN_EGG = ITEMS.registerItem(
            "bee_golem_spawn_egg",
            props -> new SpawnEggItem(MillagerEntityTypes.Bee_Golem.get(), 15582019, 14071391, props));

    public static final DeferredItem<SpawnEggItem> DOCTOR_SPAWN_EGG = ITEMS.registerItem(
            "doctor_spawn_egg",
            props -> new SpawnEggItem(MillagerEntityTypes.Doctors.get(), 5651507, 14254722, props));

    public static final DeferredItem<SpawnEggItem> ARCHER_SPAWN_EGG = ITEMS.registerItem(
            "archer_spawn_egg",
            props -> new SpawnEggItem(MillagerEntityTypes.Archers.get(), 5651507, 14262383, props));

    public static final DeferredItem<SpawnEggItem> BREACHER_SPAWN_EGG = ITEMS.registerItem(
            "breacher_spawn_egg",
            props -> new SpawnEggItem(MillagerEntityTypes.Breachers.get(), 5651507, 14272879, props));

    public static final DeferredItem<SpawnEggItem> LANCER_SPAWN_EGG = ITEMS.registerItem(
            "lancer_spawn_egg",
            props -> new SpawnEggItem(MillagerEntityTypes.Lancers.get(), 5651507, 11984326, props));

    public static final DeferredItem<SpawnEggItem> SWORDMASTER_SPAWN_EGG = ITEMS.registerItem(
            "swordmaster_spawn_egg",
            props -> new SpawnEggItem(MillagerEntityTypes.Swordmasters.get(), 5651507, 8833415, props));

    public static final DeferredItem<SpawnEggItem> MAULER_SPAWN_EGG = ITEMS.registerItem(
            "mauler_spawn_egg",
            props -> new SpawnEggItem(MillagerEntityTypes.Maulers.get(), 5651507, 7980228, props));

    public static final DeferredItem<SpawnEggItem> RIOTER_SPAWN_EGG = ITEMS.registerItem(
            "rioter_spawn_egg",
            props -> new SpawnEggItem(MillagerEntityTypes.Rioters.get(), 5651507, 8560593, props));

    public static final DeferredItem<SpawnEggItem> SCOUTER_SPAWN_EGG = ITEMS.registerItem(
            "scouter_spawn_egg",
            props -> new SpawnEggItem(MillagerEntityTypes.Scouters.get(), 5651507, 11635401, props));

    public static void initialize() {
        ITEMS.register();
        POTIONS.register();
    }
}
