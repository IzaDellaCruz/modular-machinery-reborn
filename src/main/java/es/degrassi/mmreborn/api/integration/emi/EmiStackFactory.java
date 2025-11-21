package es.degrassi.mmreborn.api.integration.emi;

import dev.emi.emi.api.stack.EmiStack;
import es.degrassi.mmreborn.api.crafting.requirement.IRequirement;
import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.common.machine.MachineComponent;

import java.util.List;

public interface EmiStackFactory<R extends RecipeRequirement<? extends MachineComponent<?>, ? extends IRequirement<?, ?>, ?>> {
  List<EmiStack> create(R requirement);
}
