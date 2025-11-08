package es.degrassi.mmreborn.client.integration.athena.model.controller;

import earth.terrarium.athena.api.client.neoforge.WrappedGetter;
import earth.terrarium.athena.api.client.utils.NullableEnumMap;
import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.client.integration.athena.model.MMRBakedModel;
import es.degrassi.mmreborn.client.integration.athena.utils.MMRAthenaQuad;
import es.degrassi.mmreborn.client.model.controller.ControllerOverrideList;
import es.degrassi.mmreborn.common.block.BlockMachineComponent;
import es.degrassi.mmreborn.common.item.ControllerItem;
import es.degrassi.mmreborn.common.util.MMRLogger;
import es.degrassi.mmreborn.common.util.MachineModelLocation;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.RenderTypeHelper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ControllerBakedModel implements MMRBakedModel {

  private static final Direction[] DIRECTIONS = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN};
  public static final ModelProperty<ControllerData> DATA = new ModelProperty<>();

  private final ControllerOverrideList overrideList = new ControllerOverrideList();

  private final ControllerBlockModel model;
  @Getter
  private final Int2ObjectMap<TextureAtlasSprite> textures;

  public ControllerBakedModel(ControllerBlockModel model, Function<Material, TextureAtlasSprite> function) {
    this.model = model;
    this.textures = this.model.getTextures(function);
  }

  @Override
  public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                  RandomSource rand, ModelData data, @Nullable RenderType type) {
    try {
      var machine = data.get(DATA);
      assert machine != null;
      var isCustom = machine.hasCustomModel();
      if (isCustom || (state != null && !state.getValue(BlockMachineComponent.CONNECT_TEXTURES))) {
        return new es.degrassi.mmreborn.client.model.controller.ControllerBakedModel().getQuads(
            state, side, rand,
            ModelData.builder().with(es.degrassi.mmreborn.client.model.controller.ControllerBakedModel.MACHINE, machine.machine()).build(),
            type
        );
      }

      List<BakedQuad> quads = new ArrayList<>();
      Map<Direction, List<MMRAthenaQuad>> values = machine.hasData()
          ? machine.data().getOrDefault(side, Map.of())
          : this.model.getDefaultQuads(side, null);
      values.forEach((dir, quadList) -> {
        quads.addAll(bakeQuads(quadList, dir));
        int texture = this.model.materials.getTexture(dir, 5);
        if (texture != 0) {
          if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            var facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            if (facing == dir) {
              quads.addAll(bakeQuads(List.of(MMRAthenaQuad.withSprite(5, -0.0001F, -1)), dir));
            }
          } else {
            quads.addAll(bakeQuads(List.of(MMRAthenaQuad.withSprite(5, -0.0001F, -1)), dir));
          }
        }
      });
      return quads;
    } catch (Exception e) {
      MMRLogger.INSTANCE.error("Error occurred while getting quads of Controller Athena block model", e);
      throw e;
    }
  }

  @Override
  public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData data) {
    var textureData = data.get(DATA);
    assert textureData != null;
    if (textureData.hasCustomModel()) return data.derive().build();
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
    return data.derive().with(DATA, new ControllerData(textureData.machine(), quads)).build();
  }

  @Override
  public boolean isGui3d() {
    return true;
  }

  @Override
  public boolean isCustomRenderer() {
    return true;
  }

  @Override
  public @NotNull ItemOverrides getOverrides() {
    return overrideList;
  }

  @Override
  public TextureAtlasSprite getParticleIcon() {
    return this.getParticleIcon(ModelData.EMPTY);
  }

  @Override
  public TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
    return getMachineModel(data).getParticleIcon(data);
  }

  @Override
  public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
    try {
      return getMachineModel(data).getRenderTypes(state, rand, data);
    } catch (IllegalArgumentException ignored) {
      return ChunkRenderTypeSet.all();
    }
  }

  @Override
  public List<RenderType> getRenderTypes(ItemStack stack, boolean fabulous) {
    return ControllerItem.getMachine(stack)
        .map(machine -> getMachineItemModel(machine.getControllerModel()).getRenderTypes(stack, fabulous))
        .orElse(List.of(RenderTypeHelper.getFallbackItemRenderType(stack, this, fabulous)));
  }

  private BakedModel getMachineModel(@NotNull ModelData data) {
    ControllerData machine = data.get(DATA);
    BakedModel model;
    if (machine != null)
      model = getMachineBlockModel(machine.modelLocation());
    else
      model = Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(MachineModelLocation.DEFAULT.getLoc()));
    return model;
  }

  public BakedModel getMachineBlockModel(@Nullable MachineModelLocation blockModelLocation) {
    BakedModel missing = Minecraft.getInstance().getModelManager().getMissingModel();
    BakedModel model = missing;

    if (blockModelLocation != null) {
      if (blockModelLocation.getState() != null)
        model = Minecraft.getInstance().getBlockRenderer().getBlockModel(blockModelLocation.getState());
      else if (blockModelLocation.getLoc() != null && blockModelLocation.getProperties() != null)
        model = Minecraft.getInstance().getModelManager().getModel(new ModelResourceLocation(blockModelLocation.getLoc(), blockModelLocation.getProperties()));
      else if (blockModelLocation.getLoc() != null)
        model = Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(blockModelLocation.getLoc()));
    }

    if (model == missing)
      model = Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(MachineModelLocation.DEFAULT.getLoc()));

    return model;
  }

  @Nullable
  public BakedModel getMachineItemModel(@Nullable MachineModelLocation itemModelLocation) {
    BakedModel missing = Minecraft.getInstance().getModelManager().getMissingModel();
    BakedModel model = missing;
    if (itemModelLocation != null) {
      if (itemModelLocation.getItem() != null && itemModelLocation.getItem() != Items.AIR)
        model = Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(itemModelLocation.getItem());
      else if (itemModelLocation.getLoc() != null && !itemModelLocation.getLoc().equals(MachineModelLocation.DEFAULT.getLoc())) {
        Item item = BuiltInRegistries.ITEM.get(itemModelLocation.getLoc());
        if (itemModelLocation.getProperties() != null)
          model = Minecraft.getInstance().getModelManager().getModel(new ModelResourceLocation(itemModelLocation.getLoc(), itemModelLocation.getProperties()));
        else if (item != Items.AIR && Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(item) != null)
          model = Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(item);
        else
          model = Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(itemModelLocation.getLoc()));
      }
    }

    if (model == missing)
      model = getMachineBlockModel(itemModelLocation);
    if (model == missing)
      model = Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(ModularMachineryReborn.rl("controller")));

    return model;
  }
}
