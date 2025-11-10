package es.degrassi.mmreborn.common.machine.component;

import es.degrassi.mmreborn.api.capability.BasicFuelHandler;
import es.degrassi.mmreborn.api.capability.IFuelHandler;
import es.degrassi.mmreborn.common.crafting.ComponentType;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.MachineComponent;
import es.degrassi.mmreborn.common.registration.ComponentRegistration;

@SuppressWarnings("unchecked")
public class FuelComponent extends MachineComponent<IFuelHandler> {
  private final IFuelHandler fuelHandler;
  public FuelComponent(IFuelHandler fuelHandler) {
    super(IOType.INPUT);
    this.fuelHandler = fuelHandler;
  }

  @Override
  public ComponentType getComponentType() {
    return ComponentRegistration.COMPONENT_FUEL.get();
  }

  @Override
  public IFuelHandler getContainerProvider() {
    return fuelHandler;
  }

  @Override
  public <C extends MachineComponent<?>> C merge(C c) {
    FuelComponent comp = (FuelComponent) c;

    return (C) new FuelComponent(new BasicFuelHandler(null) {
      @Override
      public void addFuel(long fuel) {
        if (fuelHandler.getFuel() + fuel <= fuelHandler.getMaxFuel()) {
          fuelHandler.addFuel(fuel);
          return;
        }
        var rest = Math.min(fuelHandler.getMaxFuel() - fuelHandler.getFuel(), fuel);
        fuelHandler.addFuel(rest);
        fuel -= rest;
        if (fuel <= 0) return;
        if (comp.fuelHandler.getFuel() + fuel <= comp.fuelHandler.getMaxFuel()) {
          comp.fuelHandler.addFuel(fuel);
          return;
        }
        rest = Math.min(comp.fuelHandler.getMaxFuel() - comp.fuelHandler.getFuel(), fuel);
        comp.fuelHandler.addFuel(rest);
      }

      @Override
      public boolean burn(long amount) {
        if (!fuelHandler.burn(amount) && !comp.fuelHandler.burn(amount)) {
          var first = fuelHandler.getFuel();
          var second = comp.fuelHandler.getFuel();
          if (first + second >= amount) {
            fuelHandler.addFuel(-first);
            amount -= first;
            comp.fuelHandler.addFuel(-amount);
            return true;
          }
          return false;
        }
        return true;
      }

      @Override
      public boolean hasSpace(long amount) {
        if (!fuelHandler.hasSpace(amount) && !comp.fuelHandler.hasSpace(amount)) {
          var first = fuelHandler.getMaxFuel() - fuelHandler.getFuel();
          var second = comp.fuelHandler.getMaxFuel() - comp.fuelHandler.getFuel();
          return first + second >= amount;
        }
        return true;
      }

      @Override
      public long getFuel() {
        return fuelHandler.getFuel() + comp.fuelHandler.getFuel();
      }

      @Override
      public long getMaxFuel() {
        return fuelHandler.getMaxFuel() + comp.fuelHandler.getMaxFuel();
      }
    });
  }

  public boolean canStartRecipe(long amount) {
    return this.getContainerProvider().getFuel() >= amount;
  }
}
