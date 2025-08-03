package es.degrassi.mmreborn.common.util;

import es.degrassi.mmreborn.api.network.ISyncable;
import es.degrassi.mmreborn.api.network.ISyncableStuff;
import es.degrassi.mmreborn.api.network.syncable.ItemStackSyncable;
import es.degrassi.mmreborn.common.machine.IOType;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class ItemSlot implements IItemHandlerModifiable, ISyncableStuff {
  @Getter
  private final int capacity;
  private final int maxInput;
  private final int maxOutput;
  @Setter
  @Getter
  private Predicate<ItemStack> filter;
  private ItemStack stack = ItemStack.EMPTY;
  private boolean bypassLimit = false;
  @Getter
  private final IOInventory manager;
  @Getter
  private final int slot;

  public ItemSlot(int slot, IOInventory manager, int capacity, int maxInput, int maxOutput,
                  Predicate<ItemStack> filter) {
    this.capacity = capacity;
    this.maxInput = maxInput;
    this.maxOutput = maxOutput;
    this.filter = filter;
    this.manager = manager;
    this.slot = slot;
  }

  public ItemSlot(IOInventory manager, Predicate<ItemStack> filter, CompoundTag nbt, HolderLookup.Provider registries) {
    this.manager = manager;
    this.filter = filter;
    if (nbt.contains("item"))
      stack = ItemStack.parseOptional(registries, nbt.getCompound("item"));
    this.slot = nbt.getInt("slot");
    this.capacity = nbt.getInt("capacity");
    this.maxInput = nbt.getInt("maxInput");
    this.maxOutput = nbt.getInt("maxOutput");
  }

  public void deserialize(HolderLookup.Provider registries, CompoundTag nbt) {
    if (nbt.contains("item"))
      stack = ItemStack.parseOptional(registries, nbt.getCompound("item"));
  }

  public CompoundTag serializeNBT(HolderLookup.Provider registries) {
    var nbt = new CompoundTag();
    if(!this.stack.isEmpty())
      nbt.put("item", this.stack.save(registries));
    nbt.putInt("slot", this.slot);
    nbt.putInt("capacity", capacity);
    nbt.putInt("maxInput", maxInput);
    nbt.putInt("maxOutput", maxOutput);
    return nbt;
  }

  public IOType getMode() {
    if (maxInput > 0) return IOType.INPUT;
    return IOType.OUTPUT;
  }

  @Override
  public void setStackInSlot(int slot, ItemStack stack) {
    this.stack = stack;
    this.getManager().setChanged();
  }

  @Override
  public int getSlots() {
    return 1;
  }

  @Override
  public ItemStack getStackInSlot(int slot) {
    return stack;
  }

  public ItemStack insertItemBypassLimit(ItemStack stack, boolean simulate) {
    this.bypassLimit = true;
    ItemStack remainder = this.insertItem(0, stack, simulate);
    this.bypassLimit = false;
    return remainder;
  }

  public ItemStack extractItemBypassLimit(int amount, boolean simulate) {
    this.bypassLimit = true;
    ItemStack extracted = this.extractItem(0, amount, simulate);
    this.bypassLimit = false;
    return extracted;
  }

  public boolean canOutput() {
    return true;
  }

  public ItemStack getItemStack() {
    return this.stack;
  }

  public void setItemStack(ItemStack stack) {
    this.stack = stack;
    getManager().setChanged();
  }

  @Override
  public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
    if(stack.isEmpty() || !isItemValid(0, stack) || (!this.stack.isEmpty() && !ItemStack.isSameItemSameComponents(this.stack, stack)))
      return stack;

    int amountToInsert = stack.getCount();

    //Check the per-tick limit
    if(!this.bypassLimit)
      amountToInsert = Math.min(amountToInsert, this.maxInput);

    //Check the inserted stack max size, in case a mod like AE2 try to insert a stack of non-stackable items
    amountToInsert = Math.min(amountToInsert, stack.getMaxStackSize());

    //Check the current stack limit (if not empty stack)
    if(!this.stack.isEmpty())
      amountToInsert = Math.min(amountToInsert, this.stack.getMaxStackSize() - this.stack.getCount());

    //Check the slot capacity
    amountToInsert = Math.min(amountToInsert, this.capacity - this.stack.getCount());

    //If nothing can be inserted return input
    if(amountToInsert <= 0)
      return stack;

    //If this slot is empty copy the input and insert the max amount
    if(this.stack.isEmpty()) {
      if(!simulate) {
        this.stack = stack.copyWithCount(amountToInsert);
        getManager().setChanged();
      }
    } else {//If this slot is not empty simply grow the contained stack
      if(!simulate) {
        this.stack.grow(amountToInsert);
        getManager().setChanged();
      }
    }

    //If everything from input was inserted return empty, else copy input and return remainder
    if(amountToInsert == stack.getCount())
      return ItemStack.EMPTY;
    else
      return stack.copyWithCount(stack.getCount() - amountToInsert);
  }

  @Override
  public ItemStack extractItem(int slot, int amount, boolean simulate) {
    if(amount <= 0 || this.stack.isEmpty() || !this.canOutput())
      return ItemStack.EMPTY;

    //Check output limit
    if(!this.bypassLimit)
      amount = Math.min(amount, this.maxOutput);

    //Check current stack size
    amount = Math.min(amount, this.stack.getCount());

    ItemStack extracted = this.stack.copyWithCount(amount);

    if(!simulate) {
      this.stack.shrink(amount);
      getManager().setChanged();
    }
    return extracted;
  }

  @Override
  public int getSlotLimit(int slot) {
    return capacity;
  }

  @Override
  public boolean isItemValid(int slot, ItemStack stack) {
    return filter.test(stack);
  }

  @Override
  public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {
    container.accept(ItemStackSyncable.create(() -> this.stack, stack -> this.stack = stack));
  }
}
