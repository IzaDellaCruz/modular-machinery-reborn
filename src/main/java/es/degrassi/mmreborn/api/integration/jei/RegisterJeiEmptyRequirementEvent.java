package es.degrassi.mmreborn.api.integration.jei;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import es.degrassi.mmreborn.common.util.EmptyRequirementType;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

import java.util.Map;

public class RegisterJeiEmptyRequirementEvent extends Event implements IModBusEvent {
  private final Map<EmptyRequirementType, JeiConsumer> consumers = Maps.newHashMap();

  public void register(EmptyRequirementType type, JeiConsumer factory) {
    if (consumers.containsKey(type)) {
      throw new IllegalArgumentException("JEI Consumer already registered for empty requirement: " + type.getId().getPath());
    }
    consumers.put(type, factory);
  }

  public Map<EmptyRequirementType, JeiConsumer> getConsumers() {
    return ImmutableMap.copyOf(consumers);
  }
}
