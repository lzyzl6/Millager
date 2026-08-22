package org.lzyzl.millager.client.render.block;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.blockentity.state.SkullBlockRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lzyzl.millager.block.AbstractHeadBlock;
import org.lzyzl.millager.block.entity.HeadBlockEntity;
import org.lzyzl.millager.client.MillagerModelLayers;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static org.lzyzl.millager.Millager.MOD_ID;

public class HeadBlockRenderer implements BlockEntityRenderer<HeadBlockEntity, SkullBlockRenderState> {
    private final Function<SkullBlock.Type, SkullModelBase> modelByType;

    public static final Map<SkullBlock.Type, Identifier> SKIN_BY_TYPE = Util.make(Maps.newHashMap(), (hashMap) -> {
        hashMap.put(AbstractHeadBlock.MillagerTypes.VILLAGER, Identifier.fromNamespaceAndPath(MOD_ID, "textures/entity/villager/villager.png"));
        hashMap.put(AbstractHeadBlock.MillagerTypes.ILLAGER, Identifier.fromNamespaceAndPath(MOD_ID, "textures/entity/illager/pillager.png"));
    });

    public HeadBlockRenderer(BlockEntityRendererProvider.Context context) {
        EntityModelSet entityModelSet = context.entityModelSet();
        this.modelByType = Util.memoize((type) -> Objects.requireNonNull(createModel(entityModelSet, type)));
    }

    public static @Nullable SkullModelBase createModel(@NonNull EntityModelSet entityModelSet, SkullBlock.Type type) {
        if (type instanceof AbstractHeadBlock.MillagerTypes types) {
            SkullModelBase var10000;
            switch (types) {
                case VILLAGER -> var10000 = new VillagerHeadModel(entityModelSet.bakeLayer(MillagerModelLayers.VILLAGER_HEAD));
                case ILLAGER -> var10000 = new VillagerHeadModel(entityModelSet.bakeLayer(MillagerModelLayers.ILLAGER_HEAD));
                default -> throw new MatchException(null, null);
            }
            return var10000;
        } else {
            return null;
        }
    }

    @Override
    public SkullBlockRenderState createRenderState() {
        return new SkullBlockRenderState();
    }

    @Override
    public void extractRenderState(@NonNull HeadBlockEntity headBlockEntity, @NonNull SkullBlockRenderState skullBlockRenderState, float f, @NonNull Vec3 vec3, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(headBlockEntity, skullBlockRenderState, f, vec3, crumblingOverlay);
        skullBlockRenderState.animationProgress = headBlockEntity.getAnimation(f);
        BlockState blockState = headBlockEntity.getBlockState();
        boolean bl = blockState.getBlock() instanceof WallSkullBlock;
        skullBlockRenderState.direction = bl ? blockState.getValue(WallSkullBlock.FACING) : Direction.NORTH;
        int i = bl ? RotationSegment.convertToSegment(skullBlockRenderState.direction.getOpposite()) : blockState.getValue(SkullBlock.ROTATION);
        skullBlockRenderState.rotationDegrees = RotationSegment.convertToDegrees(i);
        skullBlockRenderState.skullType = ((AbstractSkullBlock)blockState.getBlock()).getType();
        skullBlockRenderState.renderType = getSkullRenderType(skullBlockRenderState.skullType);
    }

    @Override
    public void submit(SkullBlockRenderState skullBlockRenderState, @NonNull PoseStack poseStack, @NonNull SubmitNodeCollector submitNodeCollector, @NonNull CameraRenderState cameraRenderState) {
        SkullModelBase skullModelBase = this.modelByType.apply(skullBlockRenderState.skullType);
        SkullBlockRenderer.submitSkull(skullBlockRenderState.direction, skullBlockRenderState.rotationDegrees, skullBlockRenderState.animationProgress, poseStack, submitNodeCollector, skullBlockRenderState.lightCoords, skullModelBase, skullBlockRenderState.renderType, 0, skullBlockRenderState.breakProgress);
    }

    public static RenderType getSkullRenderType(SkullBlock.Type type) {
        return RenderTypes.entityCutoutNoCullZOffset(SKIN_BY_TYPE.get(type));
    }
}
