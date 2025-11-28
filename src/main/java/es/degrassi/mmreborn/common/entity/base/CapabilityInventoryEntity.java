package es.degrassi.mmreborn.common.entity.base;

import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.util.IOInventory;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ItemCapability;

import java.util.List;

public interface CapabilityInventoryEntity<T> extends ItemDroppeable {
  default IOInventory createCapabilityInventory() {
    return new IOInventory(
        getMode().isInput() ? new int[]{ 0 } : new int[]{},
        getMode().isOutput() ? new int[]{ 0 } : new int[]{},
        stack -> stack.getCapability(getCapability()) != null,
        64,
        Direction.values());
  }

  default void addDrops(List<ItemStack> drops) {
    getCapabilityInventory().getAllStacks().forEach(stack -> drops.add(stack.copy()));
  }

  IOType getMode();

  ItemCapability<T, Void> getCapability();

  IOInventory getCapabilityInventory();

  void tickInventory();

  boolean shouldTickInventory();
}
