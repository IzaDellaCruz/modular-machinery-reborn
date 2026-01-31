package es.degrassi.mmreborn.client.screen;

import com.google.common.collect.Lists;
import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.client.container.FluidHatchContainer;
import es.degrassi.mmreborn.client.screen.widget.GuiElement;
import es.degrassi.mmreborn.client.screen.widget.IGuiWrapper;
import es.degrassi.mmreborn.client.screen.widget.tabs.AutoInputTabWidget;
import es.degrassi.mmreborn.client.screen.widget.tabs.AutoOutputTabWidget;
import es.degrassi.mmreborn.client.screen.widget.tabs.ITabGroupScreen;
import es.degrassi.mmreborn.client.screen.widget.tabs.TabGroupWidget;
import es.degrassi.mmreborn.client.util.FluidRenderer;
import es.degrassi.mmreborn.client.util.GuiUtils;
import es.degrassi.mmreborn.common.entity.FluidInputHatchEntity;
import es.degrassi.mmreborn.common.entity.FluidOutputHatchEntity;
import es.degrassi.mmreborn.common.entity.base.FluidTankEntity;
import es.degrassi.mmreborn.common.manager.handler.slot.HybridTank;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.fluids.FluidStack;
import org.lwjgl.glfw.GLFW;

import java.util.List;

@Getter
public class FluidHatchScreen extends BaseScreen<FluidHatchContainer, FluidTankEntity> implements ITabGroupScreen {
  private TabGroupWidget tabs;
  private final List<FluidTankWidget> tanks = Lists.newArrayList();

  public FluidHatchScreen(FluidHatchContainer pMenu, Inventory pPlayerInventory, Component pTitle) {
    super(pMenu, pPlayerInventory, pTitle, false);
  }

  @Override
  protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
  }

  @Override
  protected void init() {
    super.init();

    tabs = TabGroupWidget.createRight(getGuiLeft() + getXSize(), getGuiTop());
    if (this.entity.getIoType().isInput()) tabs.addTab(new AutoInputTabWidget<>((FluidInputHatchEntity)this.entity));
    else tabs.addTab(new AutoOutputTabWidget<>((FluidOutputHatchEntity)this.entity));

    addRenderableWidget(tabs);

    int startX = 15, width = 20;
    for (HybridTank tank : entity.getTank().getInventory()) {
      tanks.add(addRenderableWidget(new FluidTankWidget(tank, startX + leftPos, 10 + topPos)));
      startX += width;
    }
  }

  @Override
  public ResourceLocation getTexture() {
    return ModularMachineryReborn.rl("textures/gui/guibar.png");
  }

  @Override
  protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
    // render image background:
    super.renderBg(guiGraphics, partialTick, mouseX, mouseY);
    renderSlots(guiGraphics);
  }

  @Override
  protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
    super.renderTooltip(guiGraphics, x, y);
    tanks.forEach(tank -> tank.renderTooltip(guiGraphics, x, y));
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    hasClicked = true;
    for (var element : children()) {
      if (element instanceof TabGroupWidget widget) {
        if (widget.mouseClicked(mouseX, mouseY, button)) return true;
      }
    }
    GuiEventListener clickedChild = GuiUtils.findChild(children(), mouseX, mouseY, button, GuiEventListener::mouseClicked);

    if (clickedChild != null) {
      setFocused(clickedChild);
      if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
        setDragging(true);
      }
      return super.mouseClicked(mouseX, mouseY, button);
    } else {
      //If we can't find a child, allow clearing whatever focus we currently have
      clearFocus();
    }
    return super.mouseClicked(mouseX, mouseY, button);
  }

  @Override
  public boolean mouseReleased(double mouseX, double mouseY, int button) {
    if (hasClicked) {
      // always pass mouse released events to windows for drag checks
      return super.mouseReleased(mouseX, mouseY, button);
    }
    return false;
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    return GuiUtils.checkChildren(children(), keyCode, scanCode, modifiers, (child, k, s, m) -> child instanceof GuiElement && child.keyPressed(k, s, m)) ||
        super.keyPressed(keyCode, scanCode, modifiers);
  }

  @Override
  public boolean charTyped(char c, int keyCode) {
    return GuiUtils.checkChildrenChar(children(), c, keyCode, (child, ch, k) -> child instanceof GuiElement && child.charTyped(ch, k)) || super.charTyped(c, keyCode);
  }

  private static class FluidTankWidget extends AbstractWidget {
    private final HybridTank tank;
    public FluidTankWidget(HybridTank tank, int x, int y) {
      super(x, y, 20, 61, Component.empty());
      this.tank = tank;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int x, int y, float v) {
      FluidRenderer.renderFluid(guiGraphics.pose(), this.getX(), this.getY(), width, height, tank.getValue(), tank.getCapacity());
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    public void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
      if (!isMouseOver(x, y)) return;
      List<Component> text = Lists.newArrayList();

      FluidStack content = tank.getValue();
      int amt;
      if (content.getAmount() <= 0) {
        text.add(Component.translatable("tooltip.fluidhatch.empty"));
        amt = 0;
      } else {
        text.add(content.getHoverName());
        amt = content.getAmount();
      }
      text.add(Component.translatable("tooltip.fluidhatch.tank", String.valueOf(amt), String.valueOf(tank.getCapacity())));

      Font font = Minecraft.getInstance().font;
      guiGraphics.renderTooltip(font, text.stream().map(Component::getVisualOrderText).toList(), x, y);
    }
  }
}
