package es.degrassi.mmreborn.client;

import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.client.entity.renderer.StructureCreatorRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = ModularMachineryReborn.MODID, value = Dist.CLIENT)
public abstract class MMRClientEvents {
  private MMRClientEvents() {}
  @SubscribeEvent
  public static void renderLevel(final RenderLevelStageEvent event) {
    if(event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
      StructureCreatorRenderer.renderSelectedBlocks(event.getPoseStack());
    }
  }

  @SubscribeEvent
  public static void clientTick(ClientTickEvent.Post event) {
  }
}
