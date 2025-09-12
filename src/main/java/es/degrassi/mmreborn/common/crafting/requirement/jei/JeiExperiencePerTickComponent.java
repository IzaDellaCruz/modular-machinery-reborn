package es.degrassi.mmreborn.common.crafting.requirement.jei;

import es.degrassi.experiencelib.util.ExperienceUtils;
import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.common.crafting.MachineRecipe;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementExperience;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementExperiencePerTick;
import es.degrassi.mmreborn.common.integration.jei.category.MMRRecipeCategory;
import es.degrassi.mmreborn.common.machine.component.ExperienceComponent;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class JeiExperiencePerTickComponent extends JeiComponent<Long, RecipeRequirement<ExperienceComponent,
    RequirementExperiencePerTick>> {
  public JeiExperiencePerTickComponent(RecipeRequirement<ExperienceComponent, RequirementExperiencePerTick> requirement) {
    super(requirement, 0, 0);
  }

  @Override
  public int getWidth() {
    return 0;
  }

  @Override
  public int getHeight() {
    return 0;
  }

  @Override
  public void render(GuiGraphics guiGraphics, @NotNull Long ingredient) {
    super.render(guiGraphics, ingredient);
  }

  @Override
  @SuppressWarnings("removal")
  public @NotNull List<Component> getTooltip(@NotNull Long ingredient, @NotNull TooltipFlag tooltipFlag) {
    return super.getTooltip(ingredient, tooltipFlag);
  }

  @Override
  public List<Long> ingredients() {
    return Collections.singletonList(requirement.requirement().getRequired());
  }

  @Override
  public void setRecipe(MMRRecipeCategory category, IRecipeLayoutBuilder builder, MachineRecipe recipe, IFocusGroup focuses) {
    String literal = String.format("%s XP/t", ExperienceUtils.format(requirement.requirement().getRequired()));
    String level =  ExperienceUtils.format(ExperienceUtils.getLevelFromXp(requirement.requirement().getRequired()));
    recipe.textsToRender.add(
        Component.translatable("mmr.gui.element.experience.tooltip." + requirement.requirement().getMode().getSerializedName(),
            literal,
            Component.translatable("mmr.gui.element.experience.level", level)
        )
    );
  }
}
