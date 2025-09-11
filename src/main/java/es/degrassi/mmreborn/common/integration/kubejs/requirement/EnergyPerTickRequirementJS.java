package es.degrassi.mmreborn.common.integration.kubejs.requirement;

import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.common.crafting.requirement.PositionedRequirement;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementEnergyPerTick;
import es.degrassi.mmreborn.common.integration.kubejs.MachineRecipeBuilderJS;
import es.degrassi.mmreborn.common.integration.kubejs.RecipeJSBuilder;
import es.degrassi.mmreborn.common.machine.IOType;

public interface EnergyPerTickRequirementJS extends RecipeJSBuilder {

  default MachineRecipeBuilderJS requireEnergyPerTick(int amount, int x, int y) {
    return addRequirement(new RecipeRequirement<>(new RequirementEnergyPerTick(IOType.INPUT, amount, new PositionedRequirement(x, y))));
  }

  default MachineRecipeBuilderJS produceEnergyPerTick(int amount, int x, int y) {
    return addRequirement(new RecipeRequirement<>(new RequirementEnergyPerTick(IOType.OUTPUT, amount, new PositionedRequirement(x, y))));
  }
  default MachineRecipeBuilderJS requireEnergyPerTick(int amount) {
    return requireEnergyPerTick(amount, 0, 0);
  }

  default MachineRecipeBuilderJS produceEnergyPerTick(int amount) {
    return produceEnergyPerTick(amount, 0, 0);
  }
}
