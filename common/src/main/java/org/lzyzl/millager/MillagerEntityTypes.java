package org.lzyzl.millager;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.lzyzl.millager.registry.DeferredHolder;
import org.lzyzl.millager.registry.DeferredRegister;
import org.lzyzl.millager.entity.golem.BeeGolem;
import org.lzyzl.millager.entity.millager.*;
import org.lzyzl.millager.entity.projectile.ExplosiveArrow;
import org.lzyzl.millager.entity.projectile.MolotovCocktail;
import org.lzyzl.millager.entity.projectile.MolotovCocktailPlus;
import org.lzyzl.millager.entity.projectile.TNTOnAStick;

import static org.lzyzl.millager.Millager.MOD_ID;

public class MillagerEntityTypes {

    public static final DeferredRegister.Entities ENTITY_TYPES = DeferredRegister.createEntities(MOD_ID);

    // .sized(f, g)  f：宽度  g：高度
    // Millager
    public static final DeferredHolder<EntityType<?>, EntityType<Archer>> Archers =
            ENTITY_TYPES.registerEntityType("archer", Archer::new, MobCategory.MISC,
                    b -> b.sized(0.6F, 1.95F).eyeHeight(1.62F).clientTrackingRange(8));

    public static final DeferredHolder<EntityType<?>, EntityType<Breacher>> Breachers =
            ENTITY_TYPES.registerEntityType("breacher", Breacher::new, MobCategory.MISC,
                    b -> b.sized(0.6F, 1.95F).eyeHeight(1.62F)
                            .passengerAttachments(2.0F).ridingOffset(-0.6F).clientTrackingRange(8));

    public static final DeferredHolder<EntityType<?>, EntityType<Lancer>> Lancers =
            ENTITY_TYPES.registerEntityType("lancer", Lancer::new, MobCategory.MISC,
                    b -> b.sized(0.6F, 1.95F).eyeHeight(1.62F)
                            .passengerAttachments(2.0F).ridingOffset(-0.6F).clientTrackingRange(8));

    public static final DeferredHolder<EntityType<?>, EntityType<Doctor>> Doctors =
            ENTITY_TYPES.registerEntityType("doctor", Doctor::new, MobCategory.MISC,
                    b -> b.sized(0.6F, 1.95F).eyeHeight(1.62F).clientTrackingRange(8));

    public static final DeferredHolder<EntityType<?>, EntityType<Mauler>> Maulers =
            ENTITY_TYPES.registerEntityType("mauler", Mauler::new, MobCategory.MISC,
                    b -> b.sized(0.6F, 1.95F).eyeHeight(1.62F).clientTrackingRange(8));

    public static final DeferredHolder<EntityType<?>, EntityType<Rioter>> Rioters =
            ENTITY_TYPES.registerEntityType("rioter", Rioter::new, MobCategory.MISC,
                    b -> b.sized(0.6F, 1.95F).eyeHeight(1.62F).clientTrackingRange(8));

    public static final DeferredHolder<EntityType<?>, EntityType<Swordmaster>> Swordmasters =
            ENTITY_TYPES.registerEntityType("swordmaster", Swordmaster::new, MobCategory.MISC,
                    b -> b.sized(0.6F, 1.95F).eyeHeight(1.62F).clientTrackingRange(8));

    public static final DeferredHolder<EntityType<?>, EntityType<Scouter>> Scouters =
            ENTITY_TYPES.registerEntityType("scouter", Scouter::new, MobCategory.MISC,
                    b -> b.sized(0.6F, 1.95F).eyeHeight(1.62F)
                            .passengerAttachments(2.0F).ridingOffset(-0.6F).clientTrackingRange(8));

//    public static final DeferredHolder<EntityType<?>, EntityType<Scholar>> Scholars =
//            ENTITY_TYPES.registerEntityType("scholar", Scholar::new, MobCategory.MISC,
//                    b -> b.sized(0.6F, 1.95F).eyeHeight(1.62F).clientTrackingRange(8));
//
//    public static final DeferredHolder<EntityType<?>, EntityType<Conjurer>> Conjurers =
//            ENTITY_TYPES.registerEntityType("conjurer", Conjurer::new, MobCategory.MISC,
//                    b -> b.sized(0.6F, 1.95F).eyeHeight(1.62F).clientTrackingRange(8));

    // Golem
    public static final DeferredHolder<EntityType<?>, EntityType<BeeGolem>> Bee_Golem =
            ENTITY_TYPES.registerEntityType("bee_golem", BeeGolem::new, MobCategory.MISC,
                    b -> b.sized(0.55F, 0.45F).eyeHeight(0.25F).clientTrackingRange(10));

    // Projectile
    public static final DeferredHolder<EntityType<?>, EntityType<ExplosiveArrow>> Explosive_Arrow =
            ENTITY_TYPES.registerEntityType("explosive_arrow", ExplosiveArrow::new, MobCategory.MISC,
                    b -> b.noLootTable().sized(0.5F, 0.5F).eyeHeight(0.13F).clientTrackingRange(4).updateInterval(20));

    public static final DeferredHolder<EntityType<?>, EntityType<MolotovCocktail>> Cocktail_Projectile =
            ENTITY_TYPES.registerEntityType("molotov_cocktail", MolotovCocktail::new, MobCategory.MISC,
                    b -> b.noLootTable().sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10));

    public static final DeferredHolder<EntityType<?>, EntityType<MolotovCocktailPlus>> Cocktail_Plus_Projectile =
            ENTITY_TYPES.registerEntityType("molotov_cocktail_plus", MolotovCocktailPlus::new, MobCategory.MISC,
                    b -> b.noLootTable().sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10));

    public static final DeferredHolder<EntityType<?>, EntityType<TNTOnAStick>> TNT_Projectile =
            ENTITY_TYPES.registerEntityType("tnt_projectile", TNTOnAStick::new, MobCategory.MISC,
                    b -> b.noLootTable().sized(0.4F, 0.4F).clientTrackingRange(4).updateInterval(10));

    public static void initialize() {
        ENTITY_TYPES.register();
    }
}
