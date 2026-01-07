package es.degrassi.mmreborn.common.machine.component;

import es.degrassi.mmreborn.common.crafting.ComponentType;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.MachineComponent;
import es.degrassi.mmreborn.common.manager.handler.FluidHandler;
import es.degrassi.mmreborn.common.registration.ComponentRegistration;
import es.degrassi.mmreborn.common.manager.handler.slot.HybridTank;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class FluidComponent extends MachineComponent<FluidHandler> {
  private final FluidHandler handler;

  public FluidComponent(FluidHandler handler, IOType ioType) {
    super(ioType);
    this.handler = handler;
  }

  @Override
  public ComponentType<FluidHandler> getComponentType() {
    return ComponentRegistration.COMPONENT_FLUID.get();
  }

  @Override
  public FluidHandler getContainerProvider() {
    return handler;
  }

  @Override
  public <C extends MachineComponent<FluidHandler>> boolean canMerge(C c) {
    return getIOType().equals(c.getIOType());
  }

  @Override
  @SuppressWarnings("unchecked")
  public <C extends MachineComponent<FluidHandler>> C merge(C c) {
    FluidComponent comp = (FluidComponent) c;
    return (C) new FluidComponent(
        FluidHandler.mergeBuild(handler, comp.handler),
        getIOType()
    );
  }

  @Override
  public int compareTo(MachineComponent<FluidHandler> o) {
    FluidHandler one = getContainerProvider();
    FluidHandler two = o.getContainerProvider();
    if (one.isEmpty() && two.isEmpty()) return 0;
    if (one.isEmpty() && !two.isEmpty()) return -1;
    if (!one.isEmpty() && !two.isEmpty()) return 0;
    return 1;
  }

  public void removeFromInputs(FluidIngredient ingredient, int amount) {
    AtomicInteger toRemove = new AtomicInteger(amount);
    Arrays.stream(ingredient.getStacks())
        .map(fluid -> new FluidStack(fluid.getFluid(), amount))
        .forEach(fluid -> {
          if (toRemove.get() <= 0) return;
          int maxExtract = Math.min(handler.getFluidAmount(fluid), toRemove.get());
          toRemove.addAndGet(-maxExtract);
          handler.removeFromInputs(fluid, maxExtract);
        });
  }

  public void addToOutputs(FluidStack stack) {
    handler.addToOutputs(stack, stack.getAmount());
  }

  @Override
  public CompoundTag asTag(HolderLookup.Provider provider) {
    CompoundTag tag = super.asTag(provider);
    tag.put("handler", handler.writeNBT(provider));
    return tag;
  }
}
