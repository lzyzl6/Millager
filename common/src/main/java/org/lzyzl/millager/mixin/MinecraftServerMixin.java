package org.lzyzl.millager.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.lzyzl.millager.Millager;
import org.lzyzl.millager.behavior.MiscConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Inject(method = "createLevels", at = @At("TAIL"))
    private void millager$addInfantryHuts(CallbackInfo ci) {
        if (!MiscConfig.GENERATE_INFANTRY_HUTS) return;
        Registry<StructureTemplatePool> pools = ((MinecraftServer) (Object) this).registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL);
        for (String style : List.of("plains", "desert", "savanna", "snowy", "taiga")) {
            millager$appendInfantryHuts(pools, style);
        }
    }

    @Unique
    private void millager$appendInfantryHuts(Registry<StructureTemplatePool> pools, String style) {
        StructureTemplatePool targetPool = pools.getValue(Identifier.fromNamespaceAndPath("minecraft", "village/" + style + "/houses"));
        StructureTemplatePool sourcePool = pools.getValue(Identifier.fromNamespaceAndPath(Millager.MOD_ID, "village/" + style + "/infantry_huts"));
        if (targetPool == null || sourcePool == null) {
            Millager.LOGGER.warn("Unable to inject infantry huts for village style {}", style);
            return;
        }

        StructureTemplatePoolAccessor target = (StructureTemplatePoolAccessor) (Object) targetPool;
        StructureTemplatePoolAccessor source = (StructureTemplatePoolAccessor) (Object) sourcePool;
        List<Pair<StructurePoolElement, Integer>> templates = new ArrayList<>(target.millager$getRawTemplates());
        boolean changed = false;
        for (Pair<StructurePoolElement, Integer> template : source.millager$getRawTemplates()) {
            if (templates.contains(template)) continue;
            templates.add(template);
            for (int i = 0; i < template.getSecond(); i++) target.millager$getTemplates().add(template.getFirst());
            changed = true;
        }
        if (changed) {
            target.millager$setRawTemplates(templates);
            target.millager$setMaxSize(Integer.MIN_VALUE);
        }
    }
}
