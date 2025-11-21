package es.degrassi.mmreborn.common.machine.component;

import es.degrassi.mmreborn.common.crafting.ComponentType;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.MachineComponent;
import es.degrassi.mmreborn.common.registration.ComponentRegistration;
import es.degrassi.mmreborn.common.util.IntRange;
import org.jetbrains.annotations.Nullable;

public class HeightComponent extends MachineComponent<IntRange> {

  public HeightComponent() {
    super(IOType.INPUT);
  }

  @Override
  public ComponentType<IntRange> getComponentType() {
    return ComponentRegistration.COMPONENT_HEIGHT.get();
  }

  @Override
  public @Nullable IntRange getContainerProvider() {
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <C extends MachineComponent<IntRange>> C merge(C c) {
    return (C) this;
  }
}
