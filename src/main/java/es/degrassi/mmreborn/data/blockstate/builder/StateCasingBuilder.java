package es.degrassi.mmreborn.data.blockstate.builder;

import es.degrassi.mmreborn.ModularMachineryReborn;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class StateCasingBuilder extends MMRStateBuilder<StateCasingBuilder> {

  @Override
  protected ResourceLocation loader() {
    return ModularMachineryReborn.rl("casing");
  }
}
