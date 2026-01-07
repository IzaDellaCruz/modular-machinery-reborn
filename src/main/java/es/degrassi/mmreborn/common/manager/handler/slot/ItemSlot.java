package es.degrassi.mmreborn.common.manager.handler.slot;

import com.mojang.datafixers.util.Pair;
import es.degrassi.mmreborn.api.handler.FilteredSlot;
import es.degrassi.mmreborn.api.network.ISyncable;
import es.degrassi.mmreborn.api.network.syncable.ItemStackSyncable;
import es.degrassi.mmreborn.common.manager.handler.ItemHandler;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ItemSlot implements IItemHandlerModifiable, FilteredSlot<ItemStack> {
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
  private final ItemHandler manager;
  @Getter
  private final int slot;

  public ItemSlot(int slot, ItemHandler manager, int capacity, int maxInput, int maxOutput,
                  Predicate<ItemStack> filter) {
    this.capacity = capacity;
    this.maxInput = maxInput;
    this.maxOutput = maxOutput;
    this.filter = filter;
    this.manager = manager;
    this.slot = slot;
  }

  public ItemSlot(ItemHandler manager, Predicate<ItemStack> filter, CompoundTag nbt, HolderLookup.Provider registries) {
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
    if (nbt.contains("item")) {
      var ops = registries.createSerializationContext(NbtOps.INSTANCE);
      var item = nbt.getCompound("item");
      var id = item.get("id");
      var components = item.get("components");
      var count = item.getInt("count");
      AtomicReference<DataComponentPatch> comps = new AtomicReference<>(DataComponentPatch.EMPTY);
      DataComponentPatch.CODEC.decode(ops, components).result()
          .map(Pair::getFirst)
          .ifPresent(comps::set);
      ItemStack.ITEM_NON_AIR_CODEC.decode(ops, id).result()
          .map(Pair::getFirst)
          .ifPresent(itemId -> {
            stack = new ItemStack(itemId, count, comps.get());
          });
    }
  }

  public CompoundTag serializeNBT(HolderLookup.Provider registries) {
    var nbt = new CompoundTag();
    var ops = registries.createSerializationContext(NbtOps.INSTANCE);
    if(!this.stack.isEmpty()) {
      CompoundTag item = new CompoundTag();
      ItemStack.ITEM_NON_AIR_CODEC.encodeStart(ops,
          stack.getItemHolder()).result().ifPresent(id -> {
          DataComponentPatch.CODEC.encodeStart(ops, this.stack.getComponentsPatch()).result().ifPresent(components -> {
            item.put("id", id);
            item.put("components", components);
            item.putInt("count", this.stack.getCount());
          });
      });
      nbt.put("item", item);
    }
    nbt.putInt("slot", this.slot);
    nbt.putInt("capacity", capacity);
    nbt.putInt("maxInput", maxInput);
    nbt.putInt("maxOutput", maxOutput);
    return nbt;
  }

  public boolean isInput() {
    return maxInput > 0;
  }

  public boolean isOutput() {
    return maxOutput > 0;
  }

  @Override
  public void setStackInSlot(int slot, ItemStack stack) {
    this.stack = stack;
    this.setChanged();
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
    setChanged();
  }

  public boolean isEmpty() {
    return this.stack == null || this.stack.isEmpty();
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
    amountToInsert = stack.isStackable() ? amountToInsert : stack.getMaxStackSize();
    //Check the current stack limit (if not empty stack)
    if(!this.stack.isEmpty())
      amountToInsert = Math.min(amountToInsert, this.capacity - this.stack.getCount());

    //Check the slot capacity
    amountToInsert = Math.min(amountToInsert, this.capacity - this.stack.getCount());

    //If nothing can be inserted return input
    if(amountToInsert <= 0)
      return stack;

    //If this slot is empty copy the input and insert the max amount
    if(this.stack.isEmpty()) {
      if(!simulate) {
        this.stack = stack.copyWithCount(amountToInsert);
        setChanged();
      }
    } else {//If this slot is not empty simply grow the contained stack
      if(!simulate) {
        this.stack.grow(amountToInsert);
        setChanged();
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
      setChanged();
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
  
  public void setChanged() {
    getManager().setChanged(slot, stack);
  }

  @Override
  public ItemStack getValue() {
    return stack;
  }
}
