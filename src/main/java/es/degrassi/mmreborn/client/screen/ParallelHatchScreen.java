package es.degrassi.mmreborn.client.screen;

import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.api.client.screen.TooltipRender;
import es.degrassi.mmreborn.client.container.ParallelHatchContainer;
import es.degrassi.mmreborn.client.screen.widget.CoreActionButton;
import es.degrassi.mmreborn.client.screen.widget.CoreActionType;
import es.degrassi.mmreborn.common.entity.ParallelHatchEntity;
import es.degrassi.mmreborn.common.network.client.CCoreButtonClickedPacked;
import es.degrassi.mmreborn.common.util.TextureSizeHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public class ParallelHatchScreen extends BaseScreen<ParallelHatchContainer, ParallelHatchEntity> {
  public ParallelHatchScreen(ParallelHatchContainer menu, Inventory playerInventory, Component title) {
    super(menu, playerInventory, title, false);
  }

  @Override
  protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
  }

  @Override
  public @Nullable ResourceLocation getTexture() {
    return ModularMachineryReborn.rl("background");
  }

  @Override
  protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
    clearWidgets();
    if (getTexture() != null) {
      guiGraphics.pose().pushPose();
      guiGraphics.setColor(1f, 1f, 1f, 1f);
      this.leftPos = (this.width - this.imageWidth) / 2;
      this.topPos = (this.height - this.imageHeight) / 2;
      guiGraphics.blitSprite(getTexture(), leftPos, topPos, 176, 176);
      guiGraphics.pose().popPose();
    }
    GridLayout layout = new GridLayout(leftPos + 8, topPos + 25);
    GridLayout.RowHelper row = layout.createRowHelper(5);
    row.defaultCellSetting().alignHorizontallyCenter().alignVerticallyMiddle().paddingHorizontal(20);
    row.addChild(
        new CoreActionButton(this::action, CoreActionType.REMOVE).renderTooltip(true)
    );
    row.addChild(
        new MultiLineTextWidget(Component.literal(String.format("%s", getMenu().getEntity().getCores())), font),
        3,
        row.newCellSettings().alignVerticallyMiddle().alignHorizontallyCenter()
    );
    row.addChild(
        new CoreActionButton(this::action, CoreActionType.ADD).renderTooltip(true)
    );
    layout.arrangeElements();
    layout.visitWidgets(this::addRenderableWidget);
    for (Slot slot : getMenu().slots) {
      guiGraphics.blit(BASE_SLOT, slot.x + getGuiLeft() - 1, slot.y + getGuiTop() - 1, 0, 0,
          TextureSizeHelper.getWidth(BASE_SLOT),
          TextureSizeHelper.getHeight(BASE_SLOT),
          TextureSizeHelper.getWidth(BASE_SLOT),
          TextureSizeHelper.getHeight(BASE_SLOT));
    }
  }

  private void action(CoreActionType action) {
    int amount = Screen.hasShiftDown() ? 10 : Screen.hasControlDown() ? -1 : 1;
    int minAmount = 1;
    int maxAmount = getMenu().getEntity().getSize().max;
    int toSet = getMenu().getEntity().getCores();
    if (action == CoreActionType.ADD) {
      if (amount == -1) {
        toSet = maxAmount;
      } else {
        toSet += amount;
        toSet = Math.min(maxAmount, toSet);
      }
    } else if (action == CoreActionType.REMOVE) {
      if (amount == -1) {
        toSet = minAmount;
      } else {
        toSet -= amount;
        toSet = Math.max(minAmount, toSet);
      }
    }
    PacketDistributor.sendToServer(new CCoreButtonClickedPacked(getMenu().getEntity().getBlockPos(), toSet));
  }

  @Override
  protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
    super.renderTooltip(guiGraphics, x, y);
    this.children()
        .stream()
        .filter(listener -> listener instanceof TooltipRender)
        .map(listener -> (TooltipRender) listener)
        .forEach(renderer -> renderer.renderTooltip(guiGraphics, x, y));
  }
}
