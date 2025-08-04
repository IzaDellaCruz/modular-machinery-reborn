package es.degrassi.mmreborn.common.entity.base;

import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.util.IOInventory;
import es.degrassi.mmreborn.common.util.ItemSlot;
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
        Direction.values());
  }

  IOType getMode();

  ItemCapability<T, Void> getCapability();

  IOInventory getCapabilityInventory();

  void tickInventory();

  boolean shouldTickInventory();

  default void addDrops(List<ItemStack> drops) {
    getCapabilityInventory().getInventory().stream().map(ItemSlot::getItemStack)
        .filter(stack -> !stack.isEmpty()).forEach(stack -> drops.add(stack.copy()));
  }
}
