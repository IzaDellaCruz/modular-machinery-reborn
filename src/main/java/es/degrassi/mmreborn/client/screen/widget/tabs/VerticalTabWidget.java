package es.degrassi.mmreborn.client.screen.widget.tabs;

import es.degrassi.mmreborn.client.screen.widget.ItemOrIconButton;
import net.minecraft.client.renderer.Rect2i;
import org.jetbrains.annotations.Nullable;

public class VerticalTabWidget extends TabWidget {
  public VerticalTabWidget(int x, int y, @Nullable ItemOrIconButton icon, @Nullable TabWidget.OnClick onClick) {
    super(x, y, icon, onClick);
  }

  public VerticalTabWidget(int x, int y, @Nullable ItemOrIconButton icon) {
    super(x, y, icon);
  }

  public Rect2i getTooltipArea() {
    return new Rect2i(this.getX(), this.getY(), this.getWidth(), this.getHeight() - 5);
  }
}
