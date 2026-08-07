package org.lzyzl.millager.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.lzyzl.millager.Millager;
import org.lzyzl.millager.behavior.MiscConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;
import java.util.stream.Collectors;

@Mixin(ChunkGenerator.class)
public abstract class ChunkGeneratorMixin {

    @Inject(method = "findNearestMapStructure", at = @At("HEAD"), cancellable = true)
    private void millager$skipDisabledStructureSearch(ServerLevel level,
                                                      HolderSet<Structure> structures,
                                                      BlockPos pos,
                                                      int radius,
                                                      boolean skipKnownStructures,
                                                      CallbackInfoReturnable<Pair<BlockPos, Holder<Structure>>> cir) {
        if (structures.stream().allMatch(ChunkGeneratorMixin::millager$isStructureDisabled)) {
            cir.setReturnValue(null);
        }
    }

    @ModifyVariable(method = "getStructureGeneratingAt", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private static Set<Holder<Structure>> millager$filterDisabledStructures(Set<Holder<Structure>> structures) {
        if (structures.stream().noneMatch(ChunkGeneratorMixin::millager$isStructureDisabled)) {
            return structures;
        }
        return structures.stream()
                .filter(structure -> !millager$isStructureDisabled(structure))
                .collect(Collectors.toSet());
    }

    @Inject(method = "tryGenerateStructure", at = @At("HEAD"), cancellable = true)
    private void millager$preventStructureGeneration(StructureSet.StructureSelectionEntry entry,
                                                     StructureManager structureManager,
                                                     RegistryAccess registryAccess,
                                                     RandomState randomState,
                                                     StructureTemplateManager templateManager,
                                                     long seed,
                                                     ChunkAccess chunk,
                                                     ChunkPos chunkPos,
                                                     SectionPos sectionPos,
                                                     ResourceKey<Level> levelKey,
                                                     CallbackInfoReturnable<Boolean> cir) {
        if (millager$isStructureDisabled(entry.structure())) {
            cir.setReturnValue(false);
        }
    }

    private static boolean millager$isStructureDisabled(Holder<Structure> structure) {
        return structure.unwrapKey()
                .filter(key -> key.identifier().getNamespace().equals(Millager.MOD_ID))
                .filter(key -> !MiscConfig.shouldGenerateStructure(key.identifier().getPath())).isPresent();
    }
}
