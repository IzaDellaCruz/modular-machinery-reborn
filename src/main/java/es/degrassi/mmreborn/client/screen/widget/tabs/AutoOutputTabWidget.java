package es.degrassi.mmreborn.client.screen.widget.tabs;

import es.degrassi.mmreborn.api.capability.config.IOSideMode;
import es.degrassi.mmreborn.api.capability.config.ISideConfigComponent;
import es.degrassi.mmreborn.common.entity.base.IAutoEntity;
import es.degrassi.mmreborn.common.entity.base.IAutoOutputEntity;
import net.minecraft.network.chat.Component;

public class AutoOutputTabWidget<T extends IAutoEntity<?> & IAutoOutputEntity & ISideConfigComponent<IOSideMode>> extends AutoTabWidget<T> {
  public AutoOutputTabWidget(T entity) {
    super(entity, Component.translatable("mmr.gui.tooltip.auto_output"));
  }
}
