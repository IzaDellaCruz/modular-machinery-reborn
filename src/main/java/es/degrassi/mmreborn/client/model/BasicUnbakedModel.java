package es.degrassi.mmreborn.client.model;

import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

public interface BasicUnbakedModel extends UnbakedModel {
  @Override
  default Collection<ResourceLocation> getDependencies() {
    return Collections.emptyList();
  }

  @Override
  default void resolveParents(Function<ResourceLocation, UnbakedModel> function) {
    for (ResourceLocation dependency : getDependencies()) {
      function.apply(dependency).resolveParents(function);
    }
  }
}
