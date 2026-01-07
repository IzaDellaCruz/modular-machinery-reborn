package es.degrassi.mmreborn.common.util;

import es.degrassi.mmreborn.common.crafting.MachineRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.Comparator;

public final class Comparators {
  private Comparators() {}

  private static final Comparator<MachineRecipe> RECIPE_PRIORITY_COMPARATOR = Comparator.comparingInt(MachineRecipe::getConfiguredPriority).reversed();

  public static int compare(RecipeHolder<MachineRecipe> holder1, RecipeHolder<MachineRecipe> holder2) {
    return RECIPE_PRIORITY_COMPARATOR.compare(holder1.value(), holder2.value());
  }
}
