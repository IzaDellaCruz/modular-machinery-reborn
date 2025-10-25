package es.degrassi.mmreborn.client.screen.widget;

import es.degrassi.mmreborn.client.screen.BaseScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.Slot;

public class SlotWidget extends GuiElement {
  private final Slot slot;
  private final BaseScreen<?, ?> parent;

  public SlotWidget(Slot slot, BaseScreen<?, ?> parent) {
    super(parent, slot.x, slot.y, BaseScreen.SLOT_SIZE, BaseScreen.SLOT_SIZE, slot.getItem().getHoverName());
    this.slot = slot;
    this.parent = parent;
  }

  public SlotWidget(Slot slot, int x, int y, BaseScreen<?, ?> parent) {
    super(parent, x, y, BaseScreen.SLOT_SIZE, BaseScreen.SLOT_SIZE, slot.getItem().getHoverName());
    this.slot = slot;
    this.parent = parent;
  }

  @Override
  public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    int slotX = slot.x - 1;
    int slotY = slot.y - 1;
    guiGraphics.blit(BaseScreen.BASE_SLOT, slotX, slotY, 0, 0, BaseScreen.SLOT_SIZE, BaseScreen.SLOT_SIZE, BaseScreen.SLOT_SIZE, BaseScreen.SLOT_SIZE);
    parent.renderSlot(guiGraphics, slot);
    if (parent.isHovering(slot, mouseX, mouseY)) {
      parent.setHoveredSlot(slot);
      parent.renderSlotHighlight(guiGraphics, slot, mouseX, mouseY, partialTick, 100);
    }
  }

  @Override
  public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {

  }
}
