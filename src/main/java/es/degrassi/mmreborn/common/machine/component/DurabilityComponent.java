package es.degrassi.mmreborn.common.machine.component;

import es.degrassi.mmreborn.common.crafting.ComponentType;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.MachineComponent;
import es.degrassi.mmreborn.common.registration.ComponentRegistration;
import es.degrassi.mmreborn.common.manager.handler.ItemHandler;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public class DurabilityComponent extends MachineComponent<ItemHandler> {
  private final ItemHandler handler;

  public DurabilityComponent(ItemHandler handler) {
    super(IOType.INPUT);
    this.handler = handler;
  }

  @Override
  public ComponentType<ItemHandler> getComponentType() {
    return ComponentRegistration.COMPONENT_DURABILITY.get();
  }

  @Override
  public ItemHandler getContainerProvider() {
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
  public <C extends MachineComponent<ItemHandler>> C merge(C c) {
    DurabilityComponent comp = (DurabilityComponent) c;
    return (C) new DurabilityComponent(ItemHandler.mergeBuild(handler, comp.handler));
  }
}
