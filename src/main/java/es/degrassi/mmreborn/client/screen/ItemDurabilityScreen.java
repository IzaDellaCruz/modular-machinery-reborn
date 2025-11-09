package es.degrassi.mmreborn.client.screen;

import es.degrassi.mmreborn.client.container.ItemDurabilityContainer;
import es.degrassi.mmreborn.common.entity.DurabilityHatchEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ItemDurabilityScreen extends BaseScreen<ItemDurabilityContainer, DurabilityHatchEntity> {

  public ItemDurabilityScreen(ItemDurabilityContainer pMenu, Inventory pPlayerInventory, Component pTitle) {
    super(pMenu, pPlayerInventory, pTitle, false);
  }

  @Override
  protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
    clearWidgets();
    renderBgWithSlotSize(guiGraphics, getMenu().getEntity().getSize().cols, getMenu().getEntity().getSlots());
    renderSlots(guiGraphics);
  }
}
