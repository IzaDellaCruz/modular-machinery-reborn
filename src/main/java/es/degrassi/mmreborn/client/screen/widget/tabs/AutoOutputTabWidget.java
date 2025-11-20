package es.degrassi.mmreborn.client.screen.widget.tabs;

import es.degrassi.mmreborn.api.client.Icon;
import es.degrassi.mmreborn.client.screen.widget.ItemOrIconButton;
import es.degrassi.mmreborn.common.entity.base.IAutoEntity;
import es.degrassi.mmreborn.common.entity.base.IAutoOutputEntity;
import es.degrassi.mmreborn.common.network.client.CChangeAutoOutputPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AutoOutputTabWidget<T extends IAutoEntity<?> & IAutoOutputEntity> extends LeftTabWidget {
  private final T entity;
  public AutoOutputTabWidget(T entity) {
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
    PacketDistributor.sendToServer(new CChangeAutoOutputPacket(!entity.isShouldAutoOutput(), entity.getBlockPos()));
    setFocused(true);
  }

  @Override
  public void renderTooltip(@NotNull GuiGraphics guiGraphics, int x, int y) {
    super.renderTooltip(guiGraphics, x, y);
    guiGraphics.renderTooltip(
        Minecraft.getInstance().font,
        List.of(
            Component.translatable("mmr.gui.tooltip.auto_output").getVisualOrderText(),
            Component.translatable(
                "mmr.gui.tooltip.auto_output.change",
                Component.literal(entity.isShouldAutoOutput() + "").withStyle(ChatFormatting.AQUA),
                Component.literal(!entity.isShouldAutoOutput() + "").withStyle(ChatFormatting.AQUA)
            ).getVisualOrderText()
        ),
        x,
        y
    );
  }

  private static class VariableItemOrIconButton<T extends IAutoEntity<?> & IAutoOutputEntity> extends ItemOrIconButton {
    private final T entity;
    public VariableItemOrIconButton(int x, int y, OnPress onPress, T entity) {
      super(x, y, (Item) null, onPress);
      this.entity = entity;
    }

    @Override
    public @Nullable Icon getIcon() {
      return this.entity.isShouldAutoOutput() ? Icon.AUTO_EXPORT_ON : Icon.AUTO_EXPORT_OFF;
    }
  }
}
