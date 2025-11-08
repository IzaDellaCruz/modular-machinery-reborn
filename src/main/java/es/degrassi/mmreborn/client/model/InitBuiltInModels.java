package es.degrassi.mmreborn.client.model;

import es.degrassi.mmreborn.ModularMachineryReborn;
import net.minecraft.client.resources.model.UnbakedModel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public final class InitBuiltInModels {
  private InitBuiltInModels() {}

  public static void init() {
  }

  private static <T extends UnbakedModel> void addBuiltInModel(String id, Supplier<T> modelFactory) {
    BuiltInModelHooks.addBuiltInModel(ModularMachineryReborn.rl(id), modelFactory.get());
  }
}
