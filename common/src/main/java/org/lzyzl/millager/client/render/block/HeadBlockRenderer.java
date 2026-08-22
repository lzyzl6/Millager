package org.lzyzl.millager.client.render.block;
import net.minecraft.Util;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.lzyzl.millager.block.AbstractHeadBlock;
import org.lzyzl.millager.block.entity.HeadBlockEntity;
import org.lzyzl.millager.client.MillagerModelLayers;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static org.lzyzl.millager.Millager.MOD_ID;

public class HeadBlockRenderer implements BlockEntityRenderer<HeadBlockEntity> {
    private final Function<SkullBlock.Type, SkullModelBase> modelByType;

    public static final Map<SkullBlock.Type, ResourceLocation> SKIN_BY_TYPE = Util.make(Maps.newHashMap(), (hashMap) -> {
        hashMap.put(AbstractHeadBlock.MillagerTypes.VILLAGER, ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/entity/villager/villager.png"));
        hashMap.put(AbstractHeadBlock.MillagerTypes.ILLAGER, ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/entity/illager/pillager.png"));
    });

    public HeadBlockRenderer(BlockEntityRendererProvider.Context context) {
        EntityModelSet entityModelSet = context.getModelSet();
        this.modelByType = Util.memoize((type) -> Objects.requireNonNull(createModel(entityModelSet, type)));
    }

    public static SkullModelBase createModel(EntityModelSet entityModelSet, SkullBlock.Type type) {
        if (type instanceof AbstractHeadBlock.MillagerTypes types) {
            return switch (types) {
                case VILLAGER -> new VillagerHeadModel(entityModelSet.bakeLayer(MillagerModelLayers.VILLAGER_HEAD));
                case ILLAGER -> new VillagerHeadModel(entityModelSet.bakeLayer(MillagerModelLayers.ILLAGER_HEAD));
            };
        }
        return null;
    }

    public static RenderType getSkullRenderType(SkullBlock.Type type) {
        ResourceLocation id = SKIN_BY_TYPE.get(type);
        if (id != null) {
            return RenderType.entityCutoutNoCullZOffset(id);
        }
        return RenderType.entityCutoutNoCullZOffset(ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/skeleton/skeleton.png"));
    }

    @Override
    public void render(HeadBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState blockState = entity.getBlockState();
        Direction direction = blockState.getBlock() instanceof WallSkullBlock ? blockState.getValue(WallSkullBlock.FACING) : null;
        float animation = entity.getAnimation(partialTick);
        float rotation = direction != null ? direction.getOpposite().toYRot()
                : blockState.getValue(SkullBlock.ROTATION) * 22.5F;
        SkullBlock.Type type = ((AbstractSkullBlock) blockState.getBlock()).getType();
        SkullModelBase model = this.modelByType.apply(type);
        ResourceLocation texture = SKIN_BY_TYPE.get(type);

        if (model != null && texture != null) {
            RenderType renderType = RenderType.entityCutoutNoCullZOffset(texture);
            SkullBlockRenderer.renderSkull(direction, rotation, animation, poseStack, buffer, packedLight, model, renderType);
        }
    }
}
