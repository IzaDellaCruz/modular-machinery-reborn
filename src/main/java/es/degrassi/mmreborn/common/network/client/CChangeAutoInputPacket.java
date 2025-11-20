package es.degrassi.mmreborn.common.network.client;

import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.common.entity.base.IAutoInputEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CChangeAutoInputPacket(boolean autoOutput, BlockPos pos) implements CustomPacketPayload {
  public static final Type<CChangeAutoInputPacket> TYPE = new Type<>(ModularMachineryReborn.rl("change_auto_input"));

  public static final StreamCodec<ByteBuf, CChangeAutoInputPacket> CODEC = StreamCodec.composite(
      ByteBufCodecs.BOOL,
      CChangeAutoInputPacket::autoOutput,
      BlockPos.STREAM_CODEC,
      CChangeAutoInputPacket::pos,
      CChangeAutoInputPacket::new
  );

  @Override
  public Type<CChangeAutoInputPacket> type() {
    return TYPE;
  }

  public static void handle(CChangeAutoInputPacket packet, IPayloadContext context) {
    if (context.player() instanceof ServerPlayer sp) {
      context.enqueueWork(() -> {
        if (sp.level().getBlockEntity(packet.pos) instanceof IAutoInputEntity entity) {
          entity.setShouldAutoInput(packet.autoOutput);
          ((BlockEntity) entity).setChanged();
        }
      });
    }
  }
}
