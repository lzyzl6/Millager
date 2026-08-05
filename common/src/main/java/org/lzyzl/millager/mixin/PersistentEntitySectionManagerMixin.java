package org.lzyzl.millager.mixin;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PersistentEntitySectionManager.class)
public abstract class PersistentEntitySectionManagerMixin {

    @Final
    @Shadow private LongSet chunksToUnload;

    @Shadow
    private boolean processChunkUnload(long chunkPos) {
        throw new AssertionError();
    }

    @Inject(method = "processUnloads", at = @At("HEAD"), cancellable = true)
    private void millager$processUnloads(CallbackInfo ci) {
        LongArrayList chunks = new LongArrayList(this.chunksToUnload);
        for (long chunkPos : chunks) {
            if (this.processChunkUnload(chunkPos)) {
                this.chunksToUnload.remove(chunkPos);
            }
        }
        ci.cancel();
    }
}
