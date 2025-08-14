package es.degrassi.mmreborn.common.block;

import es.degrassi.experiencelib.api.capability.IExperienceHandler;
import es.degrassi.mmreborn.client.container.ExperienceHatchContainer;
import es.degrassi.mmreborn.common.block.prop.ExperienceHatchSize;
import es.degrassi.mmreborn.common.entity.base.ExperienceHatchEntity;
import es.degrassi.mmreborn.common.util.RedstoneHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlockExperienceHatch extends BlockMachineComponent implements BlockTickEntity {
  protected final ExperienceHatchSize size;

  public BlockExperienceHatch(ExperienceHatchSize size) {
    super(
        Properties.of()
            .dynamicShape()
            .noOcclusion()
            .strength(2F, 10F)
            .sound(SoundType.METAL)
    );
    this.size = size;
  }

  @Override
  public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag pTooltipFlag) {
    tooltip.add(
        Component.translatable("tooltip.experiencehatch.tank.info", size.getCapacity()).withStyle(ChatFormatting.GRAY)
    );
  }

  @Override
  protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
    BlockEntity te = level.getBlockEntity(pos);
    if (te instanceof ExperienceHatchEntity entity) {
      if (player instanceof ServerPlayer serverPlayer) {
        ExperienceHatchContainer.open(serverPlayer, entity);
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
  public int getExpDrop(BlockState state, LevelAccessor level, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Entity breaker, ItemStack tool) {
    if (level.isClientSide()) return super.getExpDrop(state, level, pos, blockEntity, breaker, tool);
    if (level.getBlockEntity(pos) instanceof ExperienceHatchEntity entity) {
      IExperienceHandler tank = entity.getTank();
      if (tank.getExperience() > 0) {
        for (int i = 0; i < tank.getTanks(); i++) {
          long extracted = tank.extractExperienceRecipe(i, Long.MAX_VALUE, false);
          while (extracted > Integer.MAX_VALUE) {
            popExperience((ServerLevel) level, pos, Integer.MAX_VALUE);
            extracted -= Integer.MAX_VALUE;
          }
        }
        return (int) tank.getExperience();
      }
    }
    return super.getExpDrop(state, level, pos, blockEntity, breaker, tool);
  }
}
