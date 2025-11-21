package es.degrassi.mmreborn.common.crafting.requirement.emi;

import com.google.common.collect.Lists;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.screen.EmiScreenManager;
import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.client.requirement.ItemRendering;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementLootTable;
import es.degrassi.mmreborn.common.machine.component.ItemComponent;
import es.degrassi.mmreborn.common.util.IOInventory;
import es.degrassi.mmreborn.common.util.LootTableHelper;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class EmiLootTableComponent extends EmiComponent<ResourceLocation,
    RecipeRequirement<ItemComponent, RequirementLootTable, IOInventory>> implements SlotTooltip,
    ItemRendering {
  private int item;
  @Getter
  private int width = 16, height = 16;
  @Getter
  private EmiRecipe recipe;
  private final List<ItemStack> loots;

  public EmiLootTableComponent(RecipeRequirement<ItemComponent, RequirementLootTable, IOInventory> requirement) {
    super(requirement, 36, 0);
    List<LootTableHelper.LootData> loots = LootTableHelper.getLootsForTable(requirement.requirement().getLootTable());
    this.loots = Lists.newArrayList(loots.stream().map(LootTableHelper.LootData::stack).toList());
  }

  public void recipeContext(EmiRecipe recipe) {
    this.recipe = recipe;
  }

  @Override
  public EmiStack getStack() {
    return EmiStack.of(loots().get(item));
  }

  @Override
  public List<ResourceLocation> ingredients() {
    return Collections.singletonList(requirement.requirement().getLootTable());
  }

  private List<ItemStack> loots() {
    return loots;
  }

  @Override
  public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    item = (int) (System.currentTimeMillis() / 1000 % loots().size());
    width += 2;
    height += 2;
    super.render(guiGraphics, mouseX, mouseY);
    width -= 2;
    height -= 2;
    drawStack(guiGraphics, 1, 1, -1);
  }

  @Override
  public List<Component> getTooltip() {
    List<Component> list = Lists.newArrayList();
    List<LootTableHelper.LootData> loots = LootTableHelper.getLootsForTable(requirement.requirement().getLootTable());
    LootTableHelper.LootData data = loots
        .stream()
        .filter(lootData -> ItemStack.isSameItemSameComponents(lootData.stack(), loots().get(item))).findFirst()
        .orElse(null);
    if (data == null)
      return list;
    if (data.chance() != 1) {
      double percentage = data.chance() * 100;
      if (percentage < 0.01F)
        list.add(Component.translatable("modular_machinery_reborn.ingredient.chance." + requirement.requirement().getMode().name().toLowerCase(Locale.ROOT), "<0.01", "%"));
      else {
        BigDecimal decimal = BigDecimal.valueOf(percentage).setScale(2, RoundingMode.HALF_UP);
        if (decimal.scale() <= 0 || decimal.signum() == 0 || decimal.stripTrailingZeros().scale() <= 0)
          list.add(Component.translatable("modular_machinery_reborn.ingredient.chance." + requirement.requirement().getMode().name().toLowerCase(Locale.ROOT), decimal.intValue(), "%"));
        else
          list.add(Component.translatable("modular_machinery_reborn.ingredient.chance." + requirement.requirement().getMode().name().toLowerCase(Locale.ROOT), decimal.doubleValue(), "%"));
      }
      if (!data.rolls().isEmpty())
        list.add(Component.literal(data.rolls()));
      if (!data.bonusRolls().isEmpty())
        list.add(Component.literal(data.bonusRolls()));
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
