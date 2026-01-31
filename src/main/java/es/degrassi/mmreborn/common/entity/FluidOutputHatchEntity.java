package es.degrassi.mmreborn.common.entity;

import es.degrassi.mmreborn.common.block.prop.FluidHatchSize;
import es.degrassi.mmreborn.common.entity.base.FluidTankEntity;
import es.degrassi.mmreborn.common.entity.base.IAutoOutputEntity;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.registration.EntityRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class FluidOutputHatchEntity extends FluidTankEntity implements IAutoOutputEntity {

  public FluidOutputHatchEntity(BlockPos pos, BlockState state) {
    super(EntityRegistration.FLUID_OUTPUT_HATCH.get(), pos, state, FluidHatchSize.TINY, IOType.OUTPUT);
  }

  public FluidOutputHatchEntity(BlockPos pos, BlockState state, FluidHatchSize size) {
    super(EntityRegistration.FLUID_OUTPUT_HATCH.get(), pos, state, size, IOType.OUTPUT);
  }

  public void tickAutoOutput() {
    if (!this.getConfig().isEnabled()) return;
    for (Direction side : getTank().accessibleSides) {
      if (!getConfig().canAutoIO(side)) continue;
      if (getTank().isEmpty()) continue;
      IFluidHandler neighbour = getNeighbour(Capabilities.FluidHandler.BLOCK, side);
      if(neighbour == null)
        continue;
      FluidUtil.tryFluidTransfer(neighbour, getTank(), Integer.MAX_VALUE, true);
    }
  }
}
