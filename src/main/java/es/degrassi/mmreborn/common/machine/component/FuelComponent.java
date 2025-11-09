package es.degrassi.mmreborn.common.machine.component;

import es.degrassi.mmreborn.api.capability.IFuelHandler;
import es.degrassi.mmreborn.common.crafting.ComponentType;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.MachineComponent;
import es.degrassi.mmreborn.common.registration.ComponentRegistration;
import es.degrassi.mmreborn.common.util.IOInventory;
import es.degrassi.mmreborn.common.util.ItemSlot;
import net.minecraft.world.item.crafting.RecipeType;

public class FuelComponent extends MachineComponent<IFuelHandler> {
  private final IFuelHandler fuelHandler;
  private final IOInventory inv;
  public FuelComponent(IFuelHandler fuelHandler, IOInventory inv) {
    super(IOType.INPUT);
    this.fuelHandler = fuelHandler;
    this.inv = inv;
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
    return null;
  }

  public boolean canStartRecipe(long amount) {
    if(this.getContainerProvider().getFuel()  >= amount)
      return true;
    return inv
        .getInventory()
        .stream()
        .filter(ItemSlot::isInput)
        .filter(slot -> !slot.getItemStack().isEmpty())
        .anyMatch(slot -> slot.getItemStack().getBurnTime(RecipeType.SMELTING) > 0);
  }
}
