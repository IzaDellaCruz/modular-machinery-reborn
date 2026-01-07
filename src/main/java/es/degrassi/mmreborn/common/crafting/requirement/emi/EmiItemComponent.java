package es.degrassi.mmreborn.common.crafting.requirement.emi;

import com.google.common.collect.Lists;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.screen.EmiScreenManager;
import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.client.requirement.ChanceRendering;
import es.degrassi.mmreborn.client.requirement.ItemRendering;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementItem;
import es.degrassi.mmreborn.common.integration.emi.EmiIngredientRegistry;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.component.ItemComponent;
import es.degrassi.mmreborn.common.manager.handler.ItemHandler;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public class EmiItemComponent extends EmiComponent<ItemStack, RecipeRequirement<ItemComponent, RequirementItem, ItemHandler>> implements SlotTooltip, ItemRendering, ChanceRendering, IngredientHolder {
  private int item;
  @Getter
  private int width = 16, height = 16;
  @Getter
  @Nullable
  private EmiRecipe recipe;
  public EmiItemComponent(RecipeRequirement<ItemComponent, RequirementItem, ItemHandler> requirement) {
    super(requirement, 36, 0);
  }

  public void recipeContext(EmiRecipe recipe) {
    this.recipe = recipe;
  }

  @Override
  public EmiStack getStack() {
    return EmiStack.of(ingredients().get(item).copyWithCount(requirement.requirement().getIngredient().count()));
  }

  @Override
  public EmiIngredient getIngredient() {
    return EmiIngredientRegistry.create(requirement);
  }

  @Override
  public List<ItemStack> ingredients() {
    return getIngredient().getEmiStacks().stream().map(EmiStack::getItemStack).toList();
  }

  @Override
  public Bounds getBounds() {
    return super.getBounds();
  }

  @Override
  public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    item = (int) (System.currentTimeMillis() / 1000 % ingredients().size());
    width += 2;
    height += 2;
    super.render(guiGraphics, mouseX, mouseY);
    width -= 2;
    height -= 2;
    drawStack(guiGraphics, 1, 1, -1);
    drawChance(guiGraphics, true);
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
    List<Component> list = Lists.newArrayList();
    addChanceTooltips(list);
    if (getStack().isEmpty()) {
      return list;
    }
    list.addAll(getStack().getTooltipText());
    return list;
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
