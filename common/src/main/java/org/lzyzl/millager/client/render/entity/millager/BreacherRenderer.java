package org.lzyzl.millager.client.render.entity.millager;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.client.MillagerModelLayers;
import org.lzyzl.millager.entity.millager.Breacher;

import static org.lzyzl.millager.Millager.MOD_ID;

public class BreacherRenderer extends MillagerRenderer<Breacher, BreacherRenderState> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(MOD_ID, "textures/entity/millager/breacher.png");

    public BreacherRenderer(EntityRendererProvider.Context context) {
        super(context, new MillagerModel<>(context.bakeLayer(MillagerModelLayers.BREACHER)), 0.5F);
    }

    @Override
    public @NonNull Identifier getTextureLocation(BreacherRenderState state) {
        return TEXTURE;
    }

    @Override
    public BreacherRenderState createRenderState() {
        return new BreacherRenderState();
    }
}

