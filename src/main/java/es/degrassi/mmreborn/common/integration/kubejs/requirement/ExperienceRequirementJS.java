package es.degrassi.mmreborn.common.integration.kubejs.requirement;

import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementExperience;
import es.degrassi.mmreborn.common.integration.kubejs.MachineRecipeBuilderJS;
import es.degrassi.mmreborn.common.integration.kubejs.RecipeJSBuilder;
import es.degrassi.mmreborn.common.machine.IOType;

public interface ExperienceRequirementJS extends RecipeJSBuilder {

  default MachineRecipeBuilderJS requireExperience(long amount) {
    return addRequirement(new RecipeRequirement<>(new RequirementExperience(IOType.INPUT, amount)));
  }

  default MachineRecipeBuilderJS produceExperience(long amount) {
    return addRequirement(new RecipeRequirement<>(new RequirementExperience(IOType.OUTPUT, amount)));
  }
}
