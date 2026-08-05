package org.lzyzl.millager;

import net.minecraft.world.level.block.ComposterBlock;
import org.lzyzl.millager.advancement.MillagerCriteria;
import org.lzyzl.millager.block.MillagerMenuType;
import org.lzyzl.millager.util.DispenserHelper;
import org.lzyzl.millager.util.LiquorCauldronHelper;
import org.lzyzl.millager.worldgen.MillagerStructures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Millager {

    public static final String MOD_ID = "millager";
    public static final Logger LOGGER = LoggerFactory.getLogger("Millager");

    private Millager() {
    }

    public static void init() {
        MillagerSounds.initialize();
        MillagerBlocks.initialize();
        MillagerMenuType.initialize();
        MillagerStructures.initialize();
        MillagerEntityTypes.initialize();
        MillagerItems.initialize();
        MillagerItemGroups.initialize();
        MillagerProfessionAndPoi.initialize();
        MillagerGameRules.initialize();
        MillagerCriteria.initialize();
    }

    public static void commonSetup() {
        ComposterBlock.COMPOSTABLES.put(MillagerItems.rose.asItem(), 0.65F);
        DispenserHelper.registerDispenserProjectile();
        LiquorCauldronHelper.registerLiquorInteraction();
        MillagerProfessionAndPoi.registerPoiBlockStates();
    }
}
