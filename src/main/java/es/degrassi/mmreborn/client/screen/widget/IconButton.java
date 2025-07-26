package es.degrassi.mmreborn.client.screen.widget;

import com.google.common.collect.Lists;
import es.degrassi.mmreborn.api.client.Blitter;
import es.degrassi.mmreborn.api.client.screen.TooltipRender;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@Getter
@Setter
@ParametersAreNonnullByDefault
public class IconButton extends Button implements TooltipRender {
  private boolean halfSize = false;
  private boolean disableClickSound = false;
  private boolean disableBackground = false;
  @Nullable
  private final Icon icon;

  private boolean renderTooltip = true;
  private List<Component> tooltips;

  public IconButton(int x, int y, Button.OnPress onPress) {
    super(x, y, 16, 16, Component.empty(), onPress, Button.DEFAULT_NARRATION);
    this.icon = null;
  }

  public IconButton(int x, int y, Icon icon, Button.OnPress onPress) {
    super(x, y, 16, 16, Component.empty(), onPress, Button.DEFAULT_NARRATION);
    this.icon = icon;
  }

  public IconButton(Button.Builder builder) {
    super(builder);
    this.icon = null;
  }

  public IconButton renderTooltip(boolean render) {
    this.renderTooltip = render;
    return this;
  }

  public IconButton setTooltips(Component... components) {
    this.tooltips = Lists.newArrayList(components);
    return this;
  }

  public void setVisibility(boolean vis) {
    this.visible = vis;
    this.active = vis;
  }

  public void playDownSound(SoundManager soundHandler) {
    if (!this.disableClickSound) {
      super.playDownSound(soundHandler);
    }
  }

  public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partial) {
    if (this.visible) {
      Icon icon = this.getIcon();
      Item item = this.getItemOverlay();
      if (this.halfSize) {
        this.width = 8;
        this.height = 8;
      }

      int yOffset = this.isHovered() ? 1 : 0;
      if (this.halfSize) {
        if (!this.disableBackground) {
          Icon.TOOLBAR_BUTTON_BACKGROUND.getBlitter().dest(this.getX(), this.getY()).zOffset(10).blit(guiGraphics);
        }

        if (item != null) {
          guiGraphics.renderItem(new ItemStack(item), this.getX(), this.getY(), 0, 20);
        } else if (icon != null) {
          Blitter blitter = icon.getBlitter();
          if (!this.active) {
            blitter.opacity(0.5F);
          }

          blitter.dest(this.getX(), this.getY()).zOffset(20).blit(guiGraphics);
        }
      } else {
        if (!this.disableBackground) {
          Icon bgIcon = this.isHovered() ? Icon.TOOLBAR_BUTTON_BACKGROUND_HOVER : (this.isFocused() ? Icon.TOOLBAR_BUTTON_BACKGROUND_FOCUS : Icon.TOOLBAR_BUTTON_BACKGROUND);
          bgIcon.getBlitter().dest(this.getX() - 1, this.getY() + yOffset, 18, 20).zOffset(2).blit(guiGraphics);
        }

        if (item != null) {
          guiGraphics.renderItem(new ItemStack(item), this.getX(), this.getY() + 1 + yOffset, 0, 3);
        } else if (icon != null) {
          icon.getBlitter().dest(this.getX(), this.getY() + 1 + yOffset).zOffset(3).blit(guiGraphics);
        }
      }
    }

  }

  @Override
  public boolean isMouseOver(double mouseX, double mouseY) {
    return getTooltipArea().contains((int) mouseX, (int) mouseY);
  }

  protected @Nullable Item getItemOverlay() {
    return null;
  }

  public List<Component> getTooltipMessage() {
    return tooltips;
  }

  public Rect2i getTooltipArea() {
    return new Rect2i(this.getX(), this.getY(), this.halfSize ? 8 : 16, this.halfSize ? 8 : 16);
  }

  public boolean isTooltipAreaVisible() {
    return this.visible;
  }

  @Override
  public void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    if (isMouseOver(mouseX, mouseY) && renderTooltip) {
      guiGraphics.renderTooltip(Minecraft.getInstance().font, getTooltipMessage().stream().map(Component::getVisualOrderText).toList(), mouseX, mouseY);
    }
  }

  @Override
  public String toString() {
    return "IconButton{" +
        "halfSize=" + halfSize +
        ", disableClickSound=" + disableClickSound +
        ", disableBackground=" + disableBackground +
        ", icon=" + icon +
        ", renderTooltip=" + renderTooltip +
        ", x=" + getX() +
        ", y=" + getY() +
        ", width=" + getWidth() +
        ", height=" + getHeight() +
        '}';
  }
}
