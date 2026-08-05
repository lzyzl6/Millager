package org.lzyzl.millager.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;
import org.lzyzl.millager.client.render.item.BucklerModel;
import org.lzyzl.millager.client.render.block.VillagerHeadModel;
import org.lzyzl.millager.client.render.entity.golem.BeeGolemModel;
import org.lzyzl.millager.client.render.entity.golem.BeeGolemFlashModel;
import org.lzyzl.millager.client.render.entity.millager.MillagerModel;
import org.lzyzl.millager.client.render.entity.projectile.TNTOnAStickModel;

import static org.lzyzl.millager.Millager.MOD_ID;

public class MillagerModelLayers {

    public static final ModelLayerLocation BUCKLER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(MOD_ID, "buckler"), "main");
    public static final ModelLayerLocation TNT_ON_A_STICK = new ModelLayerLocation(Identifier.fromNamespaceAndPath(MOD_ID, "tnt_on_a_stick"), "main");
    public static final ModelLayerLocation BEE_GOLEM = new ModelLayerLocation(Identifier.fromNamespaceAndPath(MOD_ID,"bee_golem"), "main");
    public static final ModelLayerLocation BEE_GOLEM_FLASH = new ModelLayerLocation(Identifier.fromNamespaceAndPath(MOD_ID,"bee_golem_flash"), "main");

    public static final ModelLayerLocation DOCTOR = new ModelLayerLocation(Identifier.fromNamespaceAndPath(MOD_ID,"doctor"), "main");
    public static final ModelLayerLocation LANCER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(MOD_ID,"lancer"), "main");
    public static final ModelLayerLocation ARCHER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(MOD_ID,"archer"), "main");
    public static final ModelLayerLocation BREACHER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(MOD_ID,"breacher"), "main");
    public static final ModelLayerLocation SWORDMASTER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(MOD_ID,"swordmaster"), "main");
    public static final ModelLayerLocation MAULER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(MOD_ID,"mauler"), "main");
    public static final ModelLayerLocation RIOTER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(MOD_ID,"rioter"), "main");
    public static final ModelLayerLocation SCHOLAR = new ModelLayerLocation(Identifier.fromNamespaceAndPath(MOD_ID,"scholar"), "main");
//    public static final ModelLayerLocation CONJURER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(MOD_ID,"conjurer"), "main");
    public static final ModelLayerLocation SCOUTER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(MOD_ID,"scouter"), "main");

    public static final ModelLayerLocation VILLAGER_HEAD = new ModelLayerLocation(Identifier.fromNamespaceAndPath(MOD_ID,"villager_head"), "main");
    public static final ModelLayerLocation ILLAGER_HEAD = new ModelLayerLocation(Identifier.fromNamespaceAndPath(MOD_ID,"illager_head"), "main");


    public static void registerModelLayers(ClientRegistrationContext ctx) {
        ctx.registerLayerDefinition(BUCKLER, BucklerModel::createLayer);
        ctx.registerLayerDefinition(TNT_ON_A_STICK, TNTOnAStickModel::createLayer);
        ctx.registerLayerDefinition(BEE_GOLEM, BeeGolemModel::createLayer);
        ctx.registerLayerDefinition(BEE_GOLEM_FLASH, BeeGolemFlashModel::createLayer);

        ctx.registerLayerDefinition(DOCTOR, MillagerModel::createLayer);
        ctx.registerLayerDefinition(LANCER, MillagerModel::createLayer);
        ctx.registerLayerDefinition(ARCHER, MillagerModel::createLayer);
        ctx.registerLayerDefinition(BREACHER, MillagerModel::createLayer);
        ctx.registerLayerDefinition(SWORDMASTER, MillagerModel::createLayer);
        ctx.registerLayerDefinition(MAULER, MillagerModel::createLayer);
        ctx.registerLayerDefinition(RIOTER, MillagerModel::createLayer);
//        ctx.registerLayerDefinition(SCHOLAR, MillagerModel::createLayer);
//        ctx.registerLayerDefinition(CONJURER, MillagerModel::createLayer);
        ctx.registerLayerDefinition(SCOUTER, MillagerModel::createLayer);

        ctx.registerLayerDefinition(VILLAGER_HEAD, VillagerHeadModel::createLayer);
        ctx.registerLayerDefinition(ILLAGER_HEAD, VillagerHeadModel::createLayer);
    }
}
