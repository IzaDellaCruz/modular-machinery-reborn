package es.degrassi.mmreborn.common.entity.base;

import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.util.IOInventory;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.ItemCapability;

public interface CapabilityInventoryEntity<T> {
  default IOInventory createCapabilityInventory() {
    return new IOInventory(
        getMode().isInput() ? new int[]{ 0 } : new int[]{},
        getMode().isOutput() ? new int[]{ 0 } : new int[]{},
        stack -> stack.getCapability(getCapability()) != null,
        Direction.values());
  }

  IOType getMode();

  ItemCapability<T, Void> getCapability();

  IOInventory getCapabilityInventory();

  void tickInventory();
}
