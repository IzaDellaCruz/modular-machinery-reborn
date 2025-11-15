package es.degrassi.mmreborn.client.entity.renderer;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import es.degrassi.mmreborn.api.Structure;
import es.degrassi.mmreborn.common.data.MMRConfig;
import es.degrassi.mmreborn.common.entity.StructureCheckerEntity;
import es.degrassi.mmreborn.common.machine.DynamicMachine;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

import java.util.Map;

public class StructureCheckerRenderer implements BlockEntityRenderer<StructureCheckerEntity> {
  public static final Map<DynamicMachine, StructureRenderer> renderers = Maps.newHashMap();
  private final BlockEntityRendererProvider.Context context;
  public StructureCheckerRenderer(BlockEntityRendererProvider.Context context) {
    this.context = context;
  }

  @Override
  public void render(StructureCheckerEntity machineControllerEntity, float v, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int i1) {
    if (machineControllerEntity.getLevel() == null) return;

    if (renderers.containsKey(machineControllerEntity.getBoundMachine())) {
      Direction machineFacing = machineControllerEntity.getControllerFacing();
      StructureRenderer renderer = renderers.get(machineControllerEntity.getBoundMachine());
      if (renderer.shouldRender()) {
        renderer.render(context, poseStack, multiBufferSource, machineFacing, machineControllerEntity.getLevel(), machineControllerEntity.getBlockPos());
      } else {
        renderers.remove(machineControllerEntity.getBoundMachine());
      }
    }
  }

  public static void add(DynamicMachine machine, Structure structure) {
    renderers.put(machine, new StructureRenderer(MMRConfig.get().structureRenderTime.get(), structure::getBlocks));
  }
}
