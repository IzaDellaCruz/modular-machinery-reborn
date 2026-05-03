package es.degrassi.mmreborn.common.integration.kubejs;

import es.degrassi.mmreborn.common.network.client.CDynamicTooltipEventCallPacket;
import net.neoforged.neoforge.network.PacketDistributor;

public class CKubeJSIntegration {
  private CKubeJSIntegration() {}

  public static void collectDynamicTooltip(int windowId) {
    PacketDistributor.sendToServer(new CDynamicTooltipEventCallPacket(windowId));
  }
}
