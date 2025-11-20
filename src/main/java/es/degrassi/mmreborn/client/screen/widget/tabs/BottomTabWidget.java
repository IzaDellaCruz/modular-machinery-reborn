package es.degrassi.mmreborn.client.screen.widget.tabs;

import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.client.screen.widget.ItemOrIconButton;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class BottomTabWidget extends HorizontalTabWidget {
  public static final ResourceLocation TAB = ModularMachineryReborn.rl("textures/gui/widget/base_tab_bottom.png");
  private static final ResourceLocation TAB_HOVERED = ModularMachineryReborn.rl("textures/gui/widget/base_tab_hovered_bottom.png");

  public BottomTabWidget(int x, int y, @Nullable ItemOrIconButton icon) {
    super(x, y, icon);
  }

  public BottomTabWidget(int x, int y, @Nullable ItemOrIconButton icon, @Nullable TabWidget.OnClick onClick) {
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
