package es.degrassi.mmreborn.common.entity.base;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class BlockEntityRestrictedTick extends ColorableMachineComponentEntity {

  protected BlockEntityRestrictedTick(BlockEntityType<?> entityType, BlockPos pos, BlockState blockState) {
    super(entityType, pos, blockState);
  }

  public final void tick() {
    if (getLevel() == null) return;
    if (getLevel().isClientSide()) {
      doClientTick();
      return;
    }
    doRestrictedTick();
  }

  public abstract void doRestrictedTick();
  public void doClientTick() {}
}
