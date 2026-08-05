package org.lzyzl.millager.client.render.entity.millager;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.resources.ResourceLocation;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.entity.millager.AbstractMillager;

public class MillagerRenderer<T extends AbstractMillager> extends MobRenderer<T, MillagerModel<T>> {

    private final ResourceLocation texture;

    public MillagerRenderer(EntityRendererProvider.Context context, MillagerModel<T> model, float shadow, ResourceLocation texture) {
        super(context, model, shadow);
        this.texture = texture;
        this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getItemInHandRenderer()));
        this.addLayer(new MillagerItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public @NonNull ResourceLocation getTextureLocation(@NonNull T entity) {
        return texture;
    }
}
