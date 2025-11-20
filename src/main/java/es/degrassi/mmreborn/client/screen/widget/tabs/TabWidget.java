package es.degrassi.mmreborn.client.screen.widget.tabs;

import com.mojang.datafixers.util.Either;
import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.client.screen.widget.ItemOrIconButton;
import es.degrassi.mmreborn.common.util.TextureSizeHelper;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class TabWidget extends AbstractWidget {
  private static final ResourceLocation TAB = ModularMachineryReborn.rl("textures/gui/widget/base_tab_top.png");
  private static final ResourceLocation TAB_HOVERED = ModularMachineryReborn.rl("textures/gui/widget/base_tab_hovered_top.png");

  @Getter
  private final ItemOrIconButton iconButton;
  @Nullable
  private final OnClick onClick;

  public TabWidget(int x, int y, @Nullable ItemOrIconButton icon, @Nullable OnClick onClick) {
    super(x, y, 0, 0, Component.empty());
    this.iconButton = icon;
    this.onClick = onClick;
  }

  public TabWidget(int x, int y, @Nullable ItemOrIconButton icon) {
    this(x, y, icon, null);
  }

  public ResourceLocation getTab() {
    return TAB;
  }

  public ResourceLocation getTabHovered() {
    return TAB_HOVERED;
  }

  @Override
  public int getWidth() {
    return TextureSizeHelper.getWidth(getTab());
  }

  @Override
  public int getHeight() {
    return TextureSizeHelper.getHeight(getTab());
  }

  @Override
  public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    ResourceLocation tab = isHoveredOrFocused() ? getTabHovered() : getTab();
    int x = getX();
    int y = getY();
    int width = TextureSizeHelper.getWidth(tab), height = TextureSizeHelper.getHeight(tab);
    this.width = width;
    this.height = height;
    guiGraphics.blit(tab, x, y, 0, 0, width, height, width, height);
    if (getIconButton() != null) {
      getIconButton().setDisableBackground(true);
      getIconButton().setPosition(5 + x, 5 + y);
      getIconButton().renderTooltip(false);
      getIconButton().renderWidget(guiGraphics, mouseX, mouseY, partialTick);
    }
  }

  @Override
  protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    this.defaultButtonNarrationText(narrationElementOutput);
  }

  @Override
  protected boolean clicked(double mouseX, double mouseY) {
    return isMouseOver(mouseX, mouseY);
  }

  public void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
    if (getIconButton() != null) {
      getIconButton().renderTooltip(guiGraphics, x, y);
    }
  }

  @Override
  public void onClick(double mouseX, double mouseY, int button) {
    if (onClick != null) {
      onClick.onClick(mouseX, mouseY, button);
    }

    if (getIconButton() != null) {
      getIconButton().onClick(mouseX, mouseY, button);
    }
  }

  public void gatherComponents(List<Either<FormattedText, TooltipComponent>> components) {
    // Used on subclasses but by default it should not collect anything
  }

  public Rect2i getTooltipArea() {
    return new Rect2i(this.getX(), this.getY(), this.getWidth() - 5, this.getHeight());
  }

  @Override
  public boolean isMouseOver(double mouseX, double mouseY) {
    return getTooltipArea().contains((int) mouseX, (int) mouseY);
  }

  public interface OnClick {
    void onClick(double mouseX, double mouseY, int button);
  }
}
