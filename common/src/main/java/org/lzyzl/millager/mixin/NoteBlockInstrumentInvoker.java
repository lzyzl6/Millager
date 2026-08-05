package org.lzyzl.millager.mixin;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(NoteBlockInstrument.class)
public interface NoteBlockInstrumentInvoker {
    @Invoker("<init>")
    static NoteBlockInstrument millager$create(String internalName, int internalId, String name, Holder<SoundEvent> sound, NoteBlockInstrument.Type type) {
        throw new AssertionError();
    }
}
