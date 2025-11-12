package es.degrassi.mmreborn.common.integration.kubejs.requirement;

import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementHeight;
import es.degrassi.mmreborn.common.integration.kubejs.MachineRecipeBuilderJS;
import es.degrassi.mmreborn.common.integration.kubejs.RecipeJSBuilder;
import es.degrassi.mmreborn.common.util.IntRange;

public interface HeightRequirementJS extends RecipeJSBuilder {

  default MachineRecipeBuilderJS requireHeight(String height) {
    try {
      IntRange range = IntRange.createFromString(height);
      return this.addRequirement(new RecipeRequirement<>(new RequirementHeight(range)));
    } catch (IllegalArgumentException e) {
      return error("Impossible to parse height range: \"{}\", ", height, e);
    }
  }
}
