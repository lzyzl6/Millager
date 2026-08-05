package org.lzyzl.millager.advancement.trigger;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.NonNull;

public class HoldTotemsTrigger extends SimpleCriterionTrigger<HoldTotemsTrigger.TriggerInstance> {

    private final ResourceLocation id;

    public HoldTotemsTrigger(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public @NonNull ResourceLocation getId() {
        return this.id;
    }

    @Override
    protected @NonNull TriggerInstance createInstance(JsonObject json, @NonNull ContextAwarePredicate player, @NonNull DeserializationContext context) {
        return new TriggerInstance(this.id, player, MinMaxBounds.Ints.fromJson(json.get("count")));
    }

    public void trigger(ServerPlayer player, int totemCount) {
        this.trigger(player, instance -> instance.matches(totemCount));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {

        private final MinMaxBounds.Ints count;

        public TriggerInstance(ResourceLocation id, ContextAwarePredicate player, MinMaxBounds.Ints count) {
            super(id, player);
            this.count = count;
        }

        public boolean matches(int totemCount) {
            return this.count.matches(totemCount);
        }

        @Override
        public @NonNull JsonObject serializeToJson(@NonNull SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            json.add("count", this.count.serializeToJson());
            return json;
        }
    }
}
