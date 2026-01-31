package es.degrassi.mmreborn.client.screen.widget.tabs;

import es.degrassi.mmreborn.api.capability.config.IOSideMode;
import es.degrassi.mmreborn.api.capability.config.ISideConfigComponent;
import es.degrassi.mmreborn.common.entity.base.IAutoEntity;
import es.degrassi.mmreborn.common.entity.base.IAutoInputEntity;
import net.minecraft.network.chat.Component;

public class AutoInputTabWidget<T extends IAutoEntity<?> & IAutoInputEntity & ISideConfigComponent<IOSideMode>> extends AutoTabWidget<T> {
  public AutoInputTabWidget(T entity) {
    super(entity, Component.translatable("mmr.gui.tooltip.auto_input"));
  }
}
