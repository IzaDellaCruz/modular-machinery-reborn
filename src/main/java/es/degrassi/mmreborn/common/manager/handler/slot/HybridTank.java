package es.degrassi.mmreborn.common.manager.handler.slot;

import com.mojang.datafixers.util.Pair;
import es.degrassi.mmreborn.api.handler.FilteredSlot;
import es.degrassi.mmreborn.api.network.ISyncable;
import es.degrassi.mmreborn.api.network.syncable.FluidStackSyncable;
import es.degrassi.mmreborn.common.manager.handler.FluidHandler;
import es.degrassi.mmreborn.common.util.InventoryUpdateListener;
import es.degrassi.mmreborn.common.util.Utils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Getter
@Setter
public class HybridTank extends AbstractSlot<FluidStack, Integer> implements FilteredSlot<FluidStack>, IFluidHandler {
  private InventoryUpdateListener listener;
  @Getter
  private final FluidHandler manager;

  public HybridTank(int slot, FluidHandler manager, int capacity, int maxInput, int maxOutput, Predicate<FluidStack> filter) {
    super(slot, FluidStack.EMPTY, capacity, maxInput, maxOutput, filter, v -> v > 0);
    this.manager = manager;
  }

  public HybridTank(FluidHandler manager, Predicate<FluidStack> filter, CompoundTag nbt, HolderLookup.Provider registries) {
    super(
        nbt.getInt("slot"),
        getFromNBT(nbt, registries),
        nbt.getInt("capacity"),
        nbt.getInt("maxInput"),
        nbt.getInt("maxOutput"),
        filter,
        v -> v > 0
    );
    this.manager = manager;
  }

  private static FluidStack getFromNBT(CompoundTag nbt, HolderLookup.Provider registries) {
    if (nbt.contains("fluid"))
      return FluidStack.parseOptional(registries, nbt.getCompound("fluid"));
    return FluidStack.EMPTY;
  }

  public HybridTank readFromNBT(HolderLookup.Provider registries, CompoundTag nbt) {
    if (nbt.contains("fluid")) {
      var ops = registries.createSerializationContext(NbtOps.INSTANCE);
      var fluid = nbt.getCompound("fluid");
      var id = fluid.get("id");
      var components = fluid.get("components");
      var count = fluid.getInt("count");
      AtomicReference<DataComponentPatch> comps = new AtomicReference<>(DataComponentPatch.EMPTY);
      DataComponentPatch.CODEC.decode(ops, components).result()
          .map(Pair::getFirst)
          .ifPresent(comps::set);
      FluidStack.FLUID_NON_EMPTY_CODEC.decode(ops, id).result()
          .map(Pair::getFirst)
          .ifPresent(itemId -> {
            this.value = new FluidStack(itemId, count, comps.get());
          });
    }
    return this;
  }

  public CompoundTag writeToNBT(HolderLookup.Provider registries, CompoundTag nbt) {
    var ops = registries.createSerializationContext(NbtOps.INSTANCE);
    if(!this.value.isEmpty()) {
      CompoundTag item = new CompoundTag();
      FluidStack.FLUID_NON_EMPTY_CODEC.encodeStart(ops, value.getFluidHolder()).result().ifPresent(id -> {
        DataComponentPatch.CODEC.encodeStart(ops, this.value.getComponentsPatch()).result().ifPresent(components -> {
          item.put("id", id);
          item.put("components", components);
          item.putInt("count", this.value.getAmount());
        });
      });
      nbt.put("fluid", item);
    }
    nbt.putInt("slot", this.slot);
    nbt.putInt("capacity", capacity);
    nbt.putInt("maxInput", maxInput);
    nbt.putInt("maxOutput", maxOutput);
    return nbt;
  }

  @Override
  public CompoundTag serializeNBT(HolderLookup.Provider pRegistries) {
    return writeToNBT(pRegistries, new CompoundTag());
  }

  @Override
  public void deserialize(HolderLookup.Provider pRegistries, CompoundTag componentNBT) {
    readFromNBT(pRegistries, componentNBT);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder("HybridTank{fluids:[");
    for (int i = 0; i < getTanks(); ++i, builder.append(", ")) {
      FluidStack fluid = getFluidInTank(i);
      builder.append(fluid.getAmount()).append("x ").append(fluid.getHoverName().getString());
    }
    builder.append("]}");
    return builder.toString();
  }

  protected void onContentsChanged() {
    if (listener != null)
      listener.onChange();
  }

  public void recipeExtract(long amount) {
    if (amount <= 0) return;
    amount = Utils.clamp(amount, 0, this.value.getAmount());
    drain((int)amount, FluidAction.EXECUTE);
  }

  public void recipeInsert(Fluid fluid, long amount, @Nullable CompoundTag nbt) {
    if (amount <= 0) return;
    fill(new FluidStack(fluid, this.value.getAmount() + (int) amount), FluidAction.EXECUTE);
  }

  public int getIngredientAmount(FluidIngredient ingredient) {
    return ingredient.test(this.getFluid()) ? getFluidAmount() : 0;
  }

  public int getSpaceForFluid(FluidStack stack) {
    return this.isFluidValid(0, stack) ? getSpace() : 0;
  }

  @Override
  public boolean isEmpty() {
    return value == null || value.isEmpty();
  }

  @Override
  public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {
    container.accept(FluidStackSyncable.create(this::getValue, this::setValue));
  }

  public void setChanged() {
    getManager().setChanged(slot, value);
  }

  public FluidStack insertFluidBypassLimit(FluidStack stack, boolean simulate) {
    this.bypassLimit = true;
    int remainder = this.fill(stack, fluidAction(simulate));
    this.bypassLimit = false;
    return stack.copyWithAmount(stack.getAmount() - remainder);
  }

  public FluidStack extractFluidBypassLimit(int amount, boolean simulate) {
    this.bypassLimit = true;
    FluidStack extracted = this.drain(amount, fluidAction(simulate));
    this.bypassLimit = false;
    return extracted;
  }

  private static FluidAction fluidAction(boolean simulate) {
    return simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE;
  }

  public boolean isFull() {
    return getFluidAmount() >= getCapacity();
  }

  public FluidStack getFluid() {
    return value;
  }

  public int getFluidAmount() {
    return value.getAmount();
  }

  public boolean isFluidValid(FluidStack fluidStack) {
    return filter.test(fluidStack);
  }

  @Override
  public int getTanks() {
    return 1;
  }

  @Override
  public FluidStack getFluidInTank(int i) {
    if (i > 0) return FluidStack.EMPTY;
    return value;
  }

  @Override
  public int getTankCapacity(int i) {
    if (i > 0) return 0;
    return capacity;
  }

  @Override
  public boolean isFluidValid(int i, FluidStack fluidStack) {
    if (i > 0) return false;
    return filter.test(fluidStack);
  }

  @Override
  public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
    if (!resource.isEmpty() && this.isFluidValid(resource)) {
      if (action.simulate()) {
        if (this.value.isEmpty()) {
          return Math.min(this.capacity, resource.getAmount());
        } else {
          return !FluidStack.isSameFluidSameComponents(this.value, resource) ? 0 : Math.min(this.capacity - this.value.getAmount(), resource.getAmount());
        }
      } else if (this.value.isEmpty()) {
        this.value = resource.copyWithAmount(Math.min(this.capacity, resource.getAmount()));
        this.onContentsChanged();
        return this.value.getAmount();
      } else if (!FluidStack.isSameFluidSameComponents(this.value, resource)) {
        return 0;
      } else {
        int filled = this.capacity - this.value.getAmount();
        if (resource.getAmount() < filled) {
          this.value.grow(resource.getAmount());
          filled = resource.getAmount();
        } else {
          this.value.setAmount(this.capacity);
        }

        if (filled > 0) {
          this.onContentsChanged();
        }

        return filled;
      }
    } else {
      return 0;
    }
  }

  @Override
  public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
    return !resource.isEmpty() && FluidStack.isSameFluidSameComponents(resource, this.value) ? this.drain(resource.getAmount(), action) : FluidStack.EMPTY;
  }

  @Override
  public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
    int drained = Math.min(this.value.getAmount(), maxDrain);

    FluidStack stack = this.value.copyWithAmount(drained);
    if (action.execute() && drained > 0) {
      this.value.shrink(drained);
      this.onContentsChanged();
    }

    return stack;
  }

  public int getSpace() {
    return Math.max(0, this.capacity - this.value.getAmount());
  }
}
