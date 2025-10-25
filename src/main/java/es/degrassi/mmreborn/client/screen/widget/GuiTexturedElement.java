package es.degrassi.mmreborn.client.screen.widget;

import es.degrassi.mmreborn.client.screen.BaseScreen;
import lombok.AccessLevel;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;

public abstract class GuiTexturedElement extends GuiElement {
  @Getter(AccessLevel.PROTECTED)
  protected final ResourceLocation resource;

  public GuiTexturedElement(ResourceLocation resource, BaseScreen<? ,?> gui, int x, int y, int width, int height) {
    super(gui, x, y, width, height);
    this.resource = resource;
  }
}
