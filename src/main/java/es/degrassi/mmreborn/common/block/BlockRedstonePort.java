package es.degrassi.mmreborn.common.block;

import es.degrassi.mmreborn.client.container.RedstonePortContainer;
import es.degrassi.mmreborn.common.entity.RedstonePortEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockRedstonePort extends BlockMachineComponent {
  public BlockRedstonePort() {
    super(
        Properties.of()
            .strength(2F, 10F)
            .sound(SoundType.METAL)
            .requiresCorrectToolForDrops()
            .dynamicShape()
            .noOcclusion()
    );
  }

  @Override
  public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
    return new RedstonePortEntity(blockPos, blockState);
  }

  @Override
  protected @NotNull ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
    BlockEntity te = level.getBlockEntity(pos);
    if(te instanceof RedstonePortEntity entity) {
      if (player instanceof ServerPlayer serverPlayer) {
        RedstonePortContainer.open(serverPlayer, entity);
      }
      return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
    return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
  }

  @Override
  public boolean isSignalSource(BlockState state) {
    return true;
  }

  @Override
  public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
    if (level.getBlockEntity(pos) instanceof RedstonePortEntity entity) {
      return entity.getOutputAmount();
    }
    return super.getDirectSignal(state, level, pos, side);
  }

  @Override
  protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
    if (level.getBlockEntity(pos) instanceof RedstonePortEntity entity) {
      return entity.getOutputAmount();
    }
    return super.getSignal(state, level, pos, direction);
  }

  @Override
  public boolean hasAnalogOutputSignal(BlockState pState) {
    return true;
  }

  @Override
  protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
    if (level.getBlockEntity(pos) instanceof RedstonePortEntity entity) {
      return entity.getOutputAmount();
    }
    return super.getAnalogOutputSignal(state, level, pos);
  }
}
