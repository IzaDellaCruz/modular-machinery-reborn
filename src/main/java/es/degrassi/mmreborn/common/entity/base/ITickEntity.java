package es.degrassi.mmreborn.common.entity.base;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface ITickEntity {
  @Nullable Level getLevel();

  BlockPos getBlockPos();

  default void tick() {
    if (getLevel() == null) return;
    if (getLevel().isClientSide() && this instanceof IClientTickEntity ce) {
      ce.doClientTick();
    } else if (!getLevel().isClientSide() && this instanceof IServerTickEntity se) {
      se.doRestrictedTick();
    }
  }
}
