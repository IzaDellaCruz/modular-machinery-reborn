package es.degrassi.mmreborn.client.screen;

import es.degrassi.mmreborn.client.container.ItemBusContainer;
import es.degrassi.mmreborn.common.entity.base.TileItemBus;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ItemBusScreen extends BaseScreen<ItemBusContainer, TileItemBus> {

  public ItemBusScreen(ItemBusContainer pMenu, Inventory pPlayerInventory, Component pTitle) {
    super(pMenu, pPlayerInventory, pTitle, false);
  }

  @Override
  protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
    clearWidgets();
    renderBgWithSlotSize(guiGraphics, getMenu().getEntity().getSize().cols, getMenu().getEntity().getSlots());
    renderSlots(guiGraphics);
  }
}
