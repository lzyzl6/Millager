package org.lzyzl.millager;

import org.lzyzl.millager.advancement.MillagerCriteria;
import org.lzyzl.millager.block.MillagerMenuType;
import net.minecraft.world.level.block.ComposterBlock;
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

    /**
     * 通用内容注册。各加载器入口在设置好注册工厂(NeoForge 还需先设置 modId 事件总线)后调用一次。
     * 顺序保证依赖:方块先于物品(BlockItem/方块实体),实体类型先于物品(刷怪蛋),POI 依赖方块。
     */
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

    /**
     * 公共 setup 阶段逻辑(需在内容注册完成后、主线程执行)。
     * NeoForge: FMLCommonSetupEvent.enqueueWork / Fabric: onInitialize 末尾。
     */
    public static void commonSetup() {
        ComposterBlock.COMPOSTABLES.put(MillagerItems.rose.asItem(), 0.65F);
        DispenserHelper.registerDispenserProjectile();
        LiquorCauldronHelper.registerLiquorInteraction();
        MillagerProfessionAndPoi.registerPoiBlockStates();
    }
}
