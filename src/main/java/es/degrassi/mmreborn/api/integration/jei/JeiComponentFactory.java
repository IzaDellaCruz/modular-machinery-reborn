package es.degrassi.mmreborn.api.integration.jei;

import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.common.crafting.requirement.jei.JeiComponent;

@FunctionalInterface
public interface JeiComponentFactory<R extends RecipeRequirement<?, ?, ?>, X> {
  JeiComponent<X, R> create(R requirement);
}
