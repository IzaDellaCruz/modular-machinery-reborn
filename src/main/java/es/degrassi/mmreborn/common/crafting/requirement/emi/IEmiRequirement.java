package es.degrassi.mmreborn.common.crafting.requirement.emi;

import dev.emi.emi.api.widget.WidgetHolder;
import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.common.crafting.requirement.PositionedRequirement;
import es.degrassi.mmreborn.common.integration.emi.recipe.MMREmiRecipe;

public interface IEmiRequirement<R extends RecipeRequirement<?, ?, ?>> {
  R getRequirement();

  void addWidgets(WidgetHolder widgets, MMREmiRecipe recipe);

  default PositionedRequirement getPosition() {
    return getRequirement().requirement().getPosition();
  }
}
