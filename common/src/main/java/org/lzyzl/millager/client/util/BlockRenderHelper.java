package org.lzyzl.millager.client.util;

import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;
import org.lzyzl.millager.MillagerBlocks;
import org.lzyzl.millager.block.entity.BrewingCauldronBlockEntity;
import org.lzyzl.millager.client.ClientRegistrationContext;

import java.util.List;

public class BlockRenderHelper {

    // 26.1 的 BlockTintSource 颜色按 ARGB 解析并尊重 alpha(vanilla 常量均为负值 = 0xFF alpha)。
    // 旧 BlockColor 系统忽略 alpha 强制不透明,这里的 RGB 常量必须补 0xFF alpha,否则透明 → 水面/火焰消失。
    private static final int OPAQUE = 0xFF000000;

    public static void registerBlockColors(ClientRegistrationContext ctx) {
        ctx.registerBlockColor(List.of(BlockTintSources.constant(OPAQUE | 5578058)), MillagerBlocks.LIQUOR_CAULDRON.get());

        ctx.registerBlockColor(List.of(new BlockTintSource() {
            @Override
            public int color(@NonNull BlockState state) {
                return OPAQUE | 4159204;
            }

            @Override
            public int colorInWorld(@NonNull BlockState state, @NonNull BlockAndTintGetter level, @NonNull BlockPos pos) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof BrewingCauldronBlockEntity brewingBe) {
                    return OPAQUE | BrewingCauldronBlockEntity.getInterpolatedColor(brewingBe.getBrewingProgress());
                }
                return OPAQUE | 4159204;
            }
        }), MillagerBlocks.BREWING_CAULDRON.get());

        ctx.registerBlockColor(List.of(BlockTintSources.constant(OPAQUE | 14589720)), MillagerBlocks.TIMED_FIRE.get());
    }

    /*
     * 特殊模型渲染器(head)注册改在各加载器客户端入口完成,引用 common 的 HeadSpecialRenderer:
     *   NeoForge: RegisterSpecialModelRendererEvent.register(id, HeadSpecialRenderer.Unbaked.MAP_CODEC)
     *   Fabric:   对应的 SpecialModelRenderer 注册点
     *
     * NeoForge 1.21.4+ 中方块渲染层(CUTOUT / TRANSLUCENT)通过模型 JSON 的 "render_type" 字段设置,
     * 不再走 Java API。
     */
}
