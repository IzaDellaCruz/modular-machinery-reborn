package es.degrassi.mmreborn.client.screen.widget.tabs;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import es.degrassi.mmreborn.client.screen.widget.ItemOrIconButton;
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
public class TabGroupWidget extends AbstractWidget {
  private final List<TabWidget> tabs = Lists.newArrayList();
  private final AtomicInteger lastX = new AtomicInteger();

  public TabGroupWidget(int x, int y) {
    super(x, y, 0, 0, Component.empty());
    this.lastX.set(x);
  }

  @Override
  public int getWidth() {
    return tabs.stream().mapToInt(TabWidget::getWidth).sum();
  }

  @Override
  public int getHeight() {
    return tabs.stream().mapToInt(TabWidget::getHeight).max().orElse(1);
  }

  public TabGroupWidget addTab(TabWidget tab) {
    return addTab(0, 0, tab);
  }

  public TabGroupWidget addTab(int xOffset, int yOffset, TabWidget tab) {
    tab.setX(lastX.getAndAdd(tab.getWidth() + xOffset));
    tab.setY(this.getY() + yOffset);
    tabs.add(tab);
    return this;
  }

  public TabGroupWidget addTab(ItemOrIconButton icon, @Nullable TabWidget.OnClick action) {
   return addTab(0, 0, icon, action);
  }

  public TabGroupWidget addTab(int xOffset, int yOffset, ItemOrIconButton icon, @Nullable TabWidget.OnClick action) {
    TabWidget tab = new TabWidget(lastX.get() + xOffset, getY() + yOffset, icon, action);
    lastX.getAndAdd(tab.getWidth() + xOffset);
    return addTab(tab);
  }

  public TabGroupWidget addTab(ItemOrIconButton icon) {
    return addTab(0, 0, icon);
  }

  public TabGroupWidget addTab(int xOffset, int yOffset, ItemOrIconButton icon) {
    TabWidget tab = new TabWidget(lastX.get() + xOffset, getY() + yOffset, icon);
    lastX.getAndAdd(tab.getWidth() + xOffset);
    return addTab(tab);
  }

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
}
