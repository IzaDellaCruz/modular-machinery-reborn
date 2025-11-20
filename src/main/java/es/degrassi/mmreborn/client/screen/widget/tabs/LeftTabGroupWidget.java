package es.degrassi.mmreborn.client.screen.widget.tabs;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import es.degrassi.mmreborn.client.screen.widget.ItemOrIconButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@ParametersAreNonnullByDefault
@SuppressWarnings("unused")
public class LeftTabGroupWidget extends VerticalTabGroupWidget {
  private final AtomicInteger lastY = new AtomicInteger();

  public LeftTabGroupWidget(int x, int y) {
    super(x, y);
    this.lastY.set(y);
  }

  public LeftTabGroupWidget addTab(LeftTabWidget tab) {
    return addTab(0, 0, tab);
  }

  public LeftTabGroupWidget addTab(int xOffset, int yOffset, LeftTabWidget tab) {
    tab.setY(lastY.getAndAdd(tab.getHeight() + yOffset));
    tab.setX(this.getX() + xOffset);
    tabs.add(tab);
    return this;
  }

  public LeftTabGroupWidget addTab(ItemOrIconButton icon, @Nullable LeftTabWidget.OnClick action) {
   return addTab(0, 0, icon, action);
  }

  public LeftTabGroupWidget addTab(int xOffset, int yOffset, ItemOrIconButton icon, @Nullable LeftTabWidget.OnClick action) {
    LeftTabWidget tab = new LeftTabWidget(getX() + xOffset, lastY.get() + yOffset, icon, action);
    lastY.getAndAdd(tab.getHeight() + yOffset);
    return addTab(tab);
  }

  public LeftTabGroupWidget addTab(ItemOrIconButton icon) {
    return addTab(0, 0, icon);
  }

  public LeftTabGroupWidget addTab(int xOffset, int yOffset, ItemOrIconButton icon) {
    LeftTabWidget tab = new LeftTabWidget(getX() + xOffset, lastY.get() + yOffset, icon);
    lastY.getAndAdd(tab.getHeight() + yOffset);
    return addTab(tab);
  }
}
