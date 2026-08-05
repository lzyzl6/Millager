package org.lzyzl.millager;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import org.lzyzl.millager.util.ResourceLocationHelper;

import static org.lzyzl.millager.Millager.MOD_ID;

public class MillagerSounds {

    public static final Holder<SoundEvent> QUAFFING = registerForHolder("quaffing");

    public static final Holder<SoundEvent> NOTE_BLOCK_IMITATE_VILLAGER = registerForHolder("block.note_block.imitate.villager");
    public static final Holder<SoundEvent> NOTE_BLOCK_IMITATE_ILLAGER = registerForHolder("block.note_block.imitate.illager");

    public static final SoundEvent ARCHER_CRAFTING_ARROW = registerSound("entity.archer.crafting_arrow");
    public static final SoundEvent ARCHER_DRINKING_POTION = registerSound("entity.archer.drinking_potion");

    public static final SoundEvent BEE_GOLEM_GLASS_BREAK = registerSound("entity.bee_golem.glass_break");
    public static final SoundEvent BEE_GOLEM_WAX_BREAK = registerSound("entity.bee_golem.wax_break");
    public static final SoundEvent BEE_GOLEM_BURST_OUT = registerSound("entity.bee_golem.burst_out");
    public static final SoundEvent BEE_GOLEM_HURT = registerSound("entity.bee_golem.hurt");

    public static final SoundEvent DOCTOR_DRINKING_POTION = registerSound("entity.doctor.drinking_potion");

    public static final SoundEvent SWORD_SHIELD_BLOCK = registerSound("entity.swordmaster.shield_block");

    public static final SoundEvent RIOTER_TAUNTING = registerSound("entity.rioter.taunting");

    public static final SoundEvent BREACHER_SHIELD_BASH = registerSound("entity.breacher.shield_bash");

    public static final SoundEvent VILLAGER_WORK_COMMANDER = registerSound("entity.villager.work_commander");

    public static final SoundEvent REINFORCE_HORN = registerSound("reinforce_horn");

    public static final SoundEvent MUSTER_ORDER_START = registerSound("item.muster_order.start");
    public static final SoundEvent MUSTER_ORDER_COMPLETE = registerSound("item.muster_order.complete");
    public static final SoundEvent PROFESSION_ORDER_BREAK = registerSound("item.profession_order.break");

    public static final SoundEvent TOTEM_INFUSE = registerSound("totem_infuse");

    public static final SoundEvent ROSE_WILT = registerSound("block.rose.wilt");


    private static Holder.Reference<SoundEvent> registerForHolder(String string) {
        ResourceLocation identifier = ResourceLocationHelper.create(MOD_ID, string);
        return registerForHolder(identifier, identifier);
    }

    private static Holder.Reference<SoundEvent> registerForHolder(ResourceLocation identifier, ResourceLocation identifier2) {
        return Registry.registerForHolder(BuiltInRegistries.SOUND_EVENT, identifier, SoundEvent.createVariableRangeEvent(identifier2));
    }

    private static SoundEvent registerSound(String id) {
        ResourceLocation identifier = ResourceLocationHelper.create(MOD_ID, id);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, identifier, SoundEvent.createVariableRangeEvent(identifier));
    }

    public static void initialize() {

    }

}
