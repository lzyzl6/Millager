package org.lzyzl.millager.mixin.client;

import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityType.class)
public interface EntityTypeAccessor {

    @Accessor("factory")
    EntityType.EntityFactory<?> millager$getFactory();

}
