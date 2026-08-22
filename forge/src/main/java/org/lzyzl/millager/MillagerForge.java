package org.lzyzl.millager;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.client.MillagerForgeConfigScreen;
import org.lzyzl.millager.compat.goety.GoetyCompat;
import org.lzyzl.millager.config.MillagerConfig;
import org.lzyzl.millager.entity.golem.BeeGolem;
import org.lzyzl.millager.entity.millager.*;
import org.lzyzl.millager.entity.projectile.RioterProjectile;
import org.lzyzl.millager.registry.ForgeRegistryFactory;
import org.lzyzl.millager.util.EnemyAttackHelper;
import org.lzyzl.millager.util.VillagerTradeHelper;

@Mod(Millager.MOD_ID)
@SuppressWarnings("deprecation")
public class MillagerForge {

    public MillagerForge(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();
        ForgeRegistryFactory.setModBus(modBus);
        MillagerConfig.load(FMLPaths.CONFIGDIR.get());
        Millager.init();

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> MillagerForgeConfigScreen::register);

        modBus.addListener(this::commonSetup);
        modBus.addListener(this::addCreative);
        modBus.addListener(this::onEntityAttributeCreation);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Millager.commonSetup();
            BrewingRecipeRegistry.addRecipe(new PotionMixRecipe(
                    Potions.AWKWARD, Ingredient.of(Items.TORCHFLOWER), MillagerItems.ASCENSION.get()));
            BrewingRecipeRegistry.addRecipe(new PotionMixRecipe(
                    MillagerItems.ASCENSION.get(), Ingredient.of(Items.REDSTONE), MillagerItems.LONG_ASCENSION.get()));
        });
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(MillagerItemGroups.MILLAGER_CREATIVE_TAB_KEY)) {
            MillagerItemGroups.addExtraContents(event::accept, event.getParameters());
        }
    }

    private void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(MillagerEntityTypes.Bee_Golem.get(), BeeGolem.createAttributes().build());
        event.put(MillagerEntityTypes.Archers.get(), Archer.createAttributes().build());
        event.put(MillagerEntityTypes.Breachers.get(), Breacher.createAttributes().build());
        event.put(MillagerEntityTypes.Lancers.get(), Lancer.createAttributes().build());
        event.put(MillagerEntityTypes.Doctors.get(), Doctor.createAttributes().build());
        event.put(MillagerEntityTypes.Maulers.get(), Mauler.createAttributes().build());
        event.put(MillagerEntityTypes.Swordmasters.get(), Swordmaster.createAttributes().build());
        event.put(MillagerEntityTypes.Rioters.get(), Rioter.createAttributes().build());
        event.put(MillagerEntityTypes.Scouters.get(), Scouter.createAttributes().build());
    }

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        EnemyAttackHelper.onEntityJoinLevel(event.getEntity());
    }

    @SubscribeEvent
    public void onLivingEntityUseItemFinish(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof Player player && isGoetyRaidingHorn(event.getItem())) {
            GoetyCompat.onRaidingHornUsed(player);
        }
    }

    @SubscribeEvent
    public void onExplosionDetonate(ExplosionEvent.Detonate event) {
        if (event.getExplosion().getDirectSourceEntity() instanceof RioterProjectile rioterProjectile && rioterProjectile.isRioterProjectile()) {
            event.getAffectedEntities().removeIf(entity -> !(entity instanceof LivingEntity) || entity instanceof ArmorStand);
        }
    }

    @SubscribeEvent
    public void onVillagerTrades(VillagerTradesEvent event) {
        BuiltInRegistries.VILLAGER_PROFESSION.getResourceKey(event.getType()).ifPresent(profession ->
                VillagerTradeHelper.registerVillagerTrades(profession,
                        (level, listing) -> event.getTrades().get(level).add(listing)));
    }

    private static boolean isGoetyRaidingHorn(ItemStack stack) {
        return "goety:raiding_horn".equals(String.valueOf(BuiltInRegistries.ITEM.getKey(stack.getItem())));
    }

    private record PotionMixRecipe(Potion input, Ingredient ingredient, Potion output) implements IBrewingRecipe {

        @Override
            public boolean isInput(ItemStack input) {
                return input.getItem() instanceof PotionItem && PotionUtils.getPotion(input) == this.input;
            }

            @Override
            public boolean isIngredient(@NonNull ItemStack ingredient) {
                return this.ingredient.test(ingredient);
            }

            @Override
            public @NonNull ItemStack getOutput(@NonNull ItemStack input, @NonNull ItemStack ingredient) {
                if (!isInput(input) || !isIngredient(ingredient)) return ItemStack.EMPTY;
                return PotionUtils.setPotion(input.copyWithCount(1), this.output);
            }
        }
}
