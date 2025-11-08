package es.degrassi.mmreborn.client.integration.athena.model.casing;

import earth.terrarium.athena.api.client.neoforge.WrappedGetter;
import earth.terrarium.athena.api.client.utils.NullableEnumMap;
import es.degrassi.mmreborn.client.integration.athena.model.MMRBakedModel;
import es.degrassi.mmreborn.client.integration.athena.utils.MMRAthenaQuad;
import es.degrassi.mmreborn.common.util.MMRLogger;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import lombok.Getter;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class CasingBakedModel implements MMRBakedModel {

  private static final Direction[] DIRECTIONS = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN};

  public static final ModelProperty<NullableEnumMap<Direction, Map<Direction, List<MMRAthenaQuad>>>> DATA = new ModelProperty<>();

  private final CasingBlockModel model;
  @Getter
  private final Int2ObjectMap<TextureAtlasSprite> textures;

  public CasingBakedModel(CasingBlockModel model, Function<Material, TextureAtlasSprite> function) {
    this.model = model;
    this.textures = this.model.getTextures(function);
  }

  @Override
  public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType type) {
    List<BakedQuad> quads = new ArrayList<>();
    try {
      Map<Direction, List<MMRAthenaQuad>> values = data.has(DATA) ?
          Objects.requireNonNull(data.get(DATA)).getOrDefault(side, Map.of()) :
          this.model.getDefaultQuads(side, null);
      values.forEach((dir, quadList) -> {
        quads.addAll(bakeQuads(quadList, dir));
        int texture = this.model.materials.getTexture(dir, 5);
        if (texture != 0) {
          quads.addAll(bakeQuads(List.of(MMRAthenaQuad.withSprite(5, -0.0001F, -1)), dir));
        }
      });
      return quads;
    } catch (Exception e) {
      MMRLogger.INSTANCE.error("Error occurred while getting quads of Casing Athena block model", e);
      throw e;
    }
  }

  @Override
  public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData data) {
    WrappedGetter getter = new WrappedGetter(level);
    final NullableEnumMap<Direction, Map<Direction, List<MMRAthenaQuad>>> quads = new NullableEnumMap<>(Direction.class);
    Map<Direction, List<MMRAthenaQuad>> nonCullQuads = new HashMap<>();
    for (Direction direction : DIRECTIONS) {
      List<MMRAthenaQuad> culledQuads = new ArrayList<>();
      List<MMRAthenaQuad> unculledQuads = new ArrayList<>();
      for (MMRAthenaQuad quad : this.model.getQuads(getter, state, pos, direction, null)) {
        if (quad.cull()) {
          culledQuads.add(quad);
        } else {
          unculledQuads.add(quad);
        }
      }
      quads.put(direction, Map.of(direction, culledQuads));
      nonCullQuads.put(direction, unculledQuads);
    }
    quads.put(null, nonCullQuads);
    return data.derive().with(DATA, quads).build();
  }
}
