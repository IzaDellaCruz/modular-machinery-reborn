package es.degrassi.mmreborn.common.machine.component;

import es.degrassi.mmreborn.common.crafting.ComponentType;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.MachineComponent;
import es.degrassi.mmreborn.common.registration.ComponentRegistration;
import org.jetbrains.annotations.Nullable;

public class DurationComponent extends MachineComponent<Void> {
  public DurationComponent() {
    super(IOType.INPUT);
  }

  @Override
  public ComponentType<Void> getComponentType() {
    return ComponentRegistration.COMPONENT_DURATION.get();
  }

  @Override
  public @Nullable Void getContainerProvider() {
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <C extends MachineComponent<Void>> C merge(C c) {
    return (C) this;
  }
}
