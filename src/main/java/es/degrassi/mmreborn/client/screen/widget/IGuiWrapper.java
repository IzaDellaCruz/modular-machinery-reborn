package es.degrassi.mmreborn.client.screen.widget;

import es.degrassi.mmreborn.client.screen.BaseScreen;
import es.degrassi.mmreborn.client.util.GuiUtils;
import es.degrassi.mmreborn.common.util.MMRLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public interface IGuiWrapper extends ContainerEventHandler, IFancyFontRenderer {

  @NotNull
  default ItemStack getCarriedItem() {
    return ItemStack.EMPTY;
  }

  int getGuiLeft();

  int getGuiTop();

  @Override
  int getXSize();

  int getYSize();

  default boolean currentlyQuickCrafting() {
    return false;
  }

  default void renderItem(GuiGraphics guiGraphics, @NotNull ItemStack stack, int xAxis, int yAxis) {
    renderItem(guiGraphics, stack, xAxis, yAxis, 1);
  }

  default void renderItem(GuiGraphics guiGraphics, @NotNull ItemStack stack, int xAxis, int yAxis, float scale) {
    GuiUtils.renderItem(guiGraphics, stack, xAxis, yAxis, scale, font(), null, false);
  }

  default void renderItemTooltipWithExtra(GuiGraphics guiGraphics, @NotNull ItemStack stack, int xAxis, int yAxis, List<Component> toAppend) {
    if (toAppend.isEmpty()) {
      guiGraphics.renderTooltip(font(), stack, xAxis, yAxis);
    } else {
      List<Component> tooltip = new ArrayList<>(Screen.getTooltipFromItem(Minecraft.getInstance(), stack));
      tooltip.addAll(toAppend);
      guiGraphics.renderTooltip(font(), tooltip, stack.getTooltipImage(), stack, xAxis, yAxis);
    }
  }

  default void renderItemWithOverlay(GuiGraphics guiGraphics, @NotNull ItemStack stack, int xAxis, int yAxis, float scale, @Nullable String text) {
    GuiUtils.renderItem(guiGraphics, stack, xAxis, yAxis, scale, font(), text, true);
  }

  @Nullable
  default BaseScreen<?,?> getWindowHovering(double mouseX, double mouseY) {
    return null;
  }

  default void addWindow(BaseScreen<?, ?> window) {
    MMRLogger.INSTANCE.error("Tried to call 'addWindow' but unsupported in {}", getClass().getName());
  }

  default void removeWindow(BaseScreen<?, ?> window) {
    MMRLogger.INSTANCE.error("Tried to call 'removeWindow' but unsupported in {}", getClass().getName());
  }

  void renderSlotHighlight(GuiGraphics guiGraphics, Slot slot, int mouseX, int mouseY, float partialTick);

  int getSlotColor(int index);
  int getSlotColor();

  default void renderSlotHighlight(GuiGraphics guiGraphics, Slot slot, int mouseX, int mouseY, float partialTick, int z) {
    if (slot.isHighlightable()) {
      BaseScreen.renderSlotHighlight(guiGraphics, slot.x, slot.y, getSlotColor(slot.index), z);
    }
  }
}
