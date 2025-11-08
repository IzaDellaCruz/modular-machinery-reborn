package es.degrassi.mmreborn.client.model;

import es.degrassi.mmreborn.ModularMachineryReborn;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class BuiltInModelHooks {
  private static final Map<ResourceLocation, UnbakedModel> builtInModels = new HashMap<>();

  public static void addBuiltInModel(ResourceLocation id, UnbakedModel model) {
    if (builtInModels.put(id, model) != null) {
      throw new IllegalArgumentException("Duplicate built.in model ID: " + id);
    }
  }

  @Nullable
  public static UnbakedModel getBuiltInModel(ResourceLocation id) {
    if (!ModularMachineryReborn.MODID.equals(id.getNamespace())) {
      return null;
    }
    return builtInModels.get(id);
  }
}
