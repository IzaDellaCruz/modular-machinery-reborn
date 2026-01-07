package es.degrassi.mmreborn.common.entity;

import es.degrassi.mmreborn.common.block.prop.FluidHatchSize;
import es.degrassi.mmreborn.common.entity.base.FluidTankEntity;
import es.degrassi.mmreborn.common.entity.base.IAutoInputEntity;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.registration.EntityRegistration;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

@MethodsReturnNonnullByDefault
public class FluidInputHatchEntity extends FluidTankEntity implements IAutoInputEntity {

  public FluidInputHatchEntity(BlockPos pos, BlockState state) {
    super(EntityRegistration.FLUID_INPUT_HATCH.get(), pos, state, FluidHatchSize.TINY, IOType.INPUT);
  }

  public FluidInputHatchEntity(BlockPos pos, BlockState state, FluidHatchSize size) {
    super(EntityRegistration.FLUID_INPUT_HATCH.get(), pos, state, size, IOType.INPUT);
  }

  public void tickAutoInput() {
    if (!shouldAutoInput) return;
    for (Direction side : getTank().accessibleSides) {
      IFluidHandler neighbour = getNeighbour(Capabilities.FluidHandler.BLOCK, side);
      if(neighbour == null)
        continue;
      FluidUtil.tryFluidTransfer(getTank(), neighbour, Integer.MAX_VALUE, true);
    }
  }
}
