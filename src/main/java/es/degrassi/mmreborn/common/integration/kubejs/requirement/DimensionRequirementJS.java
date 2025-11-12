package es.degrassi.mmreborn.common.integration.kubejs.requirement;

import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementDimension;
import es.degrassi.mmreborn.common.integration.kubejs.MachineRecipeBuilderJS;
import es.degrassi.mmreborn.common.integration.kubejs.RecipeJSBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public interface DimensionRequirementJS extends RecipeJSBuilder {

  default MachineRecipeBuilderJS dimensions(List<ResourceLocation> dimensions) {
    return dimensions(dimensions, false);
  }

  default MachineRecipeBuilderJS dimensions(List<ResourceLocation> dimensions, boolean blacklist) {
    return addRequirement(new RecipeRequirement<>(new RequirementDimension(dimensions, blacklist)));
  }
}
