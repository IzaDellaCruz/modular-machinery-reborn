package es.degrassi.mmreborn.common.crafting.requirement.emi;

import dev.emi.emi.api.widget.WidgetHolder;
import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.common.integration.emi.EmiEmptyRequirementRegistry;
import es.degrassi.mmreborn.common.integration.emi.recipe.MMREmiRecipe;
import es.degrassi.mmreborn.common.util.EmptyRequirementType;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementEmpty;
import es.degrassi.mmreborn.common.machine.component.EmptyComponent;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public class EmiEmptyComponent extends EmiComponent<Void, RecipeRequirement<EmptyComponent, RequirementEmpty>> {
  private int width;
  private int height;
  private final EmptyRequirementType type;
  public EmiEmptyComponent(RecipeRequirement<EmptyComponent, RequirementEmpty> requirement) {
    super(
        requirement,
        requirement.requirement().getRequirementType().getUOffset(),
        requirement.requirement().getRequirementType().getVOffset()
    );
    this.type = requirement.requirement().getRequirementType();
    this.width = requirement.requirement().getRequirementType().getWidth();
    this.height = requirement.requirement().getRequirementType().getHeight();
  }

  @Override
  public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    width += 2;
    height += 2;
    super.render(guiGraphics, mouseX, mouseY);
    width -= 2;
    height -= 2;
  }

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public int getHeight() {
    return height;
  }

  @Override
  public List<Void> ingredients() {
    return List.of();
  }

  @Override
  public void addWidgets(WidgetHolder widgets, MMREmiRecipe recipe) {
    if (EmiEmptyRequirementRegistry.hasEmiConsumer(type)) {
      EmiEmptyRequirementRegistry.getConsumer(type).execute(this, widgets, recipe);
    }
  }
}
