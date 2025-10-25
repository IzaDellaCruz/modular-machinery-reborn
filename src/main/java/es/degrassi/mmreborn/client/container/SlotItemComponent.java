package es.degrassi.mmreborn.client.container;

import es.degrassi.mmreborn.common.util.ItemSlot;
import lombok.Getter;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

@Getter
public class SlotItemComponent extends Slot {
  private static final Container EMPTY = new SimpleContainer(0);
  private final ItemSlot component;

  public SlotItemComponent(ItemSlot container, int slot, int x, int y) {
    super(EMPTY, slot, x, y);
    this.component = container;
  }

  @Override
  public ItemStack getItem() {
    return this.component.getItemStack();
  }

  @Override
  public boolean mayPlace(ItemStack stack) {
    return this.component.isItemValid(0, stack);
  }

  @Override
  public void set(ItemStack stack) {
    this.component.setItemStack(stack);
  }

  @Override
  public int getMaxStackSize() {
    return this.component.getCapacity();
  }

  @Override
  public ItemStack remove(int amount) {
    return this.component.extractItemBypassLimit(amount, false);
  }

  @Override
  public void setChanged() {
    this.component.getManager().setChanged();
  }

  @Override
  public String toString() {
    return "SlotItemComponent{" +
        "component=" + component +
        ", index=" + index +
        ", slot=" + getSlotIndex() +
        ", x=" + x +
        ", y=" + y +
        '}';
  }
}
