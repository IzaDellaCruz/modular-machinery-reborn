package es.degrassi.mmreborn.client.screen.widget.tabs;

import es.degrassi.mmreborn.api.client.Icon;
import es.degrassi.mmreborn.client.screen.widget.ItemOrIconButton;
import es.degrassi.mmreborn.common.entity.base.IAutoEntity;
import es.degrassi.mmreborn.common.entity.base.IAutoInputEntity;
import es.degrassi.mmreborn.common.network.client.CChangeAutoInputPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AutoInputTabWidget<T extends IAutoEntity<?> & IAutoInputEntity> extends LeftTabWidget {
  private final T entity;
  public AutoInputTabWidget(T entity) {
    super(
        0,
        0,
        new VariableItemOrIconButton<>(
            0,
            0,
            button -> {},
            entity
        ),
        null
    );
    this.entity = entity;
  }

  @Override
  public void onClick(double mouseX, double mouseY, int button) {
    PacketDistributor.sendToServer(new CChangeAutoInputPacket(!entity.isShouldAutoInput(), entity.getBlockPos()));
    setFocused(true);
  }

  @Override
  public void renderTooltip(@NotNull GuiGraphics guiGraphics, int x, int y) {
    super.renderTooltip(guiGraphics, x, y);
    guiGraphics.renderTooltip(
        Minecraft.getInstance().font,
        List.of(
            Component.translatable("mmr.gui.tooltip.auto_input").getVisualOrderText(),
            Component.translatable(
                "mmr.gui.tooltip.auto_input.change",
                Component.translatable("mmr.gui.tooltip.enabled." + entity.isShouldAutoInput()).withStyle(ChatFormatting.AQUA),
                Component.translatable("mmr.gui.tooltip.enabled." + !entity.isShouldAutoInput()).withStyle(ChatFormatting.AQUA)
            ).getVisualOrderText()
        ),
        x,
        y
    );
  }

  private static class VariableItemOrIconButton<T extends IAutoEntity<?> & IAutoInputEntity> extends ItemOrIconButton {
    private final T entity;
    public VariableItemOrIconButton(int x, int y, OnPress onPress, T entity) {
      super(x, y, (Item) null, onPress);
      this.entity = entity;
    }

    @Override
    public @Nullable Icon getIcon() {
      return this.entity.isShouldAutoInput() ? Icon.AUTO_EXPORT_ON : Icon.AUTO_EXPORT_OFF;
    }
  }
}
