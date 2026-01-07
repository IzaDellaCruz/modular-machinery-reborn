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
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Getter
@Setter
public class HybridTank extends FluidTank implements FilteredSlot<FluidStack> {

  private InventoryUpdateListener listener;
  @Getter
  private final int slot;
  private final int maxInput;
  private final int maxOutput;
  @Getter
  private final FluidHandler manager;
  private boolean bypassLimit = false;

  public HybridTank(int slot, FluidHandler manager, int capacity, int maxInput, int maxOutput, Predicate<FluidStack> filter) {
    super(capacity);
    this.slot = slot;;
    this.maxInput = maxInput;
    this.maxOutput = maxOutput;
    this.manager = manager;
    this.validator = filter;
  }

  public HybridTank(FluidHandler manager, Predicate<FluidStack> filter, CompoundTag nbt, HolderLookup.Provider registries) {
    super(0);
    this.manager = manager;
    this.validator = filter;
    if (nbt.contains("fluid"))
      fluid = FluidStack.parseOptional(registries, nbt.getCompound("fluid"));
    this.slot = nbt.getInt("slot");
    this.capacity = nbt.getInt("capacity");
    this.maxInput = nbt.getInt("maxInput");
    this.maxOutput = nbt.getInt("maxOutput");
  }

  public FluidTank readFromNBT(HolderLookup.Provider registries, CompoundTag nbt) {
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
            this.fluid = new FluidStack(itemId, count, comps.get());
          });
    }
    return this;
  }

  public CompoundTag writeToNBT(HolderLookup.Provider registries, CompoundTag nbt) {
    var ops = registries.createSerializationContext(NbtOps.INSTANCE);
    if(!this.fluid.isEmpty()) {
      CompoundTag item = new CompoundTag();
      FluidStack.FLUID_NON_EMPTY_CODEC.encodeStart(ops,
          fluid.getFluidHolder()).result().ifPresent(id -> {
        DataComponentPatch.CODEC.encodeStart(ops, this.fluid.getComponentsPatch()).result().ifPresent(components -> {
          item.put("id", id);
          item.put("components", components);
          item.putInt("count", this.fluid.getAmount());
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

  public boolean isInput() {
    return maxInput > 0;
  }

  public boolean isOutput() {
    return maxOutput > 0;
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

  @Override
  protected void onContentsChanged() {
    super.onContentsChanged();
    if (listener != null)
      listener.onChange();
  }

  public void recipeExtract(long amount) {
    if (amount <= 0) return;
    amount = Utils.clamp(amount, 0, this.fluid.getAmount());
    drain((int)amount, FluidAction.EXECUTE);
  }

  public void recipeInsert(Fluid fluid, long amount, @Nullable CompoundTag nbt) {
    if (amount <= 0) return;
    fill(new FluidStack(fluid, this.fluid.getAmount() + (int) amount), FluidAction.EXECUTE);
  }

  public int getIngredientAmount(FluidIngredient ingredient) {
    return ingredient.test(this.getFluid()) ? getFluidAmount() : 0;
  }

  public int getSpaceForFluid(FluidStack stack) {
    return this.isFluidValid(0, stack) ? getSpace() : 0;
  }

  @Override
  public void setFilter(Predicate<FluidStack> filter) {
    this.validator = filter;
  }

  @Override
  public FluidStack getValue() {
    return getFluidInTank(0);
  }

  @Override
  public void getStuffToSync(Consumer<ISyncable<?, ?>> container) {
    container.accept(FluidStackSyncable.create(this::getValue, this::setFluid));
  }

  public void setChanged() {
    getManager().setChanged(slot, fluid);
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
}
