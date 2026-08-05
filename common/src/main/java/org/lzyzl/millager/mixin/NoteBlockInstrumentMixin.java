package org.lzyzl.millager.mixin;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import org.lzyzl.millager.MillagerSounds;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mixin(NoteBlockInstrument.class)
public class NoteBlockInstrumentMixin {

    @Mutable
    @Shadow
    @Final
    private static NoteBlockInstrument[] $VALUES;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void millager$addInstruments(CallbackInfo ci) {
        List<NoteBlockInstrument> instruments = new ArrayList<>(Arrays.asList($VALUES));
        int nextId = instruments.size();
        instruments.add(millager$addVariant("VILLAGER", nextId++, "villager",
                MillagerSounds.NOTE_BLOCK_IMITATE_VILLAGER));
        instruments.add(millager$addVariant("ILLAGER", nextId, "illager",
                MillagerSounds.NOTE_BLOCK_IMITATE_ILLAGER));
        $VALUES = instruments.toArray(new NoteBlockInstrument[0]);
    }

    @Unique
    private static NoteBlockInstrument millager$addVariant(String internalName, int internalId, String name, Holder<SoundEvent> sound) {
        return NoteBlockInstrumentInvoker.millager$create(internalName, internalId, name, sound, NoteBlockInstrument.Type.MOB_HEAD);
    }
}
