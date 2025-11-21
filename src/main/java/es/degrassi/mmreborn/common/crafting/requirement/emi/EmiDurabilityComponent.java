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
import es.degrassi.mmreborn.common.crafting.requirement.RequirementDurability;
import es.degrassi.mmreborn.common.integration.emi.EmiIngredientRegistry;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.component.DurabilityComponent;
import es.degrassi.mmreborn.common.util.IOInventory;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class EmiDurabilityComponent extends EmiComponent<ItemStack, RecipeRequirement<DurabilityComponent,
    RequirementDurability, IOInventory>> implements SlotTooltip, ItemRendering, IngredientHolder {
  private int item;
  @Getter
  private int width = 16, height = 16;
  @Getter
  @Nullable
  private EmiRecipe recipe;
  private final EmiIngredient ingredient;
  private final List<ItemStack> items;
  public EmiDurabilityComponent(RecipeRequirement<DurabilityComponent, RequirementDurability, IOInventory> requirement) {
    super(requirement, 36, 0);
    this.ingredient = EmiIngredientRegistry.getIngredient(requirement.getType()).create(requirement);
    this.items = generateWithDurability(requirement.requirement().ingredient);
  }

  private List<ItemStack> generateWithDurability(Ingredient original) {
    return Arrays.stream(original.getItems())
        .map(this::generateWithDurability)
        .flatMap(List::stream)
        .unordered()
        .toList();
  }

  private List<ItemStack> generateWithDurability(ItemStack stack) {
    if (!stack.isDamageableItem()) throw new IllegalArgumentException("Invalid not damageable item in durability requirement");
    int maxDamage = stack.getMaxDamage();
    List<ItemStack> damagedItems = Lists.newArrayList();
    if (maxDamage <= 10) {
      for (int i = 0; i <= maxDamage; i++) {
        ItemStack copy = stack.copy();
        copy.setDamageValue(i);
        damagedItems.add(copy);
      }
    } else {
      for (int i = 0; i <= 10; i++) {
        ItemStack copy = stack.copy();
        copy.setDamageValue(Mth.randomBetweenInclusive(RandomSource.create(), 0, maxDamage));
        damagedItems.add(copy);
      }
    }
    return damagedItems.stream().unordered().toList();
  }

  public void recipeContext(EmiRecipe recipe) {
    this.recipe = recipe;
  }

  @Override
  public EmiStack getStack() {
    return EmiStack.of(ingredients().get(item));
  }

  @Override
  public EmiIngredient getIngredient() {
    return ingredient;
  }

  @Override
  public List<ItemStack> ingredients() {
    return items;
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
  }

  public IOType getActionType() {
    return requirement.requirement().getMode();
  }

  @Override
  public List<Component> getTooltip() {
    List<Component> list = Lists.newArrayList();
    if (getStack().isEmpty()) {
      return list;
    }
    list.addAll(getStack().getTooltipText());
    if(getActionType().isInput())
      list.add(Component.translatable("modular_machinery_reborn.ingredient.durability.consume", this.requirement.requirement().getAmount()));
    else if(getActionType() == IOType.OUTPUT)
      list.add(Component.translatable("modular_machinery_reborn.ingredient.durability.repair", this.requirement.requirement().getAmount()));
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
