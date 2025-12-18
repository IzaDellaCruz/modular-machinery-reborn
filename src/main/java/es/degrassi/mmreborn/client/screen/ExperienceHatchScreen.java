package es.degrassi.mmreborn.client.screen;

import com.google.common.collect.Maps;
import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.client.container.ExperienceHatchContainer;
import es.degrassi.mmreborn.client.screen.widget.ExperienceButton;
import es.degrassi.mmreborn.api.client.ExperienceButtonType;
import es.degrassi.mmreborn.client.screen.widget.ExperienceWidget;
import es.degrassi.mmreborn.client.screen.widget.GuiElement;
import es.degrassi.mmreborn.client.screen.widget.IGuiWrapper;
import es.degrassi.mmreborn.client.screen.widget.tabs.AutoInputTabWidget;
import es.degrassi.mmreborn.client.screen.widget.tabs.AutoOutputTabWidget;
import es.degrassi.mmreborn.client.screen.widget.tabs.ITabGroupScreen;
import es.degrassi.mmreborn.client.screen.widget.tabs.TabGroupWidget;
import es.degrassi.mmreborn.client.util.GuiUtils;
import es.degrassi.mmreborn.common.entity.ExperienceInputHatchEntity;
import es.degrassi.mmreborn.common.entity.ExperienceOutputHatchEntity;
import es.degrassi.mmreborn.common.entity.base.ExperienceHatchEntity;
import es.degrassi.mmreborn.common.network.client.CExperienceButtonClickedPacket;
import es.degrassi.mmreborn.common.util.TextureSizeHelper;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ExperienceHatchScreen extends BaseScreen<ExperienceHatchContainer, ExperienceHatchEntity> implements IGuiWrapper, ITabGroupScreen {
  private ExperienceWidget experienceWidget;
  @Getter
  private TabGroupWidget tabs;
  private final Map<ExperienceButtonType, ExperienceButton> experienceButtons = Maps.newEnumMap(ExperienceButtonType.class);

  public ExperienceHatchScreen(ExperienceHatchContainer menu, Inventory playerInventory, Component title) {
    super(menu, playerInventory, title, false);
  }

  @Override
  @Nullable
  public ResourceLocation getTexture() {
    return ModularMachineryReborn.rl("textures/gui/guiexperience.png");
  }

  @Override
  protected void init() {
    super.init();

    tabs = TabGroupWidget.createLeft(getGuiLeft() - TextureSizeHelper.getWidth(AutoOutputTabWidget.TAB), getGuiTop());
    if (this.entity.getMode().isInput()) tabs.addTab(new AutoInputTabWidget<>((ExperienceInputHatchEntity)this.entity));
    else tabs.addTab(new AutoOutputTabWidget<>((ExperienceOutputHatchEntity)this.entity));
  }

  @Override
  protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
    super.renderBg(guiGraphics, partialTick, mouseX, mouseY);
    clearWidgets();
    experienceWidget = addRenderableWidget(new ExperienceWidget(getGuiLeft(), getGuiTop() + 20, imageWidth, getMenu().getEntity()));
    AtomicInteger x = new AtomicInteger(8 + getGuiLeft());
    if (getMenu().getEntity() instanceof ExperienceInputHatchEntity e) {
      for (ExperienceButtonType type : ExperienceButtonType.insertions()) {
        experienceButtons.put(
            type,
            new ExperienceButton(
                x.getAndAdd(18),
                45 + getGuiTop(),
                t -> PacketDistributor.sendToServer(new CExperienceButtonClickedPacket(e.getBlockPos(), t, t.extract())),
                type
            )
        );

        addRenderableWidget(experienceButtons.get(type));
      }
      x.getAndAdd(16);
    } else if (getMenu().getEntity() instanceof ExperienceOutputHatchEntity e) {
      x.set(getGuiLeft() + imageWidth - 8 - 17);
      for (ExperienceButtonType type : ExperienceButtonType.extractions().reversed()) {
        experienceButtons.put(
            type,
            new ExperienceButton(
                x.getAndAdd(-18),
                45 + getGuiTop(),
                t -> PacketDistributor.sendToServer(new CExperienceButtonClickedPacket(e.getBlockPos(), t, t.extract())),
                type
            )
        );

        addRenderableWidget(experienceButtons.get(type));
      }
    }

    if (tabs != null)
      addRenderableWidget(tabs);
    renderSlots(guiGraphics);
  }

  @Override
  protected void renderTooltip(@NotNull GuiGraphics guiGraphics, int x, int y) {
    super.renderTooltip(guiGraphics, x, y);

    if (experienceWidget.isHovered()) {
      guiGraphics.renderTooltip(font, experienceWidget.getTooltipMessage(), x, y);
    }

    for (ExperienceButton button : experienceButtons.values()) {
      if (button.isHovered()) {
        guiGraphics.renderTooltip(font, button.getTooltipMessage().stream().map(Component::getVisualOrderText).toList(), x, y);
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
