package es.degrassi.mmreborn.common.machine.component;

import es.degrassi.mmreborn.common.crafting.ComponentType;
import es.degrassi.mmreborn.common.entity.DimensionalDetectorEntity;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.MachineComponent;
import es.degrassi.mmreborn.common.registration.ComponentRegistration;
import net.minecraft.resources.ResourceLocation;

public class DimensionComponent extends MachineComponent<ResourceLocation> {
  private final DimensionalDetectorEntity entity;

  public DimensionComponent(DimensionalDetectorEntity entity) {
    super(IOType.INPUT);
    this.entity = entity;
  }

  @Override
  public ComponentType<ResourceLocation> getComponentType() {
    return ComponentRegistration.COMPONENT_DIMENSION.get();
  }

  @Override
  public ResourceLocation getContainerProvider() {
    return entity.getLevel().dimension().location();
  }

  @Override
  @SuppressWarnings("unchecked")
  public <C extends MachineComponent<ResourceLocation>> C merge(C c) {
    return (C) this;
  }
}
