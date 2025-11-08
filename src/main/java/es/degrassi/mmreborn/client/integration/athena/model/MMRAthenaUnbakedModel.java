package es.degrassi.mmreborn.client.integration.athena.model;

import earth.terrarium.athena.api.client.models.AthenaBlockModel;
import earth.terrarium.athena.api.client.models.NotNullUnbakedModel;
import earth.terrarium.athena.api.client.neoforge.AthenaBakedModel;
import es.degrassi.mmreborn.client.integration.athena.model.casing.CasingBakedModel;
import es.degrassi.mmreborn.client.integration.athena.model.casing.CasingBlockModel;
import es.degrassi.mmreborn.client.integration.athena.model.controller.ControllerBakedModel;
import es.degrassi.mmreborn.client.integration.athena.model.controller.ControllerBlockModel;
import es.degrassi.mmreborn.client.integration.athena.model.hatch.HatchBakedModel;
import es.degrassi.mmreborn.client.integration.athena.model.hatch.HatchBlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class MMRAthenaUnbakedModel implements NotNullUnbakedModel {

  private final Supplier<AthenaBlockModel> model;

  public MMRAthenaUnbakedModel(Supplier<AthenaBlockModel> athenaBlockModelSupplier) {
    this.model = athenaBlockModelSupplier;
  }

  @Override
  public @NotNull Collection<ResourceLocation> getDependencies() {
    return List.of();
  }

  @Override
  public void resolveParents(@NotNull Function<ResourceLocation, UnbakedModel> function) {

  }

  @Override
  public @NotNull BakedModel bake(@NotNull ModelBaker baker, @NotNull Function<Material, TextureAtlasSprite> function, @NotNull ModelState modelState) {
    if (model.get() instanceof CasingBlockModel casingBlockModel) {
      return new CasingBakedModel(casingBlockModel, function);
    } else if (model.get() instanceof HatchBlockModel hatchBlockModel) {
      return new HatchBakedModel(baker, hatchBlockModel, function);
    } else if (model.get() instanceof ControllerBlockModel controllerBlockModel) {
      return new ControllerBakedModel(controllerBlockModel, function);
    }
    return new AthenaBakedModel(model.get(), function);
  }
}
