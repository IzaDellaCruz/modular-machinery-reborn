package es.degrassi.mmreborn.data.blockstate.builder;

import es.degrassi.mmreborn.ModularMachineryReborn;
import net.minecraft.resources.ResourceLocation;

public class StateHatchBuilder extends MMRStateBuilder<StateHatchBuilder> {

  @Override
  protected ResourceLocation loader() {
    return ModularMachineryReborn.rl("hatch");
  }
}
