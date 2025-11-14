package es.degrassi.mmreborn.common.item;

import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.common.block.BaseEntityBlock;
import es.degrassi.mmreborn.common.block.BlockEntityDetector;
import es.degrassi.mmreborn.common.block.BlockEntityKiller;
import es.degrassi.mmreborn.common.block.BlockEntitySpawner;
import es.degrassi.mmreborn.common.block.BlockEntityHealer;
import es.degrassi.mmreborn.common.block.BlockEntityDamager;
import es.degrassi.mmreborn.common.registration.DataComponentRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class EntityItem extends ItemBlockMachineComponent implements ItemHatch {
  private static final ResourceLocation BASE_TEXTURE = ModularMachineryReborn.rl("block/casing_plain");
  public EntityItem(BaseEntityBlock block) {
    super(
        block,
        new Properties()
            .component(DataComponentRegistration.BASE_TEXTURE, BASE_TEXTURE)
            .component(DataComponentRegistration.OVERLAY_TEXTURE, ModularMachineryReborn.rl("block/overlay_entity" + fromBlock(block)))
            .component(DataComponentRegistration.DEFAULT_MODEL, ModularMachineryReborn.rl("default/hatches/entity_" + fromBlock(block)))
    );
  }

  private static String fromBlock(Block block) {
    return switch (block) {
      case BlockEntityDetector $ -> "detector";
      case BlockEntitySpawner $ -> "spawner";
      case BlockEntityKiller $ -> "killer";
      case BlockEntityDamager $ -> "damager";
      case BlockEntityHealer $ -> "healer";
      default -> "";
    };
  }

  @Override
  public ResourceLocation getDefaultBaseTexture() {
    return BASE_TEXTURE;
  }

  @Override
  public ResourceLocation getDefaultOverlayTexture() {
    return ModularMachineryReborn.rl("block/overlay_entity" + fromBlock(getBlock()));
  }
}
