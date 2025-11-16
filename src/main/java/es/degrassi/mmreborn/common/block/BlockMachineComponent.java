package es.degrassi.mmreborn.common.block;

import es.degrassi.mmreborn.common.data.Config;
import es.degrassi.mmreborn.common.entity.base.ColorableMachineComponentEntity;
import es.degrassi.mmreborn.common.entity.base.ColorableMachineEntity;
import es.degrassi.mmreborn.common.entity.base.ItemDroppeable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class BlockMachineComponent extends Block implements BlockDynamicColor, EntityBlock {
  public static final Property<Boolean> CONNECT_TEXTURES = BooleanProperty.create("connect_textures");
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
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    super.createBlockStateDefinition(builder);
    builder.add(CONNECT_TEXTURES);
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    return defaultBlockState().setValue(CONNECT_TEXTURES, true);
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

  @Override
  protected @NotNull List<ItemStack> getDrops(@NotNull BlockState state, LootParams.@NotNull Builder builder) {
    List<ItemStack> drops = super.getDrops(state, builder);
    BlockEntity be = builder.getParameter(LootContextParams.BLOCK_ENTITY);
    if (be instanceof ItemDroppeable entity) {
      entity.addDrops(drops);
    }
    return drops;
  }

  private void updateNeighbours(Level level, BlockPos pos) {
    level.updateNeighborsAt(pos, this);
    for(Direction direction : Direction.values()) {
      level.updateNeighborsAt(pos.relative(direction), this);
    }
  }

  @Override
  protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
    super.onRemove(state, level, pos, newState, movedByPiston);
    updateNeighbours(level, pos);
  }

  @Override
  protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
    super.onPlace(state, level, pos, oldState, movedByPiston);
    updateNeighbours(level, pos);
  }

  @Override
  public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
    super.setPlacedBy(level, pos, state, placer, stack);
    updateNeighbours(level, pos);
  }

  @Override
  public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
    super.playerDestroy(level, player, pos, state, blockEntity, tool);
    updateNeighbours(level, pos);
  }

  @Override
  public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
    updateNeighbours(level, pos);
    return super.playerWillDestroy(level, pos, state, player);
  }

  @Override
  protected boolean canBeReplaced(BlockState state, Fluid fluid) {
    return false;
  }

  @Override
  protected boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
    return false;
  }
}
