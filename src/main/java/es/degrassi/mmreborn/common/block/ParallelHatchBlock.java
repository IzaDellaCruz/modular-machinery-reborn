package es.degrassi.mmreborn.common.block;

import es.degrassi.mmreborn.client.container.ParallelHatchContainer;
import es.degrassi.mmreborn.common.block.prop.ParallelHatchSize;
import es.degrassi.mmreborn.common.entity.ParallelHatchEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ParallelHatchBlock extends BlockMachineComponent {
  protected final ParallelHatchSize type;
  public ParallelHatchBlock(ParallelHatchSize type) {
    super(
        Properties.of()
            .dynamicShape()
            .noOcclusion()
            .strength(2F, 10F)
            .sound(SoundType.METAL)
    );
    this.type = type;
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    super.createBlockStateDefinition(builder);
    builder.add(BlockStateProperties.HORIZONTAL_FACING);
  }

  @Nullable
  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    return defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
  }

  @Override
  public BlockState rotate(BlockState state, Rotation rotation) {
    return state.setValue(BlockStateProperties.HORIZONTAL_FACING, rotation.rotate(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
  }

  @Override
  public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag tooltipFlag) {
    tooltip.add(
        Component.translatable(
            "tooltip.parallelhatch.size",
            type.max
        ).withStyle(ChatFormatting.GRAY)
    );
  }

  @Override
  protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
    if (level.isClientSide) return ItemInteractionResult.sidedSuccess(true);
    BlockEntity te = level.getBlockEntity(pos);
    if (te instanceof ParallelHatchEntity entity) {
      if (player instanceof ServerPlayer sp) {
        ParallelHatchContainer.open(sp, entity);
      }
      return ItemInteractionResult.SUCCESS;
    }
    return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
  }

  @Override
  public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
    return new ParallelHatchEntity(blockPos, blockState, type);
  }

  @Override
  public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
    if (level.getBlockEntity(pos) instanceof ParallelHatchEntity entity) {
      if (level instanceof ServerLevel) {
        if (entity.getController() != null) {
          var controller = entity.getController();
          super.onBlockExploded(state, level, pos, explosion);
          controller.getComponentManager().updateComponents(true);
          controller.getProcessor().updateActiveCores(1);
        }
      } else {
        super.onBlockExploded(state, level, pos, explosion);
      }
    } else {
      super.onBlockExploded(state, level, pos, explosion);
    }
  }

  @Override
  protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
    if (level.getBlockEntity(pos) instanceof ParallelHatchEntity entity) {
      if (level instanceof ServerLevel) {
        if (entity.getController() != null) {
          var controller = entity.getController();
          super.onRemove(state, level, pos, newState, movedByPiston);
          controller.getComponentManager().updateComponents(true);
          controller.getProcessor().updateActiveCores(1);
        } else {
          super.onRemove(state, level, pos, newState, movedByPiston);
        }
      } else {
        super.onRemove(state, level, pos, newState, movedByPiston);
      }
    } else {
      super.onRemove(state, level, pos, newState, movedByPiston);
    }
  }

  @Override
  public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
    if (level.getBlockEntity(pos) instanceof ParallelHatchEntity entity) {
      if (level instanceof ServerLevel) {
        if (entity.getController() != null) {
          var controller = entity.getController();
          var toReturn = super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
          controller.getComponentManager().updateComponents(true);
          controller.getProcessor().updateActiveCores(1);
          return toReturn;
        } else {
          return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
        }
      } else {
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
      }
    } else {
      return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }
  }

  @Override
  public void onDestroyedByPushReaction(BlockState state, Level level, BlockPos pos, Direction pushDirection, FluidState fluid) {
    if (level.getBlockEntity(pos) instanceof ParallelHatchEntity entity) {
      if (level instanceof ServerLevel) {
        if (entity.getController() != null) {
          var controller = entity.getController();
          super.onDestroyedByPushReaction(state, level, pos, pushDirection, fluid);
          controller.getComponentManager().updateComponents(true);
          controller.getProcessor().updateActiveCores(1);
        } else {
          super.onDestroyedByPushReaction(state, level, pos, pushDirection, fluid);
        }
      } else {
        super.onDestroyedByPushReaction(state, level, pos, pushDirection, fluid);
      }
    } else{
      super.onDestroyedByPushReaction(state, level, pos, pushDirection, fluid);
    }
  }
}
