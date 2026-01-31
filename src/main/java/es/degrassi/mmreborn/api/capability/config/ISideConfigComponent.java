package es.degrassi.mmreborn.api.capability.config;

import es.degrassi.mmreborn.api.controller.ControllerAccessible;
import net.neoforged.neoforge.common.extensions.IBlockEntityExtension;

public interface ISideConfigComponent<M extends SideConfig.SideMode> extends ControllerAccessible, IBlockEntityExtension {
  SideConfig<M> getConfig();

  default void configChanged(RelativeSide side, IOSideMode oldMode, IOSideMode newMode) {
    if (oldMode.isDisabled() != newMode.isDisabled()) {
      invalidateCapabilities();
    }
  }
}
