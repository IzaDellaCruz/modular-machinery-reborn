package es.degrassi.mmreborn.client.model.controller;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

import java.util.function.Function;

public class ControllerModelLoader implements IGeometryLoader<ControllerModelLoader.ControllerModelGeometry> {

  public static final ControllerModelLoader INSTANCE = new ControllerModelLoader();

  @Override
  public ControllerModelGeometry read(JsonObject json, JsonDeserializationContext deserializationContext) {
    return new ControllerModelGeometry();
  }

  public static class ControllerModelGeometry implements IUnbakedGeometry<ControllerModelGeometry> {

    @Override
    public BakedModel bake(IGeometryBakingContext iGeometryBakingContext, ModelBaker arg, Function<Material, TextureAtlasSprite> function, ModelState arg2, ItemOverrides arg3) {
      return new ControllerBakedModel();
    }
  }
}
