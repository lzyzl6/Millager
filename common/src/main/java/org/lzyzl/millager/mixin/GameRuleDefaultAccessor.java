package org.lzyzl.millager.mixin;

import net.minecraft.world.level.gamerules.GameRule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameRule.class)
public interface GameRuleDefaultAccessor<T> {

    @Mutable
    @Accessor("defaultValue")
    void millager$setDefaultValue(T defaultValue);
}
