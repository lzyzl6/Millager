package org.lzyzl.millager.mixin.fabric;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
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

    @Inject(method = "placeEntities(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Mirror;Lnet/minecraft/world/level/block/Rotation;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/levelgen/structure/BoundingBox;ZLnet/minecraft/util/ProblemReporter;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate;createEntityIgnoreException(Lnet/minecraft/util/ProblemReporter;Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/nbt/CompoundTag;)Ljava/util/Optional;"),
            require = 0,
            locals = LocalCapture.CAPTURE_FAILSOFT)
    private void millager$updateHangingEntityPosition(ServerLevelAccessor level, BlockPos offset, Mirror mirror, Rotation rotation,
                                                       BlockPos pivot, BoundingBox box, boolean finalizeEntities, ProblemReporter reporter,
                                                       CallbackInfo ci, Iterator<StructureTemplate.StructureEntityInfo> iterator,
                                                       StructureTemplate.StructureEntityInfo entityInfo, BlockPos entityBlockPos,
                                                       CompoundTag entityTag) {
        HangingEntityPositionFix.update(entityTag, entityBlockPos);
    }
}
