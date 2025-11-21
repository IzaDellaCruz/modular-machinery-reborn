package es.degrassi.mmreborn.common.machine.component;

import es.degrassi.mmreborn.common.crafting.ComponentType;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.MachineComponent;
import es.degrassi.mmreborn.common.registration.ComponentRegistration;
import es.degrassi.mmreborn.common.util.IOInventory;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public class DurabilityComponent extends MachineComponent<IOInventory> {
  private final IOInventory handler;

  public DurabilityComponent(IOInventory handler) {
    super(IOType.INPUT);
    this.handler = handler;
  }

  @Override
  public ComponentType<IOInventory> getComponentType() {
    return ComponentRegistration.COMPONENT_DURABILITY.get();
  }

  @Override
  public IOInventory getContainerProvider() {
    return handler;
  }

  @Override
  public CompoundTag asTag(HolderLookup.Provider provider) {
    CompoundTag tag = super.asTag(provider);
    tag.put("handler", handler.writeNBT(provider));
    return tag;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <C extends MachineComponent<IOInventory>> C merge(C c) {
    DurabilityComponent comp = (DurabilityComponent) c;
    return (C) new DurabilityComponent(IOInventory.mergeBuild(handler, comp.handler));
  }
}
