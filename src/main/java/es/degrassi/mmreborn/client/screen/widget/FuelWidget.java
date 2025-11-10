package es.degrassi.mmreborn.client.screen.widget;

import com.google.common.collect.Lists;
import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.api.capability.IFuelHandler;
import es.degrassi.mmreborn.common.util.TextureSizeHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class FuelWidget extends AbstractWidget {
  private static final ResourceLocation emptyTexture = ModularMachineryReborn.rl("textures/gui/empty_fuel.png");
  private static final ResourceLocation filledTexture = ModularMachineryReborn.rl("textures/gui/filled_fuel.png");
  private final IFuelHandler handler;

  public FuelWidget(int x, int y, IFuelHandler handler) {
    super(x, y, TextureSizeHelper.getWidth(emptyTexture), TextureSizeHelper.getHeight(emptyTexture), Component.empty());
    this.handler = handler;
  }

  @Override
  protected void renderWidget(GuiGraphics guiGraphics, int i, int i1, float v) {
    guiGraphics.blit(emptyTexture, getX(), getY(), 0, 0, getWidth(), getHeight(), getWidth(), getHeight());
    var calcHeight = (int) ((handler.getFuel() * 1d / handler.getMaxFuel()) * getHeight());
    var offset = getHeight() - calcHeight;
    guiGraphics.blit(filledTexture, getX() + 1, getY() + offset + 1, 0, offset, getWidth(), calcHeight, getWidth(),
        getHeight());
  }

  public void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
    List<Component> text = Lists.newArrayList();

    text.add(Component.translatable("tooltip.fuel_tank.tank", String.valueOf(handler.getFuel()), String.valueOf(handler.getMaxFuel())));

    Font font = Minecraft.getInstance().font;
    guiGraphics.renderTooltip(font, text.stream().map(Component::getVisualOrderText).toList(), x, y);
  }

  @Override
  protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
