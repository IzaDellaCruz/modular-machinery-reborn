package es.degrassi.mmreborn.common.network.client;

import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.common.entity.base.IAutoOutputEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CChangeAutoOutputPacket(boolean autoOutput, BlockPos pos) implements CustomPacketPayload {
  public static final Type<CChangeAutoOutputPacket> TYPE = new Type<>(ModularMachineryReborn.rl("change_auto_output"));

  public static final StreamCodec<ByteBuf, CChangeAutoOutputPacket> CODEC = StreamCodec.composite(
      ByteBufCodecs.BOOL,
      CChangeAutoOutputPacket::autoOutput,
      BlockPos.STREAM_CODEC,
      CChangeAutoOutputPacket::pos,
      CChangeAutoOutputPacket::new
  );

  @Override
  public Type<CChangeAutoOutputPacket> type() {
    return TYPE;
  }

  public static void handle(CChangeAutoOutputPacket packet, IPayloadContext context) {
    if (context.player() instanceof ServerPlayer sp) {
      context.enqueueWork(() -> {
        if (sp.level().getBlockEntity(packet.pos) instanceof IAutoOutputEntity entity) {
          entity.setShouldAutoOutput(packet.autoOutput);
          ((BlockEntity) entity).setChanged();
        }
      });
    }
  }
}
