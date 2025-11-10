package es.degrassi.mmreborn.common.item;

import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.common.block.BlockFuelTank;
import es.degrassi.mmreborn.common.block.prop.FuelTankSize;
import es.degrassi.mmreborn.common.registration.DataComponentRegistration;
import net.minecraft.resources.ResourceLocation;

public class FuelTankItem extends ItemBlockMachineComponent implements ItemHatch {
  private static final ResourceLocation BASE_TEXTURE = ModularMachineryReborn.rl("block/casing_plain");
  private final FuelTankSize type;

  public FuelTankItem(BlockFuelTank block, FuelTankSize type) {
    super(
        block,
        new Properties()
            .component(DataComponentRegistration.BASE_TEXTURE, BASE_TEXTURE)
            .component(DataComponentRegistration.OVERLAY_TEXTURE, ModularMachineryReborn.rl("block/overlay_fueltank_" + type.getSerializedName()))
            .component(DataComponentRegistration.DEFAULT_MODEL, ModularMachineryReborn.rl("default/hatches/fuel_tank_" + type.getSerializedName()))
    );
    this.type = type;
  }

  @Override
  public ResourceLocation getDefaultBaseTexture() {
    return BASE_TEXTURE;
  }

  @Override
  public ResourceLocation getDefaultOverlayTexture() {
    return ModularMachineryReborn.rl("block/overlay_fueltank_" + type.getSerializedName());
  }
}
