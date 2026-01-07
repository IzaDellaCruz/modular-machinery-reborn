package es.degrassi.mmreborn.common.manager.handler;

import com.google.common.collect.Lists;
import es.degrassi.mmreborn.api.handler.FilteredSlot;
import es.degrassi.mmreborn.api.network.ISyncable;
import es.degrassi.mmreborn.api.network.ISyncableStuff;
import es.degrassi.mmreborn.common.util.InventoryUpdateListener;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Unmodifiable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class AbstractHandler<SLOT extends FilteredSlot<VALUE>, VALUE> implements ISyncableStuff {
  protected int[] inSlots = new int[0], outSlots = new int[0];
  @Getter
  private final List<SLOT> inputs = new ArrayList<>();
  @Getter
  private final List<SLOT> outputs = new ArrayList<>();
  @Getter
  private final List<SLOT> inventory = Lists.newArrayList();

  @Getter
  @Setter
  private HandlerUpdateListener<VALUE> listener;
  public List<Direction> accessibleSides = new ArrayList<>();

  @Getter
  @Setter
  private Level level;

  @Getter
  private final Predicate<VALUE> defaultFilter;

  @Getter
  protected int slotLimit;

  protected AbstractHandler(int slotLimit) {
    this.slotLimit = slotLimit;
    this.accessibleSides = Arrays.asList(Direction.values());
    this.defaultFilter = stack -> true;
    this.inventory.addAll(generateInventory());
  }

  protected AbstractHandler(int[] inSlots, int[] outSlots, int slotLimit) {
    this.slotLimit = slotLimit;
    this.accessibleSides = Arrays.asList(Direction.values());
    this.defaultFilter = stack -> true;
    this.inSlots = inSlots;
    this.outSlots = outSlots;
    this.inventory.addAll(generateInventory());
  }

  protected AbstractHandler(int[] inSlots, int[] outSlots, int slotLimit, Direction... accessibleFrom) {
    this.slotLimit = slotLimit;
    this.defaultFilter = stack -> true;
    this.inSlots = inSlots;
    this.outSlots = outSlots;
    this.inventory.addAll(generateInventory());
    this.accessibleSides = Arrays.asList(accessibleFrom);
  }

  protected AbstractHandler(int[] inSlots, int[] outSlots, Predicate<VALUE> filter, int slotLimit, Direction... accessibleFrom) {
    this.defaultFilter = filter;
    this.slotLimit = slotLimit;
    this.inSlots = inSlots;
    this.outSlots = outSlots;
    this.inventory.addAll(generateInventory(filter));
    this.accessibleSides = Arrays.asList(accessibleFrom);
  }

  @Override
  public final void getStuffToSync(Consumer<ISyncable<?, ?>> container) {
    this.inventory.forEach(component -> component.getStuffToSync(container));
  }

  public abstract void removeFromInputs(VALUE value, int amount);
  public abstract void addToOutputs(VALUE value, int amount);

  public final int getSlots() {
    return inventory.size();
  }

  public final @Unmodifiable List<VALUE> getAllInputStacks() {
    return this.inputs.stream().map(SLOT::getValue).toList();
  }

  public final @Unmodifiable List<VALUE> getAllOutputStacks() {
    return this.outputs.stream().map(SLOT::getValue).toList();
  }

  public final @Unmodifiable List<VALUE> getAllStacks() {
    return this.inventory.stream().map(SLOT::getValue).toList();
  }

  public boolean isEmpty() {
    return getInventory().stream().allMatch(SLOT::isEmpty);
  }

  protected final List<SLOT> generateInventory() {
    return generateInventory(value -> true);
  }

  protected abstract List<SLOT> generateInventory(Predicate<VALUE> filter);

  public final void setChanged() {
    if (listener != null) {
      listener.onChange();
    }
  }

  public final void setChanged(int slot, VALUE value) {
    if (listener != null) {
      listener.onChange(slot, value);
    }
  }

  public void setFilter(int slot, Predicate<VALUE> filter) {
    this.inventory.stream()
        .filter(s -> s.getSlot() == slot)
        .forEach(s -> s.setFilter(filter));
  }

  public CompoundTag writeNBT(HolderLookup.Provider pRegistries) {
    CompoundTag tag = new CompoundTag();
    tag.putIntArray("inSlots", this.inSlots);
    tag.putIntArray("outSlots", this.outSlots);
    tag.putInt("slotLimit", slotLimit);

    ListTag components = new ListTag();
    this.getInventory().forEach((value) -> components.add(value.serializeNBT(pRegistries)));
    tag.put("slots", components);
    return tag;
  }

  public void readNBT(CompoundTag tag, HolderLookup.Provider pRegistries) {
    this.inSlots = tag.getIntArray("inSlots");
    this.outSlots = tag.getIntArray("outSlots");
    this.slotLimit = tag.getInt("slotLimit");

    if (tag.contains("slots")) {
      ListTag components = tag.getList("slots", Tag.TAG_COMPOUND);
      components.stream()
          .filter(t -> t instanceof CompoundTag)
          .map(t -> (CompoundTag) t)
          .forEach(componentNBT -> {
            if (componentNBT.contains("slot")) {
              this.getInventory().stream()
                  .filter(inv -> inv.getSlot() == componentNBT.getInt("slot"))
                  .findFirst()
                  .ifPresentOrElse(
                      inv -> inv.deserialize(pRegistries, componentNBT),
                      () -> this.getInventory().add(this.createSlot(pRegistries, componentNBT))
                  );
            }
          });
      this.getInputs().clear();
      this.getOutputs().clear();
      this.getInputs().addAll(this.getInventory().stream().filter(SLOT::isInput).toList());
      this.getOutputs().addAll(this.getInventory().stream().filter(SLOT::isOutput).toList());
      this.setChanged();
    }
  }

  protected abstract SLOT createSlot(HolderLookup.Provider pRegistries, CompoundTag componentNBT);

  public interface HandlerUpdateListener<VALUE> extends InventoryUpdateListener {
    void onChange(int slot, VALUE value);

    @Override
    default void onChange() {
    }
  }
}
