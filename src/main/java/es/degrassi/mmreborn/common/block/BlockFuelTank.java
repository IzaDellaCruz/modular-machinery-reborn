package es.degrassi.mmreborn.common.block;

import es.degrassi.mmreborn.client.container.FuelTankContainer;
import es.degrassi.mmreborn.common.block.prop.FuelTankSize;
import es.degrassi.mmreborn.common.entity.FuelTankEntity;
import es.degrassi.mmreborn.common.util.RedstoneHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class BlockFuelTank extends BlockMachineComponent implements BlockTickEntity {
  protected final FuelTankSize size;
  public BlockFuelTank(FuelTankSize size) {
    super(
        Properties.of()
            .strength(2f, 10f)
            .sound(SoundType.METAL)
            .dynamicShape()
            .noOcclusion()
    );
    this.size = size;
  }

  @Override
  protected @NotNull ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
    BlockEntity te = level.getBlockEntity(pos);
    if(te instanceof FuelTankEntity entity) {
      if (player instanceof ServerPlayer serverPlayer) {
        FuelTankContainer.open(serverPlayer, entity);
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

  @Override
  public void appendHoverText(ItemStack stack, Item.TooltipContext pContext, List<Component> tooltip, TooltipFlag flag) {
    tooltip.add(Component.translatable("tooltip.fueltank.storage", size.burnTimeCapacity).withStyle(ChatFormatting.GRAY));
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
    return new FuelTankEntity(pos, state, size);
  }
}
