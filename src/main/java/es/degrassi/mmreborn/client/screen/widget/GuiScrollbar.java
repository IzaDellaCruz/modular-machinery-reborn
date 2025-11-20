package es.degrassi.mmreborn.client.screen.widget;

import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.client.screen.BaseScreen;
import es.degrassi.mmreborn.common.util.TextureSizeHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntSupplier;

public class GuiScrollbar extends GuiScrollableElement {
  private static final ResourceLocation BAR = ModularMachineryReborn.rl("small_scroller");
  private static final ResourceLocation BACK_BAR = ModularMachineryReborn.rl("small_scroller_disabled");

  private static final int TEXTURE_WIDTH = TextureSizeHelper.getWidth(BAR.withPrefix("textures/gui/sprites/"));
  private static final int TEXTURE_HEIGHT = TextureSizeHelper.getHeight(BAR.withPrefix("textures/gui/sprites/"));

  private final IntSupplier maxElements;
  private final IntSupplier focusedElements;
  private final boolean needsScrolling;

  public GuiScrollbar(BaseScreen<?, ?> gui, int x, int y, int height, IntSupplier maxElements,
                      IntSupplier focusedElements, boolean needsScrolling) {
    super(BAR, BACK_BAR, gui, x, y, TEXTURE_WIDTH, height, 1, -1, TEXTURE_WIDTH * 3/2, TEXTURE_HEIGHT, height);
    this.maxElements = maxElements;
    this.focusedElements = focusedElements;
    this.needsScrolling = needsScrolling;
  }

  @Override
  public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
    super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
    if (needsScrolling) {
      //Draw background and border
      guiGraphics.blitSprite(BACK_BAR,
          barX, barY,
          0,
          barWidth, height);
      guiGraphics.blitSprite(getResource(),
          barX, barY + getScroll(),
          0,
          barWidth, barHeight);
    }
  }

  @Override
  protected int getMaxElements() {
    return maxElements.getAsInt();
  }

  @Override
  protected int getFocusedElements() {
    return focusedElements.getAsInt();
  }
}
