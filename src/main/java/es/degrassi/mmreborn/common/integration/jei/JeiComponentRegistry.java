package es.degrassi.mmreborn.common.integration.jei;

import es.degrassi.mmreborn.api.crafting.requirement.IRequirement;
import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.api.integration.jei.JeiComponentFactory;
import es.degrassi.mmreborn.api.integration.jei.RegisterJeiComponentEvent;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementType;
import es.degrassi.mmreborn.common.machine.MachineComponent;
import net.neoforged.fml.ModLoader;

import java.util.Map;

public class JeiComponentRegistry {
  private JeiComponentRegistry() {}
  private static Map<RequirementType<?>, JeiComponentFactory<?, ?>> components;

  public static void init() {
    RegisterJeiComponentEvent event = new RegisterJeiComponentEvent();
    ModLoader.postEventWrapContainerInModOrder(event);
    components = event.getComponents();
  }

  public static boolean hasJeiComponent(RequirementType<?> requirement) {
    return components.containsKey(requirement);
  }

  @SuppressWarnings("unchecked")
  public static <R extends RecipeRequirement<C, T>, C extends MachineComponent<?>, T extends IRequirement<C>, X> JeiComponentFactory<R, X> getJeiComponent(RequirementType<T> requirement) {
    return (JeiComponentFactory<R, X>) components.get(requirement);
  }
}
