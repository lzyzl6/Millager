package org.lzyzl.millager.advancement;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import org.lzyzl.millager.advancement.trigger.BrewLiquorTrigger;
import org.lzyzl.millager.advancement.trigger.HoldTotemsTrigger;
import org.lzyzl.millager.advancement.trigger.SimplePlayerTrigger;
import org.lzyzl.millager.advancement.trigger.TntKillTrigger;
import org.lzyzl.millager.util.ResourceLocationHelper;

import java.util.function.Supplier;

import static org.lzyzl.millager.Millager.MOD_ID;

public final class MillagerCriteria {

    public static final Supplier<BrewLiquorTrigger> BREW_LIQUOR =
            register(new BrewLiquorTrigger(id("brew_liquor")));

    public static final Supplier<SimplePlayerTrigger> OBTAIN_LIQUOR =
            register(new SimplePlayerTrigger(id("obtain_liquor")));

    public static final Supplier<TntKillTrigger> TNT_KILL =
            register(new TntKillTrigger(id("tnt_kill")));

    public static final Supplier<SimplePlayerTrigger> HEAL_NITWIT =
            register(new SimplePlayerTrigger(id("heal_nitwit")));

    public static final Supplier<SimplePlayerTrigger> GRAND_BATTLE =
            register(new SimplePlayerTrigger(id("grand_battle")));

    public static final Supplier<HoldTotemsTrigger> HOLD_TOTEMS =
            register(new HoldTotemsTrigger(id("hold_totems")));

    private static ResourceLocation id(String path) {
        return ResourceLocationHelper.create(MOD_ID, path);
    }

    private static <T extends CriterionTrigger<?>> Supplier<T> register(T trigger) {
        T registered = CriteriaTriggers.register(trigger);
        return () -> registered;
    }

    public static void initialize() {
    }
}
