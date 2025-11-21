package es.degrassi.mmreborn.common.integration.theoneprobe.element;

import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.common.util.TextureSizeHelper;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import mcjty.theoneprobe.api.IElement;
import mcjty.theoneprobe.api.IElementFactory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

@Getter
@Setter
@Accessors(chain = true)
public class FuelElement implements IElement {
  private ResourceLocation emptyTexture = ModularMachineryReborn.rl("textures/gui/empty_fuel.png");
  private ResourceLocation filledTexture = ModularMachineryReborn.rl("textures/gui/filled_fuel.png");
  public static final ResourceLocation ID = ModularMachineryReborn.rl("fuel_element");
  private final float progress;

  public FuelElement(float progress) {
    this.progress = progress;
  }

  public FuelElement(RegistryFriendlyByteBuf buf) {
    this.progress = buf.readFloat();
  }

  private float scale() {
    return 1.25f;
  }

  @Override
  public void render(GuiGraphics guiGraphics, int x, int y) {
    x = (int) (x * scale());
    y = (int) (y * scale());
    guiGraphics.pose().pushPose();
    guiGraphics.pose().scale(1/scale(), 1/scale(), 1/scale());
    guiGraphics.blit(emptyTexture, x, y, 0, 0, getWidth(), getHeight(), getWidth(), getHeight());
    var calcHeight = (int) (progress * getHeight());
    var offset = getHeight() - calcHeight;
    guiGraphics.blit(filledTexture, x + 1, y + offset + 1, 0, offset, getWidth(), calcHeight, getWidth(), getHeight());
    guiGraphics.pose().popPose();
  }

  @Override
  public int getWidth() {
    return TextureSizeHelper.getWidth(emptyTexture);
  }

  @Override
  public int getHeight() {
    return TextureSizeHelper.getHeight(emptyTexture);
  }

  @Override
  public void toBytes(RegistryFriendlyByteBuf buf) {
    buf.writeFloat(this.progress);
  }

  @Override
  public ResourceLocation getID() {
    return ID;
  }

  public static class FuelElementFactory implements IElementFactory {

    @Override
    public FuelElement createElement(RegistryFriendlyByteBuf buffer) {
      return new FuelElement(buffer);
    }

    @Override
    public ResourceLocation getId() {
      return ID;
    }
  }
}
