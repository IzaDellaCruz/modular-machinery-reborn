package es.degrassi.mmreborn.common.integration.emi;

import es.degrassi.mmreborn.api.crafting.requirement.IRequirement;
import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.api.integration.emi.EmiIngredientFactory;
import es.degrassi.mmreborn.api.integration.emi.RegisterEmiRequirementToIngredientEvent;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementType;
import es.degrassi.mmreborn.common.machine.MachineComponent;
import net.neoforged.fml.ModLoader;

import java.util.Map;

public class EmiIngredientRegistry {
  private EmiIngredientRegistry() {}
  private static Map<RequirementType<?>, EmiIngredientFactory<?>> stacks;

  public static void init() {
    RegisterEmiRequirementToIngredientEvent event = new RegisterEmiRequirementToIngredientEvent();
    ModLoader.postEventWrapContainerInModOrder(event);
    stacks = event.getStacks();
  }

  public static boolean hasEmiIngredient(RequirementType<?> type) {
    return stacks.containsKey(type);
  }

  @SuppressWarnings("unchecked")
  public static <R extends RecipeRequirement<C, T>, T extends IRequirement<C>, C extends MachineComponent<?>> EmiIngredientFactory<R> getIngredient(RequirementType<T> type) {
    return (EmiIngredientFactory<R>) stacks.get(type);
  }
}
