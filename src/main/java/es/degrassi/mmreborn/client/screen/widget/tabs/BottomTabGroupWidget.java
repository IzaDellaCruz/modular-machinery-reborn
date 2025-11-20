package es.degrassi.mmreborn.client.screen.widget.tabs;

import es.degrassi.mmreborn.client.screen.widget.ItemOrIconButton;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.atomic.AtomicInteger;

@ParametersAreNonnullByDefault
@SuppressWarnings("unused")
public class BottomTabGroupWidget extends HorizontalTabGroupWidget {
  private final AtomicInteger lastX = new AtomicInteger();

  public BottomTabGroupWidget(int x, int y) {
    super(x, y);
    this.lastX.set(x);
  }

  public BottomTabGroupWidget addTab(BottomTabWidget tab) {
    return addTab(0, 0, tab);
  }

  public BottomTabGroupWidget addTab(int xOffset, int yOffset, BottomTabWidget tab) {
    tab.setX(lastX.getAndAdd(tab.getWidth() + xOffset));
    tab.setY(this.getY() + yOffset);
    tabs.add(tab);
    return this;
  }

  public BottomTabGroupWidget addTab(ItemOrIconButton icon, @Nullable BottomTabWidget.OnClick action) {
   return addTab(0, 0, icon, action);
  }

  public BottomTabGroupWidget addTab(int xOffset, int yOffset, ItemOrIconButton icon, @Nullable BottomTabWidget.OnClick action) {
    BottomTabWidget tab = new BottomTabWidget(lastX.get() + xOffset, getY() + yOffset, icon, action);
    lastX.getAndAdd(tab.getWidth() + xOffset);
    return addTab(tab);
  }

  public BottomTabGroupWidget addTab(ItemOrIconButton icon) {
    return addTab(0, 0, icon);
  }

  public BottomTabGroupWidget addTab(int xOffset, int yOffset, ItemOrIconButton icon) {
    BottomTabWidget tab = new BottomTabWidget(lastX.get() + xOffset, getY() + yOffset, icon);
    lastX.getAndAdd(tab.getWidth() + xOffset);
    return addTab(tab);
  }
}
