package es.degrassi.mmreborn.common.machine.component;

import es.degrassi.mmreborn.common.crafting.ComponentType;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.MachineComponent;
import es.degrassi.mmreborn.common.registration.ComponentRegistration;
import es.degrassi.mmreborn.common.util.HybridTank;
import net.neoforged.neoforge.fluids.FluidStack;

public class FluidComponent extends MachineComponent<HybridTank> {
  private final HybridTank handler;

  public FluidComponent(HybridTank handler, IOType ioType) {
    super(ioType);
    this.handler = handler;
  }

  @Override
  public ComponentType getComponentType() {
    return ComponentRegistration.COMPONENT_FLUID.get();
  }

  @Override
  public HybridTank getContainerProvider() {
    return handler;
  }

  @Override
  public <C extends MachineComponent<?>> boolean canMerge(C c) {
    FluidComponent comp = (FluidComponent) c;
    if (getIOType().isInput())
      return handler.getFluid().is(comp.handler.getFluid().getFluid());
    else
      return handler.isEmpty() || comp.handler.isEmpty() || handler.getFluid().is(comp.handler.getFluid().getFluid());
  }

  @Override
  @SuppressWarnings("unchecked")
  public <C extends MachineComponent<?>> C merge(C c) {
    FluidComponent comp = (FluidComponent) c;
    return (C) new FluidComponent(
        new HybridTank(handler.getCapacity() + comp.handler.getCapacity()) {
          @Override
          public FluidStack getFluid() {
            FluidStack one = handler.getFluid(), second = comp.getContainerProvider().getFluid();
            if (!one.isEmpty()) return one;
            if (!second.isEmpty()) return second;
            return FluidStack.EMPTY;
          }

          @Override
          public int getFluidAmount() {
            int one = handler.getFluidAmount(), second = comp.handler.getFluidAmount();
            return one + second;
          }

          @Override
          public boolean isFluidValid(FluidStack stack) {
            return handler.isFluidValid(stack) || comp.handler.isFluidValid(stack);
          }

          @Override
          public void setFluid(FluidStack stack) {
            // can't set data on the merged components
          }

          @Override
          public boolean isEmpty() {
            return handler.isEmpty() && comp.handler.isEmpty();
          }

          @Override
          public int getSpace() {
            return handler.getSpace() + comp.handler.getSpace();
          }

          @Override
          public int fill(FluidStack resource, FluidAction action) {
            int filled1 = handler.fill(resource, action);
            resource = resource.copyWithAmount(resource.getAmount() - filled1);
            int filled2 = comp.handler.fill(resource, action);
            return filled1 + filled2;
          }

          @Override
          public FluidStack drain(FluidStack resource, FluidAction action) {
            FluidStack drained1 = handler.drain(resource, action);
            resource = resource.copyWithAmount(resource.getAmount() - drained1.getAmount());
            FluidStack drained2 = comp.handler.drain(resource, action);
            return drained2.isEmpty() ? drained1 : resource.copyWithAmount(drained1.getAmount() + drained2.getAmount());
          }

          @Override
          public FluidStack drain(int maxDrain, FluidAction action) {
            FluidStack drained1 = handler.drain(maxDrain, action);
            maxDrain -= drained1.getAmount();
            FluidStack drained2 = handler.drain(maxDrain, action);
            return drained1.copyWithAmount(drained1.getAmount() + drained2.getAmount());
          }
        },
        getIOType()
    );
  }

  @Override
  public int compareTo(MachineComponent<HybridTank> o) {
    HybridTank one = getContainerProvider();
    HybridTank two = o.getContainerProvider();
    if (one.isEmpty() && two.isEmpty()) return 0;
    if (one.isEmpty() && !two.isEmpty()) return -1;
    if (!one.isEmpty() && !two.isEmpty()) return 0;
    return 1;
  }
}
