package es.degrassi.mmreborn.common.block;

import es.degrassi.mmreborn.common.data.Config;
import es.degrassi.mmreborn.common.entity.base.BlockEntitySynchronized;
import es.degrassi.mmreborn.common.entity.base.ColorableMachineComponentEntity;
import es.degrassi.mmreborn.common.entity.base.ColorableMachineEntity;
import es.degrassi.mmreborn.common.entity.base.TileInventory;
import es.degrassi.mmreborn.common.util.IOInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class BlockMachineComponent extends Block implements BlockDynamicColor, EntityBlock {
  protected BlockMachineComponent(Properties properties) {
    super(properties.requiresCorrectToolForDrops());
  }

  @Override
  public int getColorMultiplier(BlockState state, @Nullable BlockAndTintGetter worldIn, @Nullable BlockPos pos, int tintIndex) {
    if(worldIn == null || pos == null) {
      return Config.machineColor;
    }
    BlockEntity te = worldIn.getBlockEntity(pos);
    if (te instanceof ColorableMachineEntity) {
      return ((ColorableMachineEntity) te).getMachineColor();
    }
    return Config.machineColor;
  }

  @Override
  protected boolean triggerEvent(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, int pId, int pParam) {
    super.triggerEvent(pState, pLevel, pPos, pId, pParam);
    final BlockEntity be = pLevel.getBlockEntity(pPos);
    return be != null && be.triggerEvent(pId, pParam);
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
    return new ColorableMachineComponentEntity(blockPos, blockState);
  }

  @Nullable
  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
    return (level, pos, state, blockEntity) -> {
      if (blockEntity.getType() == pBlockEntityType && blockEntity instanceof BlockEntitySynchronized entity) {
        entity.tick();
      }
    };
  }

  @Override
  protected @NotNull List<ItemStack> getDrops(@NotNull BlockState state, LootParams.@NotNull Builder builder) {
    List<ItemStack> drops = super.getDrops(state, builder);
    if (builder.getParameter(LootContextParams.BLOCK_ENTITY) instanceof TileInventory entity) {
      IOInventory inv = entity.getInventory();
      for (int i = 0; i < inv.getSlots(); i++) {
        ItemStack stack = inv.getStackInSlot(i);
        if(!stack.isEmpty()) {
          drops.add(stack);
        }
      }
    }
    return drops;
  }

  @Override
  protected boolean canBeReplaced(BlockState state, Fluid fluid) {
    return false;
  }
}
