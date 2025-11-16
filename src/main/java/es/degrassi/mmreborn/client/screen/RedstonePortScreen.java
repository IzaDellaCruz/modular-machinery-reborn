package es.degrassi.mmreborn.client.screen;

import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.api.client.screen.TooltipRender;
import es.degrassi.mmreborn.client.container.RedstonePortContainer;
import es.degrassi.mmreborn.client.screen.widget.EnumButton;
import es.degrassi.mmreborn.common.entity.RedstonePortEntity;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.network.client.CRedstoneButtonModeClickedPacket;
import es.degrassi.mmreborn.common.util.TextureSizeHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RedstonePortScreen extends BaseScreen<RedstonePortContainer, RedstonePortEntity> {
  public RedstonePortScreen(RedstonePortContainer menu, Inventory playerInventory, Component title) {
    super(menu, playerInventory, title, true);
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
  @SuppressWarnings("rawtypes")
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
        Button
            .builder(Component.empty(), b -> {
              if (b instanceof EnumButton enumButton && enumButton.getValue() instanceof IOType v) {
                PacketDistributor.sendToServer(new CRedstoneButtonModeClickedPacket(v, entity.getBlockPos()));
                getMenu().broadcastChanges();
                entity.setMode(v);
              }
            })
            .build(builder -> new EnumButton<>(
                builder,
                value -> Component.translatable("mmr.gui.tooltip.redstone.button.mode." + value.getSerializedName()),
                List.of(IOType.INPUT, IOType.OUTPUT),
                entity.getMode()
            ))
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
