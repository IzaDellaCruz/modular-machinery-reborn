package es.degrassi.mmreborn.client.screen.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import es.degrassi.mmreborn.client.container.SlotItemComponent;
import es.degrassi.mmreborn.client.screen.BaseScreen;
import es.degrassi.mmreborn.common.util.TextComponentUtil;
import es.degrassi.mmreborn.common.util.TextureSizeHelper;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.text.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GuiSlotScroll extends GuiElement {
  private static final Component ZERO = Component.literal("").withStyle(ChatFormatting.YELLOW);

  private final GuiScrollbar scrollBar;

  private final int xSlots, ySlots;
  private final Supplier<@NotNull List<SlotItemComponent>> slotList;
  private final ISlotClickHandler clickHandler;


  public GuiSlotScroll(BaseScreen<?, ?> gui, int x, int y, int xSlots, int ySlots,
                       Supplier<@NotNull List<SlotItemComponent>> slotList, ISlotClickHandler clickHandler,
                       boolean needsScrolling) {
    super(gui, x, y, xSlots * BaseScreen.SLOT_SIZE + BaseScreen.getScrollbarWidth() + 8, ySlots * BaseScreen.SLOT_SIZE);
    this.xSlots = xSlots;
    this.ySlots = ySlots;
    this.slotList = slotList;
    this.clickHandler = clickHandler;
    this.scrollBar = addChild(new GuiScrollbar(
        gui,
        relativeX + xSlots * BaseScreen.SLOT_SIZE + 4,
        y,
        ySlots * BaseScreen.SLOT_SIZE,
        () -> Mth.ceil((double) getSlotList().size() / this.xSlots),
        () -> this.ySlots, needsScrolling
    ));
  }

  @Override
  public void visitWidgets(Consumer<AbstractWidget> consumer) {
    children().forEach(consumer);
  }

  public List<SlotItemComponent> getSlotList() {
    return slotList.get();
  }

  @Override
  public void drawBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
    super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
    List<SlotItemComponent> list = getSlotList();
    if (!list.isEmpty()) {
      int slotStart = scrollBar.getCurrentSelection() * xSlots, max = xSlots * ySlots;
      for (int i = 0; i < max; i++) {
        int slot = slotStart + i;
        // terminate if we've exceeded max slot pos
        if (slot >= list.size()) {
          break;
        }
        renderSlot(guiGraphics, list.get(slot), BaseScreen.SLOT_SIZE * (i % xSlots) + relativeX - 1,BaseScreen.SLOT_SIZE * (i / xSlots) + relativeY - 1);
      }
    }
  }

  @Override
  public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    super.renderForeground(guiGraphics, mouseX, mouseY);
    if (getSlot(mouseX, mouseY) == null) return;
    int xAxis = mouseX - getGuiLeft(), yAxis = mouseY - getGuiTop();
    int slotX = (xAxis - relativeX) / BaseScreen.SLOT_SIZE, slotY = (yAxis - relativeY) / BaseScreen.SLOT_SIZE;
    if (slotX >= 0 && slotY >= 0 && slotX < xSlots && slotY < ySlots) {
      int slotStartX = relativeX + slotX * BaseScreen.SLOT_SIZE, slotStartY = relativeY + slotY * BaseScreen.SLOT_SIZE;
      if (xAxis >= slotStartX && xAxis < slotStartX + BaseScreen.SLOT_SIZE-2 && yAxis >= slotStartY && yAxis < slotStartY + BaseScreen.SLOT_SIZE-2 && checkWindows(mouseX, mouseY)) {
        guiGraphics.blit(BaseScreen.BASE_SLOT_HOVERED, slotStartX - 1, slotStartY - 1, 0, 0, BaseScreen.SLOT_SIZE,
            BaseScreen.SLOT_SIZE,
            TextureSizeHelper.getWidth(BaseScreen.BASE_SLOT_HOVERED), TextureSizeHelper.getHeight(BaseScreen.BASE_SLOT_HOVERED));
        BaseScreen.renderSlotHighlight(guiGraphics, slotStartX, slotStartY, gui().getSlotColor(), 0);
        renderToolTip(guiGraphics, mouseX, mouseY);
      }
    }
  }

  @Override
  public void renderToolTip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    super.renderToolTip(guiGraphics, mouseX, mouseY);
    SlotItemComponent slot = getSlot(mouseX, mouseY);
    if (slot != null) {
      renderSlotTooltip(guiGraphics, slot, mouseX, mouseY);
    }
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double xDelta, double yDelta) {
    return scrollBar.adjustScroll(yDelta) || super.mouseScrolled(mouseX, mouseY, xDelta, yDelta);
  }

  // TODO: look where is being the slot clicked when is not visible
  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    return Optional.ofNullable(getSlot(mouseX, mouseY))
        .map(slot -> {
          if (!slot.isActive()) return false;
          getChildAt(mouseX, mouseY).ifPresentOrElse(this::setFocused, gui()::clearFocus);
          if (this.active && this.visible) {
            if (this.isValidClickButton(button)) {
              boolean flag = this.clicked(mouseX, mouseY);
              if (flag) {
                this.playDownSound(Minecraft.getInstance().getSoundManager());
                this.onClick(mouseX, mouseY, button);
                return true;
              }
            }
          }
          return false;
        })
        .orElse(false);
  }

  protected boolean isValidClickButton(int button) {
    return false;
  }

  @Override
  public boolean mouseReleased(double mouseX, double mouseY, int button) {
    if (gui().currentlyQuickCrafting()) {
      //If the player is currently quick crafting don't do any special handling for as if they clicked in the screen
      return super.mouseReleased(mouseX, mouseY, button);
    }
    super.mouseReleased(mouseX, mouseY, button);
    clickHandler.onClick(() -> getSlot(mouseX, mouseY), button, Screen.hasShiftDown(), gui().getCarriedItem());
    return true;
  }

  @Nullable
  private SlotItemComponent getSlot(double mouseX, double mouseY) {
    List<SlotItemComponent> list = getSlotList();
    if (list.isEmpty()) {
      return null;
    }
    int slotX = (int) ((mouseX - getX()) / BaseScreen.SLOT_SIZE), slotY = (int) ((mouseY - getY()) / BaseScreen.SLOT_SIZE);
    // terminate if we clicked the border of a slot
    int slotStartX = getX() + slotX * BaseScreen.SLOT_SIZE + 1, slotStartY = getY() + slotY * BaseScreen.SLOT_SIZE + 1;
    if (mouseX < slotStartX || mouseX >= slotStartX + BaseScreen.SLOT_SIZE - 2 || mouseY < slotStartY || mouseY >= slotStartY + BaseScreen.SLOT_SIZE - 2) {
      return null;
    }
    // terminate if we aren't looking at a slot on-screen
    if (slotX < 0 || slotY < 0 || slotX >= xSlots || slotY >= ySlots) {
      return null;
    }
    int slot = (slotY + scrollBar.getCurrentSelection()) * xSlots + slotX;
    // terminate if the slot doesn't exist
    if (slot >= list.size()) {
      return null;
    }
    return list.get(slot);
  }

  private void renderSlot(GuiGraphics guiGraphics, SlotItemComponent slot, int slotX, int slotY) {
    guiGraphics.blit(BaseScreen.BASE_SLOT, slotX, slotY, 0, 0, TextureSizeHelper.getWidth(BaseScreen.BASE_SLOT),
        TextureSizeHelper.getHeight(BaseScreen.BASE_SLOT), TextureSizeHelper.getWidth(BaseScreen.BASE_SLOT),
        TextureSizeHelper.getHeight(BaseScreen.BASE_SLOT));
    ItemStack stack = slot.getItem();
    if (stack.isEmpty()) {//Sanity check
      return;
    }
    gui().renderItemWithOverlay(guiGraphics, stack, relativeX + slotX + 2 - BaseScreen.SLOT_SIZE / 2,
        relativeY + slotY + 1 - BaseScreen.SLOT_SIZE / 2, 1, "");
    long count = slot.getItem().getCount();
    guiGraphics.pose().pushPose();
    guiGraphics.pose().translate(0, 0, 100);
    Component text = null;
    if (count == 0) {
      //If there is no items stored, display the text in yellow, similar to what mojang does when it has to display a zero count
      // See: AbstractContainerScreen#render(GuiGraphics, int, int, float) and rendering the dragging item
      text = ZERO;
    } else if (count > 1) {
      //Note: For cases like 9,999,999 we intentionally display as 9999.9K instead of 10M so that people
      // do not think they have more stored than they actually have just because it is rounding up
      if (count < 100) {
        text = TextComponentUtil.getString(Long.toString(count));
      } else {
        text = UnitDisplayUtils.getDisplay(count, 1);
      }
    }
    if (text != null) {
      renderSlotText(guiGraphics, text, slotX + 2 - BaseScreen.SLOT_SIZE / 2, slotY + 1 - BaseScreen.SLOT_SIZE / 2);
    }
    guiGraphics.pose().popPose();
  }

  private void renderSlotTooltip(GuiGraphics guiGraphics, SlotItemComponent slot, int slotX, int slotY) {
    ItemStack stack = slot.getItem();
    if (stack.isEmpty()) {//Sanity check
      return;
    }
    long count = slot.getItem().getCount();
    if (count < 100) {
      guiGraphics.renderTooltip(font(), stack, slotX - getGuiLeft(), slotY - getGuiTop());
    } else {
      //If the slot's displayed count is truncated, make sure we also add the actual amount to the tooltip
      gui().renderItemTooltipWithExtra(guiGraphics, stack, slotX - getGuiLeft(), slotY - getGuiTop(),
          Collections.singletonList(Component.literal(TextUtils.format(count)).withStyle(ChatFormatting.GOLD).append(Component.literal("/").withStyle(ChatFormatting.GRAY))
              .append(Component.literal(TextUtils.format(slot.getMaxStackSize())).withStyle(ChatFormatting.GOLD))));
    }
  }

  private void renderSlotText(GuiGraphics guiGraphics, Component text, int x, int y) {
    float scale = 0.6F;
    float scaledWidth = font().width(text) * scale;
    if (scaledWidth >= 16) {
      //If we need a lower scale slightly due to having a lot of text, calculate it
      //Note: If it would still overflow, then we just let the scrolling text handle it
      scale = 0.5F;
    }
    PoseStack pose = guiGraphics.pose();
    pose.pushPose();
    pose.translate(0, 0, 200);
    drawScaledScrollingString(guiGraphics, text, x, y + 9, IFancyFontRenderer.TextAlignment.RIGHT, 0xFFFFFF, 16, 0, true, scale);
    pose.popPose();
  }
}
