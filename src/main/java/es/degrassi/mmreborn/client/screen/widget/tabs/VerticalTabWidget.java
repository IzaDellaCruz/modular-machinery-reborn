package es.degrassi.mmreborn.client.screen.widget.tabs;

import es.degrassi.mmreborn.client.screen.widget.ItemOrIconButton;
import org.jetbrains.annotations.Nullable;

public class VerticalTabWidget extends TabWidget {
  public VerticalTabWidget(int x, int y, @Nullable ItemOrIconButton icon, @Nullable TabWidget.OnClick onClick) {
    super(x, y, icon, onClick);
  }

  public VerticalTabWidget(int x, int y, @Nullable ItemOrIconButton icon) {
    super(x, y, icon);
  }
}
