package es.degrassi.mmreborn.common.integration.kubejs.function;

import dev.latvian.mods.kubejs.level.CachedLevelBlock;
import dev.latvian.mods.rhino.Wrapper;
import es.degrassi.mmreborn.common.entity.MachineControllerEntity;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.MachineComponent;
import es.degrassi.mmreborn.common.machine.component.EnergyComponent;
import es.degrassi.mmreborn.common.machine.component.FluidComponent;
import es.degrassi.mmreborn.common.machine.component.ItemComponent;
import es.degrassi.mmreborn.common.registration.ComponentRegistration;
import es.degrassi.mmreborn.common.util.Chunkloader;
import es.degrassi.mmreborn.common.util.IEnergyHandler;
import es.degrassi.mmreborn.common.util.IOInventory;
import es.degrassi.mmreborn.common.util.ItemSlot;
import es.degrassi.mmreborn.common.util.TaskDelayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

public class MachineControllerJS {
  private final MachineControllerEntity internal;

  protected MachineControllerJS(MachineControllerEntity internal) {
    this.internal = internal;
  }

  public static MachineControllerJS of(Object o) {
    if (o instanceof Wrapper w) {
      o = w.unwrap();
    }
    if (o instanceof BlockEntity be && be instanceof MachineControllerEntity mce) {
      return new MachineControllerJS(mce);
    }
    if (o instanceof CachedLevelBlock bc) {
      return of(bc.getEntity());
    }
    return null;
  }

  public String getId() {
    return this.internal.getId().toString();
  }

  public void setId(String id) {
    ResourceLocation loc = ResourceLocation.tryParse(id);
    if (loc != null) {
      TaskDelayer.enqueue(0, () -> this.internal.setMachine(loc));
    } else {
      throw new IllegalArgumentException("Invalid machine ID: " + id);
    }
  }

  public boolean getPaused() {
    return this.internal.isPaused();
  }

  public void setPaused(boolean paused) {
    this.internal.setPaused(paused);
  }

  /** ENERGY STUFF **/

  public long getEnergyCapacity(IOType mode) {
    return this.internal.getComponentManager()
        .getFoundComponentsList()
        .stream()
        .filter(c -> c instanceof EnergyComponent)
        .map(c -> (EnergyComponent) c)
        .filter(c -> c.getIOType().equals(mode))
        .mapToLong(c -> c.getContainerProvider().getMaxEnergyStored())
        .sum();
  }

  public long getEnergyStored(IOType mode) {
    return this.internal.getComponentManager()
        .getFoundComponentsList()
        .stream()
        .filter(c -> c instanceof EnergyComponent)
        .map(c -> (EnergyComponent) c)
        .filter(c -> c.getIOType().equals(mode))
        .mapToLong(c -> c.getContainerProvider().getCurrentEnergy())
        .sum();
  }

  /**
   * This method is only available for energy output hatches
   * @param energy to be added
   * @return energy added
   */
  public long addEnergy(long energy) {
    AtomicLong energyAtomic = new AtomicLong(energy);
    AtomicLong inserted = new AtomicLong(0);
    this.internal.getComponentManager()
        .getFoundComponentsList()
        .stream()
        .filter(c -> c instanceof EnergyComponent)
        .map(c -> (EnergyComponent) c)
        .filter(c -> !c.getIOType().isInput())
        .forEach(c -> {
          if (energyAtomic.get() <= 0) return;
          IEnergyHandler handler = c.getContainerProvider();
          boolean prevInsert = handler.canReceive();
          handler.setCanInsert(true);
          int received = handler.receiveEnergy((int) energyAtomic.get(), false);
          energyAtomic.addAndGet(-received);
          inserted.addAndGet(received);
          handler.setCanInsert(prevInsert);
        });
    return inserted.get();
  }

  /**
   * This method is only available for energy input hatches
   * @param energy to be removed
   * @return energy extracted
   */
  public long removeEnergy(long energy) {
    AtomicLong energyAtomic = new AtomicLong(energy);
    AtomicLong extracted = new AtomicLong(0);
    this.internal.getComponentManager()
        .getFoundComponentsList()
        .stream()
        .filter(c -> c instanceof EnergyComponent)
        .map(c -> (EnergyComponent) c)
        .filter(c -> c.getIOType().isInput())
        .forEach(c -> {
          if (energyAtomic.get() <= 0) return;
          IEnergyHandler handler = c.getContainerProvider();
          boolean prevExtract = handler.canExtract();
          handler.setCanExtract(true);
          int drained = handler.extractEnergy((int) energyAtomic.get(), false);
          energyAtomic.addAndGet(-drained);
          extracted.addAndGet(drained);
          handler.setCanExtract(prevExtract);
        });
    return extracted.get();
  }

  /** FLUID STUFF **/

  public List<FluidStack> getFluidsStored(IOType mode) {
    return this.internal.getComponentManager()
        .getFoundComponentsList()
        .stream()
        .filter(c -> c instanceof FluidComponent)
        .map(c -> (FluidComponent) c)
        .filter(c -> c.getIOType().equals(mode))
        .map(c -> c.getContainerProvider().getFluid())
        .toList();
  }

  public int getFluidCapacity(IOType mode) {
    return this.internal.getComponentManager()
        .getFoundComponentsList()
        .stream()
        .filter(c -> c instanceof FluidComponent)
        .map(c -> (FluidComponent) c)
        .filter(c -> c.getIOType().equals(mode))
        .mapToInt(c -> c.getContainerProvider().getCapacity())
        .sum();
  }

  public int getFluidCapacity(FluidStack fluid, IOType mode) {
    return this.internal.getComponentManager()
        .getFoundComponentsList()
        .stream()
        .filter(c -> c instanceof FluidComponent)
        .map(c -> (FluidComponent) c)
        .filter(c -> c.getIOType().equals(mode))
        .filter(c -> FluidStack.isSameFluidSameComponents(c.getContainerProvider().getFluid(), fluid))
        .mapToInt(c -> c.getContainerProvider().getCapacity())
        .sum();
  }


  /**
   * This method is only available for fluid input hatches
   * @param stack to be added
   * @return fluid added
   */
  public int addFluid(FluidStack stack) {
    AtomicReference<FluidStack> fluid = new AtomicReference<>(stack);
    AtomicInteger filled = new AtomicInteger(0);
    this.internal.getComponentManager()
        .getFoundComponentsList()
        .stream()
        .filter(c -> c instanceof FluidComponent)
        .map(c -> (FluidComponent) c)
        .filter(c -> !c.getIOType().isInput())
        .filter(c -> c.getContainerProvider().isEmpty() || FluidStack.isSameFluidSameComponents(c.getContainerProvider().getFluid(), stack))
        .forEach(c -> {
          if (fluid.get().isEmpty()) return;
          if (fluid.get().getAmount() <= 0) return;
          int inserted = c.getContainerProvider().fill(fluid.get(), IFluidHandler.FluidAction.EXECUTE);
          fluid.get().shrink(inserted);
          filled.getAndAdd(inserted);
        });

    return filled.get();
  }

  /**
   * This method is only available for fluid output hatches
   * @param stack to be removed
   * @return fluid extracted
   */
  public FluidStack removeFluid(FluidStack stack) {
    AtomicReference<FluidStack> fluid = new AtomicReference<>(stack);
    AtomicReference<FluidStack> extracted = new AtomicReference<>(FluidStack.EMPTY);

    this.internal.getComponentManager()
        .getFoundComponentsList()
        .stream()
        .filter(c -> c instanceof FluidComponent)
        .map(c -> (FluidComponent) c)
        .filter(c -> c.getIOType().isInput())
        .filter(c -> FluidStack.isSameFluidSameComponents(c.getContainerProvider().getFluid(), stack))
        .forEach(c -> {
          if (fluid.get().isEmpty()) return;
          if (c.getContainerProvider().isEmpty()) return;
          FluidStack drained = c.getContainerProvider().drain(fluid.get(), IFluidHandler.FluidAction.EXECUTE);
          fluid.get().shrink(drained.getAmount());
          if (extracted.get().isEmpty())
            extracted.set(drained);
          else {
            extracted.get().grow(drained.getAmount());
          }
        });

    return extracted.get();
  }

  /** ITEM STUFF **/

  public List<ItemStack> getItemsStored(IOType mode) {
    return this.internal.getComponentManager()
        .getFoundComponentsList()
        .stream()
        .filter(c -> c instanceof ItemComponent)
        .map(c -> (ItemComponent) c)
        .filter(c -> c.getIOType().equals(mode))
        .map(ItemComponent::getContainerProvider)
        .map(IOInventory::getInventory)
        .map(c -> c.stream().map(ItemSlot::getItemStack).toList())
        .flatMap(List::stream)
        .toList();
  }

  /**
   * This method is only available for item output buses
   * @param stack to be added
   * @return items that couldn't be added
   */
  public ItemStack addItem(ItemStack stack) {
    AtomicReference<ItemStack> item = new AtomicReference<>(stack);
    AtomicReference<ItemStack> inserted = new AtomicReference<>(ItemStack.EMPTY);
    this.internal.getComponentManager()
        .getFoundComponentsList()
        .stream()
        .filter(c -> c instanceof ItemComponent)
        .map(c -> (ItemComponent) c)
        .filter(c -> !c.getIOType().isInput())
        .forEach(c -> {
          if (item.get().isEmpty()) return;
          if (c.getSpaceForItem(item.get()) <= 0) return;
          AtomicInteger toAdd = new AtomicInteger(stack.getCount());
          c.getContainerProvider().getOutputs().stream().filter(component -> c.getContainerProvider().canPlaceOutput(component,
              stack)).forEach(component -> {
            int maxInsert = toAdd.get() - component.insertItemBypassLimit(stack, true).getCount();
            toAdd.addAndGet(-maxInsert);
            ItemStack remaining = component.insertItemBypassLimit(stack.copyWithCount(maxInsert), false);
            item.set(remaining);
          });
        });

    return inserted.get();
  }

  /**
   * This method is only available for item input buses
   * @param stack to be removed
   * @return extracted
   */
  public ItemStack removeItem(ItemStack stack) {
    AtomicReference<ItemStack> item = new AtomicReference<>(stack);
    AtomicReference<ItemStack> extracted = new AtomicReference<>(ItemStack.EMPTY);
    this.internal.getComponentManager()
        .getFoundComponentsList()
        .stream()
        .filter(c -> c instanceof ItemComponent)
        .map(c -> (ItemComponent) c)
        .filter(c -> c.getIOType().isInput())
        .forEach(c -> {
          if (item.get().isEmpty()) return;
          if (c.getItemAmount(item.get()) <= 0) return;
          AtomicInteger toRemove = new AtomicInteger(item.get().getCount());
          AtomicInteger removed = new AtomicInteger(0);
          Predicate<ItemSlot> slotPredicate = component -> true;
          c.getContainerProvider().getInputs().stream().filter(component -> ItemStack.isSameItemSameComponents(component.getItemStack(),
              stack) && slotPredicate.test(component)).forEach(component -> {
            int maxExtract = Math.min(component.getItemStack().getCount(), toRemove.get());
            toRemove.addAndGet(-maxExtract);
            component.getItemStack().shrink(maxExtract);
            removed.getAndAdd(maxExtract);
            if (extracted.get().isEmpty()) {
              extracted.set(item.get().copyWithCount(maxExtract));
            } else {
              extracted.get().grow(maxExtract);
            }
          });
          item.get().shrink(removed.get());
        });

    return extracted.get();
  }

  /** FUEL STUFF **/
  public long getFuelAmount() throws ExecutionException {
    return internal.getComponentManager()
        .getComponent(ComponentRegistration.COMPONENT_FUEL.get(), IOType.INPUT)
        .stream()
        .mapToLong(comp -> comp.getContainerProvider().getFuel())
        .sum();
  }

  public long getFuelCapacity() throws ExecutionException {
    return internal.getComponentManager()
        .getComponent(ComponentRegistration.COMPONENT_FUEL.get(), IOType.INPUT)
        .stream()
        .mapToLong(comp -> comp.getContainerProvider().getMaxFuel())
        .sum();
  }

  public void addFuel(long amount) throws ExecutionException {
    AtomicLong amt = new AtomicLong(amount);
    internal.getComponentManager()
        .getComponent(ComponentRegistration.COMPONENT_FUEL.get(), IOType.INPUT)
        .map(MachineComponent::getContainerProvider)
        .ifPresent(comp -> {
          if (amt.get() <= 0) return;
          long toInsert = Math.min(comp.getMaxFuel() - comp.getFuel(), amt.get());
          amt.addAndGet(-toInsert);
          comp.addFuel(toInsert);
        });
  }

  public void removeFuel(long amount) throws ExecutionException {
    AtomicLong amt = new AtomicLong(amount);
    internal.getComponentManager()
        .getComponent(ComponentRegistration.COMPONENT_FUEL.get(), IOType.INPUT)
        .map(MachineComponent::getContainerProvider)
        .ifPresent(comp -> {
          if (amt.get() <= 0) return;
          long toRemove = Math.min(comp.getFuel(), amt.get());
          amt.addAndGet(-toRemove);
          comp.removeFuel(toRemove);
        });
  }

  /** CHUNKLOAD STUFF **/

  public void enableChunkload(int radius) throws ExecutionException {
    this.internal.getComponentManager()
        .getComponent(ComponentRegistration.COMPONENT_CHUNKLOAD.get(), IOType.OUTPUT)
        .map(MachineComponent::getContainerProvider)
        .ifPresent(component -> component.setActive((ServerLevel) this.internal.getLevel(), radius));
  }

  public void disableChunkload() throws ExecutionException {
    this.internal.getComponentManager()
        .getComponent(ComponentRegistration.COMPONENT_CHUNKLOAD.get(), IOType.OUTPUT)
        .map(MachineComponent::getContainerProvider)
        .ifPresent(component -> component.setInactive((ServerLevel) this.internal.getLevel()));
  }

  public boolean isChunkloadEnabled() throws ExecutionException {
    return this.internal.getComponentManager()
        .getComponent(ComponentRegistration.COMPONENT_CHUNKLOAD.get(), IOType.OUTPUT)
        .map(MachineComponent::getContainerProvider)
        .map(Chunkloader::isActive)
        .orElse(false);
  }

  public int getChunkloadRadius() throws ExecutionException {
    return this.internal.getComponentManager()
        .getComponent(ComponentRegistration.COMPONENT_CHUNKLOAD.get(), IOType.OUTPUT)
        .map(MachineComponent::getContainerProvider)
        .map(Chunkloader::getRadius)
        .orElse(0);
  }
}
