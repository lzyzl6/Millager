package org.lzyzl.millager;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.block.state.BlockState;
import org.lzyzl.millager.mixin.PoiTypesInvoker;
import org.lzyzl.millager.registry.DeferredHolder;
import org.lzyzl.millager.registry.DeferredRegister;

import java.util.HashSet;

import static org.lzyzl.millager.Millager.MOD_ID;

public class MillagerProfessionAndPoi {

    public static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, MOD_ID);

    public static final DeferredRegister<VillagerProfession> PROFESSIONS =
            DeferredRegister.create(Registries.VILLAGER_PROFESSION, MOD_ID);

    public static final ResourceKey<PoiType> COMMAND_POI_KEY =
            ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE,
                    ResourceLocation.fromNamespaceAndPath(MOD_ID, "command_poi"));

    public static final ResourceKey<VillagerProfession> COMMANDER_KEY =
            ResourceKey.create(Registries.VILLAGER_PROFESSION,
                    ResourceLocation.fromNamespaceAndPath(MOD_ID, "commander"));

    public static final DeferredHolder<PoiType, PoiType> COMMAND_POI = POI_TYPES.register(
            "command_poi",
            () -> new PoiType(
                    new HashSet<>(MillagerBlocks.SAND_TABLE.get()
                            .getStateDefinition().getPossibleStates()),
                    1, 1
            )
    );

    public static final DeferredHolder<VillagerProfession, VillagerProfession> COMMANDER = PROFESSIONS.register(
            "commander",
            () -> new VillagerProfession(
                    "commander",
                    holder -> holder.is(COMMAND_POI_KEY),
                    holder -> holder.is(COMMAND_POI_KEY),
                    ImmutableSet.of(),
                    ImmutableSet.of(),
                    MillagerSounds.VILLAGER_WORK_COMMANDER
            )
    );

    public static void initialize() {
        POI_TYPES.register();
        PROFESSIONS.register();
    }

    public static void registerPoiBlockStates() {
        HashSet<BlockState> states =
                new HashSet<>(MillagerBlocks.SAND_TABLE.get().getStateDefinition().getPossibleStates());
        states.removeIf(PoiTypes::hasPoi);
        if (!states.isEmpty()) {
            PoiTypesInvoker.millager$registerBlockStates(
                    BuiltInRegistries.POINT_OF_INTEREST_TYPE.wrapAsHolder(COMMAND_POI.get()),
                    states);
        }
    }
}
