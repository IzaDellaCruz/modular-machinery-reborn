package es.degrassi.mmreborn.common.machine.component;

import es.degrassi.mmreborn.common.crafting.ComponentType;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.MachineComponent;
import es.degrassi.mmreborn.common.registration.ComponentRegistration;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unchecked")
public class EmptyComponent extends MachineComponent<Void> {
  protected EmptyComponent() {
    super(IOType.INPUT);
  }

  @Override
  public ComponentType<Void> getComponentType() {
    return ComponentRegistration.COMPONENT_EMPTY.get();
  }

  @Override
  public @Nullable Void getContainerProvider() {
    return null;
  }

  @Override
  public <C extends MachineComponent<Void>> C merge(C c) {
    return (C) this;
  }
}
