package org.lzyzl.millager.advancement.trigger;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.NonNull;

public class SimplePlayerTrigger extends SimpleCriterionTrigger<SimplePlayerTrigger.TriggerInstance> {

    private final ResourceLocation id;

    public SimplePlayerTrigger(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public @NonNull ResourceLocation getId() {
        return this.id;
    }

    @Override
    protected @NonNull TriggerInstance createInstance(@NonNull JsonObject json, @NonNull ContextAwarePredicate player, @NonNull DeserializationContext context) {
        return new TriggerInstance(this.id, player);
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, instance -> true);
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {

        public TriggerInstance(ResourceLocation id, ContextAwarePredicate player) {
            super(id, player);
        }
    }
}
