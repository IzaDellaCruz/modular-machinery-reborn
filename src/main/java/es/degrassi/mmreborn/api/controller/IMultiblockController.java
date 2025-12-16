package es.degrassi.mmreborn.api.controller;

import es.degrassi.mmreborn.common.entity.MachineControllerEntity;
import es.degrassi.mmreborn.common.machine.DynamicMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public interface IMultiblockController {

  default MachineControllerEntity self() {
    return (MachineControllerEntity) this;
  }

  void asyncCheckPattern(long periodID);

  ResourceLocation getId();

  void onStructureFormed();

  void onStructureUnformed();

  default DynamicMachine getController() {
    return self().getFoundMachine();
  }

  BlockPos getBlockPos();

  Direction getFacing();

  BlockState getBlockState();

  boolean isPosInCache(BlockPos pos);

  void onBlockStateChanged(BlockPos pos, BlockState newState);
}
