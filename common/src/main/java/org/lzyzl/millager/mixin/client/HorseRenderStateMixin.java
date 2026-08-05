package org.lzyzl.millager.mixin.client;

import net.minecraft.client.renderer.entity.state.HorseRenderState;
import org.lzyzl.millager.client.render.entity.horse.MillagerHorseRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(HorseRenderState.class)
public class HorseRenderStateMixin implements MillagerHorseRenderState {

    @Unique
    private boolean millager$usesCombinedTexture;

    @Override
    public boolean millager$usesCombinedTexture() {
        return this.millager$usesCombinedTexture;
    }

    @Override
    public void millager$setUsesCombinedTexture(boolean usesCombinedTexture) {
        this.millager$usesCombinedTexture = usesCombinedTexture;
    }
}
