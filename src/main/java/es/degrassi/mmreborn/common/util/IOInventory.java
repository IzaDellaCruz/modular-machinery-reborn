package es.degrassi.mmreborn.common.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import es.degrassi.mmreborn.api.network.ISyncable;
import es.degrassi.mmreborn.api.network.ISyncableStuff;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class IOInventory implements IItemHandlerModifiable, Container, ISyncableStuff {
  @Getter
  private final List<ItemSlot> inputs = new ArrayList<>();
  @Getter
  private final List<ItemSlot> outputs = new ArrayList<>();

  private final Map<Integer, Integer> slotLimits = Maps.newHashMap(); //Value not present means default, aka 64.
  @Getter
  private final List<ItemSlot> inventory = Lists.newArrayList();
  private int[] inSlots = new int[0], outSlots = new int[0], miscSlots = new int[0];

  @Setter
  private IOInventoryChangedListener listener = null;
  public List<Direction> accessibleSides = new ArrayList<>();

  @Getter
  @Setter
  private Level level;

  @Getter
  private final Predicate<ItemStack> defaultFilter;

  private IOInventory() {
    accessibleSides = Arrays.asList(Direction.values());
    defaultFilter = stack -> true;
  }

  public IOInventory(int[] inSlots, int[] outSlots) {
    this(inSlots, outSlots, Direction.values());
  }

  public IOInventory(int[] inSlots, int[] outSlots, Direction... accessibleFrom) {
    this.inSlots = inSlots;
    this.outSlots = outSlots;
    defaultFilter = stack -> true;
    this.inventory.addAll(generateInventory());
    this.accessibleSides = Arrays.asList(accessibleFrom);
  }

  public IOInventory(int[] inSlots, int[] outSlots, Predicate<ItemStack> filter, Direction... accessibleFrom) {
    this.inSlots = inSlots;
    this.outSlots = outSlots;
    defaultFilter = filter;
    this.inventory.addAll(generateInventory(filter));
    this.accessibleSides = Arrays.asList(accessibleFrom);
  }

  @Override
  public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {
    this.inventory.forEach(component -> component.getStuffToSync(container));
  }

  public List<ItemStack> getAllInputStacks() {
    return this.inputs.stream().map(slot -> slot.getStackInSlot(0)).toList();
  }

  public List<ItemStack> getAllOutputStacks() {
    return this.outputs.stream().map(slot -> slot.getStackInSlot(0)).toList();
  }

  public List<ItemStack> getAllStacks() {
    return this.inventory.stream().map(slot -> slot.getStackInSlot(0)).toList();
  }

  public IOInventory setMiscSlots(int... miscSlots) {
    this.miscSlots = miscSlots;
    for (Integer slot : miscSlots) {
      this.inventory.add(new ItemSlot(slot, this, 64, 64, 0, item -> true));
    }
    return this;
  }

  public IOInventory setStackLimit(int limit, int... slots) {
    for (int slot : slots) {
      this.slotLimits.put(slot, limit);
    }
    return this;
  }

  @Override
  public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
    inventory.stream().filter(s -> s.getSlot() == slot)
        .findFirst()
        .ifPresent(s -> {
          s.setItemStack(stack);
        });
    setChanged();
  }

  @Override
  public int getSlots() {
    return inventory.size();
  }

  @Override
  public int getSlotLimit(int slot) {
    if (slotLimits.containsKey(slot)) {
      return slotLimits.get(slot);
    }
    return 64;
  }

  public void setFilter(int slot, Predicate<ItemStack> filter) {
    this.inventory.stream()
        .filter(s -> s.getSlot() == slot)
        .forEach(s -> s.setFilter(filter));
  }

  @Override
  public boolean isItemValid(int slot, ItemStack stack) {
    return inventory.stream()
        .filter(s -> s.getSlot() == slot)
        .findFirst()
        .map(s -> s.isItemValid(0, stack))
        .orElse(false);
  }

  @Override
  @Nonnull
  public ItemStack getStackInSlot(int slot) {
    return Optional.ofNullable(inventory.get(slot)).map(ItemSlot::getItemStack).orElse(ItemStack.EMPTY);
  }

  public ItemStack insertItem(@Nonnull ItemStack stack, boolean simulate) {
    ItemStack toInsert = stack.copy();
    for (int i = 0; i < getSlots(); i++) {
      toInsert = insertItem(i, toInsert.copy(), simulate);
      if (toInsert.isEmpty()) return ItemStack.EMPTY;
    }
    return toInsert;
  }

  public ItemStack extractItem(@Nonnull ItemStack stack, boolean simulate) {
    int toExtract = stack.getCount();
    for (int i = 0; i < getSlots(); i++) {
      if (getItem(i).isEmpty() || !ItemStack.isSameItemSameComponents(getItem(i), stack))
        continue;
      toExtract -= extractItem(i, toExtract, simulate).getCount();
      if (toExtract <= 0) return ItemStack.EMPTY;
    }
    return stack.copyWithCount(toExtract);
  }

  @Override
  @Nonnull
  public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
    return inventory.get(slot).insertItem(0, stack, simulate);
  }

  @Override
  @Nonnull
  public ItemStack extractItem(int slot, int amount, boolean simulate) {
    return inventory.get(slot).extractItem(0, amount, simulate);
  }

  @Override
  public int getContainerSize() {
    return getSlots();
  }

  @Override
  public ItemStack getItem(int slot) {
    return getStackInSlot(slot);
  }

  @Override
  public ItemStack removeItem(int slot, int amount) {
    return extractItem(slot, amount, false);
  }

  @Override
  public ItemStack removeItemNoUpdate(int slot) {
    ItemStack prevStack = getStackInSlot(slot);
    setStackInSlot(slot, ItemStack.EMPTY);
    return prevStack;
  }

  @Override
  public void setItem(int slot, ItemStack stack) {
    setStackInSlot(slot, stack);
  }

  @Override
  public void setChanged() {
    if (listener != null) {
      listener.onChange();
    }
  }

  @Override
  public boolean stillValid(Player player) {
    return true;
  }

  @Override
  public void clearContent() {
    for (int j = 0; j < getContainerSize(); j++)
      removeItemNoUpdate(j);
  }

  @Override
  public boolean isEmpty() {
    return inventory.stream().map(ItemSlot::getItemStack).allMatch(ItemStack::isEmpty);
  }

  public int getDurabilityAmount(ItemStack stack) {
    return this.inputs.stream().filter(component -> isSameItem(component.getItemStack(), stack) && component.getItemStack().isDamageableItem())
        .mapToInt(component -> component.getItemStack().getMaxDamage() - component.getItemStack().getDamageValue())
        .sum();
  }

  public int getSpaceForDurability(ItemStack stack) {
    return this.inputs.stream().filter(component -> isSameItem(component.getItemStack(), stack) && component.getItemStack().isDamageableItem())
        .mapToInt(component -> component.getItemStack().getDamageValue())
        .sum();
  }

  public void repairItem(ItemStack stack, int amount) {
    AtomicInteger toRepair = new AtomicInteger(amount);
    this.inputs.stream()
        .filter(component -> isSameItem(component.getItemStack(), stack) && component.getItemStack().isDamageableItem())
        .forEach(component -> {
          int maxRepair = Math.min(component.getItemStack().getDamageValue(), toRepair.get());
          toRepair.addAndGet(-maxRepair);
          component.getItemStack().setDamageValue(component.getItemStack().getDamageValue() - maxRepair);
        });
    setChanged();
  }

  public void removeDurability(ItemStack input, int amount) {
    AtomicInteger toRemove = new AtomicInteger(amount);
    this.inputs.stream()
        .filter(component -> isSameItem(component.getItemStack(), input) && component.getItemStack().isDamageableItem())
        .forEach(component -> {
          int maxRemove = Math.min(component.getItemStack().getMaxDamage() - component.getItemStack().getDamageValue(), toRemove.get());
          ItemStack stack = component.getItemStack();
          maxRemove = stack.getItem().damageItem(stack, maxRemove, null, s -> {});
          if (maxRemove > 0) {
            maxRemove = EnchantmentHelper.processDurabilityChange((ServerLevel)getLevel(), stack, maxRemove);
            if (maxRemove <= 0) {
              return;
            }
          }
          toRemove.addAndGet(-maxRemove);
          stack.setDamageValue(stack.getDamageValue() + maxRemove);
          if(stack.getDamageValue() >= stack.getMaxDamage())
            stack.shrink(1);
        });
    setChanged();
  }

  private static boolean isSameItem(ItemStack toTest, ItemStack ingredient) {
    if(toTest.getItem() != ingredient.getItem())
      return false;
    return ingredient.getComponents().stream()
        .allMatch(component -> component.type() == DataComponents.DAMAGE || (toTest.has(component.type()) && Objects.equals(toTest.get(component.type()), component.value())));
  }

  public CompoundTag writeNBT(HolderLookup.Provider pRegistries) {
    CompoundTag tag = new CompoundTag();
    tag.putIntArray("inSlots", this.inSlots);
    tag.putIntArray("outSlots", this.outSlots);
    tag.putIntArray("miscSlots", this.miscSlots);

    ListTag components = new ListTag();
    this.inventory.forEach((value) -> components.add(value.serializeNBT(pRegistries)));
    tag.put("items", components);
    return tag;
  }

  public void readNBT(CompoundTag tag, HolderLookup.Provider pRegistries) {
    this.inSlots = tag.getIntArray("inSlots");
    this.outSlots = tag.getIntArray("outSlots");
    this.miscSlots = tag.getIntArray("miscSlots");

    if (tag.contains("items")) {
      ListTag components = tag.getList("items", Tag.TAG_COMPOUND);
      components.stream()
          .filter(t -> t instanceof CompoundTag)
          .map(t -> (CompoundTag) t)
          .forEach(componentNBT -> {
            if (componentNBT.contains("slot")) {
              this.inventory.stream()
                  .filter(inv -> inv.getSlot() == componentNBT.getInt("slot"))
                  .findFirst()
                  .ifPresentOrElse(
                      inv -> inv.deserialize(pRegistries, componentNBT),
                      () -> this.inventory.add(new ItemSlot(this, defaultFilter, componentNBT, pRegistries))
                  );
            }
          });
      this.inputs.clear();
      this.outputs.clear();
      this.inputs.addAll(this.inventory.stream().filter(slot -> Arrays.stream(this.inSlots).anyMatch(i -> i == slot.getSlot())).toList());
      this.outputs.addAll(this.inventory.stream().filter(slot -> Arrays.stream(this.outSlots).anyMatch(i -> i == slot.getSlot())).toList());
      this.setChanged();
    }
  }

  private List<ItemSlot> generateInventory() {
    return generateInventory(item -> true);
  }

  private List<ItemSlot> generateInventory(Predicate<ItemStack> filter) {
    List<ItemSlot> inventory = new ArrayList<>();
    for (Integer slot : inSlots) {
      ItemSlot itemSlot = new ItemSlot(slot, this, getSlotLimit(slot), getSlotLimit(slot), 0, filter);
      this.inputs.add(itemSlot);
      inventory.add(itemSlot);
    }
    for (Integer slot : outSlots) {
      ItemSlot itemSlot = new ItemSlot(slot, this, getSlotLimit(slot), 0, getSlotLimit(slot), filter);
      this.outputs.add(itemSlot);
      inventory.add(itemSlot);
    }
    return inventory;
  }

  public void deserialize(CompoundTag tag, HolderLookup.Provider pRegistries) {
    readNBT(tag, pRegistries);
  }

  public int calcRedstoneFromInventory() {
    int i = 0;
    float f = 0.0F;
    for (int j = 0; j < getSlots(); ++j) {
      ItemStack itemstack = getStackInSlot(j);
      if (!itemstack.isEmpty()) {
        f += (float) itemstack.getCount() / (float) Math.min(getSlotLimit(j), itemstack.getMaxStackSize());
        ++i;
      }
    }
    f = f / (float) getSlots();
    return Mth.floor(f * 14.0F) + (i > 0 ? 1 : 0);

  }

  public static IOInventory mergeBuild(IOInventory... inventories) {
    IOInventory merged = new IOInventory();
    int slotOffset = 0;
    Map<Integer, IOInventory> slotLimitIndex = Maps.newHashMap();
    List<Integer> inSlots = Lists.newArrayList();
    List<Integer> outSlots = Lists.newArrayList();
    List<Integer> miscSlots = Lists.newArrayList();
    List<Direction> sides = Lists.newArrayList(Direction.values());
    List<ItemSlot> inputs = Lists.newArrayList();
    List<ItemSlot> outputs = Lists.newArrayList();
    for (IOInventory inventory : inventories) {
      for (ItemSlot key : inventory.inventory) {
        merged.inventory.add(key.getSlot() + slotOffset, key);
      }
      for (Integer key : inventory.slotLimits.keySet()) {
        merged.slotLimits.put(key + slotOffset, inventory.slotLimits.get(key));
      }
      int finalSlotOffset = slotOffset;
      Arrays.stream(inventory.inSlots).map(in -> in + finalSlotOffset).forEach(inSlots::add);
      Arrays.stream(inventory.outSlots).map(out -> out + finalSlotOffset).forEach(outSlots::add);
      Arrays.stream(inventory.miscSlots).map(misc -> misc + finalSlotOffset).forEach(miscSlots::add);
      sides = sides.stream().map(side -> {
        if (inventory.accessibleSides.contains(side))
          return side;
        return null;
      }).filter(Objects::nonNull).toList();
      slotOffset += inventory.inventory.size();
      slotLimitIndex.put(slotOffset, inventory);
      inputs.addAll(inventory.inputs);
      outputs.addAll(inventory.outputs);
    }
    merged.accessibleSides = sides;
    merged.inSlots = inSlots.stream().mapToInt(i -> i).toArray();
    merged.outSlots = outSlots.stream().mapToInt(i -> i).toArray();
    merged.miscSlots = miscSlots.stream().mapToInt(i -> i).toArray();
    merged.inputs.addAll(inputs);
    merged.outputs.addAll(outputs);
    merged.setListener((slot, stack) ->
        slotLimitIndex.forEach((slotLimit, inventory) -> {
          if (slotLimit < slot)
            inventory.listener.onChange(slot - slotLimit, stack);
        })
    );
    return merged;
  }

  public List<Slot> createInventorySlots(List<Slot> slots, int i) {
    for (int j = 0; j < getSlots(); j++) {
      slots.add(createSlot(j, i));
    }
    return slots;
  }

  public List<Slot> createSlots(Player player) {
    List<Slot> slots = Lists.newArrayList();
    int i;
    for (i = 0; i < player.getInventory().getContainerSize(); i++) {
      slots.add(new Slot(player.getInventory(), i, 0, 0));
    }
    return createInventorySlots(slots, i);
  }

  private Slot createSlot(int i, int increment) {
    return new Slot(this, i + increment, 0, 0);
  }

  public void removeFromInputs(ItemStack stack, int amount) {
    AtomicInteger toRemove = new AtomicInteger(amount);
    Predicate<ItemSlot> slotPredicate = component -> true;
    this.inputs.stream().filter(component -> ItemStack.isSameItemSameComponents(component.getItemStack(), stack) && slotPredicate.test(component)).forEach(component -> {
      int maxExtract = Math.min(component.getItemStack().getCount(), toRemove.get());
      toRemove.addAndGet(-maxExtract);
      component.getItemStack().shrink(maxExtract);
    });
    setChanged();
  }

  public void addToOutputs(ItemStack stack, int amount) {
    AtomicInteger toAdd = new AtomicInteger(amount);
    this.outputs.stream().filter(component -> canPlaceOutput(component, stack)).forEach(component -> {
      int maxInsert = toAdd.get() - component.insertItemBypassLimit(stack, true).getCount();
      toAdd.addAndGet(-maxInsert);
      component.insertItemBypassLimit(stack.copyWithCount(maxInsert), false);
    });
    setChanged();
  }

  public boolean canPlaceOutput(@NotNull ItemSlot component, ItemStack stack) {
    //Check component filter and variant
    if (!component.isItemValid(0, stack))
      return false;

    //If the slot is empty, any item can go inside
    if (component.getItemStack().isEmpty())
      return true;

    //If the item present in the slot in not the same item, they won't stack
    if (!ItemStack.isSameItemSameComponents(component.getItemStack(), stack))
      return false;

    //Check if the stack present in the slot can accept more items
    return component.getItemStack().getCount() < Math.min(stack.getMaxStackSize(), component.getCapacity());
  }

  public int getSpaceForItem(ItemStack stack) {
    return this.outputs.stream().filter(component -> canPlaceOutput(component, stack))
        .mapToInt(component -> {
          if (component.getItemStack().isEmpty())
            return Math.min(component.getCapacity(), stack.getMaxStackSize());
          else
            return Math.min(component.getCapacity() - component.getItemStack().getCount(), stack.getMaxStackSize() - component.getItemStack().getCount());
        })
        .sum();
  }

  public int getItemAmount(ItemStack stack) {
    Predicate<ItemSlot> slotPredicate = component -> true;
    return this.inputs.stream().filter(component -> ItemStack.isSameItemSameComponents(component.getItemStack(), stack) && slotPredicate.test(component))
        .mapToInt(component -> component.getItemStack().getCount())
        .sum();
  }

  public interface IOInventoryChangedListener extends InventoryUpdateListener {
    void onChange(int slot, ItemStack stack);

    @Override
    default void onChange() {
    }
  }
}
