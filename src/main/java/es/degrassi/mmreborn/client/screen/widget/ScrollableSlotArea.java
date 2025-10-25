package es.degrassi.mmreborn.client.screen.widget;

import es.degrassi.mmreborn.client.container.ContainerBase;
import es.degrassi.mmreborn.client.screen.BaseScreen;
import es.degrassi.mmreborn.common.entity.base.TileInventory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.Slot;

public class ScrollableSlotArea<
    S extends BaseScreen<M, E>,
    M extends ContainerBase<E>,
    E extends TileInventory
> extends ScrollableArea<S, M, E> {

  public ScrollableSlotArea(int x, int y, int width, int height, int contentHeight, S screen, M menu) {
    super(x, y, width, height, contentHeight, screen, menu);
  }

  @Override
  public void init() {
    clearWidgets();
    int firstComponentSlot = getMenu().getFirstComponentSlotIndex();
    int totalSlots = getMenu().getEntity().getSlots() + firstComponentSlot;

    // Calculate which slots are visible in the viewport
    int scrollOffset = (int) scrollAmount();
    int viewportTop = getY();
    int viewportBottom = getY() + height;
    for (int i = firstComponentSlot; i < totalSlots; i++) {
      if (i < getMenu().slots.size()) {
        Slot slot = getMenu().slots.get(i);
        if (slot.isActive()) {
          // Calculate slot position with scroll offset applied
          int slotY = slot.y - 1;
          int translatedSlotY = slotY - scrollOffset;
          if (translatedSlotY >= viewportTop && translatedSlotY + BaseScreen.SLOT_SIZE <= viewportBottom) {
            addRenderableWidget(new SlotWidget(slot, getScreen()));
          }
        }
      }
    }
  }

  @Override
  protected void renderBackground(GuiGraphics guiGraphics) {
  }
}
