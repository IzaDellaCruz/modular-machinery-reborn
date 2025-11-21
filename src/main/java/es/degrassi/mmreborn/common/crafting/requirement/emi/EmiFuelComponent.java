package es.degrassi.mmreborn.common.crafting.requirement.emi;

import dev.emi.emi.api.widget.AnimatedTextureWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import es.degrassi.mmreborn.api.capability.IFuelHandler;
import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.common.crafting.helper.Direction;
import es.degrassi.mmreborn.common.crafting.helper.FuelData;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementFuel;
import es.degrassi.mmreborn.common.integration.emi.recipe.MMREmiRecipe;
import es.degrassi.mmreborn.common.machine.component.FuelComponent;
import es.degrassi.mmreborn.common.util.TextureSizeHelper;
import es.degrassi.mmreborn.common.util.Utils;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Getter
public class EmiFuelComponent extends EmiComponent<Long, RecipeRequirement<FuelComponent, RequirementFuel, IFuelHandler>> {
  private final FuelData fuelData;
  private final AnimatedTextureWidget fuel;

  public EmiFuelComponent(RecipeRequirement<FuelComponent, RequirementFuel, IFuelHandler> requirement) {
    super(requirement, 0, 0, false);
    this.fuelData = requirement.requirement().displayData();
    this.fuel = createProgress();
  }

  @Override
  public int getWidth() {
    return fuel.getBounds().width();
  }

  @Override
  public int getHeight() {
    return fuel.getBounds().height();
  }

  @Override
  public List<Long> ingredients() {
    return Collections.singletonList(requirement.requirement().required);
  }

  @Override
  public @Nullable ResourceLocation texture() {
    return fuelData.getEmptyTexture();
  }

  public Direction getDirection() {
    return fuelData.direction();
  }

  private AnimatedTextureWidget createProgress() {
    return new AnimatedTextureWidget(fuelData.getFillTexture(),
        1,
        1,
        TextureSizeHelper.getWidth(fuelData.getFillTexture()),
        TextureSizeHelper.getHeight(fuelData.getFillTexture()),
        0,
        0,
        TextureSizeHelper.getWidth(fuelData.getFillTexture()),
        TextureSizeHelper.getHeight(fuelData.getFillTexture()),
        TextureSizeHelper.getWidth(fuelData.getFillTexture()),
        TextureSizeHelper.getHeight(fuelData.getFillTexture()),
        2000,
        getDirection().horizontal(),
        getDirection().endToStart(),
        true
    );
  }

  @Override
  public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    super.render(guiGraphics, mouseX, mouseY);
    fuel.render(guiGraphics, mouseX, mouseY, 0);
  }

  @Override
  public List<Component> getTooltip() {
    List<Component> tooltip = new LinkedList<>();
    tooltip.add(
        Component.translatable(
          "modular_machinery_reborn.jei.ingredient.fuel",
          Utils.format(requirement.requirement().required)
      )
    );
    return tooltip;
  }

  @Override
  public void addWidgets(WidgetHolder widgets, MMREmiRecipe recipe) {
    super.addWidgets(widgets, recipe);
  }
}
