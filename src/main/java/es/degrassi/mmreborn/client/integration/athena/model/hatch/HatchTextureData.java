package es.degrassi.mmreborn.client.integration.athena.model.hatch;

import es.degrassi.mmreborn.ModularMachineryReborn;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record HatchTextureData(
    String baseTextureName,
    @Nullable ResourceLocation baseTexture,
    ResourceLocation defaultBaseTexture,
    String overlayTextureName,
    @Nullable ResourceLocation overlayTexture,
    ResourceLocation defaultOverlayTexture,
    boolean usesReinforcedOverlay,
    ResourceLocation defaultModel
) {
  public ResourceLocation baseTexture() {
    return Optional.ofNullable(baseTexture).orElse(defaultBaseTexture);
  }

  public ResourceLocation overlayTexture() {
    return Optional.ofNullable(overlayTexture).orElse(defaultOverlayTexture);
  }

  public boolean hasDefaultTextures() {
    if (baseTexture == null || overlayTexture == null || defaultBaseTexture == null || defaultOverlayTexture == null) return true;
    return defaultBaseTexture.equals(baseTexture) && defaultOverlayTexture.equals(overlayTexture);
  }

  public static HatchTextureData withDefault(String modelId) {
    return new HatchTextureData(
        null,
        null,
        null,
        null,
        null,
        null,
        false,
        ModularMachineryReborn.rl("default/hatch_" + modelId)
    );
  }

  public HatchTextureData derive(String name, ResourceLocation texture, ResourceLocation defaultTexture, boolean base, boolean reinforced) {
    return new HatchTextureData(
      base ? name : baseTextureName,
      base ? texture : baseTexture,
      base ? defaultTexture : defaultBaseTexture,
      !base ? name : overlayTextureName,
      !base ? texture : overlayTexture,
      !base ? defaultTexture : defaultOverlayTexture,
      reinforced,
      defaultModel
    );
  }

  public HatchTextureData derive(
      String baseTextureName,
      ResourceLocation baseTexture,
      ResourceLocation defaultBaseTexture,
      String overlayTextureName,
      ResourceLocation overlayTexture,
      ResourceLocation defaultOverlayTexture,
      boolean reinforced
  ) {
    return new HatchTextureData(
        baseTextureName,
        baseTexture,
        defaultBaseTexture,
        overlayTextureName,
        overlayTexture,
        defaultOverlayTexture,
        reinforced,
        defaultModel
    );
  }
}
