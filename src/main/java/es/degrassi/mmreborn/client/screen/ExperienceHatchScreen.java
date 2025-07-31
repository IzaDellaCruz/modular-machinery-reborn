package es.degrassi.mmreborn.client.screen;

import com.google.common.collect.Maps;
import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.client.container.ExperienceHatchContainer;
import es.degrassi.mmreborn.client.screen.widget.ExperienceButton;
import es.degrassi.mmreborn.client.screen.widget.ExperienceButtonType;
import es.degrassi.mmreborn.client.screen.widget.ExperienceWidget;
import es.degrassi.mmreborn.common.entity.ExperienceInputHatchEntity;
import es.degrassi.mmreborn.common.entity.ExperienceOutputHatchEntity;
import es.degrassi.mmreborn.common.entity.base.ExperienceHatchEntity;
import es.degrassi.mmreborn.common.network.client.CExperienceButtonClickedPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ExperienceHatchScreen extends BaseScreen<ExperienceHatchContainer, ExperienceHatchEntity> {
  private ExperienceWidget experienceWidget;
  private final Map<ExperienceButtonType, ExperienceButton> experienceButtons = Maps.newHashMap();

  public ExperienceHatchScreen(ExperienceHatchContainer menu, Inventory playerInventory, Component title) {
    super(menu, playerInventory, title, false);
  }

  @Override
  @Nullable
  public ResourceLocation getTexture() {
    return ModularMachineryReborn.rl("textures/gui/guiexperience.png");
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
}
