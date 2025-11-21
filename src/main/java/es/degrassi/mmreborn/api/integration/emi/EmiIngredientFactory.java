package es.degrassi.mmreborn.api.integration.emi;

import dev.emi.emi.api.stack.EmiIngredient;
import es.degrassi.mmreborn.api.crafting.requirement.IRequirement;
import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.common.machine.MachineComponent;

public interface EmiIngredientFactory<R extends RecipeRequirement<? extends MachineComponent<?>, ? extends IRequirement<?, ?>, ?>> {
  EmiIngredient create(R requirement);
}
