package es.degrassi.mmreborn.common.entity;

import es.degrassi.mmreborn.common.block.prop.EnergyHatchSize;
import es.degrassi.mmreborn.common.entity.base.EnergyHatchEntity;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.registration.EntityRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class EnergyOutputHatchEntity extends EnergyHatchEntity {

  public EnergyOutputHatchEntity(BlockPos pos, BlockState state) {
    super(EntityRegistration.ENERGY_OUTPUT_HATCH.get(), pos, state, EnergyHatchSize.TINY, IOType.OUTPUT);
  }

  public EnergyOutputHatchEntity(BlockPos pos, BlockState state, EnergyHatchSize size) {
    super(EntityRegistration.ENERGY_OUTPUT_HATCH.get(), pos, state, size, IOType.OUTPUT);
  }

  public void tick() {
    if (level.isClientSide()) return;
    super.tick();

    long prevEnergy = this.energy;

    long transferCap = Math.min(this.size.transferLimit, this.energy);
    for (Direction face : Direction.values()) {
      if (transferCap > 0) {
        int transferred = attemptFETransfer(face, convertDownEnergy(transferCap));
        transferCap -= transferred;
        this.energy -= transferred;
      }
      if (transferCap <= 0) {
        break;
      }
    }

    if (prevEnergy != this.energy) {
      markForUpdate();
    }
  }

  private int attemptFETransfer(Direction face, int maxTransferLeft) {
    BlockPos at = this.getBlockPos().relative(face);

    int receivedEnergy = 0;
    BlockEntity te = level.getBlockEntity(at);
    if (te != null && !(te instanceof EnergyHatchEntity)) {
      var cache = BlockCapabilityCache.create(Capabilities.EnergyStorage.BLOCK, (ServerLevel) getLevel(), at, face.getOpposite(),
        () -> !isRemoved(), () -> {});
      IEnergyStorage ce = cache.getCapability();
      if (ce != null && ce.canReceive()) {
        try {
          receivedEnergy = ce.receiveEnergy(maxTransferLeft, false);
        } catch (Exception ignored) {
        }
      }
    }
    return receivedEnergy;
  }
}
