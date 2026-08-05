package org.lzyzl.millager.advancement;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import org.lzyzl.millager.advancement.trigger.BrewLiquorTrigger;
import org.lzyzl.millager.advancement.trigger.HoldTotemsTrigger;
import org.lzyzl.millager.advancement.trigger.SimplePlayerTrigger;
import org.lzyzl.millager.advancement.trigger.TntKillTrigger;
import org.lzyzl.millager.registry.DeferredHolder;
import org.lzyzl.millager.registry.DeferredRegister;

import static org.lzyzl.millager.Millager.MOD_ID;

public final class MillagerCriteria {

    public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS =
            DeferredRegister.create(BuiltInRegistries.TRIGGER_TYPES, MOD_ID);

    public static final DeferredHolder<CriterionTrigger<?>, BrewLiquorTrigger> BREW_LIQUOR =
            TRIGGERS.register("brew_liquor", BrewLiquorTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, SimplePlayerTrigger> OBTAIN_LIQUOR =
            TRIGGERS.register("obtain_liquor", SimplePlayerTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, TntKillTrigger> TNT_KILL =
            TRIGGERS.register("tnt_kill", TntKillTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, SimplePlayerTrigger> HEAL_NITWIT =
            TRIGGERS.register("heal_nitwit", SimplePlayerTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, SimplePlayerTrigger> GRAND_BATTLE =
            TRIGGERS.register("grand_battle", SimplePlayerTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, HoldTotemsTrigger> HOLD_TOTEMS =
            TRIGGERS.register("hold_totems", HoldTotemsTrigger::new);

    public static void initialize() {
        TRIGGERS.register();
    }
}
