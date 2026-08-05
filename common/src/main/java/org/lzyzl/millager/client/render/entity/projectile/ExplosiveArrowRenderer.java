package org.lzyzl.millager.client.render.entity.projectile;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.entity.projectile.ExplosiveArrow;
import org.lzyzl.millager.util.ResourceLocationHelper;

import static org.lzyzl.millager.Millager.MOD_ID;

public class ExplosiveArrowRenderer extends ArrowRenderer<ExplosiveArrow> {

    private static final ResourceLocation EXPLOSIVE_ARROW_LOCATION = ResourceLocationHelper.create(MOD_ID, "textures/projectile/explosive_arrow.png");

    public ExplosiveArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NonNull ResourceLocation getTextureLocation(@NonNull ExplosiveArrow arrow) {
        return EXPLOSIVE_ARROW_LOCATION;
    }
}
