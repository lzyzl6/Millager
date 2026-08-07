package org.lzyzl.millager.client.render.entity.millager;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.lzyzl.millager.client.MillagerModelLayers;
import org.lzyzl.millager.entity.millager.Lancer;
import org.lzyzl.millager.util.ResourceLocationHelper;

import static org.lzyzl.millager.Millager.MOD_ID;

public class LancerRenderer extends MillagerRenderer<Lancer> {
    private static final ResourceLocation TEXTURE = ResourceLocationHelper.create(MOD_ID, "textures/entity/millager/lancer.png");

    public LancerRenderer(EntityRendererProvider.Context context) {
        super(context, new MillagerModel<>(context.bakeLayer(MillagerModelLayers.LANCER)), 0.5F, TEXTURE);
    }
}
