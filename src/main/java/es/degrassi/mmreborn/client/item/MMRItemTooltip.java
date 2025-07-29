package es.degrassi.mmreborn.client.item;

import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.common.util.TextureSizeHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class MMRItemTooltip implements ClientTooltipComponent {
  private static final ResourceLocation CHECK = ModularMachineryReborn.rl("textures/gui/check.png");
  private final ItemStack stack;
  private final Component text;

  private final boolean completed;
  private final int iconWidth;
  private final int iconHeight;
  private final int textWidth;
  private final int textHeight;
  private static final int maxTextWidth = Minecraft.getInstance().font.width("100x [#modular_machinery_reborn:energyoutputhatch]");

  public MMRItemTooltip(ItemStack stack, Component text, boolean completed) {
    this.stack = stack;
    MutableComponent tempText = Component.empty();
    if (text.getString().contains("#")) {
      tempText
          .append(Component.literal("(").withStyle(ChatFormatting.GRAY))
          .append(stack.getHoverName().copy().withStyle(ChatFormatting.GOLD))
          .append(Component.literal(") ").withStyle(ChatFormatting.GRAY));
    }
    this.text = tempText.append(text.copy().withStyle(completed ? ChatFormatting.GREEN : ChatFormatting.GRAY));
    this.iconWidth = TextureSizeHelper.getWidth(CHECK);
    this.iconHeight = TextureSizeHelper.getHeight(CHECK);
    this.completed = completed;
    this.textHeight = Minecraft.getInstance().font.wordWrapHeight(this.text, maxTextWidth);
    this.textWidth = Math.min(Minecraft.getInstance().font.width(this.text), maxTextWidth);
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
          iconWidth,
          iconHeight,
          iconWidth,
          iconHeight
      );
    }
    x += iconWidth + 4;
    guiGraphics.renderItem(stack, x, y);
    guiGraphics.renderItemDecorations(font, stack, x, y);
    guiGraphics.drawWordWrap(font, text, x + 16 + 4, y + 4, textWidth, -1);
  }
}
