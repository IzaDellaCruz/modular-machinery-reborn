package es.degrassi.mmreborn.api.crafting;

import es.degrassi.mmreborn.api.crafting.requirement.IRequirement;
import es.degrassi.mmreborn.common.crafting.ComponentType;
import es.degrassi.mmreborn.common.machine.DynamicMachine;
import net.minecraft.resources.ResourceLocation;

public class ComponentNotFoundException extends RuntimeException {

  private final DynamicMachine machine;
  private final ComponentType<?> requirement;

  public ComponentNotFoundException(DynamicMachine machine, IRequirement<?, ?> requirement) {
    this.machine = machine;
    this.requirement = requirement.getComponentType();
  }

  public ComponentNotFoundException(DynamicMachine machine, ComponentType<?> requirement) {
    this.machine = machine;
    this.requirement = requirement;
  }

  @Override
  public String getMessage() {
    return "Requirement: " +
        this.requirement +
        " try to use a component the machine: " +
        this.machine.getRegistryName() +
        " doesn't have !";
  }

  @Override
  public synchronized Throwable fillInStackTrace() {
    return this;
  }
}
