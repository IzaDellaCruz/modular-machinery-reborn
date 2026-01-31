package es.degrassi.mmreborn.common.entity;

import es.degrassi.experiencelib.api.capability.ExperienceLibCapabilities;
import es.degrassi.mmreborn.common.block.prop.ExperienceHatchSize;
import es.degrassi.mmreborn.common.entity.base.ExperienceHatchEntity;
import es.degrassi.mmreborn.common.entity.base.IAutoOutputEntity;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.registration.EntityRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
    if (!this.getConfig().isEnabled()) return;
    long prevXp = this.getTank().getExperience();

    for (Direction face : Direction.values()) {
      if (!getConfig().canAutoIO(face)) continue;
      var ce = getNeighbour(ExperienceLibCapabilities.EXPERIENCE.block(), face);
      if (ce == null) continue;
      attemptXPTransfer(getTank(), ce, Long.MAX_VALUE);
    }

    if (prevXp != this.getTank().getExperience()) {
      markForUpdate();
    }
  }
}
