package es.degrassi.mmreborn.common.block;

import es.degrassi.mmreborn.client.container.EnergyHatchContainer;
import es.degrassi.mmreborn.common.block.prop.EnergyHatchSize;
import es.degrassi.mmreborn.common.entity.base.EnergyHatchEntity;
import es.degrassi.mmreborn.common.util.RedstoneHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public class BlockEnergyHatch extends BlockMachineComponent implements BlockTickEntity {
  protected final EnergyHatchSize type;
  public BlockEnergyHatch(EnergyHatchSize type) {
    super(
      Properties.of()
        .strength(2f, 10f)
        .sound(SoundType.METAL)
        .dynamicShape()
        .noOcclusion()
    );
    this.type = type;
  }

  @Override
  protected @NotNull ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
    BlockEntity te = level.getBlockEntity(pos);
    if(te instanceof EnergyHatchEntity entity) {
      if (player instanceof ServerPlayer serverPlayer) {
        EnergyHatchContainer.open(serverPlayer, entity);
      }
      return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
    return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
  }

  @Override
  public boolean hasAnalogOutputSignal(BlockState pState) {
    return true;
  }

  @Override
  public int getAnalogOutputSignal(BlockState pState, Level pLevel, BlockPos pPos) {
    return RedstoneHelper.getRedstoneLevel(pLevel.getBlockEntity(pPos));
  }
}