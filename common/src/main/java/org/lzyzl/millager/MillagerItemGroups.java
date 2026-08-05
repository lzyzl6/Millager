package org.lzyzl.millager;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import org.lzyzl.millager.registry.DeferredRegister;
import org.lzyzl.millager.util.ResourceVariantHelper;
import org.lzyzl.millager.util.VillageBannerHelper;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class MillagerItemGroups {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, Millager.MOD_ID);

    public static final ResourceKey<CreativeModeTab> MILLAGER_CREATIVE_TAB_KEY = ResourceKey.create(
            BuiltInRegistries.CREATIVE_MODE_TAB.key(),
            Identifier.fromNamespaceAndPath(Millager.MOD_ID, "creative_tab")
    );

    public static final Supplier<CreativeModeTab> MILLAGER_CREATIVE_TAB = CREATIVE_MODE_TABS.register(
            "creative_tab",
            () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .icon(() -> new ItemStack(MillagerItems.VILLAGER_HEAD.asItem()))
                    .title(Component.translatable("itemGroup.millager"))
                    .displayItems((params, output) -> {
                        output.accept(MillagerItems.rose);
                        output.accept(MillagerItems.SAND_TABLE);
                        output.accept(MillagerItems.TOTEM_INFUSER);
                        output.accept(VillageBannerHelper.create(
                                params.holders().lookupOrThrow(Registries.BANNER_PATTERN)));
                        output.accept(MillagerBlocks.VILLAGER_HEAD.get());
                        output.accept(MillagerBlocks.ILLAGER_HEAD.get());

                        output.accept(MillagerItems.buckler);

                        output.accept(MillagerItems.tntOnAStick);
                        output.accept(MillagerItems.explosiveArrow);
                        output.accept(MillagerItems.molotovCocktail);
                        output.accept(MillagerItems.molotovCocktailPlus);
                        output.accept(MillagerItems.golemAmber);

                        output.accept(MillagerItems.doctorProfessionOrder);
                        output.accept(MillagerItems.archerProfessionOrder);
                        output.accept(MillagerItems.scouterProfessionOrder);
                        output.accept(MillagerItems.lancerProfessionOrder);
                        output.accept(MillagerItems.swordmasterProfessionOrder);
                        output.accept(MillagerItems.maulerProfessionOrder);
                        output.accept(MillagerItems.rioterProfessionOrder);
                        output.accept(MillagerItems.breacherProfessionOrder);

                        output.accept(MillagerItems.infantryMusterOrder);
                        output.accept(MillagerItems.cavalryMusterOrder);
                        output.accept(MillagerItems.randomMusterOrder);

                        output.accept(MillagerItems.totemOfHealing);
                        output.accept(MillagerItems.crackedTotemOfHealing);
                        output.accept(MillagerItems.damagedTotemOfHealing);

                        output.accept(MillagerItems.liquor);
                        output.accept(MillagerItems.elixir);

                        output.accept(MillagerItems.VILLAGER_BANNER_PATTERN);

                        output.accept(MillagerItems.BEE_GOLEM_SPAWN_EGG);
                        output.accept(MillagerItems.DOCTOR_SPAWN_EGG);
                        output.accept(MillagerItems.ARCHER_SPAWN_EGG);
                        output.accept(MillagerItems.SCOUTER_SPAWN_EGG);
                        output.accept(MillagerItems.LANCER_SPAWN_EGG);
                        output.accept(MillagerItems.SWORDMASTER_SPAWN_EGG);
                        output.accept(MillagerItems.MAULER_SPAWN_EGG);
                        output.accept(MillagerItems.RIOTER_SPAWN_EGG);
                        output.accept(MillagerItems.BREACHER_SPAWN_EGG);
//                        output.accept(MillagerItems.SCHOLAR_SPAWN_EGG);
//                        output.accept(MillagerItems.CONJURER_SPAWN_EGG);
                    })
                    .build()
    );

    public static void initialize() {
        CREATIVE_MODE_TABS.register();
    }

    public static void addExtraContents(Consumer<ItemStack> output, CreativeModeTab.ItemDisplayParameters params) {
        output.accept(PotionContents.createItemStack(Items.POTION, MillagerItems.ASCENSION.holder()));
        output.accept(PotionContents.createItemStack(Items.POTION, MillagerItems.LONG_ASCENSION.holder()));
        output.accept(PotionContents.createItemStack(Items.SPLASH_POTION, MillagerItems.ASCENSION.holder()));
        output.accept(PotionContents.createItemStack(Items.SPLASH_POTION, MillagerItems.LONG_ASCENSION.holder()));
        output.accept(PotionContents.createItemStack(Items.LINGERING_POTION, MillagerItems.ASCENSION.holder()));
        output.accept(PotionContents.createItemStack(Items.LINGERING_POTION, MillagerItems.LONG_ASCENSION.holder()));
        output.accept(PotionContents.createItemStack(Items.TIPPED_ARROW, MillagerItems.ASCENSION.holder()));
        output.accept(PotionContents.createItemStack(Items.TIPPED_ARROW, MillagerItems.LONG_ASCENSION.holder()));

        params.holders().get(ResourceVariantHelper.COCKTAIL_TUTORIAL).ifPresent(reference -> {
            ItemStack itemStack = new ItemStack(Items.PAINTING);
            itemStack.set(DataComponents.PAINTING_VARIANT, reference);
            output.accept(itemStack);
        });
    }
}
