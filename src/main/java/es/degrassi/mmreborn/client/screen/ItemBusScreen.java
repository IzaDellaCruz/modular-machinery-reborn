package es.degrassi.mmreborn.client.screen;

import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.client.container.ItemBusContainer;
import es.degrassi.mmreborn.common.entity.base.TileItemBus;
import es.degrassi.mmreborn.common.util.TextureSizeHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemBusScreen extends BaseScreen<ItemBusContainer, TileItemBus> {

  public ItemBusScreen(ItemBusContainer pMenu, Inventory pPlayerInventory, Component pTitle) {
    super(pMenu, pPlayerInventory, pTitle);
  }

  @Override
  public @Nullable ResourceLocation getTexture() {
    return ModularMachineryReborn.rl("background");
  }

  @Override
  protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
    // Do not render the default texts
  }

  @Override
  protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
    clearWidgets();
    if (getTexture() != null) {
      guiGraphics.pose().pushPose();
      guiGraphics.setColor(1f, 1f, 1f, 1f);
      this.leftPos = (this.width - this.imageWidth) / 2;
      this.topPos = (this.height - this.imageHeight) / 2;
      int slotsWidth = getMenu().getEntity().getSize().cols * 18 + 16;
      int invWidth = 18 * 9 + 16;
      int height =
          (int) Math.ceil(getMenu().getEntity().getSlots() * 1D / getMenu().getEntity().getSize().cols) * 18 + 16
          + 18 * 4 + 3 + font.wordWrapHeight(title, Math.max(slotsWidth, invWidth) - 16) + titleLabelY;

      guiGraphics.blitSprite(getTexture(), leftPos, topPos, Math.max(slotsWidth, invWidth), height);
      guiGraphics.pose().popPose();
    }
    for (Slot slot : getMenu().slots) {
      guiGraphics.blit(BASE_SLOT, slot.x + getGuiLeft() - 1, slot.y + getGuiTop() - 1, 0, 0,
          TextureSizeHelper.getWidth(BASE_SLOT),
          TextureSizeHelper.getHeight(BASE_SLOT),
          TextureSizeHelper.getWidth(BASE_SLOT),
          TextureSizeHelper.getHeight(BASE_SLOT));
    }
  }
}
