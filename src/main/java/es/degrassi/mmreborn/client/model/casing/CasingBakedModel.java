package es.degrassi.mmreborn.client.model.casing;

import com.google.common.base.Strings;
import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.client.model.RenderHelper;
import es.degrassi.mmreborn.client.util.RenderTypes;
import es.degrassi.mmreborn.common.block.BlockCasing;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.IntStream;

public class CasingBakedModel implements IDynamicBakedModel {

  public static final ModelProperty<CasingState> CASING_STATE = new ModelProperty<>();

  private final TextureAtlasSprite[] frameTextures;
  private final TextureAtlasSprite[] centerTextures;
  // Frame texture
  static final Material[] TEXTURES_FRAME = generateTexturesFrame();

  static final Material TEXTURE = new Material(TextureAtlas.LOCATION_BLOCKS,
      ModularMachineryReborn.rl("block/casing_plain_a"));

  private static Material[] generateTexturesFrame() {
    return IntStream.range(1, 16)
        .mapToObj(Integer::toBinaryString)
        .map(s -> Strings.padStart(s, 4, '0'))
        .map(c -> ModularMachineryReborn.rl("block/casing_plain" + c))
        .map(rl -> new Material(TextureAtlas.LOCATION_BLOCKS, rl))
        .toArray(Material[]::new);
  }

  public CasingBakedModel(Function<Material, TextureAtlasSprite> bakedTextureGetter) {
    this.centerTextures = new TextureAtlasSprite[] {
        bakedTextureGetter.apply(TEXTURE)
    };
    this.frameTextures = new TextureAtlasSprite[16];
    for (int i = 0; i < TEXTURES_FRAME.length; i++) {
      this.frameTextures[1 + i] = bakedTextureGetter.apply(TEXTURES_FRAME[i]);
    }
  }

  @Override
  public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
    final CasingState casingState = getCasingState(level, state, pos);
    return modelData.derive().with(CASING_STATE, casingState).build();
  }

  @Override
  public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extraData, @Nullable RenderType renderType) {
    if (side == null) return Collections.emptyList();

    CasingState casingState = Objects.requireNonNullElse(extraData.get(CASING_STATE), CasingState.DEFAULT);

    final List<BakedQuad> quads = new ArrayList<>(5);
    final List<Vector3f> corners = RenderHelper.getFaceCorners(side);

    if (casingState.hasAdjacentCasingBlock(side)) {
      return Collections.emptyList();
    }

    final TextureAtlasSprite casingTexture = this.centerTextures[0];
    quads.add(this.createQuad(side, corners, casingTexture, 0, 0));

    final int edgeBitmask = casingState.getMask(side);
    final TextureAtlasSprite sideSprite = this.frameTextures[edgeBitmask];
    if (sideSprite != null) {
      quads.add(this.createQuad(side, corners, sideSprite, 0, 0));
    }
    return quads;
  }

  private BakedQuad createQuad(Direction side, List<Vector3f> corners, TextureAtlasSprite sprite, float uOffset, float vOffset) {
    return this.createQuad(side, corners.get(0), corners.get(1), corners.get(2), corners.get(3), sprite, uOffset, vOffset);
  }

  private BakedQuad createQuad(Direction side, Vector3f c1, Vector3f c2, Vector3f c3, Vector3f c4, TextureAtlasSprite sprite, float uOffset, float vOffset) {
    Vec3 normal = new Vec3(side.getNormal().getX(), side.getNormal().getY(), side.getNormal().getZ());

    float u1 = Mth.clamp(0 - uOffset, 0, 1);
    float u2 = Mth.clamp(1 - uOffset, 0, 1);
    float v1 = Mth.clamp(0 - vOffset, 0, 1);
    float v2 = Mth.clamp(1 - vOffset, 0, 1);

    var builder = new QuadBakingVertexConsumer();
    builder.setSprite(sprite);
    builder.setDirection(side);
    this.putVertex(builder, normal, c1.x(), c1.y(), c1.z(), sprite, u1, v1);
    this.putVertex(builder, normal, c2.x(), c2.y(), c2.z(), sprite, u1, v2);
    this.putVertex(builder, normal, c3.x(), c3.y(), c3.z(), sprite, u2, v2);
    this.putVertex(builder, normal, c4.x(), c4.y(), c4.z(), sprite, u2, v1);
    builder.setTintIndex(4);
    return builder.bakeQuad();
  }

  private void putVertex(QuadBakingVertexConsumer builder, Vec3 normal, float x, float y, float z,
                         TextureAtlasSprite sprite, float u, float v) {
    builder.addVertex(x, y, z);
    builder.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    builder.setNormal((float) normal.x, (float) normal.y, (float) normal.z);
    u = sprite.getU(u);
    v = sprite.getV(v);
    builder.setUv(u, v);
  }

  @Override
  public ItemOverrides getOverrides() {
    return ItemOverrides.EMPTY;
  }

  @Override
  public boolean useAmbientOcclusion() {
    return false;
  }

  @Override
  public boolean isGui3d() {
    return false;
  }

  @Override
  public boolean isCustomRenderer() {
    return false;
  }

  @Override
  public boolean usesBlockLight() {
    return false;
  }

  @Override
  public TextureAtlasSprite getParticleIcon() {
    return this.frameTextures[this.frameTextures.length - 1];
  }

  private static CasingState getCasingState(BlockAndTintGetter level, BlockState state, BlockPos pos) {
    int[] masks = new int[6];
    for (Direction facing : Direction.values()) {
      masks[facing.get3DDataValue()] = makeBitMask(level, state, pos, facing);
    }
    boolean[] adjacentCasingBlocks = new boolean[6];
    for (Direction facing : Direction.values()) {
      adjacentCasingBlocks[facing.get3DDataValue()] = isSameCasingBlock(level, state, pos, facing, facing, facing.getOpposite());
    }
    return new CasingState(masks, adjacentCasingBlocks);
  }

  private static int makeBitMask(BlockAndTintGetter level, BlockState state, BlockPos pos, Direction side) {
    return switch (side) {
      case DOWN -> makeBitmask(level, state, pos, side, Direction.SOUTH, Direction.EAST, Direction.NORTH,
          Direction.WEST);
      case UP -> makeBitmask(level, state, pos, side, Direction.SOUTH, Direction.WEST, Direction.NORTH,
          Direction.EAST);
      case NORTH -> makeBitmask(level, state, pos, side, Direction.UP, Direction.WEST, Direction.DOWN,
          Direction.EAST);
      case SOUTH -> makeBitmask(level, state, pos, side, Direction.UP, Direction.EAST, Direction.DOWN,
          Direction.WEST);
      case WEST -> makeBitmask(level, state, pos, side, Direction.UP, Direction.SOUTH, Direction.DOWN,
          Direction.NORTH);
      case EAST -> makeBitmask(level, state, pos, side, Direction.UP, Direction.NORTH, Direction.DOWN,
          Direction.SOUTH);
    };
  }

  private static int makeBitmask(
      BlockAndTintGetter level,
      BlockState state,
      BlockPos pos,
      Direction face,
      Direction up,
      Direction right,
      Direction down,
      Direction left
  ) {
    int bitmask = 0;

    if (!isSameCasingBlock(level, state, pos, face, up, face)) {
      bitmask |= 1;
    }
    if (!isSameCasingBlock(level, state, pos, face, right, face)) {
      bitmask |= 2;
    }
    if (!isSameCasingBlock(level, state, pos, face, down, face)) {
      bitmask |= 4;
    }
    if (!isSameCasingBlock(level, state, pos, face, left, face)) {
      bitmask |= 8;
    }
    return bitmask;
  }

  private static boolean isSameCasingBlock(BlockAndTintGetter level, BlockState state, BlockPos pos,
                                      Direction queryingFace, Direction adjDir, Direction adjFace) {
    var adjacentPos = pos.relative(adjDir);
    var adjacentState = level.getBlockState(adjacentPos);
    // Checks that the adjacent block is indeed glass
    if (!(adjacentState.getAppearance(level, adjacentPos, adjFace, state, pos)
        .getBlock() instanceof BlockCasing bc) ) {
      return false;
    }
    // Checks that the current block is also glass, in other words that the adjacent block would connect to us.
    // This ensures consistency between this block and the adjacent block deciding to connect or not.
    // This is important for advanced use cases such as FramedBlocks.
    return state.getAppearance(level, pos, queryingFace, adjacentState, adjacentPos)
        .getBlock() instanceof BlockCasing bc1 && bc.getCasingType().equals(bc1.getCasingType());
  }

  @Override
  public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
    return ChunkRenderTypeSet.of(RenderTypes.CUTOUT);
  }
}
