package es.degrassi.mmreborn.client.screen.popup;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public abstract class PopupScreen<T extends AbstractContainerMenu> extends BasePopupScreen<T> {

  public final BasePopupScreen<T> parent;

  private boolean dragging;
  private double dragX;
  private double dragY;

  protected PopupScreen(BasePopupScreen<T> parent, int xSize, int ySize) {
    super(parent.getMenu(), parent.getPlayerInventory(), Component.literal("Popup"), xSize, ySize);
    this.parent = parent;
  }

  public void move(int movedX, int movedY) {
    this.x += movedX;
    this.y += movedY;
    this.children().forEach(c -> {
      if (c instanceof LayoutElement widget)
        widget.setPosition(widget.getX() + movedX, widget.getY() + movedY);
    });
  }

  @Override
  protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
    if ((int) Math.abs(this.dragX) >= 1 || (int) Math.abs(this.dragY) >= 1) {
      int changedX = (int) this.dragX;
      int changedY = (int) this.dragY;
      this.move(changedX, changedY);
      this.dragX -= changedX;
      this.dragY -= changedY;
    }
    boolean parentHasTooltip = this.parent.deferredTooltipRendering != null;
    for (Renderable renderable : this.renderables) {
      renderable.render(graphics, mouseX, mouseY, partialTicks);
    }
    //If parent didn't have tooltip before rendering this popup, but now have it that means the tooltips were added by this popup.
    //So we move them to this popup tooltips
    if (!parentHasTooltip && this.parent.deferredTooltipRendering != null) {
      if (this.deferredTooltipRendering == null)
        this.deferredTooltipRendering = this.parent.deferredTooltipRendering;
      this.parent.deferredTooltipRendering = null;
    }

    renderTooltip(graphics, mouseX, mouseY);
  }

  @Override
  public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
    blankBackground(graphics, this.x, this.y, this.xSize, this.ySize);
    renderBg(graphics, partialTicks, mouseX, mouseY);
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    boolean onRenderable = false;
    for (GuiEventListener renderable : this.children()) {
      if (renderable.mouseClicked(mouseX, mouseY, button)) {
        onRenderable = true;
        break;
      }
    }
    if (onRenderable || super.mouseClicked(mouseX, mouseY, button))
      return true;
    if (isMouseOver(mouseX, mouseY) && mouseY < this.y + 20) {
      this.dragging = true;
      return true;
    } else {
      this.setDragging(false);
      return false;
    }
  }

  @Override
  public boolean mouseReleased(double mouseX, double mouseY, int button) {
    boolean onRenderable = false;
    for (GuiEventListener renderable : this.children()) {
      if (renderable.mouseReleased(mouseX, mouseY, button)) {
        onRenderable = true;
        break;
      }
    }
    this.dragging = false;
    return onRenderable || super.mouseReleased(mouseX, mouseY, button);
  }

  @Override
  public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
    if (this.dragging) {
      this.dragX += deltaX;
      this.dragY += deltaY;
    }
    return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
  }

  @Override
  public boolean isMouseOver(double mouseX, double mouseY) {
    boolean onRenderable = false;
    for (GuiEventListener renderable : this.children()) {
      if (renderable.isMouseOver(mouseX, mouseY)) {
        onRenderable = true;
        break;
      }
    }
    return onRenderable || this.parent.getPopupUnderMouse(mouseX, mouseY) == this;
  }
}
