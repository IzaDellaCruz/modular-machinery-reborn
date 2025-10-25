package es.degrassi.mmreborn.common.crafting.requirement.emi;

import dev.emi.emi.api.widget.AnimatedTextureWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.common.crafting.helper.Direction;
import es.degrassi.mmreborn.common.crafting.helper.IDirectionalRequirement;
import es.degrassi.mmreborn.common.crafting.helper.ProgressData;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementDuration;
import es.degrassi.mmreborn.common.integration.emi.recipe.MMREmiRecipe;
import es.degrassi.mmreborn.common.machine.component.DurationComponent;
import es.degrassi.mmreborn.common.util.TextureSizeHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EmiDurationComponent extends EmiComponent<Integer, RecipeRequirement<DurationComponent, RequirementDuration>>
    implements IDirectionalRequirement {
  private int duration;
  private final ProgressData progressData;
  private final int ticks;
  private final AnimatedTextureWidget progress;
  private final boolean inverted;
  public EmiDurationComponent(RecipeRequirement<DurationComponent, RequirementDuration> requirement, int msPerCycle,
                              ProgressData progressData, boolean inverted) {
    super(requirement, 0, 0, false);
    this.progressData = progressData;
    this.ticks = msPerCycle;
    this.progress = createProgress();
    this.inverted = inverted;
  }

  @Override
  public @Nullable ResourceLocation texture() {
    return progressData.getEmptyTexture();
  }

  public Direction getDirection() {
    return progressData.direction();
  }

  private AnimatedTextureWidget createProgress() {
    return new AnimatedTextureWidget(progressData.getFillTexture(),
        0,
        0,
        TextureSizeHelper.getWidth(progressData.getFillTexture()),
        TextureSizeHelper.getHeight(progressData.getFillTexture()),
        0,
        0,
        TextureSizeHelper.getWidth(progressData.getFillTexture()),
        TextureSizeHelper.getHeight(progressData.getFillTexture()),
        TextureSizeHelper.getWidth(progressData.getFillTexture()),
        TextureSizeHelper.getHeight(progressData.getFillTexture()),
        ticks,
        getDirection().horizontal(),
        getDirection().endToStart(),
        inverted
    );
  }

  @Override
  public List<Integer> ingredients() {
    return List.of(duration);
  }

  @Override
  public int getWidth() {
    return progress.getBounds().width();
  }

  @Override
  public int getHeight() {
    return progress.getBounds().height();
  }

  @Override
  public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    super.render(guiGraphics, mouseX, mouseY);
    progress.render(guiGraphics, mouseX, mouseY, 0);
  }

  @Override
  public List<Component> getTooltip() {
    List<Component> tooltip = super.getTooltip();
    tooltip.add(Component.translatable(
        "modular_machinery_reborn.jei.ingredient.duration",
        duration
    ));
    return tooltip;
  }

  @Override
  public void addWidgets(WidgetHolder widgets, MMREmiRecipe recipe) {
    this.duration = recipe.getRecipe().getRecipeTotalTickTime();
    super.addWidgets(widgets, recipe);
  }
}
