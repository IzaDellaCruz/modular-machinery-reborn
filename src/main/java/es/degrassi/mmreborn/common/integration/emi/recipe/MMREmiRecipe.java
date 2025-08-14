package es.degrassi.mmreborn.common.integration.emi.recipe;

import com.google.common.collect.Lists;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.widget.WidgetHolder;
import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.api.integration.emi.Direction;
import es.degrassi.mmreborn.common.crafting.MachineRecipe;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementDuration;
import es.degrassi.mmreborn.common.crafting.requirement.emi.EmiDurationComponent;
import es.degrassi.mmreborn.common.integration.emi.EmiComponentRegistry;
import es.degrassi.mmreborn.common.integration.emi.EmiIngredientRegistry;
import es.degrassi.mmreborn.common.integration.emi.EmiStackRegistry;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MMREmiRecipe extends BasicEmiRecipe {
  @Getter
  private final MachineRecipe recipe;
  public final int initialX = 8, gap = 8;
  @Getter
  protected int width = 256, height = 256;

  public final List<FormattedText> textsToRender = Lists.newArrayList();

  public MMREmiRecipe(EmiRecipeCategory category, RecipeHolder<MachineRecipe> recipe) {
    super(category, recipe.id(), recipe.value().getWidth(), recipe.value().getHeight());
    this.recipe = recipe.value();
    this.inputs = this.recipe
        .getRequirements()
        .stream()
        .filter(requirement -> requirement.requirement().getMode().isInput())
        .filter(requirement -> EmiIngredientRegistry.hasEmiIngredient(requirement.getType()))
        .map(requirement -> requirement.castRequirement(requirement))
        .map(requirement -> EmiIngredientRegistry.getIngredient(requirement.getType()).create(requirement))
        .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    this.outputs = this.recipe
        .getRequirements()
        .stream()
        .filter(requirement -> !requirement.requirement().getMode().isInput())
        .filter(requirement -> EmiStackRegistry.hasEmiStack(requirement.getType()))
        .map(requirement -> requirement.castRequirement(requirement))
        .map(requirement -> EmiStackRegistry.getStack(requirement.getType()).create(requirement))
        .collect(ArrayList::new, ArrayList::addAll, ArrayList::addAll);
    this.catalysts = this.recipe
        .getRequirements()
        .stream()
        .filter(requirement -> requirement.requirement().getMode().isInput())
        .filter(requirement -> EmiStackRegistry.hasEmiStack(requirement.getType()))
        .map(requirement -> requirement.castRequirement(requirement))
        .map(requirement -> EmiStackRegistry.getStack(requirement.getType()).create(requirement))
        .collect(ArrayList::new, ArrayList::addAll, ArrayList::addAll);
  }

  @Override
  public void addWidgets(WidgetHolder widgets) {
    textsToRender.clear();
    if (recipe.isShouldRenderProgress()) {
      new EmiDurationComponent(
          new RecipeRequirement<>(
              new RequirementDuration(recipe.getRecipeTotalTickTime(), recipe.getProgressPosition())
          ),
          1000,
          Direction.LEFT,
          false
      ).addWidgets(widgets, this);
    }
    Font font = Minecraft.getInstance().font;

    (recipe.getJeiRequirements().isEmpty() ? recipe.getRequirements() : recipe.getJeiRequirements())
        .stream()
        .filter(component -> EmiComponentRegistry.hasEmiComponent(component.getType()))
        .map(requirement -> requirement.castRequirement(requirement))
        .map(component -> EmiComponentRegistry.getEmiComponent(component.getType()).create(component))
        .forEach(requirement -> requirement.addWidgets(widgets, this));

    Language language = Language.getInstance();
    AtomicInteger nextHeight = new AtomicInteger(0);
    AtomicInteger toRemove = new AtomicInteger(0);

    textsToRender.forEach(component -> {
      nextHeight.set(recipe.getHeight() - gap - font.wordWrapHeight(component, recipe.getWidth() - 8) - toRemove.get());
      widgets.addText(language.getVisualOrder(component), initialX, nextHeight.get(), 0xFF000000, false);
      toRemove.getAndAdd(font.wordWrapHeight(component, recipe.getWidth() - 8) + 2);
    });
  }
}
