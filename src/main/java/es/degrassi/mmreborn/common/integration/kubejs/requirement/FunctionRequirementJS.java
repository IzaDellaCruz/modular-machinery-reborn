package es.degrassi.mmreborn.common.integration.kubejs.requirement;

import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementFunction;
import es.degrassi.mmreborn.common.integration.kubejs.RecipeJSBuilder;

import java.util.List;

public interface FunctionRequirementJS extends RecipeJSBuilder {

  default RecipeJSBuilder requireFunctionToStart(String id, String... args) {
    return this.addRequirement(new RecipeRequirement<>(new RequirementFunction(RequirementFunction.Phase.CHECK, id, List.of(args))));
  }

  default RecipeJSBuilder requireFunctionOnStart(String id, String... args) {
    return this.addRequirement(new RecipeRequirement<>(new RequirementFunction(RequirementFunction.Phase.START, id, List.of(args))));
  }

  default RecipeJSBuilder requireFunctionEachTick(String id, String... args) {
    return this.addRequirement(new RecipeRequirement<>(new RequirementFunction(RequirementFunction.Phase.TICK, id, List.of(args))));
  }

  default RecipeJSBuilder requireFunctionOnEnd(String id, String... args) {
    return this.addRequirement(new RecipeRequirement<>(new RequirementFunction(RequirementFunction.Phase.END, id, List.of(args))));
  }
}
