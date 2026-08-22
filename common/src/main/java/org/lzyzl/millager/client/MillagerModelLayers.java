package org.lzyzl.millager.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import org.lzyzl.millager.client.render.block.VillagerHeadModel;
import org.lzyzl.millager.client.render.entity.golem.BeeGolemModel;
import org.lzyzl.millager.client.render.entity.golem.BeeGolemFlashModel;
import org.lzyzl.millager.client.render.entity.millager.MillagerModel;
import org.lzyzl.millager.client.render.entity.projectile.TNTOnAStickModel;
import org.lzyzl.millager.util.ResourceLocationHelper;

import static org.lzyzl.millager.Millager.MOD_ID;

public class MillagerModelLayers {

    public static final ModelLayerLocation TNT_ON_A_STICK = new ModelLayerLocation(ResourceLocationHelper.create(MOD_ID, "tnt_on_a_stick"), "main");
    public static final ModelLayerLocation BEE_GOLEM = new ModelLayerLocation(ResourceLocationHelper.create(MOD_ID, "bee_golem"), "main");
    public static final ModelLayerLocation BEE_GOLEM_FLASH = new ModelLayerLocation(ResourceLocationHelper.create(MOD_ID, "bee_golem_flash"), "main");

    public static final ModelLayerLocation DOCTOR = new ModelLayerLocation(ResourceLocationHelper.create(MOD_ID, "doctor"), "main");
    public static final ModelLayerLocation BREACHER = new ModelLayerLocation(ResourceLocationHelper.create(MOD_ID, "breacher"), "main");
    public static final ModelLayerLocation LANCER = new ModelLayerLocation(ResourceLocationHelper.create(MOD_ID, "lancer"), "main");
    public static final ModelLayerLocation ARCHER = new ModelLayerLocation(ResourceLocationHelper.create(MOD_ID, "archer"), "main");
    public static final ModelLayerLocation SWORDMASTER = new ModelLayerLocation(ResourceLocationHelper.create(MOD_ID, "swordmaster"), "main");
    public static final ModelLayerLocation MAULER = new ModelLayerLocation(ResourceLocationHelper.create(MOD_ID, "mauler"), "main");
    public static final ModelLayerLocation RIOTER = new ModelLayerLocation(ResourceLocationHelper.create(MOD_ID, "rioter"), "main");
    public static final ModelLayerLocation SCOUTER = new ModelLayerLocation(ResourceLocationHelper.create(MOD_ID, "scouter"), "main");

    public static final ModelLayerLocation VILLAGER_HEAD = new ModelLayerLocation(ResourceLocationHelper.create(MOD_ID, "villager_head"), "main");
    public static final ModelLayerLocation ILLAGER_HEAD = new ModelLayerLocation(ResourceLocationHelper.create(MOD_ID, "illager_head"), "main");


    public static void registerModelLayers(ClientRegistrationContext ctx) {
        ctx.registerLayerDefinition(TNT_ON_A_STICK, TNTOnAStickModel::createLayer);
        ctx.registerLayerDefinition(BEE_GOLEM, BeeGolemModel::createLayer);
        ctx.registerLayerDefinition(BEE_GOLEM_FLASH, BeeGolemFlashModel::createLayer);

        registerMillagerModelLayers(ctx);
        ctx.registerLayerDefinition(VILLAGER_HEAD, VillagerHeadModel::createLayer);
        ctx.registerLayerDefinition(ILLAGER_HEAD, VillagerHeadModel::createLayer);
    }

    private static void registerMillagerModelLayers(ClientRegistrationContext ctx) {
        for (ModelLayerLocation location : new ModelLayerLocation[]{MillagerModelLayers.DOCTOR, MillagerModelLayers.BREACHER, MillagerModelLayers.LANCER, MillagerModelLayers.ARCHER, MillagerModelLayers.SWORDMASTER, MillagerModelLayers.MAULER, MillagerModelLayers.RIOTER, MillagerModelLayers.SCOUTER}) ctx.registerLayerDefinition(location, MillagerModel::createLayer);
    }
}
