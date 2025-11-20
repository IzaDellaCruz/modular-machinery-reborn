package es.degrassi.mmreborn.common.integration.jade.elements;

import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.common.util.TextureSizeHelper;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.ProgressStyle;

@Getter
@Setter
@Accessors(chain = true)
public class FuelElement extends Element {
  private float progress;
  private ProgressStyle style;
  private BoxStyle boxStyle;
  private ResourceLocation emptyTexture = ModularMachineryReborn.rl("textures/gui/empty_fuel.png");
  private ResourceLocation filledTexture = ModularMachineryReborn.rl("textures/gui/filled_fuel.png");

  public FuelElement(float percent) {
    this.progress = percent;
  }

  public ProgressStyle getProgressStyle() {
    return style;
  }

  public BoxStyle getStyle() {
    return boxStyle;
  }

  private int getWidth() {
    return TextureSizeHelper.getWidth(emptyTexture);
  }

  private int getHeight() {
    return TextureSizeHelper.getHeight(emptyTexture);
  }

  private float scale() {
    return 2f;
  }

  @Override
  public void render(GuiGraphics guiGraphics, float x1, float y1, float maxX, float maxY) {
    int x = (int) (x1 * scale());
    int y = (int) (y1 * scale());
    guiGraphics.pose().pushPose();
    guiGraphics.pose().scale(1/scale(), 1/scale(), 1/scale());
    guiGraphics.blit(emptyTexture, x, y, 0, 0, getWidth(), getHeight(), getWidth(), getHeight());
    var calcHeight = (int) (progress * getHeight());
    var offset = getHeight() - calcHeight;
    guiGraphics.blit(filledTexture, x + 1, y + offset + 1, 0, offset, getWidth(), calcHeight, getWidth(), getHeight());
    guiGraphics.pose().popPose();
  }

  public Vec2 getSize() {
    return new Vec2(getWidth() / scale(), getHeight() / scale());
  }
}
