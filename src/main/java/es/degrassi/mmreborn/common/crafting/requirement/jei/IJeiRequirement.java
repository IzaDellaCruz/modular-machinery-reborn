package es.degrassi.mmreborn.common.crafting.requirement.jei;

import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.common.crafting.MachineRecipe;
import es.degrassi.mmreborn.common.crafting.requirement.PositionedRequirement;
import es.degrassi.mmreborn.common.integration.jei.category.MMRRecipeCategory;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;

public interface IJeiRequirement<R extends RecipeRequirement<?, ?, ?>> {
  void setRecipe(MMRRecipeCategory category, IRecipeLayoutBuilder builder, MachineRecipe recipe, IFocusGroup focuses);

  R getRequirement();

  default PositionedRequirement getPosition() {
    return getRequirement().requirement().getPosition();
  }
}
