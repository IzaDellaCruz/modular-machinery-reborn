package es.degrassi.mmreborn.common.machine.component;

import es.degrassi.experiencelib.api.capability.IExperienceHandler;
import es.degrassi.mmreborn.common.crafting.ComponentType;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.MachineComponent;
import es.degrassi.mmreborn.common.registration.ComponentRegistration;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

public class ExperienceComponent extends MachineComponent<IExperienceHandler> {
  private final IExperienceHandler handler;

  public ExperienceComponent(IExperienceHandler handler, IOType ioType) {
    super(ioType);
    this.handler = handler;
  }

  @Override
  public ComponentType getComponentType() {
    return ComponentRegistration.COMPONENT_EXPERIENCE.get();
  }

  @Override
  public @NotNull IExperienceHandler getContainerProvider() {
    return handler;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <C extends MachineComponent<?>> C merge(C c) {
    ExperienceComponent comp = (ExperienceComponent) c;
    return (C) new ExperienceComponent(
        new IExperienceHandler() {
          @Override
          public CompoundTag serializeNBT(HolderLookup.Provider provider) {
            CompoundTag nbt = new CompoundTag();
            nbt.put("tank1", handler.serializeNBT(provider));
            nbt.put("tank2", comp.handler.serializeNBT(provider));
            return nbt;
          }

          @Override
          public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
            handler.deserializeNBT(provider, nbt.getCompound("tank1"));
            comp.handler.deserializeNBT(provider, nbt.getCompound("tank2"));
          }

          @Override
          public int getTanks() {
            return comp.handler.getTanks() + handler.getTanks();
          }

          @Override
          public boolean canAcceptExperience(int tank, long l) {
            return true;
          }

          @Override
          public boolean canProvideExperience(int tank, long l) {
            return true;
          }

          @Override
          public long getMaxExtract(int tank) {
            return Math.max(handler.getMaxExtract(0), comp.handler.getMaxExtract(0));
          }

          @Override
          public long getMaxReceive(int tank) {
            return Math.max(handler.getMaxReceive(0), comp.handler.getMaxReceive(0));
          }

          @Override
          public long getExperience() {
            return handler.getExperience() + comp.handler.getExperience();
          }

          @Override
          public long getExperienceCapacity() {
            return handler.getExperienceCapacity() + comp.handler.getExperienceCapacity();
          }

          @Override
          public void setExperience(int tank, long l) {
            // can't set data on the merged components
          }

          @Override
          public void setCapacity(int tank, long l) {
            // can't set data on the merged components
          }

          @Override
          public long receiveExperience(int tank, long l, boolean b) {
            long received1 = handler.receiveExperience(0, l, b);
            l -= received1;
            long received2 = comp.handler.receiveExperience(0, l, b);
            return received1 + received2;
          }

          @Override
          public long extractExperience(int tank, long l, boolean b) {
            long extracted1 = handler.extractExperience(0, l, b);
            l -= extracted1;
            long extracted2 = comp.handler.extractExperience(0, l, b);
            return extracted1 + extracted2;
          }

          @Override
          public long extractExperienceRecipe(int tank, long l, boolean b) {
            long extracted1 = handler.extractExperienceRecipe(0, l, b);
            l -= extracted1;
            long extracted2 = comp.handler.extractExperienceRecipe(0, l, b);
            return extracted1 + extracted2;
          }

          @Override
          public long receiveExperienceRecipe(int tank, long l, boolean b) {
            long received1 = handler.receiveExperienceRecipe(0, l, b);
            l -= received1;
            long received2 = comp.handler.receiveExperienceRecipe(0, l, b);
            return received1 + received2;
          }
        },
        getIOType()
    );
  }
}
