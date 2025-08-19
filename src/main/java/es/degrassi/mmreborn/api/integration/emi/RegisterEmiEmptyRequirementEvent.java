package es.degrassi.mmreborn.api.integration.emi;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import es.degrassi.mmreborn.common.util.EmiConsumer;
import es.degrassi.mmreborn.common.util.EmptyRequirementType;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

import java.util.Map;

public class RegisterEmiEmptyRequirementEvent extends Event implements IModBusEvent {
  private final Map<EmptyRequirementType, EmiConsumer> consumers = Maps.newHashMap();

  public void register(EmptyRequirementType type, EmiConsumer factory) {
    if (consumers.containsKey(type)) {
      throw new IllegalArgumentException("EMI Consumer already registered for empty requirement: " + type.getId().getPath());
    }
    consumers.put(type, factory);
  }

  public Map<EmptyRequirementType, EmiConsumer> getConsumers() {
    return ImmutableMap.copyOf(consumers);
  }
}
