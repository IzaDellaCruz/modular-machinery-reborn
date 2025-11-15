package es.degrassi.mmreborn.common.block;

import net.minecraft.world.level.block.SoundType;

public class BlockRedstoneHatch extends BlockMachineComponent {
  protected BlockRedstoneHatch() {
    super(
        Properties.of()
            .strength(2F, 10F)
            .sound(SoundType.METAL)
            .requiresCorrectToolForDrops()
            .dynamicShape()
            .noOcclusion()
    );
  }
}
