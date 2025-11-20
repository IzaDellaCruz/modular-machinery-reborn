package es.degrassi.mmreborn.client.screen;

import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.client.container.EnergyHatchContainer;
import es.degrassi.mmreborn.client.screen.widget.GuiElement;
import es.degrassi.mmreborn.client.screen.widget.IGuiWrapper;
import es.degrassi.mmreborn.client.screen.widget.tabs.AutoInputTabWidget;
import es.degrassi.mmreborn.client.screen.widget.tabs.AutoOutputTabWidget;
import es.degrassi.mmreborn.client.screen.widget.tabs.ITabGroupScreen;
import es.degrassi.mmreborn.client.screen.widget.tabs.TabGroupWidget;
import es.degrassi.mmreborn.client.util.EnergyDisplayUtil;
import es.degrassi.mmreborn.client.util.GuiUtils;
import es.degrassi.mmreborn.common.entity.EnergyInputHatchEntity;
import es.degrassi.mmreborn.common.entity.EnergyOutputHatchEntity;
import es.degrassi.mmreborn.common.entity.base.EnergyHatchEntity;
import es.degrassi.mmreborn.common.util.TextureSizeHelper;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

@Getter
public class EnergyHatchScreen extends BaseScreen<EnergyHatchContainer, EnergyHatchEntity> implements IGuiWrapper, ITabGroupScreen {
  private TabGroupWidget tabs;
  public EnergyHatchScreen(EnergyHatchContainer pMenu, Inventory pPlayerInventory, Component pTitle) {
    super(pMenu, pPlayerInventory, pTitle, false);
  }

  @Override
  protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
  }

  @Override
  protected void init() {
    super.init();

    tabs = TabGroupWidget.createLeft(getGuiLeft() - TextureSizeHelper.getWidth(AutoOutputTabWidget.TAB), getGuiTop());
    if (this.entity.getMode().isInput()) tabs.addTab(new AutoInputTabWidget<>((EnergyInputHatchEntity) this.entity));
    else tabs.addTab(new AutoOutputTabWidget<>((EnergyOutputHatchEntity) this.entity));

    addRenderableWidget(tabs);
  }

  @Override
  public ResourceLocation getTexture() {
    return ModularMachineryReborn.rl("textures/gui/guibar.png");
  }

  @Override
  protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
    // render image background:
    super.renderBg(guiGraphics, partialTick, mouseX, mouseY);
    guiGraphics.pose().pushPose();
    float percFilled = ((float) entity.getCurrentEnergy()) / ((float) entity.getMaxEnergy());
    int pxFilled = Mth.ceil(percFilled * 61F);
    guiGraphics.blit(getTexture(), leftPos + 15,  topPos + 10 + 61 - pxFilled, 196, 61 - pxFilled, 20, pxFilled);
    guiGraphics.pose().popPose();
    renderSlots(guiGraphics);
  }

  @Override
  protected void renderTooltip(@NotNull GuiGraphics guiGraphics, int x, int y) {
    super.renderTooltip(guiGraphics, x, y);

    int offsetX = (this.width - this.getXSize()) / 2;
    int offsetZ = (this.height - this.getYSize()) / 2;

    if(x >= 15 + offsetX && x <= 35 + offsetX && y >= 10 + offsetZ && y <= 71 + offsetZ) {
      long currentEnergy = EnergyDisplayUtil.type.formatEnergyForDisplay(entity.getCurrentEnergy());
      long maxEnergy = EnergyDisplayUtil.type.formatEnergyForDisplay(entity.getMaxEnergy());

      Component text = Component.translatable("tooltip.energyhatch.charge",
          String.valueOf(currentEnergy),
          String.valueOf(maxEnergy),
          Component.translatable(EnergyDisplayUtil.type.getUnlocalizedFormat()));

      Font font = Minecraft.getInstance().font;
      guiGraphics.renderTooltip(font, text, x, y);
    }

    for (var element : children()) {
      if (element instanceof TabGroupWidget widget) {
        widget.renderTooltip(guiGraphics, x, y);
      }
    }
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    hasClicked = true;
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
    for (var element : children()) {
      if (element instanceof TabGroupWidget widget && widget.isMouseOver(mouseX, mouseY)) {
        widget.onClick(mouseX, mouseY, button);
      }
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
}
