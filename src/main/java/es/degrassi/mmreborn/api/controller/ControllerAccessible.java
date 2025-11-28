package es.degrassi.mmreborn.api.controller;

import es.degrassi.mmreborn.common.entity.MachineControllerEntity;
import es.degrassi.mmreborn.common.machine.MachineComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;

public interface ControllerAccessible {
  BlockPos getControllerPos();

  void setControllerPos(BlockPos pos);

  @Nullable
  Level getLevel();

  @Nullable
  default MachineControllerEntity getController() {
    if (getLevel() != null && getControllerPos() != null && getLevel().getBlockEntity(getControllerPos()) instanceof MachineControllerEntity entity) {
      return entity;
    }
    return null;
  }

  default Map<BlockPos, Optional<MachineComponent<?>>> getFoundComponents() {
    if (getController() == null) return Map.of();
    return getController().getFoundComponentsMap();
  }
}
