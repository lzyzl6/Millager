package org.lzyzl.millager.client.render.entity.projectile;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.entity.projectile.ExplosiveArrow;

import static org.lzyzl.millager.Millager.MOD_ID;

public class ExplosiveArrowRenderer extends ArrowRenderer<ExplosiveArrow, ArrowRenderState> {

    private static final Identifier EXPLOSIVE_ARROW_LOCATION = Identifier.fromNamespaceAndPath(MOD_ID,"textures/projectile/explosive_arrow.png");

    public ExplosiveArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected @NonNull Identifier getTextureLocation(ArrowRenderState arrowRenderState) {
        return EXPLOSIVE_ARROW_LOCATION;
    }

    @Override
    public ArrowRenderState createRenderState() {
        return new ArrowRenderState();
    }
}
