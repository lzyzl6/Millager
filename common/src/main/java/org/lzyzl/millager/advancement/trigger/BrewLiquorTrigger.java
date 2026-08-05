package org.lzyzl.millager.advancement.trigger;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public class BrewLiquorTrigger extends SimpleCriterionTrigger<BrewLiquorTrigger.TriggerInstance> {

    private final ResourceLocation id;

    public BrewLiquorTrigger(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public @NonNull ResourceLocation getId() {
        return this.id;
    }

    @Override
    protected @NonNull TriggerInstance createInstance(JsonObject json, @NonNull ContextAwarePredicate player, @NonNull DeserializationContext context) {
        return new TriggerInstance(this.id, player, ItemPredicate.fromJson(json.get("ingredient")));
    }

    public void trigger(ServerPlayer player, ItemStack ingredient) {
        this.trigger(player, instance -> instance.matches(ingredient));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {

        private final ItemPredicate ingredient;

        public TriggerInstance(ResourceLocation id, ContextAwarePredicate player, ItemPredicate ingredient) {
            super(id, player);
            this.ingredient = ingredient;
        }

        public boolean matches(ItemStack stack) {
            return this.ingredient.matches(stack);
        }

        @Override
        public @NonNull JsonObject serializeToJson(@NonNull SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            json.add("ingredient", this.ingredient.serializeToJson());
            return json;
        }
    }
}
