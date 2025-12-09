package es.degrassi.mmreborn.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.client.container.ControllerContainer;
import es.degrassi.mmreborn.client.screen.popup.BasePopupScreen;
import es.degrassi.mmreborn.client.screen.widget.tabs.CoreTabWidget;
import es.degrassi.mmreborn.client.screen.widget.tabs.ShowRecipesTabWidget;
import es.degrassi.mmreborn.client.screen.widget.tabs.StructureBreakWidget;
import es.degrassi.mmreborn.client.screen.widget.tabs.StructurePlacerWidget;
import es.degrassi.mmreborn.client.screen.widget.tabs.TabGroupWidget;
import es.degrassi.mmreborn.client.screen.widget.tabs.TopTabGroupWidget;
import es.degrassi.mmreborn.client.screen.widget.tabs.TopTabWidget;
import es.degrassi.mmreborn.common.machine.DynamicMachine;
import es.degrassi.mmreborn.common.util.Mods;
import es.degrassi.mmreborn.common.util.TextureSizeHelper;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class ControllerScreen extends BasePopupScreen<ControllerContainer> {
  protected static final ResourceLocation BASE_SLOT = ModularMachineryReborn.rl("textures/gui/base_slot.png");
  protected static final ResourceLocation BASE_SLOT_HOVERED = ModularMachineryReborn.rl("textures/gui/base_slot_hovered.png");
  private static final int screenWidth = 158;

  private TopTabGroupWidget tabs;

  public ControllerScreen(ControllerContainer pMenu, Inventory pPlayerInventory, Component pTitle) {
    super(pMenu, pPlayerInventory, pTitle, TextureSizeHelper.getWidth(getTexture()), TextureSizeHelper.getHeight(getTexture()));
  }

  @Override
  protected void init() {
    super.init();
    createWidgets();
  }

  @Override
  public void removed() {
    super.removed();
    if (this.minecraft.player != null) {
      this.menu.removed(this.minecraft.player);
    }
  }

  private void createWidgets() {
    tabs = addRenderableWidget(TabGroupWidget.createTop(x, y - TextureSizeHelper.getHeight(TopTabWidget.TAB)));
    tabs.addTab(new StructurePlacerWidget(
            this,
            getMenu().getId(),
            getMenu().getEntity().getBlockPos())
        )
        .addTab(
            new StructureBreakWidget(
                this,
                getMenu().getId(),
                getMenu().getEntity().getBlockPos()
            )
        )
        .addTab(new CoreTabWidget(this));
    if (Mods.isJEIorEMILoaded())
      tabs.addTab(new ShowRecipesTabWidget(/*ModularMachineryReborn.rl("textures/gui/tabs/recipes.png")*/ null, getMenu().getEntity().getFoundMachine()));
    tabs.setInitialFocus(getMenu().getEntity().getLastFocus());
  }

  public static ResourceLocation getTexture() {
    return ModularMachineryReborn.rl("textures/gui/guicontroller.png");
  }

  public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
    int i = this.x;
    int j = this.y;

    // render image background
    this.renderTransparentBackground(guiGraphics);
    renderBg(guiGraphics, partialTicks, mouseX, mouseY);

    for (Renderable renderable : this.renderables) {
      renderable.render(guiGraphics, mouseX, mouseY, partialTicks);
    }
    // Neo: replicate the super method's implementation to insert the event between background and widgets
    RenderSystem.disableDepthTest();
    guiGraphics.pose().pushPose();
    guiGraphics.pose().translate((float) i, (float) j, 0.0F);
    this.hoveredSlot = null;

    for (int k = 0; k < this.menu.slots.size(); k++) {
      Slot slot = this.menu.slots.get(k);
      if (slot.isActive()) {
        this.renderSlot(guiGraphics, slot);
        if (this.isHovering(slot, mouseX, mouseY)) {
          this.hoveredSlot = slot;
          this.renderSlotHighlight(guiGraphics, slot, mouseX, mouseY, partialTicks);
        }
      }
    }

    this.renderLabels(guiGraphics, mouseX, mouseY);
    ItemStack itemstack = this.draggingItem.isEmpty() ? this.menu.getCarried() : this.draggingItem;
    if (!itemstack.isEmpty()) {
      int l1 = 8;
      int i2 = this.draggingItem.isEmpty() ? 8 : 16;
      String s = null;
      if (!this.draggingItem.isEmpty() && this.isSplittingStack) {
        itemstack = itemstack.copyWithCount(Mth.ceil((float) itemstack.getCount() / 2.0F));
      } else if (this.isQuickCrafting && this.quickCraftSlots.size() > 1) {
        itemstack = itemstack.copyWithCount(this.quickCraftingRemainder);
        if (itemstack.isEmpty()) {
          s = ChatFormatting.YELLOW + "0";
        }
      }

      this.renderFloatingItem(guiGraphics, itemstack, mouseX - i - 8, mouseY - j - i2, s);
    }

    if (!this.snapbackItem.isEmpty()) {
      float f = (float) (Util.getMillis() - this.snapbackTime) / 100.0F;
      if (f >= 1.0F) {
        f = 1.0F;
        this.snapbackItem = ItemStack.EMPTY;
      }

      int j2 = this.snapbackEnd.x - this.snapbackStartX;
      int k2 = this.snapbackEnd.y - this.snapbackStartY;
      int j1 = this.snapbackStartX + (int) ((float) j2 * f);
      int k1 = this.snapbackStartY + (int) ((float) k2 * f);
      this.renderFloatingItem(guiGraphics, this.snapbackItem, j1, k1, null);
    }

    guiGraphics.pose().popPose();

    RenderSystem.enableDepthTest();

    guiGraphics.pose().pushPose();
    guiGraphics.pose().translate(this.x, this.y, 0);
    float scale = 0.72f;
    guiGraphics.pose().scale(scale, scale, scale);
    int offsetX = 14;
    int offsetY = 14;

    DynamicMachine machine = getMenu().getEntity().getFoundMachine();
    if (machine != DynamicMachine.DUMMY) {
      // render if the structure of machine is not null
      List<FormattedCharSequence> out = font.split(Component.literal(machine.getLocalizedName()), Mth.floor(screenWidth * (1 / scale)));
      offsetY -= 7;
      for (FormattedCharSequence draw : out) {
        offsetY += 7;
        guiGraphics.drawString(font, draw, offsetX, offsetY, 0xFFFFFF);
        offsetY += 7;
      }
      offsetY -= 7;
    } else {
      // render if the structure of machine is null
      Component drawnHead = Component.translatable("gui.controller.structure", Component.translatable("gui.controller.structure.none"));
      guiGraphics.drawString(font, drawnHead, offsetX, offsetY, 0xFFFFFF);
    }
    offsetY += 10;

    if (getMenu().getEntity().isPaused()) {
      // render if redstone paused the machine
      Component drawnStop = Component.translatable("gui.controller.status.redstone_stopped");
      List<FormattedCharSequence> out = font.split(drawnStop, Mth.floor(screenWidth * (1 / scale)));
      for (FormattedCharSequence draw : out) {
        offsetY += 7;
        guiGraphics.drawString(font, draw, offsetX, offsetY, 0xFFFFFF);
        offsetY += 7;
      }
      guiGraphics.pose().popPose();
      renderTooltip(guiGraphics, mouseX, mouseY);
      return;
    }

    // render the current status
    MutableComponent status = Component.translatable("gui.controller.status");
    List<FormattedCharSequence> out = font.split(status.append(getMenu().getEntity().getCraftingStatus().getUnlocMessage()), Mth.floor(screenWidth * (1 / scale)));
    for (FormattedCharSequence draw : out) {
      offsetY += 7;
      guiGraphics.drawString(font, draw, offsetX, offsetY, 0xFFFFFF);
      offsetY += 7;
    }
    Component structureError = getMenu().getEntity().getStructureError();
    out = font.split(structureError, Mth.floor(screenWidth * (1 / scale)));
    for (var draw : out) {
      offsetY += 7;
      guiGraphics.drawString(font, draw, offsetX, offsetY, 0xFFFFFF);
      offsetY += 7;
    }
    guiGraphics.pose().popPose();
    renderTooltip(guiGraphics, mouseX, mouseY);
  }

  protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
    guiGraphics.pose().pushPose();
    guiGraphics.setColor(1f, 1f, 1f, 1f);
    int leftPos = (this.width - this.xSize) / 2;
    int topPos = (this.height - this.ySize) / 2;
    guiGraphics.blit(getTexture(), leftPos, topPos, 0, 0, xSize, ySize, xSize, ySize);
    guiGraphics.pose().popPose();
  }

  protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
    // Do not render the default texts
  }

  public void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
    if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
      ItemStack itemstack = this.hoveredSlot.getItem();
      guiGraphics.renderTooltip(this.font, this.getTooltipFromContainerItem(itemstack), itemstack.getTooltipImage(), itemstack, x, y);
    }

    tabs.renderTooltip(guiGraphics, x, y);
    super.renderTooltip(guiGraphics, x, y);
  }

  protected void renderSlotHighlight(GuiGraphics guiGraphics, Slot slot, int mouseX, int mouseY, float partialTick) {
    if (slot.isHighlightable()) {
      renderSlotHighlight(guiGraphics, slot.x, slot.y, getSlotColor(slot.index));
    }
  }

  public static void renderSlotHighlight(GuiGraphics guiGraphics, int x, int y, int color) {
    guiGraphics.pose().pushPose();
    int width = TextureSizeHelper.getWidth(BASE_SLOT_HOVERED), height = TextureSizeHelper.getHeight(BASE_SLOT_HOVERED);
    guiGraphics.blit(BASE_SLOT_HOVERED, x - 1, y - 1, 0, 0, width, height, width, height);
    guiGraphics.fillGradient(RenderType.guiOverlay(), x, y, x + 16, y + 16, color, color, 0);
    guiGraphics.pose().popPose();
  }
}
