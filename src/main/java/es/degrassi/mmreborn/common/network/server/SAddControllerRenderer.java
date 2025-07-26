package es.degrassi.mmreborn.common.network.server;

import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.client.entity.renderer.ControllerRenderer;
import es.degrassi.mmreborn.common.entity.MachineControllerEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SAddControllerRenderer(BlockPos controllerPos) implements CustomPacketPayload {

  public static final Type<SAddControllerRenderer> TYPE = new Type<>(ModularMachineryReborn.rl("add_renderer"));

  public static final StreamCodec<ByteBuf, SAddControllerRenderer> CODEC =
      BlockPos.STREAM_CODEC.map(SAddControllerRenderer::new, SAddControllerRenderer::controllerPos);

  @Override
  public Type<SAddControllerRenderer> type() {
    return TYPE;
  }

  public static void handle(SAddControllerRenderer packet, IPayloadContext context) {
    if (context.flow().isClientbound() && context.player().level().getBlockEntity(packet.controllerPos) instanceof MachineControllerEntity entity) {
      ControllerRenderer.add(entity.getFoundMachine(), packet.controllerPos);
    }
  }
}
