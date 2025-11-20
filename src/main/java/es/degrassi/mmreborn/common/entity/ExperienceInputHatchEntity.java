package es.degrassi.mmreborn.common.entity;

import es.degrassi.experiencelib.api.capability.ExperienceLibCapabilities;
import es.degrassi.mmreborn.common.block.prop.ExperienceHatchSize;
import es.degrassi.mmreborn.common.entity.base.ExperienceHatchEntity;
import es.degrassi.mmreborn.common.entity.base.IAutoInputEntity;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.registration.EntityRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class ExperienceInputHatchEntity extends ExperienceHatchEntity implements IAutoInputEntity {

  public ExperienceInputHatchEntity(BlockPos pos, BlockState state) {
    super(EntityRegistration.EXPERIENCE_INPUT_HATCH.get(), pos, state, ExperienceHatchSize.TINY, IOType.INPUT);
  }

  public ExperienceInputHatchEntity(BlockPos pos, BlockState state, ExperienceHatchSize size) {
    super(EntityRegistration.EXPERIENCE_INPUT_HATCH.get(), pos, state, size, IOType.INPUT);
  }

  @Override
  public void tickAutoInput() {
    if (!this.shouldAutoInput) return;
    long prevXp = this.getTank().getExperience();
    for (Direction face : Direction.values()) {
      var ce = getNeighbour(ExperienceLibCapabilities.EXPERIENCE.block(), face);
      if (ce == null) continue;
      attemptXPTransfer(ce, getTank(), Long.MAX_VALUE);
    }

    if (prevXp != this.getTank().getExperience()) {
      markForUpdate();
    }
  }
}
