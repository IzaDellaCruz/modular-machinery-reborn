package es.degrassi.mmreborn.common.util;

import com.google.common.collect.Maps;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import java.util.Map;

public class ItemUtils {
  private ItemUtils() {}
  public static boolean consumeFromInventory(IItemHandlerModifiable handler, ItemStack toConsume, boolean simulate, boolean strict) {
    Map<Integer, ItemStack> contents = findItemsIndexedInInventory(handler, toConsume, strict);
    if (contents.isEmpty()) return false;

    int cAmt = toConsume.getCount();
    for (int slot : contents.keySet()) {
      ItemStack inSlot = handler.getStackInSlot(slot);
      int toRemove = Math.min(cAmt, inSlot.getCount());
      ItemStack extracted = handler.extractItem(slot, toRemove, simulate);
      cAmt -= extracted.getCount();
      if (cAmt <= 0) return true;
    }
    return false;
  }

  //Returns the amount inserted
  public static int tryPlaceItemInInventory(@Nonnull ItemStack stack, IItemHandlerModifiable handler, boolean simulate) {
    return tryPlaceItemInInventory(stack, handler, 0, handler.getSlots(), simulate);
  }

  public static int tryPlaceItemInInventory(@Nonnull ItemStack stack, IItemHandlerModifiable handler, int start, int end, boolean simulate) {
    ItemStack toAdd = stack.copy();
    if (!hasInventorySpace(toAdd, handler, start, end)) {
      return 0;
    }
    int insertedAmt = 0;
    int max = toAdd.getMaxStackSize();

    for (int i = start; i < end; i++) {
      ItemStack in = handler.getStackInSlot(i);
      if (in.isEmpty()) {
        int added = Math.min(stack.getCount(), max);
        stack.setCount(stack.getCount() - added);
        if(!simulate) {
          handler.setStackInSlot(i, copyStackWithSize(toAdd, added));
        }
        insertedAmt += added;
        if (stack.getCount() <= 0)
          return insertedAmt;
      } else {
        if (matchTags(toAdd, in)) {
          int space = max - in.getCount();
          int added = Math.min(stack.getCount(), space);
          insertedAmt += added;
          stack.setCount(stack.getCount() - added);
          if(!simulate) {
            handler.setStackInSlot(i, handler.getStackInSlot(i).copyWithCount(handler.getStackInSlot(i).getCount() + added));
          }
          if (stack.getCount() <= 0)
            return insertedAmt;
        }
      }
    }
    return insertedAmt;
  }

  public static boolean hasInventorySpace(@Nonnull ItemStack stack, IItemHandler handler, int rangeMin, int rangeMax) {
    int size = stack.getCount();
    int max = stack.getMaxStackSize();
    for (int i = rangeMin; i < rangeMax && size > 0; i++) {
      ItemStack in = handler.getStackInSlot(i);
      if (in.isEmpty()) {
        size -= max;
      } else {
        if (matchTags(stack, in)) {
          int space = max - in.getCount();
          size -= space;
        }
      }
    }
    return size <= 0;
  }

  public static boolean stackEqualsNonNBT(@Nonnull ItemStack stack, @Nonnull  ItemStack other) {
    if (stack.isEmpty() && other.isEmpty())
      return true;
    if (stack.isEmpty() || other.isEmpty())
      return false;
    return ItemStack.isSameItem(stack, other);
  }

  public static boolean matchTags(@Nonnull ItemStack stack, @Nonnull  ItemStack other) {
    return stackEqualsNonNBT(stack, other) && ItemStack.isSameItemSameComponents(stack, other);
  }

  @Nonnull
  public static ItemStack copyStackWithSize(@Nonnull ItemStack stack, int amount) {
    if (stack.isEmpty() || amount <= 0) return ItemStack.EMPTY;
    return stack.copyWithCount(amount);
  }

  public static Map<Integer, ItemStack> findItemsIndexedInInventory(IItemHandlerModifiable handler, ItemStack match, boolean strict) {
    Map<Integer, ItemStack> stacksOut = Maps.newHashMap();
    for (int j = 0; j < handler.getSlots(); j++) {
      ItemStack s = handler.getStackInSlot(j);
      if ((strict ? matchStacks(s, match) : matchStackLoosely(s, match))) {
        stacksOut.put(j, s.copy());
      }
    }
    return stacksOut;
  }

  public static boolean matchStacks(@Nonnull ItemStack stack, @Nonnull  ItemStack other) {
    return matchTags(stack, other);
  }

  public static boolean matchStackLoosely(@Nonnull ItemStack stack, @Nonnull  ItemStack other) {
    return stackEqualsNonNBT(stack, other);
  }
}
