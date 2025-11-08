package es.degrassi.mmreborn.client.model.hatch;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Either;
import es.degrassi.mmreborn.common.registration.DataComponentRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.RenderTypeHelper;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import net.neoforged.neoforge.common.NeoForgeConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector4f;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@ParametersAreNonnullByDefault
public class DefaultHatchBakedModel implements IDynamicBakedModel {
  public static final ModelProperty<ResourceLocation> BASE_TEXTURE = new ModelProperty<>();
  public static final ModelProperty<String> BASE_TEXTURE_NAME = new ModelProperty<>();
  public static final ModelProperty<ResourceLocation> OVERLAY_TEXTURE = new ModelProperty<>();
  public static final ModelProperty<String> OVERLAY_TEXTURE_NAME = new ModelProperty<>();
  public static final ModelProperty<ResourceLocation> MODEL = new ModelProperty<>();

  private static final Map<ModelData, BakedModel> modelByModelData = Maps.newConcurrentMap();

  private final Function<Material, TextureAtlasSprite> spriteGetter;
  private final ModelBaker baker;

  private final HatchOverrideList overrideList = new HatchOverrideList();

  private static Material createMaterial(ResourceLocation texture) {
    return new Material(InventoryMenu.BLOCK_ATLAS, texture);
  }

  private static Material copy(Material material) {
    return new Material(material.atlasLocation(), material.texture());
  }

  public DefaultHatchBakedModel(ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter) {
    this.spriteGetter = spriteGetter;
    this.baker = baker;
  }

  private BakedModel getHatchModel(ModelData data) {
    return modelByModelData.computeIfAbsent(data, this::processHatchModel);
  }

  private BakedModel processHatchModel(ModelData data) {
    BakedModel bakedModel;
    ResourceLocation baseTexture = data.get(BASE_TEXTURE);
    ResourceLocation overlayTexture = data.get(OVERLAY_TEXTURE);
    String baseTextureName = data.get(BASE_TEXTURE_NAME);
    String overlayTextureName = data.get(OVERLAY_TEXTURE_NAME);
    ResourceLocation model = data.get(MODEL);
    if (model == null) return Minecraft.getInstance().getModelManager().getMissingModel();
    var oldBlockModel = ((BlockModel) baker.getModel(model));
    Map<String, Either<Material, String>> textureMap = Maps.newHashMap();
    oldBlockModel.textureMap.forEach((string, either) -> {
      either.ifLeft(material -> textureMap.put(string, Either.left(copy(material))));
      either.ifRight(name -> textureMap.put(string, Either.right(name)));
    });
    if (baseTexture != null && baseTextureName != null) {
      switch (baseTextureName) {
        case "bg_down" -> textureMap.put("bg_down", Either.left(createMaterial(baseTexture)));
        case "bg_up" -> textureMap.put("bg_up", Either.left(createMaterial(baseTexture)));
        case "bg_north" -> textureMap.put("bg_north", Either.left(createMaterial(baseTexture)));
        case "bg_south" -> textureMap.put("bg_south", Either.left(createMaterial(baseTexture)));
        case "bg_west" -> textureMap.put("bg_west", Either.left(createMaterial(baseTexture)));
        case "bg_east" -> textureMap.put("bg_east", Either.left(createMaterial(baseTexture)));
        default -> textureMap.put("bg_all", Either.left(createMaterial(baseTexture)));
      }
    }
    if (overlayTexture != null && overlayTextureName != null) {
      switch (overlayTextureName) {
        case "ov_down" -> textureMap.put("ov_down", Either.left(createMaterial(overlayTexture)));
        case "ov_up" -> textureMap.put("ov_up", Either.left(createMaterial(overlayTexture)));
        case "ov_north" -> textureMap.put("ov_north", Either.left(createMaterial(overlayTexture)));
        case "ov_south" -> textureMap.put("ov_south", Either.left(createMaterial(overlayTexture)));
        case "ov_west" -> textureMap.put("ov_west", Either.left(createMaterial(overlayTexture)));
        case "ov_east" -> textureMap.put("ov_east", Either.left(createMaterial(overlayTexture)));
        case "ov_top" -> textureMap.put("ov_top", Either.left(createMaterial(overlayTexture)));
        case "ov_side" -> textureMap.put("ov_side", Either.left(createMaterial(overlayTexture)));
        case "ov_front" -> textureMap.put("ov_front", Either.left(createMaterial(overlayTexture)));
        default -> textureMap.put("ov_all", Either.left(createMaterial(overlayTexture)));
      }
    }
    BlockModel blockModel = new BlockModel(
        oldBlockModel.getParentLocation(),
        oldBlockModel.elements,
        textureMap,
        oldBlockModel.hasAmbientOcclusion(),
        oldBlockModel.getGuiLight(),
        oldBlockModel.getTransforms(),
        oldBlockModel.getOverrides()
    );
    blockModel.resolveParents(baker::getModel);
    bakedModel = baker.bakeUncached(blockModel, BlockModelRotation.X0_Y0, spriteGetter);
    return bakedModel;
  }

  @Override
  public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state,
                                           @Nullable Direction side,
                                           RandomSource rand,
                                           ModelData data,
                                           @Nullable RenderType type
  ) {
    BakedModel bakedModel = getHatchModel(data);
    if (bakedModel == null) return List.of();
    List<BakedQuad> quads;
    if (state != null && state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
      quads = getRotatedQuads(bakedModel, state.getValue(BlockStateProperties.HORIZONTAL_FACING), side, rand, type);
    } else {
      quads = bakedModel.getQuads(state, side, rand, ModelData.EMPTY, type);
    }
    return quads;
  }

  private List<BakedQuad> getRotatedQuads(BakedModel model, Direction machineFacing, @Nullable Direction side, RandomSource random, @Nullable RenderType type) {
    //side of the model before rotation
    Direction originalSide = getRotatedDirection(machineFacing, side);
    List<BakedQuad> finalQuads = model.getQuads(null, originalSide, random, ModelData.EMPTY, type);
    return finalQuads.stream().map(quad -> rotateQuad(quad, getRotation(machineFacing), side == null ? quad.getDirection() : side)).toList();
  }

  private Quaternionf getRotation(Direction machineFacing) {
    return switch (machineFacing) {
      case EAST -> new Quaternionf().fromAxisAngleDeg(0, -1, 0, 90);
      case SOUTH -> new Quaternionf().fromAxisAngleDeg(0, -1, 0, 180);
      case WEST -> new Quaternionf().fromAxisAngleDeg(0, -1, 0, 270);
      default -> new Quaternionf();
    };
  }

  private BakedQuad rotateQuad(BakedQuad quad, Quaternionf rotation, Direction side) {
    int[] quadData = quad.getVertices();
    int[] newQuadData = Arrays.copyOf(quadData, quadData.length);
    for (int i = 0; i < quadData.length / 8; i++) {
      float x = Float.intBitsToFloat(quadData[i * 8]);
      float y = Float.intBitsToFloat(quadData[i * 8 + 1]);
      float z = Float.intBitsToFloat(quadData[i * 8 + 2]);
      Vector4f pos = new Vector4f(x - 0.5F, y - 0.5F, z - 0.5F, 1.0F);
      pos.rotate(rotation);
      pos.div(pos.w);
      newQuadData[i * 8] = Float.floatToRawIntBits(pos.x() + 0.5F);
      newQuadData[i * 8 + 1] = Float.floatToRawIntBits(pos.y() + 0.5F);
      newQuadData[i * 8 + 2] = Float.floatToRawIntBits(pos.z() + 0.5F);

      //Wipe normal's data, don't know why, but it works better without that.
      newQuadData[i * 8 + 7] = 0;
    }
    return new BakedQuad(newQuadData, quad.getTintIndex(), side, quad.getSprite(), quad.isShade());
  }

  public Direction getRotatedDirection(Direction machineFacing, @Nullable Direction quad) {
    if (quad == null || quad.getAxis() == Direction.Axis.Y)
      return quad;

    return switch (machineFacing) {
      case WEST -> Direction.from2DDataValue((quad.get2DDataValue() + 1) % 4);
      case SOUTH -> Direction.from2DDataValue((quad.get2DDataValue() + 2) % 4);
      case EAST -> Direction.from2DDataValue((quad.get2DDataValue() + 3) % 4);
      default -> quad;
    };
  }

  @Override
  public boolean useAmbientOcclusion() {
    return NeoForgeConfig.CLIENT.experimentalForgeLightPipelineEnabled.get();
  }

  @Override
  public boolean isGui3d() {
    return true;
  }

  @Override
  public boolean usesBlockLight() {
    return true;
  }

  @Override
  public boolean isCustomRenderer() {
    return true;
  }

  @Override
  public @NotNull TextureAtlasSprite getParticleIcon() {
    return this.getParticleIcon(ModelData.EMPTY);
  }

  @Override
  public @NotNull TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
    return getMachineModel(data).getParticleIcon(data);
  }

  @Override
  public @NotNull ItemOverrides getOverrides() {
    return overrideList;
  }

  @Override
  public @NotNull ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
    try {
      return getMachineModel(data).getRenderTypes(state, rand, data);
    } catch (IllegalArgumentException ignored) {
      return ChunkRenderTypeSet.all();
    }
  }

  @Override
  public @NotNull List<RenderType> getRenderTypes(ItemStack stack, boolean fabulous) {
    return Optional.ofNullable(getMachineItemModel(stack))
        .map(machine -> machine.getRenderTypes(stack, fabulous))
        .orElse(List.of(RenderTypeHelper.getFallbackItemRenderType(stack, this, fabulous)));
  }

  private BakedModel getMachineModel(@NotNull ModelData data) {
    return getMachineBlockModel(data);
  }

  public BakedModel getMachineBlockModel(@NotNull ModelData data) {
    return Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(data.get(MODEL)));
  }

  public BakedModel getMachineItemModel(ItemStack stack) {
    return Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(stack.get(DataComponentRegistration.DEFAULT_MODEL)));
  }
}
