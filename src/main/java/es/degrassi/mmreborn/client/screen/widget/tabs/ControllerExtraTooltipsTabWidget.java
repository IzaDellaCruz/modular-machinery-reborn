package es.degrassi.mmreborn.client.screen.widget.tabs;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.api.client.Icon;
import es.degrassi.mmreborn.api.client.machine.TooltipUse;
import es.degrassi.mmreborn.client.screen.widget.ItemOrIconButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ControllerExtraTooltipsTabWidget extends TopTabWidget{
  private final ResourceLocation machineId;

  public ControllerExtraTooltipsTabWidget(ResourceLocation id) {
    super(0, 0, new ItemOrIconButton(5, 5, Icon.HELP, b -> {}));
    this.machineId = id;
  }

  @Override
  public void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
    super.renderTooltip(guiGraphics, x, y);
    List<Either<FormattedText, TooltipComponent>> components = Lists.newArrayList();
    gatherComponents(components);
    guiGraphics.renderComponentTooltipFromElements(
        Minecraft.getInstance().font,
        components,
        x,
        y,
        ItemStack.EMPTY
    );
  }

  @Override
  public void gatherComponents(List<Either<FormattedText, TooltipComponent>> components) {
    super.gatherComponents(components);
    var enumTooltips = ModularMachineryReborn.MACHINE_EXTRA_TOOLTIPS.get(machineId);
    if (enumTooltips == null || enumTooltips.isEmpty()) return;
    var tooltips = enumTooltips.get(TooltipUse.GUI);
    if (tooltips == null || tooltips.isEmpty()) return;
    tooltips.forEach(tooltip -> components.add(Either.left(tooltip)));
  }
}
