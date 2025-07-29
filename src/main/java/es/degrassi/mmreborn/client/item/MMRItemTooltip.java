package es.degrassi.mmreborn.client.item;

import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.common.util.TextureSizeHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class MMRItemTooltip implements ClientTooltipComponent {
  private static final ResourceLocation CHECK = ModularMachineryReborn.rl("textures/gui/check.png");
  private final ItemStack stack;
  private final Component text;

  private final int textWidth;
  private final int textHeight;
  private final boolean completed;
  private final int iconWidth;

  public MMRItemTooltip(ItemStack stack, Component text, boolean completed) {
    this.stack = stack;
    this.text = text;
    this.iconWidth = TextureSizeHelper.getWidth(CHECK);
    final int maxTextWidth = Minecraft.getInstance().font.width("100x [#modular_machinery_reborn:energyoutputhatch]");
    this.textWidth = Math.min(Minecraft.getInstance().font.width(text), maxTextWidth);
    this.textHeight = Minecraft.getInstance().font.wordWrapHeight(text, maxTextWidth);
    this.completed = completed;
  }

  @Override
  public int getHeight() {
    return Math.max(textHeight, 18) + 6;
  }

  @Override
  public int getWidth(Font font) {
    return 16 + 8 + textWidth + iconWidth;
  }

  @Override
  public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
    if (completed) {
      guiGraphics.blit(
          CHECK,
          x,
          y,
          0,
          0,
          TextureSizeHelper.getWidth(CHECK),
          TextureSizeHelper.getHeight(CHECK),
          TextureSizeHelper.getWidth(CHECK),
          TextureSizeHelper.getHeight(CHECK)
      );
    }
    x += TextureSizeHelper.getWidth(CHECK) + 4;
    guiGraphics.renderItem(stack, x, y);
    guiGraphics.renderItemDecorations(font, stack, x, y);
    guiGraphics.drawWordWrap(font, text.copy().withStyle(completed ? ChatFormatting.GREEN : ChatFormatting.GRAY),
        x + 16 + 4, y + 6, textWidth, -1);
  }
}
