package es.degrassi.mmreborn.client.screen.widget.tabs;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import es.degrassi.mmreborn.client.screen.widget.ItemOrIconButton;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class TabGroupWidget extends AbstractWidget {
  @Getter
  protected final List<TabWidget> tabs = Lists.newArrayList();
  protected final AtomicInteger lastX = new AtomicInteger();
  protected final AtomicInteger lastY = new AtomicInteger();

  public TabGroupWidget(int x, int y, int width, int height, Component message) {
    super(x, y, width, height, message);
    this.lastX.set(x);
    this.lastY.set(y);
  }

  @Override
  public abstract int getWidth();

  @Override
  public abstract int getHeight();

  public TabGroupWidget addTab(TabWidget tab) {
    return addTab(0, 0, tab);
  }

  public abstract TabGroupWidget addTab(int xOffset, int yOffset, TabWidget tab);

  public TabGroupWidget addTab(ItemOrIconButton icon, @Nullable TabWidget.OnClick action) {
    return addTab(0, 0, icon, action);
  }

  public abstract TabGroupWidget addTab(int xOffset, int yOffset, ItemOrIconButton icon, @Nullable TabWidget.OnClick action);

  public TabGroupWidget addTab(ItemOrIconButton icon) {
    return addTab(0, 0, icon);
  }

  public abstract TabGroupWidget addTab(int xOffset, int yOffset, ItemOrIconButton icon);

  @Override
  protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    tabs.forEach(tab -> tab.render(guiGraphics, mouseX, mouseY, partialTick));
  }

  @Override
  protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    tabs.forEach(tab -> tab.updateWidgetNarration(narrationElementOutput));
  }

  @Override
  public void onClick(double mouseX, double mouseY, int button) {
    getTabUnderMouse(mouseX, mouseY).ifPresent(tab -> tab.onClick(mouseX, mouseY, button));
  }

  private Optional<TabWidget> getTabUnderMouse(double mouseX, double mouseY) {
    return tabs.stream().filter(tab -> tab.isMouseOver(mouseX, mouseY)).filter(AbstractWidget::isActive).findFirst();
  }

  public void setInitialFocus(int lastFocus) {
    if (lastFocus < 0) lastFocus = 0;
    if (lastFocus >= tabs.size()) lastFocus = tabs.size() - 1;
    tabs.forEach(tab -> tab.setFocused(false));
    tabs.get(lastFocus).setFocused(true);
  }

  public void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
    tabs.stream().filter(tab -> tab.isMouseOver(x, y)).forEach(tab -> tab.renderTooltip(guiGraphics, x, y));
  }

  public void gatherComponents(List<Either<FormattedText, TooltipComponent>> components) {
    tabs.forEach(tab -> tab.gatherComponents(components));
  }

  public static LeftTabGroupWidget createLeft(int x, int y) {
    return new LeftTabGroupWidget(x, y);
  }

  public static RightTabGroupWidget createRight(int x, int y) {
    return new RightTabGroupWidget(x, y);
  }

  public static TopTabGroupWidget createTop(int x, int y) {
    return new TopTabGroupWidget(x, y);
  }

  public static BottomTabGroupWidget createBottom(int x, int y) {
    return new BottomTabGroupWidget(x, y);
  }
}
