package es.degrassi.mmreborn.api.integration.emi;

import dev.emi.emi.api.stack.EmiIngredient;
import es.degrassi.mmreborn.api.crafting.requirement.IRequirement;
import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.common.machine.MachineComponent;

public interface EmiIngredientFactory<
    R extends RecipeRequirement<C, I, T>,
    I extends IRequirement<C, T>,
    C extends MachineComponent<T>,
    T> {
  EmiIngredient create(R requirement);
}
