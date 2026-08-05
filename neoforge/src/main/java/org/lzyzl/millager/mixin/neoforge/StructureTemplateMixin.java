package org.lzyzl.millager.mixin.neoforge;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.lzyzl.millager.util.HangingEntityPositionFix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;

@Mixin(StructureTemplate.class)
public abstract class StructureTemplateMixin {

    @Inject(method = "addEntitiesToWorld(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructurePlaceSettings;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate;createEntityIgnoreException(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/nbt/CompoundTag;)Ljava/util/Optional;"),
            require = 0,
            locals = LocalCapture.CAPTURE_FAILSOFT)
    private void millager$updateHangingEntityPosition(ServerLevelAccessor level, BlockPos offset, StructurePlaceSettings settings,
                                                       CallbackInfo ci, Iterator<StructureTemplate.StructureEntityInfo> iterator,
                                                       StructureTemplate.StructureEntityInfo entityInfo, BlockPos entityBlockPos,
                                                       CompoundTag entityTag) {
        HangingEntityPositionFix.update(entityTag, entityBlockPos);
    }
}
