package es.degrassi.mmreborn.common.machine.component;

import es.degrassi.mmreborn.api.controller.ControllerAccessible;
import es.degrassi.mmreborn.common.crafting.ComponentType;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.MachineComponent;
import es.degrassi.mmreborn.common.registration.ComponentRegistration;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class FunctionComponent extends MachineComponent<Void> implements ControllerAccessible {
  protected BlockPos controllerPos;

  public FunctionComponent(@Nullable BlockPos controllerPos) {
    super(IOType.NONE);
    this.controllerPos = controllerPos;
  }

  @Override
  public ComponentType<Void> getComponentType() {
    return ComponentRegistration.COMPONENT_FUNCTION.get();
  }

  @Override
  public @Nullable Void getContainerProvider() {
    return null;
  }

  @Override
  public <C extends MachineComponent<Void>> boolean canMerge(C c) {
    return false;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <C extends MachineComponent<Void>> C merge(C c) {
    return (C) this;
  }

  @Override
  public @Nullable Level getLevel() {
    return getController() == null ? null : getController().getLevel();
  }
}
