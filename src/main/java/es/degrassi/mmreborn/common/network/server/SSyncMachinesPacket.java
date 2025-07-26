package es.degrassi.mmreborn.common.network.server;

import com.google.common.collect.Maps;
import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.common.integration.jei.MMRJeiPlugin;
import es.degrassi.mmreborn.common.machine.DynamicMachine;
import es.degrassi.mmreborn.common.registration.CreativeTabsRegistration;
import es.degrassi.mmreborn.common.util.MMRLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record SSyncMachinesPacket(Map<ResourceLocation, DynamicMachine> machines) implements CustomPacketPayload {
  public static final Type<SSyncMachinesPacket> TYPE = new Type<>(ModularMachineryReborn.rl("sync_machines"));

  public static final StreamCodec<RegistryFriendlyByteBuf, SSyncMachinesPacket> CODEC = new StreamCodec<>() {
    @Override
    public SSyncMachinesPacket decode(RegistryFriendlyByteBuf buf) {
      Map<ResourceLocation, DynamicMachine> map = Maps.newHashMap();
      int size = buf.readInt();
      for (int i = 0; i < size; i++) {
        try {
          ResourceLocation machineId = buf.readResourceLocation();
          DynamicMachine machine = DynamicMachine.CODEC.fromNetwork(buf);
          if (machine == null) throw new IllegalArgumentException("machine is null");
          map.put(machineId, machine);
        } catch (Exception e) {
          MMRLogger.INSTANCE.error("Decode error: ", e);
        }
      }
      return new SSyncMachinesPacket(map);
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf, SSyncMachinesPacket packet) {
      List<DynamicMachine> machines = packet.machines
          .values()
          .stream()
          .filter(Objects::nonNull)
          .filter(machine -> machine != DynamicMachine.DUMMY)
          .toList();
      buf.writeInt(machines.size());
      machines.forEach(machine -> {
        buf.writeResourceLocation(machine.getRegistryName());
        try {
          DynamicMachine.CODEC.toNetwork(machine, buf);
        } catch (Exception e) {
          MMRLogger.INSTANCE.error("Encode error: ", e);
        }
      });
    }
  };

  @Override
  public Type<SSyncMachinesPacket> type() {
    return TYPE;
  }

  public static void handle(SSyncMachinesPacket packet, IPayloadContext context) {
    if (context.flow().isClientbound()) {
      ModularMachineryReborn.MACHINES.clear();
      ModularMachineryReborn.MACHINES.putAll(packet.machines);
      Minecraft mc = Minecraft.getInstance();
      CreativeModeTab.ItemDisplayParameters params = new CreativeModeTab.ItemDisplayParameters(mc.player.connection.enabledFeatures(), mc.player.canUseGameMasterBlocks() && mc.options.operatorItemsTab().get(), mc.level.registryAccess());
      CreativeTabsRegistration.MODULAR_MACHINERY_REBORN_TAB.get().buildContents(params);
    }
  }
}
