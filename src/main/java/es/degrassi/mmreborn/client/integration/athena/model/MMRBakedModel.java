package es.degrassi.mmreborn.client.integration.athena.model;

import earth.terrarium.athena.api.client.models.TintProvider;
import es.degrassi.mmreborn.client.integration.athena.utils.CtmUtils;
import es.degrassi.mmreborn.client.integration.athena.utils.MMRAthenaQuad;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.common.NeoForgeConfig;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public interface MMRBakedModel extends IDynamicBakedModel {
  Int2ObjectMap<TextureAtlasSprite> getTextures();

  default List<BakedQuad> bakeQuads(List<MMRAthenaQuad> quads, Direction direction) {
    List<BakedQuad> bakedQuads = new ArrayList<>(quads.size());
    for (MMRAthenaQuad quad : quads) {
      TextureAtlasSprite sprite = getTextures().get(quad.sprite());
      if (sprite == null) continue;
      bakedQuads.addAll(CtmUtils.bakeQuad(
          quad,
          direction,
          sprite,
          new TintProvider.Index(quad.tint()),
          BlockModelRotation.X0_Y0
      ));
    }
    return bakedQuads;
  }

  default List<BakedQuad> bakeQuads(List<MMRAthenaQuad> quads, Direction direction, BlockModelRotation rotation) {
    List<BakedQuad> bakedQuads = new ArrayList<>(quads.size());
    for (MMRAthenaQuad quad : quads) {
      TextureAtlasSprite sprite = getTextures().get(quad.sprite());
      if (sprite == null) continue;
      bakedQuads.addAll(CtmUtils.bakeQuad(
          quad,
          direction,
          sprite,
          new TintProvider.Index(quad.tint()),
          rotation
      ));
    }
    return bakedQuads;
  }

  @Override
  default boolean useAmbientOcclusion() {
    return NeoForgeConfig.CLIENT.experimentalForgeLightPipelineEnabled.get();
  }

  @Override
  default boolean isGui3d() {
    return false;
  }

  @Override
  default boolean usesBlockLight() {
    return true;
  }

  @Override
  default boolean isCustomRenderer() {
    return false;
  }

  @Override
  default @NotNull ItemOverrides getOverrides() {
    return ItemOverrides.EMPTY;
  }

  @Override
  default @NotNull ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
    return ChunkRenderTypeSet.of(RenderType.CUTOUT);
  }

  @Override
  default @NotNull TextureAtlasSprite getParticleIcon() {
    if (getTextures().containsKey(0)) {
      return getTextures().get(0);
    }
    return Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS).getSprite(MissingTextureAtlasSprite.getLocation());
  }
}
