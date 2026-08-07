package org.lzyzl.millager.client;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import org.lzyzl.millager.MillagerBlocks;
import org.lzyzl.millager.MillagerEntityTypes;
import org.lzyzl.millager.client.render.block.HeadBlockRenderer;
import org.lzyzl.millager.client.render.entity.golem.BeeGolemRenderer;
import org.lzyzl.millager.client.render.entity.millager.*;
import org.lzyzl.millager.client.render.entity.projectile.ExplosiveArrowRenderer;
import org.lzyzl.millager.client.render.entity.projectile.TNTOnAStickRenderer;

public class MillagerEntityRenderers {

    public static void registerEntityRenderers(ClientRegistrationContext ctx) {
        ctx.registerEntityRenderer(MillagerEntityTypes.TNT_Projectile.get(), TNTOnAStickRenderer::new);
        ctx.registerEntityRenderer(MillagerEntityTypes.Cocktail_Projectile.get(), ThrownItemRenderer::new);
        ctx.registerEntityRenderer(MillagerEntityTypes.Cocktail_Plus_Projectile.get(), ThrownItemRenderer::new);
        ctx.registerEntityRenderer(MillagerEntityTypes.Explosive_Arrow.get(), ExplosiveArrowRenderer::new);

        ctx.registerEntityRenderer(MillagerEntityTypes.Bee_Golem.get(), BeeGolemRenderer::new);
        ctx.registerEntityRenderer(MillagerEntityTypes.Archers.get(), ArcherRenderer::new);
        ctx.registerEntityRenderer(MillagerEntityTypes.Breachers.get(), BreacherRenderer::new);
        ctx.registerEntityRenderer(MillagerEntityTypes.Lancers.get(), LancerRenderer::new);
        ctx.registerEntityRenderer(MillagerEntityTypes.Doctors.get(), DoctorRenderer::new);
        ctx.registerEntityRenderer(MillagerEntityTypes.Swordmasters.get(), SwordmasterRenderer::new);
        ctx.registerEntityRenderer(MillagerEntityTypes.Maulers.get(), MaulerRenderer::new);
        ctx.registerEntityRenderer(MillagerEntityTypes.Rioters.get(), RioterRenderer::new);
        ctx.registerEntityRenderer(MillagerEntityTypes.Scouters.get(), ScouterRenderer::new);

        ctx.registerBlockEntityRenderer(MillagerBlocks.HEAD_BLOCK_ENTITY.get(), HeadBlockRenderer::new);
    }
}
