package es.degrassi.mmreborn.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public interface IEntitySynchronizable {

  void markForUpdate();

  @Nullable Level getLevel();

  BlockPos getBlockPos();
}
