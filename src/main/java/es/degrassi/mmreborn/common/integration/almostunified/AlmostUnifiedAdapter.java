package es.degrassi.mmreborn.common.integration.almostunified;

import com.almostreliable.unified.api.AlmostUnified;
import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.common.crafting.MachineRecipe;
import es.degrassi.mmreborn.common.util.Mods;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.Optional;

public class AlmostUnifiedAdapter {
  private AlmostUnifiedAdapter() {}
  public static boolean isLoaded() {
    return Mods.isAULoaded();
  }

  public static boolean isRecipeModified(MachineRecipe recipe) {
    return recipe.getRequirements().stream().anyMatch(RecipeRequirement::isModified) || recipe.isModified();
  }

  public static Item getPreferredItemForTag(TagKey<Item> tag) {
    Optional<Item> optional = Optional.empty();
    if (isLoaded()) {
      optional = Adapter.getPreferredItemForTag(tag);
    }

    return optional.orElse(null);
  }

  public static Item getPreferredItemForItem(Holder<Item> item) {
    Optional<Item> optional = Optional.empty();
    if (isLoaded()) {
      optional = Adapter.getPreferredItemForItem(item);
    }

    return optional.orElse(null);
  }

  public static TagKey<Item> getRelevantItemTag(Holder<Item> item) {
    Optional<TagKey<Item>> optional = Optional.empty();
    if (isLoaded()) {
      optional = Adapter.getRelevantItemTag(item);
    }

    return optional.orElse(null);
  }

  private static class Adapter {
    public static Optional<Item> getPreferredItemForTag(TagKey<Item> tag) {
      return Optional.ofNullable(AlmostUnified.INSTANCE.getTagTargetItem(tag));
    }

    public static Optional<TagKey<Item>> getRelevantItemTag(Holder<Item> item) {
      return Optional.ofNullable(AlmostUnified.INSTANCE.getRelevantItemTag(item.value()));
    }

    public static Optional<Item> getPreferredItemForItem(Holder<Item> item) {
      return Optional.ofNullable(AlmostUnified.INSTANCE.getVariantItemTarget(item.value()));
    }
  }
}
