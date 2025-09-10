package es.degrassi.mmreborn.api.integration.emi;

import dev.emi.emi.api.widget.WidgetHolder;
import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementEmpty;
import es.degrassi.mmreborn.common.crafting.requirement.emi.EmiComponent;
import es.degrassi.mmreborn.common.integration.emi.recipe.MMREmiRecipe;
import es.degrassi.mmreborn.common.machine.component.EmptyComponent;

@FunctionalInterface
public interface EmiConsumer {
  void execute(EmiComponent<Void, RecipeRequirement<EmptyComponent, RequirementEmpty>> component, WidgetHolder widgets, MMREmiRecipe recipe);
}
