package es.degrassi.mmreborn.client.model.casing;

import es.degrassi.mmreborn.client.model.BasicUnbakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class CasingModel implements BasicUnbakedModel {
  @Override
  public @Nullable BakedModel bake(ModelBaker modelBaker, Function<Material, TextureAtlasSprite> function, ModelState modelState) {
    return new CasingBakedModel(function);
  }
}
