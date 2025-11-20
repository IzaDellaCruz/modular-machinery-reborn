package es.degrassi.mmreborn.common.entity;

import es.degrassi.experiencelib.api.capability.ExperienceLibCapabilities;
import es.degrassi.mmreborn.common.block.prop.ExperienceHatchSize;
import es.degrassi.mmreborn.common.entity.base.ExperienceHatchEntity;
import es.degrassi.mmreborn.common.entity.base.IAutoOutputEntity;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.registration.EntityRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ExperienceOutputHatchEntity extends ExperienceHatchEntity implements IAutoOutputEntity {

  public ExperienceOutputHatchEntity(BlockPos pos, BlockState state) {
    super(EntityRegistration.EXPERIENCE_OUTPUT_HATCH.get(), pos, state, ExperienceHatchSize.TINY, IOType.OUTPUT);
  }

  public ExperienceOutputHatchEntity(BlockPos pos, BlockState state, ExperienceHatchSize size) {
    super(EntityRegistration.EXPERIENCE_OUTPUT_HATCH.get(), pos, state, size, IOType.OUTPUT);
  }

  @Override
  public void tickAutoOutput() {
    if (!this.shouldAutoOutput) return;
    long prevXp = this.getTank().getExperience();

    long transferCap = Math.min(Integer.MAX_VALUE, this.getTank().getExperienceCapacity() - this.getTank().getExperience());
    for (Direction face : Direction.values()) {
      if (transferCap > 0) {
        long transferred = attemptXPTransfer(face, transferCap);
        transferCap -= transferred;
        for (int i = 0; i < getTank().getTanks(); i++) {
          getTank().extractExperience(i, transferred, false);
        }
      }
      if (transferCap <= 0) {
        break;
      }
    }

    if (prevXp != this.getTank().getExperience()) {
      markForUpdate();
    }
  }

  private long attemptXPTransfer(Direction face, long maxTransferLeft) {
    BlockPos at = this.getBlockPos().relative(face);

    long receivedEnergy = 0;
    BlockEntity te = level.getBlockEntity(at);
    if (te != null && !(te instanceof ExperienceHatchEntity)) {
      var ce = getNeighbour(ExperienceLibCapabilities.EXPERIENCE.block(), face);
      if (ce == null) return 0;
      try {
        for (int i = 0; i < ce.getTanks(); i++) {
          if (!ce.canExtract(i)) continue;
          receivedEnergy += ce.receiveExperience(i, maxTransferLeft, false);
        }
      } catch (Exception ignored) {
      }
    }
    return receivedEnergy;
  }
}
