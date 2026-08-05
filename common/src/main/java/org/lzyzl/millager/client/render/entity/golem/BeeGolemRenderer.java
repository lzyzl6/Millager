package org.lzyzl.millager.client.render.entity.golem;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.Millager;
import org.lzyzl.millager.client.MillagerModelLayers;
import org.lzyzl.millager.entity.golem.BeeGolem;
import org.lzyzl.millager.util.ResourceLocationHelper;

public class BeeGolemRenderer extends MobRenderer<BeeGolem, BeeGolemModel> {

    private static final ResourceLocation TEXTURE = ResourceLocationHelper.create(Millager.MOD_ID, "textures/entity/golem/bee_golem.png");

    public BeeGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new BeeGolemModel(context.bakeLayer(MillagerModelLayers.BEE_GOLEM)), 0.3F);
        this.addLayer(new BeeGolemFlashLayer(this, new BeeGolemFlashModel(context.bakeLayer(MillagerModelLayers.BEE_GOLEM_FLASH))));
    }

    @Override
    public @NonNull ResourceLocation getTextureLocation(@NonNull BeeGolem entity) {
        return TEXTURE;
    }
}
