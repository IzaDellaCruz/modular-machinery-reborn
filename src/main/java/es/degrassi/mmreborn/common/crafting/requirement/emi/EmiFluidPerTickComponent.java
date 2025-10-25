package es.degrassi.mmreborn.common.crafting.requirement.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.screen.EmiScreenManager;
import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.client.requirement.ChanceRendering;
import es.degrassi.mmreborn.client.requirement.FluidRendering;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementFluidPerTick;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.component.FluidComponent;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Getter
public class EmiFluidPerTickComponent extends EmiComponent<FluidStack, RecipeRequirement<FluidComponent,
    RequirementFluidPerTick>> implements SlotTooltip,
    FluidRendering, ChanceRendering {
  private EmiRecipe recipe;
  private int width = 16;
  private int height = 16;
  private int fluid;

  public EmiFluidPerTickComponent(RecipeRequirement<FluidComponent, RequirementFluidPerTick> requirement) {
    super(requirement, 0, 0);
  }

  @Override
  public List<FluidStack> ingredients() {
    return Arrays.stream(requirement.requirement().getIngredient().getFluids()).toList();
  }

  @Override
  public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    fluid = (int) (System.currentTimeMillis() / 1000 % ingredients().size());
    width += 2;
    height += 2;
    super.render(guiGraphics, mouseX, mouseY);
    width -= 2;
    height -= 2;
    renderFluid(guiGraphics);
    drawChance(guiGraphics, false);
  }

  @Override
  public float getChance() {
    return requirement.chance();
  }

  @Override
  public IOType getActionType() {
    return requirement.requirement().getMode();
  }

  @Override
  public List<Component> getTooltip() {
    List<Component> tooltip = new LinkedList<>();
    String mode = requirement.requirement().getMode().isInput() ? "input" : "output";
    tooltip.add(Component.translatable("modular_machinery_reborn.jei.ingredient.fluid." + mode,
        String.valueOf(requirement.requirement().getIngredient().ingredient()),
        requirement.requirement().getIngredient().amount()));
    tooltip.add(Component.translatable("modular_machinery_reborn.ingredient.perTick"));
    addChanceTooltips(tooltip);
    return tooltip;
  }

  @Override
  public void recipeContext(EmiRecipe recipe) {
    this.recipe = recipe;
  }

  @Override
  public EmiStack getStack() {
    return EmiStack.of(ingredients().get(fluid).getFluid(), requirement.requirement().getIngredient().amount());
  }

  @Override
  public boolean mouseClicked(int mouseX, int mouseY, int button) {
    if (slotInteraction(bind -> bind.matchesMouse(button))) {
      return true;
    }
    return EmiScreenManager.stackInteraction(new EmiStackInteraction(getStack(), getRecipe(), true),
        bind -> bind.matchesMouse(button));
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if (slotInteraction(bind -> bind.matchesKey(keyCode, scanCode))) {
      return true;
    }
    return EmiScreenManager.stackInteraction(new EmiStackInteraction(getStack(), getRecipe(), true),
        bind -> bind.matchesKey(keyCode, scanCode));
  }
}
