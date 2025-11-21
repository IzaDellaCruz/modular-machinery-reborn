package es.degrassi.mmreborn.common.machine.component;

import es.degrassi.mmreborn.common.crafting.ComponentType;
import es.degrassi.mmreborn.common.entity.RedstonePortEntity;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.MachineComponent;
import es.degrassi.mmreborn.common.manager.crafting.MachineStatus;
import es.degrassi.mmreborn.common.registration.ComponentRegistration;
import es.degrassi.mmreborn.common.util.RedstoneHelper;
import net.minecraft.network.chat.Component;

public class RedstoneComponent extends MachineComponent<Integer> {
  private final RedstonePortEntity entity;
  public RedstoneComponent(RedstonePortEntity entity) {
    super(IOType.NONE);
    this.entity = entity;
  }

  @Override
  public IOType getIOType() {
    return getMode();
  }

  public IOType getMode() {
    return entity.getMode();
  }

  @Override
  public ComponentType<Integer> getComponentType() {
    return ComponentRegistration.COMPONENT_REDSTONE.get();
  }

  @Override
  public Integer getContainerProvider() {
    return RedstoneHelper.getReceivingRedstone(entity);
  }

  public void setOutputAmount(int outputAmount) {
    this.entity.setOutputAmount(outputAmount);
    this.entity.setChanged();
  }

  @Override
  public <C extends MachineComponent<Integer>> boolean canMerge(C c) {
    return false;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <C extends MachineComponent<Integer>> C merge(C c) {
    return (C) this;
  }

  @Override
  public void onStatusChanged(MachineStatus oldStatus, MachineStatus newStatus, Component errorMessage) {
    if (newStatus != MachineStatus.RUNNING && oldStatus == MachineStatus.RUNNING) {
      setOutputAmount(0);
    }
  }
}
