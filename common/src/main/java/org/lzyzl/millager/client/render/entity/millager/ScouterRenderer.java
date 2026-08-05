package org.lzyzl.millager.client.render.entity.millager;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.lzyzl.millager.client.MillagerModelLayers;
import org.lzyzl.millager.entity.millager.Scouter;
import org.lzyzl.millager.util.ResourceLocationHelper;

import static org.lzyzl.millager.Millager.MOD_ID;

public class ScouterRenderer extends MillagerRenderer<Scouter> {
    private static final ResourceLocation TEXTURE = ResourceLocationHelper.create(MOD_ID, "textures/entity/millager/scouter.png");

    public ScouterRenderer(EntityRendererProvider.Context context) {
        super(context, new MillagerModel<>(context.bakeLayer(MillagerModelLayers.SCOUTER)), 0.5f, TEXTURE);
        this.addLayer(new ScouterHornHeldLayer(this));
    }
}
