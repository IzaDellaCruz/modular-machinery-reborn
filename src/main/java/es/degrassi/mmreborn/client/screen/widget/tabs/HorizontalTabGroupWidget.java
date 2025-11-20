package es.degrassi.mmreborn.client.screen.widget.tabs;

import es.degrassi.mmreborn.client.screen.widget.ItemOrIconButton;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@SuppressWarnings("unused")
public class HorizontalTabGroupWidget extends TabGroupWidget {
  public HorizontalTabGroupWidget(int x, int y) {
    super(x, y, 0, 0, Component.empty());
  }

  @Override
  public int getWidth() {
    return tabs.stream().mapToInt(TabWidget::getWidth).sum();
  }

  @Override
  public int getHeight() {
    return tabs.stream().mapToInt(TabWidget::getHeight).max().orElse(1);
  }

  public HorizontalTabGroupWidget addTab(TabWidget tab) {
    return addTab(0, 0, tab);
  }

  public HorizontalTabGroupWidget addTab(int xOffset, int yOffset, TabWidget tab) {
    tab.setX(lastX.getAndAdd(tab.getWidth() + xOffset));
    tab.setY(this.getY() + yOffset);
    tabs.add(tab);
    return this;
  }

  public HorizontalTabGroupWidget addTab(ItemOrIconButton icon, @Nullable TabWidget.OnClick action) {
   return addTab(0, 0, icon, action);
  }

  public HorizontalTabGroupWidget addTab(int xOffset, int yOffset, ItemOrIconButton icon, @Nullable TabWidget.OnClick action) {
    TabWidget tab = new TabWidget(lastX.get() + xOffset, getY() + yOffset, icon, action);
    lastX.getAndAdd(tab.getWidth() + xOffset);
    return addTab(tab);
  }

  public HorizontalTabGroupWidget addTab(ItemOrIconButton icon) {
    return addTab(0, 0, icon);
  }

  public HorizontalTabGroupWidget addTab(int xOffset, int yOffset, ItemOrIconButton icon) {
    TabWidget tab = new TabWidget(lastX.get() + xOffset, getY() + yOffset, icon);
    lastX.getAndAdd(tab.getWidth() + xOffset);
    return addTab(tab);
  }
}
