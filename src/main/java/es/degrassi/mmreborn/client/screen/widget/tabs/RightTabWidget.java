package es.degrassi.mmreborn.client.screen.widget.tabs;

import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.client.screen.widget.ItemOrIconButton;
import es.degrassi.mmreborn.common.util.TextureSizeHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class RightTabWidget extends VerticalTabWidget {
  public static final ResourceLocation TAB = ModularMachineryReborn.rl("textures/gui/widget/base_tab_right.png");
  private static final ResourceLocation TAB_HOVERED = ModularMachineryReborn.rl("textures/gui/widget/base_tab_hovered_right.png");

  public RightTabWidget(int x, int y, @Nullable ItemOrIconButton icon) {
    super(x, y, icon);
  }

  public RightTabWidget(int x, int y, @Nullable ItemOrIconButton icon, @Nullable TabWidget.OnClick onClick) {
    super(x, y, icon, onClick);
  }

  @Override
  public ResourceLocation getTab() {
    return TAB;
  }

  @Override
  public ResourceLocation getTabHovered() {
    return TAB_HOVERED;
  }

  @Override
  public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    ResourceLocation tab = isHoveredOrFocused() ? getTabHovered() : getTab();
    int x = getX() + (isHoveredOrFocused() ? -2 : 0);
    int y = getY();
    int width = TextureSizeHelper.getWidth(tab), height = TextureSizeHelper.getHeight(tab);
    this.width = width;
    this.height = height;
    guiGraphics.blit(tab, x, y, 0, 0, width, height, width, height);
    if (getIconButton() != null) {
      getIconButton().setDisableBackground(true);
      getIconButton().setPosition(5 + getX(), 5 + y);
      getIconButton().renderTooltip(false);
      getIconButton().renderWidget(guiGraphics, mouseX, mouseY, partialTick);
    }
  }
}
