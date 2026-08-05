package org.lzyzl.millager.client.render.entity.golem;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.lzyzl.millager.client.MillagerModelLayers;
import org.lzyzl.millager.entity.golem.BeeGolem;

import static org.lzyzl.millager.Millager.MOD_ID;

public class BeeGolemRenderer extends MobRenderer<BeeGolem,BeeGolemRenderState,BeeGolemModel> {

    private static final Identifier BG_LOCATION = Identifier.fromNamespaceAndPath(MOD_ID,"textures/entity/golem/bee_golem.png");

    public BeeGolemRenderer(EntityRendererProvider.Context context) {
        super(context,new BeeGolemModel(context.bakeLayer(MillagerModelLayers.BEE_GOLEM)),0.5f);
        this.addLayer(new BeeGolemFlashLayer(this, new BeeGolemFlashModel(context.bakeLayer(MillagerModelLayers.BEE_GOLEM_FLASH))));
    }

    @Override
    public BeeGolemRenderState createRenderState() {
        return new BeeGolemRenderState();
    }

    @Override
    public void extractRenderState(BeeGolem golem, BeeGolemRenderState state, float f) {
        super.extractRenderState(golem, state, f);
        state.isAttacking = golem.isAttacking();
        state.isSummoned = golem.isSummoned();

        int life = golem.getLifeTicks();
        float time = (float)golem.tickCount + f;

        float danger = Mth.clamp((float)life / 2400.0F, 0.0F, 1.0F);

        float freq = Mth.lerp(danger, 0.08F, 0.6F);
        float s = (Mth.sin(time * freq) + 1.0F) / 2.0F;

        int r, g, b;

        if(state.isSummoned) {
            r = 0; g = 0; b = (int)(s * 255);
        } else if (state.isAttacking) {
            r = (int)(s * 255); g = 0; b = 0;
        } else {
            r = (int)(s * Mth.lerp(danger, 0.0F, 255.0F));
            g = (int)(s * 255.0F);
            b = 0;
        }

        state.flashColor = (255 << 24) | (r << 16) | (g << 8) | b;
        state.isOnGround = golem.onGround() && golem.getDeltaMovement().lengthSqr() < 1.0E-7;
    }

    @Override
    public Identifier getTextureLocation(BeeGolemRenderState state) {
        return BG_LOCATION;
    }

}
