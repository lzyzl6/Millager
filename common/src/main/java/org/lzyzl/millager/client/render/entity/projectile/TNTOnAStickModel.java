package org.lzyzl.millager.client.render.entity.projectile;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public class TNTOnAStickModel extends EntityModel<TNTOnAStickRenderState> {

    private final ModelPart main;
    private final ModelPart core;
    private final ModelPart arm;

    public TNTOnAStickModel(ModelPart root) {
        super(root, RenderTypes::entitySolid);
        this.main = root.getChild("main");
        this.core = this.main.getChild("core");
        this.arm = this.core.getChild("arm");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition main = partdefinition.addOrReplaceChild("main", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 16.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        PartDefinition core = main.addOrReplaceChild("core", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -8.0F, 0.0F));
        PartDefinition arm = core.addOrReplaceChild("arm", CubeListBuilder.create().texOffs(28, 35).addBox(-2.0F, 8.0F, -2.0F, 4.0F, 16.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    
    public void setupAnim(TNTOnAStickRenderState tntOnAStickRenderState) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        super.setupAnim(tntOnAStickRenderState);
        this.main.yRot = tntOnAStickRenderState.yRot * ((float)Math.PI / 180f);
        this.main.xRot = tntOnAStickRenderState.totalRotation * ((float)Math.PI / 180f);
    }
}
