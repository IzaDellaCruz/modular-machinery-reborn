package es.degrassi.mmreborn.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import es.degrassi.mmreborn.client.container.ItemBusContainer;
import es.degrassi.mmreborn.client.container.SlotItemComponent;
import es.degrassi.mmreborn.client.screen.widget.GuiElement;
import es.degrassi.mmreborn.client.screen.widget.GuiSlotScroll;
import es.degrassi.mmreborn.client.screen.widget.IGuiWrapper;
import es.degrassi.mmreborn.client.util.GuiUtils;
import es.degrassi.mmreborn.common.entity.base.TileItemBus;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

public class ItemBusScreen extends BaseScreen<ItemBusContainer, TileItemBus> implements IGuiWrapper {
  private static final int MAX_VISIBLE_ROWS = ItemBusContainer.MAX_VISIBLE_ROWS;

  private int visibleRows;
  private long lastMSInitialized;
  private boolean hasClicked = false;
  private boolean needsScrolling;
  public static int maxZOffset;

  public ItemBusScreen(ItemBusContainer pMenu, Inventory pPlayerInventory, Component pTitle) {
    super(pMenu, pPlayerInventory, pTitle, false);
    calculateScrollingNeeds();
  }

  private void calculateScrollingNeeds() {
    Font font = Minecraft.getInstance().font;
    int cols = getMenu().getEntity().getSize().cols;
    int totalSlots = getMenu().getEntity().getSlots();
    int totalRows = (int) Math.ceil(totalSlots * 1.0 / cols);
    int slotsWidth = cols * SLOT_SIZE + 16;
    int invWidth = SLOT_SIZE * 9 + 16 + getScrollbarBackgroundWidth() + 8;
    this.imageHeight =
        (int) Math.ceil(totalSlots * 1D / cols) * SLOT_SIZE + 10 + SLOT_SIZE * 4 + 3 + font.wordWrapHeight(title,
        Math.max(slotsWidth, invWidth) - 16) + titleLabelY;
    this.imageWidth = cols * SLOT_SIZE + 16 + getScrollbarBackgroundWidth() * 2 + this.titleLabelX;

    this.needsScrolling = totalRows > MAX_VISIBLE_ROWS;
    this.visibleRows = needsScrolling ? MAX_VISIBLE_ROWS : totalRows;
  }

  @Override
  protected void containerTick() {
    super.containerTick();
    for (GuiEventListener child : children()) {
      if (child instanceof GuiElement element) {
        element.tick();
      }
    }
  }

  @Override
  public long getTimeOpened() {
    return lastMSInitialized;
  }

  @Override
  protected void init() {
    super.init();
    lastMSInitialized = Util.getMillis();

    var scroll = addRenderableWidget(new GuiSlotScroll(
        this,
        this.titleLabelX,
        this.titleLabelY + 3,
        getMenu().getEntity().getSize().cols,
        MAX_VISIBLE_ROWS,
        getMenu()::getItemList,
        getMenu(),
        needsScrolling
    ));

    scroll.visitWidgets(this::addRenderableWidget);
  }

  @Override
  public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    int i = this.leftPos;
    int j = this.topPos;
    // Neo: replicate the super method's implementation to insert the event between background and widgets
    this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
    NeoForge.EVENT_BUS.post(new ContainerScreenEvent.Render.Background(this, guiGraphics, mouseX, mouseY));
    for (Renderable renderable : this.renderables) {
      renderable.render(guiGraphics, mouseX, mouseY, partialTick);
    }
    RenderSystem.disableDepthTest();
    guiGraphics.pose().pushPose();
    guiGraphics.pose().translate((float)i, (float)j, 0.0F);
    this.hoveredSlot = null;

    for(int k = 0; k < this.menu.slots.size(); ++k) {
      Slot slot = this.menu.slots.get(k);
      if (!(slot instanceof SlotItemComponent)) {
        Slot copySlot = new Slot(slot.container, slot.getSlotIndex(), slot.x, slot.y);
        if (copySlot.isActive()) {
          this.renderSlot(guiGraphics, copySlot);
        }

        if (this.isHovering(copySlot, mouseX, mouseY) && copySlot.isActive()) {
          this.hoveredSlot = slot;
          this.renderSlotHighlight(guiGraphics, copySlot, mouseX, mouseY, partialTick);
        }
      }
    }

    this.renderLabels(guiGraphics, mouseX, mouseY);
    NeoForge.EVENT_BUS.post(new ContainerScreenEvent.Render.Foreground(this, guiGraphics, mouseX, mouseY));
    ItemStack itemstack = this.draggingItem.isEmpty() ? this.menu.getCarried() : this.draggingItem;
    if (!itemstack.isEmpty()) {
      int l1 = 8;
      int i2 = this.draggingItem.isEmpty() ? 8 : 16;
      String s = null;
      if (!this.draggingItem.isEmpty() && this.isSplittingStack) {
        itemstack = itemstack.copyWithCount(Mth.ceil((float)itemstack.getCount() / 2.0F));
      } else if (this.isQuickCrafting && this.quickCraftSlots.size() > 1) {
        itemstack = itemstack.copyWithCount(this.quickCraftingRemainder);
        if (itemstack.isEmpty()) {
          s = ChatFormatting.YELLOW + "0";
        }
      }

      this.renderFloatingItem(guiGraphics, itemstack, mouseX - i - 8, mouseY - j - i2, s);
    }

    if (!this.snapbackItem.isEmpty()) {
      float f = (float)(Util.getMillis() - this.snapbackTime) / 100.0F;
      if (f >= 1.0F) {
        f = 1.0F;
        this.snapbackItem = ItemStack.EMPTY;
      }

      int j2 = this.snapbackEnd.x - this.snapbackStartX;
      int k2 = this.snapbackEnd.y - this.snapbackStartY;
      int j1 = this.snapbackStartX + (int)((float)j2 * f);
      int k1 = this.snapbackStartY + (int)((float)k2 * f);
      this.renderFloatingItem(guiGraphics, this.snapbackItem, j1, k1, null);
    }

    guiGraphics.pose().popPose();
    RenderSystem.enableDepthTest();
    renderTooltip(guiGraphics, mouseX, mouseY);
  }

  protected void renderBgWithSlotSize(GuiGraphics guiGraphics, int cols, int slots) {
    if (getTexture() != null) {
      guiGraphics.pose().pushPose();
      guiGraphics.setColor(1f, 1f, 1f, 1f);
      int slotsWidth = cols * SLOT_SIZE + 16;
      int invWidth = SLOT_SIZE * 9 + 16 + getScrollbarBackgroundWidth() + 8;
      int height = (int) Math.ceil(slots * 1D / cols) * SLOT_SIZE + 10 + SLOT_SIZE * 4 + 3 + font.wordWrapHeight(title, Math.max(slotsWidth, invWidth) - 16) + titleLabelY;
      this.leftPos = (this.width - this.imageWidth) / 2;
      this.topPos = (this.height - this.imageHeight) / 2;

      guiGraphics.blitSprite(getTexture(), leftPos, topPos, Math.max(slotsWidth, invWidth), height);
      guiGraphics.pose().popPose();
    }
  }

  @Override
  protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
    renderBgWithSlotSize(guiGraphics, getMenu().getEntity().getSize().cols, visibleRows * getMenu().getEntity().getSize().cols);
  }

  @Override
  protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    PoseStack pose = guiGraphics.pose();
    pose.pushPose();
    //Shift forward as far as tooltips get shifted so that we don't risk intersecting the rendered items
    pose.translate(0, 0, 400);
    for (GuiEventListener c : children()) {
      if (c instanceof GuiElement element) {
        element.onDrawBackground(guiGraphics, mouseX, mouseY, Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false));
      }
    }
    drawForegroundText(guiGraphics, mouseX, mouseY);
    // first render general foregrounds
    int zOffset = 200;
    maxZOffset = zOffset;
    for (GuiEventListener widget : children()) {
      if (widget instanceof GuiElement element) {
        pose.pushPose();
        element.onRenderForeground(guiGraphics, mouseX, mouseY, zOffset, zOffset);
        pose.popPose();
      }
    }
    pose.popPose();
    //Additionally hacky offset to make it so that we render above items in higher z-levels for things like tooltips and held items
    maxZOffset += 200;
    // then render tooltips, translating above max z offset to prevent clashing
    // It is IMPORTANT that we do this to ensure any delayed rendering we do the for the tooltip happens above the other things
    // and so that we let the translation leak out into the super method so that the carried item renders at the correct z level
    pose.translate(0, 0, maxZOffset);

    pose.pushPose();
    //Note: Because we are doing this from renderLabels instead of as part of a render override,
    // we need to unshift back to the position the other methods expect to be called from
    pose.translate(-leftPos, -topPos, 0);
    renderTooltip(guiGraphics, mouseX, mouseY);
    pose.popPose();
  }

  protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double xDelta, double yDelta) {
    return super.mouseScrolled(mouseX, mouseY, xDelta, yDelta);
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    hasClicked = true;
    // otherwise, we send it to the current element (this is the same as super.super [ContainerEventHandler#mouseClicked], but in reverse order)
    //TODO: Why do we do this in reverse order?
    GuiEventListener clickedChild = GuiUtils.findChild(children(), mouseX, mouseY, button, GuiEventListener::mouseClicked);

    if (clickedChild != null) {
      setFocused(clickedChild);
      if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
        setDragging(true);
      }
      return super.mouseClicked(mouseX, mouseY, button);
    } else {
      //If we can't find a child, allow clearing whatever focus we currently have
      clearFocus();
    }
    return super.mouseClicked(mouseX, mouseY, button);
  }

  @Override
  public boolean mouseReleased(double mouseX, double mouseY, int button) {
    if (hasClicked) {
      // always pass mouse released events to windows for drag checks
      return super.mouseReleased(mouseX, mouseY, button);
    }
    return false;
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    return GuiUtils.checkChildren(children(), keyCode, scanCode, modifiers, (child, k, s, m) -> child instanceof GuiElement && child.keyPressed(k, s, m)) ||
        super.keyPressed(keyCode, scanCode, modifiers);
  }

  @Override
  public boolean charTyped(char c, int keyCode) {
    return GuiUtils.checkChildrenChar(children(), c, keyCode, (child, ch, k) -> child instanceof GuiElement && child.charTyped(ch, k)) || super.charTyped(c, keyCode);
  }

  /**
   * @apiNote mouseXOld and mouseYOld are just guessed mappings I couldn't find any usage from a quick glance.
   */
  @Override
  public boolean mouseDragged(double mouseX, double mouseY, int button, double mouseXOld, double mouseYOld) {
    super.mouseDragged(mouseX, mouseY, button, mouseXOld, mouseYOld);
    return getFocused() != null && isDragging() && button == GLFW.GLFW_MOUSE_BUTTON_LEFT && getFocused().mouseDragged(mouseX, mouseY, button, mouseXOld, mouseYOld);
  }

  public boolean isMouseOverSlot(Slot slot, double mouseX, double mouseY) {
    return isHovering(slot, mouseX, mouseY);
  }
}
