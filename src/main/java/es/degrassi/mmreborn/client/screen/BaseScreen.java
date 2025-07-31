package es.degrassi.mmreborn.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.client.container.ContainerBase;
import es.degrassi.mmreborn.common.entity.base.ColorableMachineComponentEntity;
import es.degrassi.mmreborn.common.util.TextureSizeHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseScreen<T extends ContainerBase<E>, E extends ColorableMachineComponentEntity> extends AbstractContainerScreen<T> {
  public static final ResourceLocation BASE_SLOT = ModularMachineryReborn.rl("textures/gui/base_slot.png");
  public static final ResourceLocation BASE_SLOT_HOVERED = ModularMachineryReborn.rl("textures/gui/base_slot_hovered.png");
  public static final ResourceLocation TAB = ModularMachineryReborn.rl("textures/gui/widget/base_tab.png");
  public static final ResourceLocation TAB_HOVERED = ModularMachineryReborn.rl("textures/gui/widget/base_tab_hovered.png");

  protected final E entity;
  protected final boolean renderLabels;
  protected BaseScreen(T menu, Inventory playerInventory, Component title, boolean renderLabels) {
    super(menu, playerInventory, title);
    this.entity = menu.getEntity();
    this.renderLabels = renderLabels;
  }

  @Nullable
  public ResourceLocation getTexture() {
    return ModularMachineryReborn.rl("background");
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

    for (int k = 0; k < this.menu.slots.size(); k++) {
      Slot slot = this.menu.slots.get(k);
      if (slot.isActive()) {
        this.renderSlot(guiGraphics, slot);
        if (this.isHovering(slot, mouseX, mouseY)) {
          this.hoveredSlot = slot;
          this.renderSlotHighlight(guiGraphics, slot, mouseX, mouseY, partialTick);
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

  @Override
  protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    if (this.renderLabels) super.renderLabels(guiGraphics, mouseX, mouseY);
  }

  @Override
  protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
    if (getTexture() != null) {
      guiGraphics.pose().pushPose();
      guiGraphics.setColor(1f, 1f, 1f, 1f);
      this.leftPos = (this.width - this.imageWidth) / 2;
      this.topPos = (this.height - this.imageHeight) / 2;
      guiGraphics.blit(getTexture(), leftPos, topPos, 0, 0, imageWidth, imageHeight);
      guiGraphics.pose().popPose();
    }
  }

  protected void renderBgWithSlotSize(GuiGraphics guiGraphics, int cols, int slots) {
    if (getTexture() != null) {
      guiGraphics.pose().pushPose();
      guiGraphics.setColor(1f, 1f, 1f, 1f);
      this.leftPos = (this.width - this.imageWidth) / 2;
      this.topPos = (this.height - this.imageHeight) / 2;
      int slotsWidth = cols * 18 + 16;
      int invWidth = 18 * 9 + 16;
      int height =
          (int) Math.ceil(slots * 1D / cols) * 18 + 16
              + 18 * 4 + 3 + font.wordWrapHeight(title, Math.max(slotsWidth, invWidth) - 16) + titleLabelY;

      guiGraphics.blitSprite(getTexture(), leftPos, topPos, Math.max(slotsWidth, invWidth), height);
      guiGraphics.pose().popPose();
    }
  }

  protected void renderSlots(GuiGraphics guiGraphics) {
    for (Slot slot : getMenu().slots) {
      guiGraphics.blit(BASE_SLOT, slot.x + getGuiLeft() - 1, slot.y + getGuiTop() - 1, 0, 0,
          TextureSizeHelper.getWidth(BASE_SLOT),
          TextureSizeHelper.getHeight(BASE_SLOT),
          TextureSizeHelper.getWidth(BASE_SLOT),
          TextureSizeHelper.getHeight(BASE_SLOT));
    }
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
