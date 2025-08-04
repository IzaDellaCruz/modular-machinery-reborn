package es.degrassi.mmreborn.common.block;

import es.degrassi.mmreborn.client.util.EnergyDisplayUtil;
import es.degrassi.mmreborn.common.block.prop.EnergyHatchSize;
import es.degrassi.mmreborn.common.entity.EnergyOutputHatchEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class BlockEnergyOutputHatch extends BlockEnergyHatch {

  public BlockEnergyOutputHatch(EnergyHatchSize type) {
    super(type);
  }

  @Override
  public void appendHoverText(ItemStack stack, Item.TooltipContext pContext, List<Component> tooltip, TooltipFlag flag) {
      if (EnergyDisplayUtil.displayFETooltip) {
        tooltip.add(Component.translatable("tooltip.energyhatch.storage", type.maxEnergy).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.energyhatch.out.transfer", type.transferLimit).withStyle(ChatFormatting.GRAY));
      }
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
    return new EnergyOutputHatchEntity(pos, state, type);
  }
}