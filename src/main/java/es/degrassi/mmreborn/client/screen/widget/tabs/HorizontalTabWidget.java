package es.degrassi.mmreborn.client.screen.widget.tabs;

import es.degrassi.mmreborn.client.screen.widget.ItemOrIconButton;
import org.jetbrains.annotations.Nullable;

public class HorizontalTabWidget extends TabWidget {
  public HorizontalTabWidget(int x, int y, @Nullable ItemOrIconButton icon, @Nullable TabWidget.OnClick onClick) {
    super(x, y, icon, onClick);
  }

  public HorizontalTabWidget(int x, int y, @Nullable ItemOrIconButton icon) {
    super(x, y, icon);
  }
}
