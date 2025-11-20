package es.degrassi.mmreborn.client.screen.widget;

import com.google.common.collect.Lists;
import es.degrassi.mmreborn.api.client.Blitter;
import es.degrassi.mmreborn.api.client.Icon;
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
public class ItemOrIconButton extends Button implements TooltipRender {
  private boolean halfSize = false;
  private boolean disableClickSound = false;
  private boolean disableBackground = false;
  @Nullable
  private final Item item;
  @Nullable
  private final Icon icon;

  private boolean renderTooltip = true;
  private List<Component> tooltips;

  public ItemOrIconButton(int x, int y, OnPress onPress) {
    super(x, y, 16, 16, Component.empty(), onPress, Button.DEFAULT_NARRATION);
    this.item = null;
    this.icon = null;
  }

  public ItemOrIconButton(int x, int y, Item item, OnPress onPress) {
    super(x, y, 16, 16, Component.empty(), onPress, Button.DEFAULT_NARRATION);
    this.item = item;
    this.icon = null;
  }

  public ItemOrIconButton(int x, int y, Icon icon, OnPress onPress) {
    super(x, y, 16, 16, Component.empty(), onPress, Button.DEFAULT_NARRATION);
    this.item = null;
    this.icon = icon;
  }

  public ItemOrIconButton(Builder builder) {
    super(builder);
    this.item = null;
    this.icon = null;
  }

  public ItemOrIconButton renderTooltip(boolean render) {
    this.renderTooltip = render;
    return this;
  }

  public ItemOrIconButton setTooltips(Component... components) {
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
      Item item = this.getItem();
      if (this.halfSize) {
        this.width = getWidth() / 2;
        this.height = getHeight() / 2;
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

  public List<Component> getTooltipMessage() {
    return getTooltips();
  }

  public Rect2i getTooltipArea() {
    return new Rect2i(this.getX(), this.getY(), this.halfSize ? getWidth() / 2 : getWidth(), this.halfSize ? getHeight() / 2 : getHeight());
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
    return "ItemButton{" +
        "halfSize=" + halfSize +
        ", disableClickSound=" + disableClickSound +
        ", disableBackground=" + disableBackground +
        ", item=" + (getItem() == null ? "null" : getItem()) +
        ", icon=" + (getIcon() == null ? "null" : getIcon()) +
        ", renderTooltip=" + renderTooltip +
        ", x=" + getX() +
        ", y=" + getY() +
        ", width=" + getWidth() +
        ", height=" + getHeight() +
        '}';
  }
}
