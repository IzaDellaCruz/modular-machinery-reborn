package es.degrassi.mmreborn.client.model.hatch;

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
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class HatchModelLoader implements IGeometryLoader<HatchModelLoader.HatchModelGeometry> {

  public static final HatchModelLoader INSTANCE = new HatchModelLoader();

  @Override
  public HatchModelGeometry read(JsonObject json, JsonDeserializationContext deserializationContext) {
    return new HatchModelGeometry();
  }

  public static class HatchModelGeometry implements IUnbakedGeometry<HatchModelGeometry> {
    @Override
    public @NotNull BakedModel bake(
        IGeometryBakingContext context,
        ModelBaker baker,
        Function<Material, TextureAtlasSprite> textureGetter,
        ModelState modelState,
        ItemOverrides itemOverrides
    ) {
      return new DefaultHatchBakedModel(baker, textureGetter);
    }
  }
}
