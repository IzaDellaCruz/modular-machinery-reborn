package es.degrassi.mmreborn.common.integration.kubejs.requirement;

import dev.latvian.mods.rhino.util.HideFromJS;
import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.common.crafting.requirement.PositionedRequirement;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementExperiencePerTick;
import es.degrassi.mmreborn.common.integration.kubejs.MachineRecipeBuilderJS;
import es.degrassi.mmreborn.common.integration.kubejs.RecipeJSBuilder;
import es.degrassi.mmreborn.common.machine.IOType;

public interface ExperiencePerTickRequirementJS extends RecipeJSBuilder {

  @HideFromJS
  default MachineRecipeBuilderJS requireExperiencePerTick(long amount, int x, int y) {
    return addRequirement(new RecipeRequirement<>(new RequirementExperiencePerTick(IOType.INPUT, amount, new PositionedRequirement(x, y))));
  }

  @HideFromJS
  default MachineRecipeBuilderJS produceExperiencePerTick(long amount, int x, int y) {
    return addRequirement(new RecipeRequirement<>(new RequirementExperiencePerTick(IOType.OUTPUT, amount, new PositionedRequirement(x, y))));
  }

  default MachineRecipeBuilderJS requireExperiencePerTick(long amount) {
    return requireExperiencePerTick(amount, 0, 0);
  }

  default MachineRecipeBuilderJS produceExperiencePerTick(long amount) {
    return produceExperiencePerTick(amount, 0, 0);
  }
}
