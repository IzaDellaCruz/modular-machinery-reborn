package es.degrassi.mmreborn.client.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import es.degrassi.mmreborn.api.IWrenchable;
import es.degrassi.mmreborn.api.capability.config.IOSideConfig;
import es.degrassi.mmreborn.api.capability.config.IOSideMode;
import es.degrassi.mmreborn.api.capability.config.ISideConfigComponent;
import es.degrassi.mmreborn.api.capability.config.RelativeSide;
import es.degrassi.mmreborn.api.client.Icon;
import es.degrassi.mmreborn.common.registration.ItemRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IWrenchableRenderer implements BlockEntityRenderer<BlockEntity> {
  private final BlockEntityRendererProvider.Context context;

  public IWrenchableRenderer(BlockEntityRendererProvider.Context context) {
    this.context = context;
  }

  /*public static void renderBlockHighlight(
      PoseStack pose,
      Camera camera,
      BlockHitResult target,
      MultiBufferSource buffer,
      float gameTimeDeltaPartialTick
  ) {
    if (Minecraft.getInstance().player == null) return;
    if (!Minecraft.getInstance().player.getMainHandItem().is(ItemRegistration.WRENCH)) return;
    var level = Minecraft.getInstance().level;
    if (level == null) return;
    BlockPos pos = target.getBlockPos();
    var entity = level.getBlockEntity(pos);
    if (!(entity instanceof IWrenchable)) return;
    if (!(entity instanceof ISideConfigComponent<?> component)) return;
    if (!(component.getConfig() instanceof IOSideConfig config)) return;
    if (target.getType() == HitResult.Type.MISS) return;
    if (!entity.getBlockPos().equals(target.getBlockPos())) return;
    var face = target.getDirection();
    var facing = Minecraft.getInstance().player.getDirection();
    byMode(config.getSideMode(RelativeSide.fromDirections(facing.getOpposite(), face))).getBlitter().blitWorld(pose, buffer, face, OverlayTexture.NO_OVERLAY);
  }*/

  @Override
  public void render(BlockEntity entity, float partialTick, PoseStack pose, MultiBufferSource buffer, int packedLight, int packedOverlay) {
    if (Minecraft.getInstance().player == null) return;
    if (!Minecraft.getInstance().player.getItemInHand(InteractionHand.MAIN_HAND).is(ItemRegistration.WRENCH)) return;
    if (!(entity instanceof IWrenchable)) return;
    if (!(entity instanceof ISideConfigComponent<?> component)) return;
    if (!(component.getConfig() instanceof IOSideConfig config)) return;
    var hit = context.getBlockEntityRenderDispatcher().cameraHitResult;
    if (!(hit instanceof BlockHitResult blockHit)) return;
    if (blockHit.getType() == HitResult.Type.MISS) return;
    if (!entity.getBlockPos().equals(blockHit.getBlockPos())) return;
    var face = blockHit.getDirection();
    var facing = Minecraft.getInstance().player.getDirection();
    byMode(config.getSideMode(RelativeSide.fromDirections(facing.getOpposite(), face))).getBlitter().blitWorld(pose, buffer, face, packedOverlay);
  }

  private static Icon byMode(IOSideMode mode) {
    return mode.isEnabled() ? Icon.AUTO_EXPORT_ON : Icon.AUTO_EXPORT_OFF;
  }
}
