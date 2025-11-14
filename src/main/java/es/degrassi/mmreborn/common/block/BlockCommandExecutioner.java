package es.degrassi.mmreborn.common.block;

import net.minecraft.world.level.block.SoundType;

public class BlockCommandExecutioner extends BlockMachineComponent implements BlockTickEntity {
  protected BlockCommandExecutioner() {
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
