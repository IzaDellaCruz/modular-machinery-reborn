package es.degrassi.mmreborn.common.integration.jei.category;

import com.mojang.datafixers.util.Pair;
import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.common.crafting.MachineRecipe;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementDuration;
import es.degrassi.mmreborn.common.crafting.requirement.jei.JeiDurationComponent;
import es.degrassi.mmreborn.common.integration.jei.JeiComponentRegistry;
import es.degrassi.mmreborn.common.integration.jei.MMRJeiPlugin;
import es.degrassi.mmreborn.common.integration.jei.category.drawable.DrawableWrappedText;
import es.degrassi.mmreborn.common.machine.DynamicMachine;
import es.degrassi.mmreborn.common.registration.DataComponentRegistration;
import es.degrassi.mmreborn.common.registration.ItemRegistration;
import lombok.Getter;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.placement.IPlaceable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MMRRecipeCategory implements IRecipeCategory<MachineRecipe> {
  private DynamicMachine machine;
  private final String title;
  private final IDrawable background, icon;

  public final int initialX = 8, gap = 8;
  @Getter
  protected int width = 256, height = 256;

  public MMRRecipeCategory(DynamicMachine machine) {
    this.machine = machine;
    this.title = machine.getLocalizedName();
    this.background = MMRJeiPlugin.jeiHelpers.getGuiHelper().createBlankDrawable(256, 256);
    ItemStack stack = new ItemStack(ItemRegistration.CONTROLLER.get());
    stack.set(DataComponentRegistration.MACHINE_DATA, machine.getRegistryName());
    this.icon = MMRJeiPlugin.jeiHelpers.getGuiHelper().createDrawableIngredient(VanillaTypes.ITEM_STACK, stack);
  }

  public void updateMachine(DynamicMachine machine) {
    this.machine = machine;
  }

  @Nullable
  @Override
  @SuppressWarnings("removal")
  public IDrawable getBackground() {
    return background;
  }

  @Override
  public @NotNull RecipeType<MachineRecipe> getRecipeType() {
    return RecipeType.create(machine.getRegistryName().getNamespace(), machine.getRegistryName().getPath(), MachineRecipe.class);
  }

  @Override
  public @NotNull Component getTitle() {
    return Component.literal(title);
  }

  @Override
  public @Nullable IDrawable getIcon() {
    return icon;
  }

  @Override
  public void setRecipe(@NotNull IRecipeLayoutBuilder builder, MachineRecipe recipe, @NotNull IFocusGroup focuses) {
    this.width = recipe.getWidth();
    this.height = recipe.getHeight();
    recipe.textsToRender.clear();
    recipe.chanceTexts.clear();
    if (recipe.isShouldRenderProgress()) {
      new JeiDurationComponent(
          new RecipeRequirement<>(new RequirementDuration(recipe.getRecipeTotalTickTime(), recipe.getProgressPosition())),
          20, IDrawableAnimated.StartDirection.LEFT
      ).setRecipe(this, builder, recipe, focuses);
    }

    (recipe.getJeiRequirements().isEmpty() ? recipe.getRequirements() : recipe.getJeiRequirements())
        .stream()
        .filter(component -> JeiComponentRegistry.hasJeiComponent(component.getType()))
        .map(requirement -> requirement.castRequirement(requirement))
        .map(component -> JeiComponentRegistry.getJeiComponent(component.getType()).create(component))
        .forEach(requirement -> requirement.setRecipe(this, builder, recipe, focuses));
  }

  @Override
  public void createRecipeExtras(IRecipeExtrasBuilder builder, @NotNull MachineRecipe recipe, @NotNull IFocusGroup focuses) {
    IPlaceable<?> text = builder.addDrawable(
        new DrawableWrappedText(
            Lists.newArrayList(recipe.textsToRender.iterator()),
            recipe.getWidth() - 8,
            false
        )
    );
    text.setPosition(initialX, recipe.getHeight() - gap - text.getHeight());

    recipe.chanceTexts.stream()
        .map(Pair::getSecond)
        .map(DrawableWrappedText.class::cast)
        .forEach(builder::addDrawable);
  }
}
