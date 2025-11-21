package es.degrassi.mmreborn.api.crafting;

import es.degrassi.mmreborn.api.crafting.requirement.IRequirement;
import es.degrassi.mmreborn.common.machine.DynamicMachine;
import net.minecraft.resources.ResourceLocation;

public class ComponentNotFoundException extends RuntimeException {

  private final ResourceLocation recipeId;
  private final DynamicMachine machine;
  private final IRequirement<?, ?> requirement;

  public ComponentNotFoundException(ResourceLocation recipeId, DynamicMachine machine, IRequirement<?, ?> requirement) {
    this.recipeId = recipeId;
    this.machine = machine;
    this.requirement = requirement;
  }

  @Override
  public String getMessage() {
    return "Invalid Modular Machinery recipe: " +
        this.recipeId +
        " | Requirement: " +
        this.requirement.getMissingComponentErrorMessage(requirement.getMode()).getString() +
        " try to use a component the machine: " +
        this.machine.getRegistryName() +
        " doesn't have !";
  }

  @Override
  public synchronized Throwable fillInStackTrace() {
    return this;
  }
}
