package es.degrassi.mmreborn.client.screen.widget;

import com.google.common.collect.Lists;
import es.degrassi.mmreborn.client.container.ContainerBase;
import es.degrassi.mmreborn.client.screen.BaseScreen;
import es.degrassi.mmreborn.common.entity.base.ColorableMachineComponentEntity;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import java.util.List;

public abstract class ScrollableArea<
    S extends BaseScreen<M, E>,
    M extends ContainerBase<E>,
    E extends ColorableMachineComponentEntity
> extends AbstractWidget implements GuiEventListener {
  private static final WidgetSprites BACKGROUND_SPRITES = new WidgetSprites(ResourceLocation.withDefaultNamespace("widget/text_field"), ResourceLocation.withDefaultNamespace("widget/text_field_highlighted"));
  @Getter
  private final S screen;
  @Getter
  private final M menu;
  private final int contentHeight;

  private boolean scrolling;
  private double scrollAmount;

  protected final List<Renderable> renderables = Lists.newArrayList();
  private final List<GuiEventListener> children = Lists.newArrayList();
  private final List<NarratableEntry> narratables = Lists.newArrayList();

  public ScrollableArea(
      int x,
      int y,
      int width,
      int height,
      int contentHeight,
      S screen,
      M menu
  ) {
    super(x, y, width, height, Component.empty());
    this.screen = screen;
    this.menu = menu;
    this.contentHeight = contentHeight;
  }

  public final double scrollAmount() {
    return scrollAmount;
  }

  protected final int innerHeight() {
    return contentHeight;
  }

  protected double scrollRate() {
    return BaseScreen.SLOT_SIZE;
  }

  @Override
  protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    if (!this.visible) return;

    this.renderBackground(guiGraphics);
    guiGraphics.enableScissor(this.getX(), this.getY(), this.getX() + width, this.getY() + height);
    guiGraphics.pose().pushPose();
    this.renderContents(guiGraphics, mouseX, mouseY, partialTick);
    guiGraphics.pose().popPose();
    guiGraphics.disableScissor();
    if (this.scrollbarVisible())
      this.renderScrollbar(guiGraphics, mouseX, mouseY, partialTick);
  }

  protected abstract void init();

  protected <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T widget) {
    this.renderables.add(widget);
    return this.addWidget(widget);
  }

  protected <T extends Renderable> T addRenderableOnly(T renderable) {
    this.renderables.add(renderable);
    return renderable;
  }

  protected <T extends GuiEventListener & NarratableEntry> T addWidget(T listener) {
    this.children.add(listener);
    this.narratables.add(listener);
    return listener;
  }

  protected void removeWidget(GuiEventListener listener) {
    if (listener instanceof Renderable ren) {
      this.renderables.remove(ren);
    }
    if (listener instanceof NarratableEntry nat) {
      this.narratables.remove(nat);
    }
    this.children.remove(listener);
  }

  protected void clearWidgets() {
    this.renderables.clear();
    this.children.clear();
    this.narratables.clear();
  }

  private void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    guiGraphics.pose().translate(screen.getGuiLeft(), screen.getGuiTop(), 0);
    for (var renderable : renderables) {
      renderable.render(guiGraphics, mouseX, mouseY, partialTick);
    }
  }

  protected void renderBackground(GuiGraphics guiGraphics) {
    this.renderBorder(guiGraphics);
  }

  private int getScrollBarHeight() {
    return Mth.clamp((int)((float)(this.height * this.height) / (float)this.innerHeight()), 32, this.height - BaseScreen.getScrollbarHeight());
  }

  protected void setScrollAmount(double scrollAmount) {
    this.scrollAmount = Mth.clamp(scrollAmount, 0.0F, this.getMaxScrollAmount());
  }

  protected int getMaxScrollAmount() {
    return Math.max(0, this.innerHeight() - this.height - 1);
  }

  protected void renderBorder(GuiGraphics guiGraphics) {
    ResourceLocation resourcelocation = BACKGROUND_SPRITES.get(this.isActive(), this.isFocused());
    guiGraphics.blitSprite(resourcelocation, getX(), getY(), width, height);
  }

  protected void renderScrollbar(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    int scrollbarX = getX() + width - BaseScreen.getScrollbarBackgroundWidth() * 5/2;
    int scrollbarY = getY() + 2;
    int scrollbarHeight = height;

    // Render scrollbar background track
    guiGraphics.blitSprite(
        BaseScreen.SCROLLBAR_BACKGROUND,
        scrollbarX,
        scrollbarY,
        0,
        BaseScreen.getScrollbarWidth(),
        scrollbarHeight
    );

    // Calculate thumb position and size
    int maxScroll = innerHeight() - height;
    if (maxScroll > 0) {
      final int thumbHeight = Math.max(20, (height * height) / innerHeight());
      int thumbY = scrollbarY + (int)((height - thumbHeight) * (scrollAmount() / maxScroll));

      guiGraphics.blitSprite(
          BaseScreen.SCROLLBAR_THUMB,
          scrollbarX,
          thumbY,
          0,
          BaseScreen.getScrollbarWidth(),
          thumbHeight
      );
    }
  }

  public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
    if (!this.visible) {
      return false;
    } else {
      this.setScrollAmount(this.scrollAmount - scrollY * this.scrollRate());
      return this.children.stream().anyMatch(child -> child.mouseScrolled(mouseX, mouseY, scrollX, scrollY));
    }
  }

  @Override
  protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    this.narratables.forEach(child -> child.updateNarration(narrationElementOutput));
  }

  protected boolean scrollbarVisible() {
    return this.innerHeight() > this.getHeight();
  }

  protected boolean withinContentAreaTopBottom(int top, int bottom) {
    return (double)bottom - this.scrollAmount >= (double)this.getY() && (double)top - this.scrollAmount <= (double)(this.getY() + this.height);
  }

  protected boolean withinContentAreaPoint(double x, double y) {
    return x >= (double)this.getX() && x < (double)(this.getX() + this.width) && y >= (double)this.getY() && y < (double)(this.getY() + this.height);
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (!this.visible) return false;

    boolean flag = this.withinContentAreaPoint(mouseX, mouseY);
    boolean flag1 = this.scrollbarVisible() && mouseX >= (this.getX() + this.width) && mouseX <= (getX() + this.width + getScrollBarWidth()) && mouseY >= getY() && mouseY < (getY() + height);

    if (flag1 && button == 0) {
      this.scrolling = true;
      return true;
    } else {
      return flag || flag1 || this.children.stream().anyMatch(child -> child.mouseClicked(mouseX, mouseY, button)) || super.mouseClicked(mouseX, mouseY, button);
    }
  }

  @Override
  public boolean mouseReleased(double mouseX, double mouseY, int button) {
    if (button == 0) {
      this.scrolling = false;
    }
    return this.children.stream().anyMatch(child -> child.mouseReleased(mouseX, mouseY, button)) || super.mouseReleased(mouseX, mouseY, button);
  }

  @Override
  public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
    if (this.visible && this.isFocused() && this.scrolling) {
      if (mouseY < (double)this.getY()) {
        this.setScrollAmount((double)0.0F);
      } else if (mouseY > (double)(this.getY() + this.height)) {
        this.setScrollAmount((double)this.getMaxScrollAmount());
      } else {
        int i = this.getScrollBarHeight();
        double d0 = (double)Math.max(1, this.getMaxScrollAmount() / (this.height - i));
        this.setScrollAmount(this.scrollAmount + dragY * d0);
      }

      return true;
    } else {
      return this.children.stream().anyMatch(child -> child.mouseDragged(mouseX, mouseY, button, dragX, dragY)) || super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    boolean flag = keyCode == 265;
    boolean flag1 = keyCode == 264;
    if (flag || flag1) {
      double d0 = this.scrollAmount;
      this.setScrollAmount(this.scrollAmount + (flag ? -1 : 1) * this.scrollRate());
      if (d0 != this.scrollAmount) {
        return true;
      }
    }

    return this.children.stream().anyMatch(child -> child.keyPressed(keyCode, scanCode, modifiers)) || super.keyPressed(keyCode, scanCode, modifiers);
  }

  protected int getScrollBarWidth() {
    return BaseScreen.getScrollbarBackgroundWidth();
  }
}
