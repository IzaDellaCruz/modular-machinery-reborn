package es.degrassi.mmreborn.common.block;

import es.degrassi.mmreborn.common.block.prop.ExperienceHatchSize;
import es.degrassi.mmreborn.common.entity.ExperienceInputHatchEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class BlockExperienceInputHatch extends BlockExperienceHatch implements BlockTickEntity {
  public BlockExperienceInputHatch(ExperienceHatchSize size) {
    super(size);
  }

  @Override
  public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
    return new ExperienceInputHatchEntity(blockPos, blockState, size);
  }
}
