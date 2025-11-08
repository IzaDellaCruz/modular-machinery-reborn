package es.degrassi.mmreborn.common.machine.component;

import es.degrassi.mmreborn.common.crafting.ComponentType;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.MachineComponent;
import es.degrassi.mmreborn.common.registration.ComponentRegistration;
import es.degrassi.mmreborn.common.util.IOInventory;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.concurrent.atomic.AtomicInteger;

public class ItemComponent extends MachineComponent<IOInventory> {
  private final IOInventory handler;

  public ItemComponent(IOInventory handler, IOType ioType) {
    super(ioType);
    this.handler = handler;
  }

  @Override
  public ComponentType getComponentType() {
    return ComponentRegistration.COMPONENT_ITEM.get();
  }

  @Override
  public IOInventory getContainerProvider() {
    return handler;
  }

  public int getIngredientAmount(Ingredient ingredient) {
    return this.handler.getInputs().stream().filter(component -> ingredient.test(component.getItemStack()))
        .mapToInt(component -> component.getItemStack().getCount())
        .sum();
  }

  public int getItemAmount(ItemStack stack) {
    return this.handler.getItemAmount(stack);
  }

  public int getSpaceForItem(ItemStack stack) {
    return handler.getSpaceForItem(stack);
  }

  public void removeFromInputs(Ingredient ingredient, int amount) {
    AtomicInteger toRemove = new AtomicInteger(amount);
    this.handler.getInputs().stream().filter(component -> ingredient.test(component.getItemStack())).forEach(component -> {
      int maxExtract = Math.min(component.getItemStack().getCount(), toRemove.get());
      toRemove.addAndGet(-maxExtract);
      component.getItemStack().shrink(maxExtract);
      component.setChanged();
    });
  }

  public void addToOutputs(ItemStack stack, int amount) {
    handler.addToOutputs(stack, amount);
  }

  @Override
  public CompoundTag asTag(HolderLookup.Provider provider) {
    CompoundTag tag = super.asTag(provider);
    tag.put("handler", handler.writeNBT(provider));
    return tag;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <C extends MachineComponent<?>> C merge(C c) {
    ItemComponent comp = (ItemComponent) c;
    return (C) new ItemComponent(
        IOInventory.mergeBuild(handler, comp.handler),
        getIOType()
    );
  }
}
