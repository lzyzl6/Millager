package org.lzyzl.millager.client.render.entity.millager;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.sprite.AtlasManager;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.client.MillagerModelLayers;
import org.lzyzl.millager.client.util.BookAnimationController;
import org.lzyzl.millager.entity.millager.Scholar;

import static org.lzyzl.millager.Millager.MOD_ID;

public class ScholarRenderer extends MillagerRenderer<Scholar, ScholarRenderState> {

    private static final Identifier SH_LOCATION = Identifier.fromNamespaceAndPath(MOD_ID,"textures/entity/millager/scholar.png");
    private static final RenderType BOOK_RENDER_TYPE = RenderTypes.entitySolid(EnchantTableRenderer.BOOK_TEXTURE.texture());
    private final BookModel bookModel;
    private final AtlasManager atlasManager;

    public ScholarRenderer(EntityRendererProvider.Context context) {
        super(context, new MillagerModel<>(context.bakeLayer(MillagerModelLayers.SCHOLAR)), 0.5f);
        this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
        this.atlasManager = new AtlasManager(new TextureManager(context.getResourceManager()), 4);
    }

    @Override
    public ScholarRenderState createRenderState() {
        return new ScholarRenderState();
    }

    @Override
    public @NonNull Identifier getTextureLocation(ScholarRenderState livingEntityRenderState) {
        return SH_LOCATION;
    }

    @Override
    public void extractRenderState(Scholar scholar, ScholarRenderState state, float f) {
        super.extractRenderState(scholar, state, f);
        BookAnimationController ctrl = scholar.bookController;

        // 1. 物理位置插值
        state.bX = Mth.lerp(f, ctrl.prevX, ctrl.x);
        state.bY = Mth.lerp(f, ctrl.prevY, ctrl.y);
        state.bZ = Mth.lerp(f, ctrl.prevZ, ctrl.z);

        state.bYaw = ctrl.prevYaw + Mth.degreesDifference(ctrl.prevYaw, ctrl.yaw) * f;
        state.bPitch = Mth.lerp(f, ctrl.prevPitch, ctrl.pitch);
        state.bRoll = Mth.lerp(f, ctrl.prevRoll, ctrl.roll);

        state.bOpen = Mth.lerp(f, ctrl.prevOpen, ctrl.open);
        state.bFlip1 = Mth.lerp(f, ctrl.prevFlip1, ctrl.flip1);
        state.bFlip2 = Mth.lerp(f, ctrl.prevFlip2, ctrl.flip2);

        state.animationTime = (float)scholar.tickCount + f;
        state.bookColor = ctrl.getBookColor() | 0xFF000000;
    }

    @Override
    public void submit(ScholarRenderState state, @NonNull PoseStack poseStack, @NonNull SubmitNodeCollector collector, @NonNull CameraRenderState cameraState) {
        super.submit(state, poseStack, collector, cameraState);

        poseStack.pushPose();

        poseStack.translate(state.bX, state.bY, state.bZ);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.bYaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(state.bPitch));
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.bRoll));

        BookModel.State modelState = BookModel.State.forAnimation(
                state.animationTime,
                Mth.clamp(state.bFlip1, 0.0F, 1.0F),
                Mth.clamp(state.bFlip2, 0.0F, 1.0F),
                state.bOpen
        );

        collector.submitModel(
                this.bookModel,
                modelState,
                poseStack,
                BOOK_RENDER_TYPE,
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                state.bookColor,
                this.atlasManager.get(EnchantTableRenderer.BOOK_TEXTURE),
                0,
                null
        );

        poseStack.popPose();
    }
}
