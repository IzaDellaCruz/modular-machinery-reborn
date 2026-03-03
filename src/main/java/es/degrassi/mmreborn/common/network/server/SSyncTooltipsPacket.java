package es.degrassi.mmreborn.common.network.server;

import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.api.client.machine.TooltipUse;
import es.degrassi.mmreborn.common.util.MMRLogger;
import es.degrassi.mmreborn.common.util.TextComponentUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.EnumMap;
import java.util.List;

public record SSyncTooltipsPacket(ResourceLocation machineId, EnumMap<TooltipUse, List<Component>> tooltips) implements CustomPacketPayload {
  public static final Type<SSyncTooltipsPacket> TYPE = new Type<>(ModularMachineryReborn.rl("sync_tooltips"));

  public static final StreamCodec<RegistryFriendlyByteBuf, SSyncTooltipsPacket> CODEC = new StreamCodec<>() {
    @Override
    public SSyncTooltipsPacket decode(RegistryFriendlyByteBuf buf) {
      var rl = buf.readResourceLocation();
      EnumMap<TooltipUse, List<Component>> tooltips = new EnumMap<>(TooltipUse.class);
      var map = buf.readMap(TooltipUse.CODEC::fromNetwork, TextComponentUtils.CODEC.listOf()::fromNetwork);
      tooltips.putAll(map);
      return new SSyncTooltipsPacket(rl, tooltips);
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf, SSyncTooltipsPacket packet) {
      try {
        buf.writeResourceLocation(packet.machineId);
        buf.writeMap(
            packet.tooltips,
            TooltipUse.CODEC::toNetwork,
            TextComponentUtils.CODEC.listOf()::toNetwork
        );
      } catch (Exception e) {
        MMRLogger.INSTANCE.error("Encode error: ", e);
      }
    }
  };

  @Override
  public Type<SSyncTooltipsPacket> type() {
    return TYPE;
  }

  public static void handle(SSyncTooltipsPacket packet, IPayloadContext context) {
    if (context.flow().isClientbound()) {
      ModularMachineryReborn.MACHINE_EXTRA_TOOLTIPS.put(packet.machineId, packet.tooltips);
    }
  }
}
