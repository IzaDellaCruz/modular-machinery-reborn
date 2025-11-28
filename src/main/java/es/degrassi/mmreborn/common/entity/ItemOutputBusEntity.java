package es.degrassi.mmreborn.common.entity;

import es.degrassi.mmreborn.common.block.prop.ItemBusSize;
import es.degrassi.mmreborn.common.entity.base.IAutoOutputEntity;
import es.degrassi.mmreborn.common.entity.base.TileItemBus;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.registration.EntityRegistration;
import es.degrassi.mmreborn.common.util.IOInventory;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemOutputBusEntity extends TileItemBus implements IAutoOutputEntity {

  public ItemOutputBusEntity(BlockPos pos, BlockState state) {
    super(EntityRegistration.ITEM_OUTPUT_BUS.get(), pos, state, ItemBusSize.TINY, IOType.OUTPUT);
  }

  public ItemOutputBusEntity(BlockPos pos, BlockState state, ItemBusSize type) {
    super(EntityRegistration.ITEM_OUTPUT_BUS.get(), pos, state, type, IOType.OUTPUT);
  }

  @Override
  public IOInventory buildInventory(int slots, int stackSize) {
    int[] outSlots = new int[slots];
    for (int i = 0; i < slots; i++) {
      outSlots[i] = i;
    }
    return new IOInventory(new int[0], outSlots, stackSize, Direction.values());
  }

  @Override
  public void tickAutoOutput() {
    if (!shouldAutoOutput) return;
    for (Direction side : Direction.values()) {
      var neighbour = getNeighbour(Capabilities.ItemHandler.BLOCK, side);
      if (neighbour == null) continue;

      inventory.getInventory()
          .stream()
          .filter(slot -> slot.isOutput() && !slot.isEmpty())
          .forEach(slot -> moveStacks(slot, neighbour, Integer.MAX_VALUE));
    }
  }
}
