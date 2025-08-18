package es.degrassi.mmreborn.common.integration.jei.ingredient;

import es.degrassi.mmreborn.ModularMachineryReborn;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class VoidIngredientHelper implements IIngredientHelper<Void> {

  @Override
  public IIngredientType<Void> getIngredientType() {
    return CustomIngredientTypes.VOID;
  }

  @Override
  public String getDisplayName(Void long_) {
    return Component.translatable("modular_machinery_reborn.jei.ingredient.void", "").getString();
  }

  //Safe to remove
  @SuppressWarnings("removal")
  @Override
  public String getUniqueId(Void long_, UidContext context) {
    return long_.toString();
  }

  @Override
  public Object getUid(Void long_, UidContext context) {
    return long_.toString();
  }

  @Override
  public Void copyIngredient(Void long_) {
    return long_;
  }

  @Override
  public String getErrorInfo(@Nullable Void long_) {
    return "";
  }

  @Override
  public ResourceLocation getResourceLocation(Void ingredient) {
    return ModularMachineryReborn.rl("void");
  }
}
