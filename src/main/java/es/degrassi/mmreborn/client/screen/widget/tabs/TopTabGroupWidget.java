package es.degrassi.mmreborn.client.screen.widget.tabs;

import es.degrassi.mmreborn.client.screen.widget.ItemOrIconButton;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.atomic.AtomicInteger;

@ParametersAreNonnullByDefault
@SuppressWarnings("unused")
public class TopTabGroupWidget extends HorizontalTabGroupWidget {
  private final AtomicInteger lastX = new AtomicInteger();

  public TopTabGroupWidget(int x, int y) {
    super(x, y);
    this.lastX.set(x);
  }

  public TopTabGroupWidget addTab(TopTabWidget tab) {
    return addTab(0, 0, tab);
  }

  public TopTabGroupWidget addTab(int xOffset, int yOffset, TopTabWidget tab) {
    tab.setX(lastX.getAndAdd(tab.getWidth() + xOffset));
    tab.setY(this.getY() + yOffset);
    tabs.add(tab);
    return this;
  }

  public TopTabGroupWidget addTab(ItemOrIconButton icon, @Nullable TopTabWidget.OnClick action) {
   return addTab(0, 0, icon, action);
  }

  public TopTabGroupWidget addTab(int xOffset, int yOffset, ItemOrIconButton icon, @Nullable TopTabWidget.OnClick action) {
    TopTabWidget tab = new TopTabWidget(lastX.get() + xOffset, getY() + yOffset, icon, action);
    lastX.getAndAdd(tab.getWidth() + xOffset);
    return addTab(tab);
  }

  public TopTabGroupWidget addTab(ItemOrIconButton icon) {
    return addTab(0, 0, icon);
  }

  public TopTabGroupWidget addTab(int xOffset, int yOffset, ItemOrIconButton icon) {
    TopTabWidget tab = new TopTabWidget(lastX.get() + xOffset, getY() + yOffset, icon);
    lastX.getAndAdd(tab.getWidth() + xOffset);
    return addTab(tab);
  }
}
