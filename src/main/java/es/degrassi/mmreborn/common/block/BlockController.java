package es.degrassi.mmreborn.common.block;

import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.client.container.ControllerContainer;
import es.degrassi.mmreborn.client.integration.athena.model.controller.ControllerBakedModel;
import es.degrassi.mmreborn.common.entity.MachineControllerEntity;
import es.degrassi.mmreborn.common.item.ControllerItem;
import es.degrassi.mmreborn.common.item.ItemBlueprint;
import es.degrassi.mmreborn.common.machine.DynamicMachine;
import es.degrassi.mmreborn.common.network.server.SAddControllerRenderer;
import es.degrassi.mmreborn.common.network.server.SMachineUpdatePacket;
import es.degrassi.mmreborn.common.network.server.SRemoveControllerRenderer;
import es.degrassi.mmreborn.common.network.server.SStopSoundInstancePacket;
import es.degrassi.mmreborn.common.util.RedstoneHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@SuppressWarnings("unused")
public class BlockController extends BlockMachineComponent implements BlockTickEntity {
  public BlockController() {
    super(
        Properties.of()
            .sound(SoundType.METAL)
            .strength(5F, 10F)
            .requiresCorrectToolForDrops()
            .dynamicShape()
            .noOcclusion()
    );
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    super.createBlockStateDefinition(builder);
    builder.add(BlockStateProperties.HORIZONTAL_FACING);
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    return super.getStateForPlacement(context)
        .setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
  }

  @Override
  protected void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pMovedByPiston) {
    ResourceLocation id = ModularMachineryReborn.MACHINES_BLOCK.inverse().get(this);
    if (id != null && pLevel.getBlockEntity(pPos) instanceof MachineControllerEntity entity) {
      entity.setMachine(id);
      if (entity.getModelData().get(ControllerBakedModel.DATA).hasCustomModel()) {
        pLevel.setBlockAndUpdate(pPos, pState.setValue(BlockMachineComponent.CONNECT_TEXTURES, false));
      }
      if (pLevel instanceof ServerLevel serverLevel)
        serverLevel.getServer().tell(new TickTask(1, () -> PacketDistributor.sendToPlayersTrackingChunk(serverLevel,
            new ChunkPos(pPos), new SMachineUpdatePacket(id, pPos))));
    }
    super.onPlace(pState, pLevel, pPos, pOldState, pMovedByPiston);
  }

  //When placed by an entity
  @Override
  public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
    super.setPlacedBy(level, pos, state, placer, stack);
    ControllerItem.getMachine(stack).ifPresent(machine -> {
      BlockEntity tile = level.getBlockEntity(pos);
      if (tile instanceof MachineControllerEntity machineTile) {
        machineTile.setMachine(machine.getRegistryName());
        if (level instanceof ServerLevel serverLevel)
          level.getServer().tell(new TickTask(1, () -> PacketDistributor.sendToPlayersTrackingChunk(serverLevel, new ChunkPos(pos), new SMachineUpdatePacket(machine.getRegistryName(), pos))));
      }
    });
  }

  @Override
  public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
    BlockEntity tile = level.getBlockEntity(pos);
    if (tile instanceof MachineControllerEntity entity) {
      return ControllerItem.makeMachineItem(entity.getId());
    }
    return super.getCloneItemStack(state, target, level, pos, player);
  }

  @Override
  public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
    return new MachineControllerEntity(blockPos, blockState);
  }

  @Override
  public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
    BlockEntity te = level.getBlockEntity(pos);
    if (te instanceof MachineControllerEntity entity) {
      PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level, new ChunkPos(pos), new SStopSoundInstancePacket(pos));
      PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level, new ChunkPos(pos), new SRemoveControllerRenderer(pos));
    }
    super.playerDestroy(level, player, pos, state, blockEntity, tool);
  }

  @Override
  public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
    if (player.getAbilities().instabuild && level instanceof ServerLevel serverLevel && level.getBlockEntity(pos) instanceof MachineControllerEntity entity) {
      PacketDistributor.sendToPlayersTrackingChunk(serverLevel, new ChunkPos(pos), new SStopSoundInstancePacket(pos));
      PacketDistributor.sendToPlayersTrackingChunk(serverLevel, new ChunkPos(pos), new SRemoveControllerRenderer(pos));
    }
    return super.playerWillDestroy(level, pos, state, player);
  }

  @Override
  public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, @Nullable Direction side) {
    return true;
  }

  @Override
  public BlockState rotate(BlockState state, Rotation rotation) {
    return state.setValue(BlockStateProperties.HORIZONTAL_FACING, rotation.rotate(getFacing(state)));
  }

  @Override
  protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
    BlockEntity te = level.getBlockEntity(pos);
    if (te instanceof MachineControllerEntity controller) {
      if (player instanceof ServerPlayer serverPlayer) {
        if (player.getItemInHand(hand).getItem() instanceof ItemBlueprint) {
          DynamicMachine machine = controller.getFoundMachine();
          PacketDistributor.sendToPlayersTrackingChunk(serverPlayer.serverLevel(), new ChunkPos(pos), new SAddControllerRenderer(machine.getRegistryName(), pos));
          return ItemInteractionResult.SUCCESS;
        }
        ControllerContainer.open(serverPlayer, controller);
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
  public boolean hasAnalogOutputSignal(BlockState state) {
    return true;
  }

  @Override
  public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
    BlockEntity tile = level.getBlockEntity(pos);
    if (tile instanceof MachineControllerEntity entity)
      RedstoneHelper.getRedstoneLevel(entity);
    return 0;
  }

  @Override
  public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
    BlockEntity tile = level.getBlockEntity(pos);
    if (tile instanceof MachineControllerEntity entity)
      RedstoneHelper.getRedstoneLevel(entity);
    return 0;
  }

  @Override
  public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
    BlockEntity tile = level.getBlockEntity(pos);
    if (tile instanceof MachineControllerEntity entity)
      RedstoneHelper.getRedstoneLevel(entity);
    return 0;
  }

  @Override
  @SuppressWarnings("deprecation")
  public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
    return Optional.ofNullable(level.getBlockEntity(pos))
        .filter(blockEntity -> blockEntity instanceof MachineControllerEntity)
        .map(tile -> ((MachineControllerEntity) tile).getInteractionSound())
        .orElse(super.getSoundType(state));
  }

  public Direction getFacing(BlockState state) {
    return state.getValue(BlockStateProperties.HORIZONTAL_FACING);
  }
}
