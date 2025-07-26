package es.degrassi.mmreborn.client.screen.widget;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.api.client.screen.TooltipRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class StringButton extends Button implements TooltipRender {
  protected static final WidgetSprites SPRITES = new WidgetSprites(
      ModularMachineryReborn.rl("widget/button/button"),
      ModularMachineryReborn.rl("widget/button/button_disabled"),
      ModularMachineryReborn.rl("widget/button/button_highlighted")
  );

  private final boolean renderString;

  private boolean renderTooltip = true;
  private List<Component> tooltips;

  public StringButton(Builder builder, boolean renderString) {
    super(builder);
    this.renderString = renderString;
  }

  public StringButton renderTooltip(boolean render) {
    this.renderTooltip = render;
    return this;
  }

  public StringButton setTooltips(Component... components) {
    this.tooltips = Lists.newArrayList(components);
    return this;
  }

  @Override
  protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    Minecraft minecraft = Minecraft.getInstance();
    guiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
    RenderSystem.enableBlend();
    RenderSystem.enableDepthTest();
    guiGraphics.blitSprite(
        SPRITES.get(this.active, this.isHoveredOrFocused()),
        this.getX(), this.getY(),
        this.getWidth(), this.getHeight());
    guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    int i = getFGColor();
    if (renderString)
      this.renderString(guiGraphics, minecraft.font, i | Mth.ceil(this.alpha * 255.0F) << 24);
  }

  protected void renderScrollingString(GuiGraphics guiGraphics, Font font, int width, int color) {
    int i = this.getX() + width;
    int j = this.getX() + this.getWidth() - width;
    renderScrollingString(guiGraphics, font, this.getMessage(), i, this.getY(), j, this.getY() + this.getHeight() - 3, color);
  }

  @Override
  public void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    if (isMouseOver(mouseX, mouseY) && renderTooltip && tooltips != null && !tooltips.isEmpty()) {
      for (Component tooltip : tooltips) {
        guiGraphics.renderTooltip(Minecraft.getInstance().font, tooltip, mouseX, mouseY);
      }
    }
  }
}
