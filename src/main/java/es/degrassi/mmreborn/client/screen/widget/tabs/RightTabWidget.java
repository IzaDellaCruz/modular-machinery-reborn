package es.degrassi.mmreborn.client.screen.widget.tabs;

import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.client.screen.widget.ItemOrIconButton;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class RightTabWidget extends HorizontalTabWidget {
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
}
