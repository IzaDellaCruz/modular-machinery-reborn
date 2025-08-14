package es.degrassi.mmreborn.client.screen.widget.tabs;

import dev.emi.emi.api.EmiApi;
import es.degrassi.mmreborn.client.screen.widget.ItemOrIconButton;
import es.degrassi.mmreborn.common.integration.emi.MMREmiPlugin;
import es.degrassi.mmreborn.common.machine.DynamicMachine;
import es.degrassi.mmreborn.common.util.Mods;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class ShowRecipesTabWidget extends TabWidget {
  public ShowRecipesTabWidget(@Nullable ItemOrIconButton icon, DynamicMachine machine) {
    super(0, 0, icon, (mouseX, mouseY, button) -> {
      if (Mods.isEMILoaded()) {
        if (MMREmiPlugin.categories.containsKey(machine))
          EmiApi.displayRecipeCategory(MMREmiPlugin.categories.get(machine));
      }
    });
  }

  @Override
  public void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
    super.renderTooltip(guiGraphics, x, y);
    Component component = Mods.isEMILoaded() ? Component.translatable("emi.tooltip.show.recipes") : null;
    if (component != null)
      guiGraphics.renderTooltip(
          Minecraft.getInstance().font,
          List.of(component.getVisualOrderText()),
          x,
          y
      );
  }
}
