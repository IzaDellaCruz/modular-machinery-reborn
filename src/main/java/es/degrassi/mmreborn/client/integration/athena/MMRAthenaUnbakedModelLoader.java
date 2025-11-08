package es.degrassi.mmreborn.client.integration.athena;

import com.google.gson.JsonObject;
import earth.terrarium.athena.api.client.models.AthenaBlockModel;
import earth.terrarium.athena.api.client.models.AthenaModelFactory;
import earth.terrarium.athena.api.client.models.NotNullUnbakedModel;
import earth.terrarium.athena.api.client.utils.AthenaUnbakedModelLoader;
import earth.terrarium.athena.impl.loading.AthenaResourceLoader;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

public class MMRAthenaUnbakedModelLoader extends AthenaUnbakedModelLoader {
  private final ResourceLocation id;
  private final AthenaModelFactory factory;
  private final Function<Supplier<AthenaBlockModel>, NotNullUnbakedModel> loader;

  public MMRAthenaUnbakedModelLoader(ResourceLocation id, AthenaModelFactory factory, Function<Supplier<AthenaBlockModel>, NotNullUnbakedModel> loader) {
    super(id, factory, loader);
    this.id = id;
    this.factory = factory;
    this.loader = loader;
  }

  public @Nullable NotNullUnbakedModel loadModel(ModelResourceLocation modelId) {
    if (modelId != null && !"inventory".equals(modelId.getVariant())) {
      JsonObject json = AthenaResourceLoader.getData(this.id, modelId.id());
      return this.loadModel(json);
    } else {
      return null;
    }
  }

  public NotNullUnbakedModel loadModel(JsonObject json) {
    return json != null ? this.loader.apply(this.factory.create(json)) : null;
  }
}
