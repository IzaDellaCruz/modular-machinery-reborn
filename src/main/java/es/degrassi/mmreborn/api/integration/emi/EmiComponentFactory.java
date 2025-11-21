package es.degrassi.mmreborn.api.integration.emi;

import es.degrassi.mmreborn.api.crafting.requirement.IRequirement;
import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.common.crafting.requirement.emi.EmiComponent;
import es.degrassi.mmreborn.common.machine.MachineComponent;

public interface EmiComponentFactory<
    R extends RecipeRequirement<C, T, X>,
    T extends IRequirement<C, X>,
    C extends MachineComponent<X>,
    X,
    Y
> {
  EmiComponent<Y, R> create(R requirement);
}
