package es.degrassi.mmreborn.client.screen;

import es.degrassi.mmreborn.client.container.ItemDurabilityContainer;
import es.degrassi.mmreborn.client.screen.widget.tabs.AutoInputTabWidget;
import es.degrassi.mmreborn.client.screen.widget.tabs.AutoOutputTabWidget;
import es.degrassi.mmreborn.client.screen.widget.tabs.ITabGroupScreen;
import es.degrassi.mmreborn.client.screen.widget.tabs.TabGroupWidget;
import es.degrassi.mmreborn.common.entity.DurabilityHatchEntity;
import es.degrassi.mmreborn.common.util.TextureSizeHelper;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

@Getter
public class ItemDurabilityScreen extends BaseScreen<ItemDurabilityContainer, DurabilityHatchEntity> implements ITabGroupScreen {
  private TabGroupWidget tabs;
  public ItemDurabilityScreen(ItemDurabilityContainer pMenu, Inventory pPlayerInventory, Component pTitle) {
    super(pMenu, pPlayerInventory, pTitle, false);
  }

  @Override
  protected void init() {
    super.init();

    tabs = TabGroupWidget.createLeft(getGuiLeft() - TextureSizeHelper.getWidth(AutoOutputTabWidget.TAB), getGuiTop());
    tabs.addTab(new AutoInputTabWidget<>(this.entity));

    addRenderableWidget(tabs);
  }

  @Override
  protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
    renderBgWithSlotSize(guiGraphics, getMenu().getEntity().getSize().cols, getMenu().getEntity().getSlots());
    renderSlots(guiGraphics);
  }
}
