package es.degrassi.mmreborn.common.integration.kubejs.requirement;

import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.api.crafting.requirement.WeatherType;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementWeather;
import es.degrassi.mmreborn.common.integration.kubejs.MachineRecipeBuilderJS;
import es.degrassi.mmreborn.common.integration.kubejs.RecipeJSBuilder;

public interface WeatherRequirementJS extends RecipeJSBuilder {

  default MachineRecipeBuilderJS weather(WeatherType type) {
    return addRequirement(new RecipeRequirement<>(new RequirementWeather(type)));
  }
}
