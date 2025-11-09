package es.degrassi.mmreborn.common.integration.kubejs.requirement;

import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementFuel;
import es.degrassi.mmreborn.common.integration.kubejs.MachineRecipeBuilderJS;
import es.degrassi.mmreborn.common.integration.kubejs.RecipeJSBuilder;
import es.degrassi.mmreborn.common.integration.kubejs.builder.FuelDataJS;

public interface FuelRequirementJS extends RecipeJSBuilder {

  default MachineRecipeBuilderJS requireFuel(int amount, FuelDataJS data) {
    return addRequirement(new RecipeRequirement<>(new RequirementFuel(amount, data.build())));
  }

  default MachineRecipeBuilderJS requireFuel(int amount) {
    return requireFuel(amount, FuelDataJS.create());
  }
}
