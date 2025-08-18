package es.degrassi.mmreborn.common.integration.jei.ingredient;

import mezz.jei.api.ingredients.IIngredientType;

public class CustomIngredientTypes {
  private CustomIngredientTypes() {}
  public static final IIngredientType<Long> LONG = () -> Long.class;
  public static final IIngredientType<Integer> INTEGER = () -> Integer.class;
  public static final IIngredientType<Void> VOID = () -> Void.class;
}
