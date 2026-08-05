package org.lzyzl.millager;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.level.block.state.BlockState;
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
                    Identifier.fromNamespaceAndPath(MOD_ID, "command_poi"));

    public static final ResourceKey<VillagerProfession> COMMANDER_KEY =
            ResourceKey.create(Registries.VILLAGER_PROFESSION,
                    Identifier.fromNamespaceAndPath(MOD_ID, "commander"));

    // Lazy: SAND_TABLE.get() is called only when the PoiType registry event fires,
    // long after blocks are registered.
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
                    Component.translatable("entity." + MOD_ID + ".villager.commander"),
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

    /**
     * 把 SAND_TABLE 的方块状态登记进 PoiTypes 的 方块状态→POI 反向映射。
     * vanilla PoiTypes.register() 内部会做这一步,但 multiloader 的 DeferredRegister 路径只调 Registry.register。
     * NeoForge 会从注册表自动补登该映射,Fabric 不会 → 需在 commonSetup 手动补,但要**幂等**:
     * 只登记尚未映射的状态,否则 NeoForge 上会因重复注册抛 "defined in more than one PoI type"。
     */
    public static void registerPoiBlockStates() {
        HashSet<BlockState> states =
                new HashSet<>(MillagerBlocks.SAND_TABLE.get().getStateDefinition().getPossibleStates());
        try {
            states.removeIf(PoiTypes::hasPoi);
        } catch (IllegalStateException e) {
            // Tags not yet bound (e.g. Nycto mixin on Collection.removeIf)
        }
        if (!states.isEmpty()) {
            PoiTypes.registerBlockStates(
                    BuiltInRegistries.POINT_OF_INTEREST_TYPE.wrapAsHolder(COMMAND_POI.get()),
                    states
            );
        }
    }
}
