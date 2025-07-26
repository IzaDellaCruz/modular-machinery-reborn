package es.degrassi.mmreborn.client.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import es.degrassi.mmreborn.client.util.RenderTypes;
import es.degrassi.mmreborn.common.item.StructureCreatorItem;
import es.degrassi.mmreborn.common.registration.ItemRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class StructureCreatorRenderer {
  private StructureCreatorRenderer() {}

  public static void renderSelectedBlocks(PoseStack pose) {
    if(Minecraft.getInstance().player != null && Minecraft.getInstance().player.getMainHandItem().getItem() == ItemRegistration.STRUCTURE_CREATOR_ITEM.get()) {
      MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
      VertexConsumer builder = buffer.getBuffer(RenderTypes.THICK_LINES);
      Vec3 playerPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
      List<BlockPos> blocks = StructureCreatorItem.getSelectedBlocks(Minecraft.getInstance().player.getMainHandItem());
      blocks.forEach(pos -> {
        AABB box = new AABB(pos);
        pose.pushPose();
        pose.translate(-playerPos.x(), -playerPos.y(), -playerPos.z());
        LevelRenderer.renderLineBox(pose, builder, box, 1.0F, 0.0F, 0.0F, 1.0F);
        pose.popPose();
      });
      buffer.endBatch(RenderTypes.THICK_LINES);
    }
  }
}
