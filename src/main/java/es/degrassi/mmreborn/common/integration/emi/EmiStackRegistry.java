package es.degrassi.mmreborn.common.integration.emi;

import es.degrassi.mmreborn.api.crafting.requirement.IRequirement;
import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.api.integration.emi.EmiStackFactory;
import es.degrassi.mmreborn.api.integration.emi.RegisterEmiRequirementToStackEvent;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementType;
import es.degrassi.mmreborn.common.machine.MachineComponent;
import net.neoforged.fml.ModLoader;

import java.util.Map;

public class EmiStackRegistry {
  private EmiStackRegistry() {}
  private static Map<RequirementType<?, ?>, EmiStackFactory<?>> stacks;

  public static void init() {
    RegisterEmiRequirementToStackEvent event = new RegisterEmiRequirementToStackEvent();
    ModLoader.postEventWrapContainerInModOrder(event);
    stacks = event.getStacks();
  }

  public static boolean hasEmiStack(RequirementType<?, ?> type) {
    return stacks.containsKey(type);
  }

  @SuppressWarnings("unchecked")
  public static <R extends RecipeRequirement<C, T, X>, T extends IRequirement<C, X>, C extends MachineComponent<X>, X> EmiStackFactory<R> getStack(RequirementType<T, X> type) {
    return (EmiStackFactory<R>) stacks.get(type);
  }
}
