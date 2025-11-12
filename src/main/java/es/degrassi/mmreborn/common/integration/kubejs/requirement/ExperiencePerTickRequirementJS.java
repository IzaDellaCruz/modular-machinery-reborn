package es.degrassi.mmreborn.common.integration.kubejs.requirement;

import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementExperiencePerTick;
import es.degrassi.mmreborn.common.integration.kubejs.MachineRecipeBuilderJS;
import es.degrassi.mmreborn.common.integration.kubejs.RecipeJSBuilder;
import es.degrassi.mmreborn.common.machine.IOType;

public interface ExperiencePerTickRequirementJS extends RecipeJSBuilder {

  default MachineRecipeBuilderJS requireExperiencePerTick(long amount) {
    return addRequirement(new RecipeRequirement<>(new RequirementExperiencePerTick(IOType.INPUT, amount)));
  }

  default MachineRecipeBuilderJS produceExperiencePerTick(long amount) {
    return addRequirement(new RecipeRequirement<>(new RequirementExperiencePerTick(IOType.OUTPUT, amount)));
  }
}
