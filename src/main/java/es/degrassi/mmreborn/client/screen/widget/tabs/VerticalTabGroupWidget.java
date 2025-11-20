package es.degrassi.mmreborn.client.screen.widget.tabs;

import es.degrassi.mmreborn.client.screen.widget.ItemOrIconButton;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@SuppressWarnings("unused")
public class VerticalTabGroupWidget extends TabGroupWidget {

  public VerticalTabGroupWidget(int x, int y) {
    super(x, y, 0, 0, Component.empty());
  }

  @Override
  public int getWidth() {
    return tabs.stream().mapToInt(TabWidget::getWidth).max().orElse(1);
  }

  @Override
  public int getHeight() {
    return tabs.stream().mapToInt(TabWidget::getHeight).sum();
  }

  public VerticalTabGroupWidget addTab(TabWidget tab) {
    return addTab(0, 0, tab);
  }

  public VerticalTabGroupWidget addTab(int xOffset, int yOffset, TabWidget tab) {
    tab.setY(lastY.getAndAdd(tab.getHeight() + yOffset));
    tab.setX(this.getX() + xOffset);
    tabs.add(tab);
    return this;
  }

  public VerticalTabGroupWidget addTab(ItemOrIconButton icon, @Nullable TabWidget.OnClick action) {
   return addTab(0, 0, icon, action);
  }

  public VerticalTabGroupWidget addTab(int xOffset, int yOffset, ItemOrIconButton icon, @Nullable TabWidget.OnClick action) {
    TabWidget tab = new TabWidget(getX() + xOffset, lastY.get() + yOffset, icon, action);
    lastY.getAndAdd(tab.getHeight() + yOffset);
    return addTab(tab);
  }

  public VerticalTabGroupWidget addTab(ItemOrIconButton icon) {
    return addTab(0, 0, icon);
  }

  public VerticalTabGroupWidget addTab(int xOffset, int yOffset, ItemOrIconButton icon) {
    TabWidget tab = new TabWidget(getX() + xOffset, lastY.get() + yOffset, icon);
    lastY.getAndAdd(tab.getHeight() + yOffset);
    return addTab(tab);
  }
}
