package es.degrassi.mmreborn.client.screen;

import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.client.container.FuelTankContainer;
import es.degrassi.mmreborn.client.screen.widget.FuelWidget;
import es.degrassi.mmreborn.client.screen.widget.tabs.AutoInputTabWidget;
import es.degrassi.mmreborn.client.screen.widget.tabs.AutoOutputTabWidget;
import es.degrassi.mmreborn.client.screen.widget.tabs.ITabGroupScreen;
import es.degrassi.mmreborn.client.screen.widget.tabs.TabGroupWidget;
import es.degrassi.mmreborn.common.entity.FuelTankEntity;
import es.degrassi.mmreborn.common.util.TextureSizeHelper;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.Nullable;

public class FuelTankScreen extends BaseScreen<FuelTankContainer, FuelTankEntity> implements ITabGroupScreen {
  private FuelWidget fuelWidget;
  @Getter
  private TabGroupWidget tabs;
  public FuelTankScreen(FuelTankContainer menu, Inventory playerInventory, Component title) {
    super(menu, playerInventory, title, true);
  }

  @Override
  @Nullable
  public ResourceLocation getTexture() {
    return ModularMachineryReborn.rl("textures/gui/guiexperience.png");
  }

  @Override
  protected void init() {
    super.init();
    this.fuelWidget = new FuelWidget(16 + getGuiLeft(), 10 + 61/2 - 8 + getGuiTop(), getMenu().getEntity().getFuelHandler());

    tabs = TabGroupWidget.createRight(getGuiLeft() + getXSize(), getGuiTop());
    tabs.addTab(new AutoInputTabWidget<>(this.entity));

    addRenderableWidget(tabs);
  }

  @Override
  protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
    // render image background:
    super.renderBg(guiGraphics, partialTick, mouseX, mouseY);
    renderSlots(guiGraphics);
    guiGraphics.pose().pushPose();
    fuelWidget.render(guiGraphics, mouseX, mouseY, partialTick);
    guiGraphics.pose().popPose();
  }

  @Override
  protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
    super.renderTooltip(guiGraphics, x, y);
    if (fuelWidget.isMouseOver(x, y)) {
      fuelWidget.renderTooltip(guiGraphics, x, y);
    }
  }
}
