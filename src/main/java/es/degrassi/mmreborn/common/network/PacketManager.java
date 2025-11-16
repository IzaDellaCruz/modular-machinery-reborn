package es.degrassi.mmreborn.common.network;

import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.common.network.client.CBreakStructurePacket;
import es.degrassi.mmreborn.common.network.client.CCoreButtonClickedPacked;
import es.degrassi.mmreborn.common.network.client.CExperienceButtonClickedPacket;
import es.degrassi.mmreborn.common.network.client.CPlaceStructurePacket;
import es.degrassi.mmreborn.common.network.client.CRedstoneButtonModeClickedPacket;
import es.degrassi.mmreborn.common.network.client.emi.FillRecipeC2SPacket;
import es.degrassi.mmreborn.common.network.server.SAddControllerRenderer;
import es.degrassi.mmreborn.common.network.server.SLootTablesPacket;
import es.degrassi.mmreborn.common.network.server.SMachineUpdatePacket;
import es.degrassi.mmreborn.common.network.server.SOpenFilePacket;
import es.degrassi.mmreborn.common.network.server.SRemoveControllerRenderer;
import es.degrassi.mmreborn.common.network.server.SStopSoundInstancePacket;
import es.degrassi.mmreborn.common.network.server.SSyncMachinesPacket;
import es.degrassi.mmreborn.common.network.server.SSyncPauseStatePacket;
import es.degrassi.mmreborn.common.network.server.SUpdateContainerPacket;
import es.degrassi.mmreborn.common.network.server.SUpdateCraftingStatusPacket;
import es.degrassi.mmreborn.common.network.server.SUpdateMachineColorPacket;
import es.degrassi.mmreborn.common.network.server.SUpdateMachineTexturePacket;
import es.degrassi.mmreborn.common.network.server.component.SUpdateCoresPacket;
import es.degrassi.mmreborn.common.network.server.component.SUpdateEffectComponent;
import es.degrassi.mmreborn.common.network.server.component.SUpdateEnergyComponentPacket;
import es.degrassi.mmreborn.common.network.server.component.SUpdateExperienceComponentPacket;
import es.degrassi.mmreborn.common.network.server.component.SUpdateFluidComponentPacket;
import es.degrassi.mmreborn.common.network.server.component.SUpdateFuelComponentPacket;
import es.degrassi.mmreborn.common.network.server.component.SUpdateItemComponentPacket;
import es.degrassi.mmreborn.common.util.Mods;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = ModularMachineryReborn.MODID)
public class PacketManager {
  private PacketManager() {}
  @SubscribeEvent
  public static void register(final RegisterPayloadHandlersEvent event) {
    final PayloadRegistrar registrar = event.registrar(ModularMachineryReborn.MODID);
    // TO CLIENT
    registrar.playToClient(SLootTablesPacket.TYPE, SLootTablesPacket.CODEC, SLootTablesPacket::handle);
    registrar.playToClient(SOpenFilePacket.TYPE, SOpenFilePacket.CODEC, SOpenFilePacket::handle);
    registrar.playToClient(SMachineUpdatePacket.TYPE, SMachineUpdatePacket.CODEC, SMachineUpdatePacket::handle);
    registrar.playToClient(SUpdateEnergyComponentPacket.TYPE, SUpdateEnergyComponentPacket.CODEC, SUpdateEnergyComponentPacket::handle);
    registrar.playToClient(SUpdateExperienceComponentPacket.TYPE, SUpdateExperienceComponentPacket.CODEC, SUpdateExperienceComponentPacket::handle);
    registrar.playToClient(SUpdateFuelComponentPacket.TYPE, SUpdateFuelComponentPacket.CODEC, SUpdateFuelComponentPacket::handle);
    registrar.playToClient(SUpdateFluidComponentPacket.TYPE, SUpdateFluidComponentPacket.CODEC, SUpdateFluidComponentPacket::handle);
    registrar.playToClient(SUpdateItemComponentPacket.TYPE, SUpdateItemComponentPacket.CODEC, SUpdateItemComponentPacket::handle);
    registrar.playToClient(SUpdateCraftingStatusPacket.TYPE, SUpdateCraftingStatusPacket.CODEC, SUpdateCraftingStatusPacket::handle);
    registrar.playToClient(SUpdateMachineColorPacket.TYPE, SUpdateMachineColorPacket.CODEC, SUpdateMachineColorPacket::handle);
    registrar.playToClient(SUpdateMachineTexturePacket.TYPE, SUpdateMachineTexturePacket.CODEC, SUpdateMachineTexturePacket::handle);
    registrar.playToClient(SSyncMachinesPacket.TYPE, SSyncMachinesPacket.CODEC, SSyncMachinesPacket::handle);
    registrar.playToClient(SSyncPauseStatePacket.TYPE, SSyncPauseStatePacket.CODEC, SSyncPauseStatePacket::handle);
    registrar.playToClient(SAddControllerRenderer.TYPE, SAddControllerRenderer.CODEC, SAddControllerRenderer::handle);
    registrar.playToClient(SRemoveControllerRenderer.TYPE, SRemoveControllerRenderer.CODEC, SRemoveControllerRenderer::handle);
    registrar.playToClient(SUpdateContainerPacket.TYPE, SUpdateContainerPacket.CODEC, SUpdateContainerPacket::handle);
    registrar.playToClient(SUpdateCoresPacket.TYPE, SUpdateCoresPacket.CODEC, SUpdateCoresPacket::handle);
    registrar.playToClient(SStopSoundInstancePacket.TYPE, SStopSoundInstancePacket.CODEC, SStopSoundInstancePacket::handle);
    registrar.playToClient(SUpdateEffectComponent.TYPE, SUpdateEffectComponent.CODEC, SUpdateEffectComponent::handle);

    // TO SERVER
    registrar.playToServer(CPlaceStructurePacket.TYPE, CPlaceStructurePacket.CODEC, CPlaceStructurePacket::handle);
    registrar.playToServer(CBreakStructurePacket.TYPE, CBreakStructurePacket.CODEC, CBreakStructurePacket::handle);
    registrar.playToServer(CExperienceButtonClickedPacket.TYPE, CExperienceButtonClickedPacket.CODEC, CExperienceButtonClickedPacket::handle);
    registrar.playToServer(CCoreButtonClickedPacked.TYPE, CCoreButtonClickedPacked.CODEC, CCoreButtonClickedPacked::handle);
    registrar.playToServer(CRedstoneButtonModeClickedPacket.TYPE, CRedstoneButtonModeClickedPacket.CODEC, CRedstoneButtonModeClickedPacket::handle);

    // EMI packet
    if (Mods.isJEIorEMILoaded()) {
      registrar.playToServer(FillRecipeC2SPacket.TYPE, FillRecipeC2SPacket.CODEC, FillRecipeC2SPacket::handle);
    }
  }
}
