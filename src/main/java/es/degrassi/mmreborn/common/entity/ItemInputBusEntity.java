package es.degrassi.mmreborn.common.entity;

import es.degrassi.mmreborn.common.block.prop.ItemBusSize;
import es.degrassi.mmreborn.common.entity.base.IAutoInputEntity;
import es.degrassi.mmreborn.common.entity.base.TileItemBus;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.manager.handler.ItemHandler;
import es.degrassi.mmreborn.common.registration.EntityRegistration;
import es.degrassi.mmreborn.common.manager.handler.slot.ItemSlot;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemInputBusEntity extends TileItemBus implements IAutoInputEntity {

  public ItemInputBusEntity(BlockPos pos, BlockState state) {
    super(EntityRegistration.ITEM_INPUT_BUS.get(), pos, state, ItemBusSize.TINY, IOType.INPUT);
  }

  public ItemInputBusEntity(BlockPos pos, BlockState state, ItemBusSize type) {
    super(EntityRegistration.ITEM_INPUT_BUS.get(), pos, state, type, IOType.INPUT);
  }

  @Override
  public ItemHandler buildInventory(int slots, int stackSize) {
    int[] inSlots = new int[slots];
    for (int i = 0; i < slots; i++) {
      inSlots[i] = i;
    }
    return new ItemHandler(inSlots, new int[0], stackSize, Direction.values());
  }

  @Override
  public void tickAutoInput() {
    if (!shouldAutoInput) return;
    for (Direction side : inventory.accessibleSides) {
      var neighbour = getNeighbour(Capabilities.ItemHandler.BLOCK, side);
      if (neighbour == null) continue;

      inventory.getInventory()
          .stream()
          .filter(ItemSlot::isInput)
          .forEachOrdered(slot -> moveStacks(neighbour, slot, Integer.MAX_VALUE));
    }
  }

}
