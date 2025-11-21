package es.degrassi.mmreborn.api.integration.emi;

import dev.emi.emi.api.stack.EmiStack;
import es.degrassi.mmreborn.api.crafting.requirement.IRequirement;
import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.common.machine.MachineComponent;

import java.util.List;

public interface EmiStackFactory<
    R extends RecipeRequirement<C, I, T>,
    I extends IRequirement<C, T>,
    C extends MachineComponent<T>,
    T
    > {
  List<EmiStack> create(R requirement);
}
