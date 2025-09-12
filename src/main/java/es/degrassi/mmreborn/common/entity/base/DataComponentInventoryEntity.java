package es.degrassi.mmreborn.common.entity.base;

import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.util.IOInventory;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface DataComponentInventoryEntity<T> extends ItemDroppeable {
  default IOInventory createDataComponentInventory() {
    return new IOInventory(
        getMode().isInput() ? new int[]{ 0 } : new int[]{},
        getMode().isOutput() ? new int[]{ 0 } : new int[]{},
        stack -> stack.get(getDataComponent()) != null,
        Direction.values()
    );
  }

  default void addDrops(List<ItemStack> drops) {
    getDataComponentInventory().getAllStacks().forEach(stack -> drops.add(stack.copy()));
  }

  IOType getMode();

  DataComponentType<? extends T> getDataComponent();

  IOInventory getDataComponentInventory();

  void tickInventory();

  boolean shouldTick();
}
