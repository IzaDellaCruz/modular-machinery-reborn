package es.degrassi.mmreborn.client.entity.renderer;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import es.degrassi.mmreborn.api.Structure;
import es.degrassi.mmreborn.common.data.MMRConfig;
import es.degrassi.mmreborn.common.entity.StructureCheckerEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class StructureCheckerRenderer implements BlockEntityRenderer<StructureCheckerEntity> {
  public static final Map<ResourceLocation, StructureRenderer> renderers = Maps.newHashMap();
  private final BlockEntityRendererProvider.Context context;
  public StructureCheckerRenderer(BlockEntityRendererProvider.Context context) {
    this.context = context;
  }

  @Override
  public void render(StructureCheckerEntity machineControllerEntity, float v, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int i1) {
    var bound = machineControllerEntity.getBoundMachine();
    var level = machineControllerEntity.getLevel();
    if (level == null || bound == null) return;
    var machine = bound.getRegistryName();
    if (renderers.containsKey(machine)) {
      Direction machineFacing = machineControllerEntity.getControllerFacing();
      StructureRenderer renderer = renderers.get(machine);
      if (renderer.shouldRender()) {
        renderer.render(context, poseStack, multiBufferSource, machineFacing, level, machineControllerEntity.getBlockPos());
      } else {
        renderers.remove(machine);
      }
    }
  }

  public static void add(ResourceLocation machine, Structure structure) {
    renderers.put(machine, new StructureRenderer(MMRConfig.get().structureRenderTime.get(), structure::getBlocks));
  }
}
