package es.degrassi.mmreborn.client.screen.widget.tabs;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.api.client.Icon;
import es.degrassi.mmreborn.api.client.machine.TooltipUse;
import es.degrassi.mmreborn.client.container.ControllerContainer;
import es.degrassi.mmreborn.client.screen.widget.ItemOrIconButton;
import es.degrassi.mmreborn.common.integration.kubejs.CKubeJSIntegration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public class ControllerExtraTooltipsTabWidget extends TopTabWidget{
  private final ResourceLocation machineId;
  private final ControllerContainer container;
  private final List<Either<ResourceLocation, Component>> tooltipList;

  public ControllerExtraTooltipsTabWidget(ResourceLocation id, ControllerContainer container) {
    super(0, 0, new ItemOrIconButton(5, 5, Icon.HELP, b -> {}));
    this.machineId = id;
    this.container = container;
    var enumTooltips = ModularMachineryReborn.MACHINE_EXTRA_TOOLTIPS.get(machineId);
    tooltipList = Optional.ofNullable(enumTooltips).map(map -> map.get(TooltipUse.GUI)).orElse(Lists.newArrayList());
  }

  @Override
  public void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
    super.renderTooltip(guiGraphics, x, y);
    List<Either<FormattedText, TooltipComponent>> components = Lists.newArrayList();
    if (container.needFullSync(20))
      CKubeJSIntegration.collectDynamicTooltip(container.containerId);
    gatherComponents(components);
    container.dynamicTooltips.forEach((id, comp) -> components.add(Either.left(comp)));
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
  }
}
