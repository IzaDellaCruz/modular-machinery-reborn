package es.degrassi.mmreborn.client.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.api.BlockIngredient;
import es.degrassi.mmreborn.api.PartialBlockState;
import es.degrassi.mmreborn.client.util.RenderTypes;
import es.degrassi.mmreborn.common.util.CycleTimer;
import es.degrassi.mmreborn.common.data.MMRConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class StructureRenderer {
  private final int time;
  private final long start;
  private final Map<Direction, Map<BlockPos, BlockIngredient>> blocksGetter = new HashMap<>();
  private final Map<Map<BlockPos, BlockIngredient>, CycleTimer> timers = new HashMap<>();

  public StructureRenderer(int time, Function<Direction, Map<BlockPos, BlockIngredient>> blocksGetter) {
    this.start = System.currentTimeMillis();
    final int configTagTime = MMRConfig.get().blockTagCycleTime.get();
    AtomicInteger maxTime = new AtomicInteger(time);
    for (var direction : Direction.values()) {
      if (direction.getAxis().isVertical()) continue;
      this.blocksGetter.put(direction, blocksGetter.apply(direction));
      Map<BlockPos, BlockIngredient> map = this.blocksGetter.get(direction);
      map.forEach((pos, ing) -> {
        int currentMax = maxTime.get();
        int possibleMax = Math.max(currentMax, ing.getAll().size() * configTagTime);
        if (possibleMax != currentMax) maxTime.set(possibleMax);
      });
      timers.put(map, new CycleTimer(() -> configTagTime, false));
    }
    this.time = maxTime.get();
  }

  public void render(BlockEntityRendererProvider.Context context, PoseStack matrix, MultiBufferSource buffer, Direction direction, Level world, BlockPos machinePos) {
    Map<BlockPos, BlockIngredient> blocks = this.blocksGetter.get(direction);
    CycleTimer timer = this.timers.get(blocks);
    if (timer == null) {
      this.timers.put(blocks, new CycleTimer(() -> MMRConfig.get().blockTagCycleTime.get(), false));
      timer = timers.get(blocks);
    }
    timer.onDraw();
    CycleTimer finalTimer = timer;
    blocks.forEach((pos, ingredient) -> {
      matrix.pushPose();
      matrix.translate(pos.getX(), pos.getY(), pos.getZ());
      if (!(pos.getX() == 0 && pos.getY() == 0 && pos.getZ() == 0) && ingredient != BlockIngredient.ANY) {
        PartialBlockState state = finalTimer.get(ingredient.getAll());
        boolean isNot = ingredient.isNot();
        BlockPos blockPos = machinePos.offset(pos);
        if (state != null && state != PartialBlockState.ANY && !state.getBlockState().isAir()) {
          if (world.getBlockState(blockPos).isAir()) {
            matrix.pushPose();
            matrix.translate(0.1F, 0.1F, 0.1F);
            matrix.scale(0.8f, 0.8f, 0.8f);
            if (isNot) renderTransparentNotBlock(context, world, blockPos, state, matrix, buffer);
            else renderTransparentBlock(context, world, blockPos, state, matrix, buffer);
            matrix.popPose();
          } else if (!ingredient.test(new BlockInWorld(world, blockPos, false))) {
            matrix.pushPose();
            matrix.translate(-0.0005, -0.0005, -0.0005);
            matrix.scale(1.001F, 1.001F, 1.001F);
            renderNope(matrix, buffer);
            matrix.popPose();
          }
        }
      }
      matrix.popPose();
    });
  }

  /**
   * Mostly copied and adapted from:
   * {@link ModelBlockRenderer#tesselateBlock(BlockAndTintGetter, BakedModel, BlockState, BlockPos, PoseStack, VertexConsumer, boolean, RandomSource, long, int, ModelData, RenderType)}
   * <br/>
   * Followed by:
   * {@link ModelBlockRenderer#tesselateWithoutAO(BlockAndTintGetter, BakedModel, BlockState, BlockPos, PoseStack, VertexConsumer, boolean, RandomSource, long, int, ModelData, RenderType)}
   * <br/>
   * Followed by:
   * {@link ModelBlockRenderer#renderModelFaceFlat(BlockAndTintGetter, BlockState, BlockPos, int, int, boolean, PoseStack, VertexConsumer, List, BitSet)}
   */
  private void renderTransparentBlock(BlockEntityRendererProvider.Context context, Level level, BlockPos pos, PartialBlockState state,
                                      PoseStack matrix,
                                      MultiBufferSource buffer) {
    VertexConsumer builder = buffer.getBuffer(RenderTypes.PHANTOM);
    BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state.getBlockState());
    var entity = state.getBlockState().getBlock() instanceof EntityBlock eb ? eb.newBlockEntity(pos, state.getBlockState()) : null;
    ModelData modelData;
    if (entity != null) {
      modelData = entity.getModelData();
    } else {
      modelData = ModelData.EMPTY;
    }

    for (Direction side : Direction.values()) {
      List<BakedQuad> quads = model.getQuads(
          state.getBlockState(),
          side,
          RandomSource.create(42L),
          modelData,
          RenderTypes.PHANTOM
      );
      if (!quads.isEmpty()) {
        int packedLight = LightTexture.FULL_BRIGHT;
        int packedOverlay = OverlayTexture.NO_OVERLAY;
        for (BakedQuad quad : quads) {
          int color;
          if (quad.isTinted()) {
            color = context.getBlockRenderDispatcher().blockColors.getColor(state.getBlockState(), level, pos, quad.getTintIndex());
          } else {
            color = 0xffffffff;
          }
          float f = level.getShade(quad.getDirection(), quad.isShade());
          putQuadData(
              color, 0.8f, builder, matrix.last(), quad, f, f, f, f, packedLight,
              packedLight, packedLight,
              packedLight, packedOverlay
          );
        }
      }
    }

    List<BakedQuad> quads = model.getQuads(
        state.getBlockState(),
        null,
        RandomSource.create(42L),
        modelData,
        RenderTypes.PHANTOM
    );
    if (!quads.isEmpty()) {
      int packedLight = LightTexture.FULL_BRIGHT;
      int packedOverlay = OverlayTexture.NO_OVERLAY;
      for (BakedQuad quad : quads) {
        int color;
        if (quad.isTinted()) {
          color = context.getBlockRenderDispatcher().blockColors.getColor(state.getBlockState(), level, pos, quad.getTintIndex());
        } else {
          color = 0xffffffff;
        }
        float f = level.getShade(quad.getDirection(), quad.isShade());
        putQuadData(
            color, 0.8f, builder, matrix.last(), quad, f, f, f, f, packedLight,
            packedLight, packedLight,
            packedLight, packedOverlay
        );
      }
    }
  }

  private void renderTransparentNotBlock(BlockEntityRendererProvider.Context context, Level level, BlockPos pos, PartialBlockState state,
        PoseStack pose,
        MultiBufferSource buffer) {
    this.renderTransparentBlock(context, level, pos, state, pose, buffer);
    this.renderNope(pose, buffer);
  }

  private void renderNope(PoseStack matrix,
                          MultiBufferSource buffer) {
    VertexConsumer builder = buffer.getBuffer(RenderTypes.NOPE);
    BakedModel model = Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(ModularMachineryReborn.rl("block/nope")));
    ModelData modelData = ModelData.EMPTY;
    Arrays.stream(Direction.values())
        .flatMap(direction -> model.getQuads(null, direction, RandomSource.create(42L), modelData, RenderTypes.NOPE).stream())
        .forEach(quad -> builder.putBulkData(matrix.last(), quad, 1.0F, 1.0F, 1.0F, 0.8F, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, false));
    model.getQuads(null, null, RandomSource.create(42L), modelData, RenderTypes.NOPE)
        .forEach(quad -> builder.putBulkData(matrix.last(), quad, 1.0F, 1.0F, 1.0F, 0.8F, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, false));
  }

  public boolean shouldRender() {
    return System.currentTimeMillis() < this.start + this.time;
  }

  /**
   *  Mostly copied from:
   * {@link ModelBlockRenderer#tesselateBlock(BlockAndTintGetter, BakedModel, BlockState, BlockPos, PoseStack, VertexConsumer, boolean, RandomSource, long, int, ModelData, RenderType)}
   * <br/>
   * Followed by:
   * {@link ModelBlockRenderer#tesselateWithoutAO(BlockAndTintGetter, BakedModel, BlockState, BlockPos, PoseStack, VertexConsumer, boolean, RandomSource, long, int, ModelData, RenderType)}
   * <br/>
   * Followed by:
   * {@link ModelBlockRenderer#renderModelFaceFlat(BlockAndTintGetter, BlockState, BlockPos, int, int, boolean, PoseStack, VertexConsumer, List, BitSet)}
   * <br/>
   * Followed by:
   * {@link ModelBlockRenderer#putQuadData(BlockAndTintGetter, BlockState, BlockPos, VertexConsumer, PoseStack.Pose, BakedQuad, float, float, float, float, int, int, int, int, int)}
   */
  private void putQuadData(
      int color,
      float alpha,
      VertexConsumer consumer,
      PoseStack.Pose pose,
      BakedQuad quad,
      float brightness0,
      float brightness1,
      float brightness2,
      float brightness3,
      int lightmap0,
      int lightmap1,
      int lightmap2,
      int lightmap3,
      int packedOverlay
  ) {
    float r = (color >> 16 & 0xFF) / 255.0F;
    float g = (color >> 8 & 0xFF) / 255.0F;
    float b = (color & 0xFF) / 255.0F;

    consumer.putBulkData(
        pose,
        quad,
        new float[]{brightness0, brightness1, brightness2, brightness3},
        r,
        g,
        b,
        alpha,
        new int[]{lightmap0, lightmap1, lightmap2, lightmap3},
        packedOverlay,
        true
    );
  }
}
