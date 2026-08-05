package org.lzyzl.millager.client.render.entity.millager;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.lzyzl.millager.client.MillagerModelLayers;
import org.lzyzl.millager.entity.millager.Archer;
import org.lzyzl.millager.util.ResourceLocationHelper;

import static org.lzyzl.millager.Millager.MOD_ID;

public class ArcherRenderer extends MillagerRenderer<Archer> {
    private static final ResourceLocation TEXTURE = ResourceLocationHelper.create(MOD_ID, "textures/entity/millager/archer.png");

    public ArcherRenderer(EntityRendererProvider.Context context) {
        super(context, new MillagerModel<>(context.bakeLayer(MillagerModelLayers.ARCHER)), 0.5f, TEXTURE);
    }
}
