package org.lzyzl.millager.client.render.entity.millager;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.lzyzl.millager.client.MillagerModelLayers;
import org.lzyzl.millager.entity.millager.Mauler;

import static org.lzyzl.millager.Millager.MOD_ID;

public class MaulerRenderer extends MillagerRenderer<Mauler> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/entity/millager/mauler.png");
    private static final ResourceLocation CRACKINESS_LOW_TEXTURE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/entity/millager/mauler_cracked_low.png");
    private static final ResourceLocation CRACKINESS_MEDIUM_TEXTURE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/entity/millager/mauler_cracked_medium.png");
    private static final ResourceLocation CRACKINESS_HIGH_TEXTURE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/entity/millager/mauler_cracked_high.png");

    public MaulerRenderer(EntityRendererProvider.Context context) {
        super(context, new MillagerModel<>(context.bakeLayer(MillagerModelLayers.MAULER)), 0.5f, TEXTURE);
    }

    @Override
    public ResourceLocation getTextureLocation(Mauler mauler) {
        if (mauler.isInvisible()) return TEXTURE;
        return switch (mauler.getCrackiness()) {
            case LOW -> CRACKINESS_LOW_TEXTURE;
            case MEDIUM -> CRACKINESS_MEDIUM_TEXTURE;
            case HIGH -> CRACKINESS_HIGH_TEXTURE;
            default -> TEXTURE;
        };
    }
}
