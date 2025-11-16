package es.degrassi.mmreborn.common.block;

import es.degrassi.mmreborn.client.ModularMachineryRebornClient;
import es.degrassi.mmreborn.common.block.prop.EffectDispenserSize;
import es.degrassi.mmreborn.common.entity.EffectDispenserEntity;
import es.degrassi.mmreborn.common.util.RedstoneHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;

import static es.degrassi.mmreborn.client.ModularMachineryRebornClient.createParticle;

public class BlockEffectDispenser extends BlockMachineComponent implements BlockTickEntity {
  private final EffectDispenserSize size;
  public BlockEffectDispenser(EffectDispenserSize size) {
    super(
        Properties.of()
            .strength(2F, 10F)
            .sound(SoundType.METAL)
            .requiresCorrectToolForDrops()
            .dynamicShape()
            .noOcclusion()
    );
    this.size = size;
  }

  @Override
  public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
    super.animateTick(state, level, pos, random);
    if (!ModularMachineryRebornClient.shouldAddParticles(random)) return;
    if (size.interdimensional || !size.showParticles) return;
    if (level.getBlockEntity(pos) instanceof EffectDispenserEntity entity) {
      entity.getEffect().ifPresent(effect -> {
        var particle = effect.getParticleOptions();
        AABB bb = new AABB(pos).inflate(size.radius)
            .setMaxY(pos.getY())
            .setMinY(pos.getY());
        for (double i = bb.minX; i <= bb.maxX; i += 1) {
          for (double j = bb.minY; j <= bb.maxY; j+= 1) {
            for (double k = bb.minZ; k <= bb.maxZ; k += 1) {
              createParticle(particle, new BlockPos((int)i, (int)j, (int)k));
            }
          }
        }
      });
    }
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
  public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
    return new EffectDispenserEntity(blockPos, blockState, size);
  }

  @Override
  public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
    if (size.interdimensional) {
      tooltipComponents.add(Component.translatable("tooltip.effectdispenser.interdimensional").withStyle(ChatFormatting.GRAY));
    } else {
      tooltipComponents.add(Component.translatable("tooltip.effectdispenser.radius", size.radius).withStyle(ChatFormatting.GRAY));
    }
  }
}
