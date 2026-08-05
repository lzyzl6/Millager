package org.lzyzl.millager.client.render.entity.millager;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.lzyzl.millager.client.MillagerModelLayers;
import org.lzyzl.millager.entity.millager.Rioter;

import static org.lzyzl.millager.Millager.MOD_ID;

public class RioterRenderer extends MillagerRenderer<Rioter> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/entity/millager/rioter.png");

    public RioterRenderer(EntityRendererProvider.Context context) {
        super(context, new MillagerModel<>(context.bakeLayer(MillagerModelLayers.RIOTER)), 0.5f, TEXTURE);
        this.addLayer(new RioterShieldBackLayer(this));
    }
}
