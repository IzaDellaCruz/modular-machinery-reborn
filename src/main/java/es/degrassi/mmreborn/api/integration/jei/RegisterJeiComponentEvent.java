package es.degrassi.mmreborn.api.integration.jei;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import es.degrassi.mmreborn.api.crafting.requirement.IRequirement;
import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementType;
import es.degrassi.mmreborn.common.machine.MachineComponent;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

import java.util.Map;

public class RegisterJeiComponentEvent extends Event implements IModBusEvent {
  private final Map<RequirementType<?, ?, ?>, JeiComponentFactory<?, ?>> components = Maps.newHashMap();
  public <
      R extends RecipeRequirement<?, T, X>,
      T extends IRequirement<C, X>,
      C extends MachineComponent<X>,
      X,
      Y
      > void register(RequirementType<T, C, X> requirement, JeiComponentFactory<R, Y> component) {
    if (components.containsKey(requirement))
      throw new IllegalArgumentException("Jei component already registered for requirement: " + requirement.getCodec().name());
    components.put(requirement, component);
  }

  public Map<RequirementType<?, ?, ?>, JeiComponentFactory<?, ?>> getComponents() {
    return ImmutableMap.copyOf(components);
  }
}
