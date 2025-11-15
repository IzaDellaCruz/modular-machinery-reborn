package es.degrassi.mmreborn.common.block;

import es.degrassi.mmreborn.common.entity.StructureCheckerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockStructureChecker extends BlockMachineComponent {
  public BlockStructureChecker() {
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
    return new StructureCheckerEntity(blockPos, blockState);
  }
}
