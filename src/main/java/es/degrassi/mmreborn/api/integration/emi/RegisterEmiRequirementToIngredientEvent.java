package es.degrassi.mmreborn.api.integration.emi;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import es.degrassi.mmreborn.api.crafting.requirement.IRequirement;
import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementType;
import es.degrassi.mmreborn.common.machine.MachineComponent;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

import java.util.Map;

public class RegisterEmiRequirementToIngredientEvent extends Event implements IModBusEvent {
  private final Map<RequirementType<?, ?, ?>, EmiIngredientFactory<?, ?, ?, ?>> stacks = Maps.newHashMap();

  public <R extends RecipeRequirement<T, C, X>, C extends IRequirement<T, X>, T extends MachineComponent<X>, X> void register(RequirementType<C, T, X> requirement, EmiIngredientFactory<R, C, T, X> factory) {
    if (stacks.containsKey(requirement)) {
      throw new IllegalArgumentException("Emi ingredient already registered for requirement: " + requirement.getCodec().name());
    }
    stacks.put(requirement, factory);
  }

  public Map<RequirementType<?, ?, ?>, EmiIngredientFactory<?, ?, ?, ?>> getStacks() {
    return ImmutableMap.copyOf(stacks);
  }
}
