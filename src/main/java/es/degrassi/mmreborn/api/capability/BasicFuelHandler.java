package es.degrassi.mmreborn.api.capability;

import es.degrassi.experiencelib.api.capability.IContentsListener;
import es.degrassi.mmreborn.common.util.IOInventory;
import es.degrassi.mmreborn.common.util.ItemSlot;
import es.degrassi.mmreborn.common.util.MMRLogger;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

@Getter
@Setter
public class BasicFuelHandler implements IFuelHandler {
  private long fuel;
  private long maxFuel;

  private final IOInventory inventory;
  private IContentsListener listener;

  public BasicFuelHandler(IOInventory inventory) {
    this.inventory = inventory;
  }

  @Override
  public void addFuel(long fuel) {
    if (this.fuel >= this.maxFuel) return;
    this.fuel += fuel;
    setChanged();
  }

  @Override
  public boolean burn(long amount) {
    //If the machine have sufficient fuel, just burn it and return true
    if(this.fuel >= amount) {
      MMRLogger.INSTANCE.debug("Burning amount: {}", amount);
      addFuel(-amount);
      return true;
    }

    //If the machine still don't have the required fuel amount return false, the fuel requirement will error
    return false;
  }

  @Override
  public void tryBurnItem() {
    this.inventory
        .getInventory()
        .stream()
        .filter(ItemSlot::isInput)
        .filter(slot -> !slot.getItemStack().isEmpty())
        .filter(slot -> slot.getItemStack().getBurnTime(RecipeType.SMELTING) > 0)
        .findFirst()
        .ifPresent(slot -> {
          long fuel = slot.getItemStack().getBurnTime(RecipeType.SMELTING);
          if (!hasSpace(fuel)) return;
          addFuel(fuel);
          ItemStack stack = slot.getItemStack();
          if (stack.hasCraftingRemainingItem()) {
            slot.setItemStack(stack.getCraftingRemainingItem());
          } else {
            slot.extractItemBypassLimit(1, false);
          }
          slot.setChanged();
        });
  }

  @Override
  public boolean hasSpace(long amount) {
    var hasSpace = (this.fuel + amount) <= maxFuel;
    MMRLogger.INSTANCE.debug("hasSpaceForFuel: {}", hasSpace);
    return hasSpace;
  }

  @Override
  public void setChanged() {
    if (listener != null)
      listener.onContentsChanged();
  }
}
