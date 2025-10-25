package es.degrassi.mmreborn.client;

import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.client.entity.renderer.StructureCreatorRenderer;
import es.degrassi.mmreborn.common.item.StructureCreatorItem;
import es.degrassi.mmreborn.common.item.StructureCreatorItemMode;
import es.degrassi.mmreborn.common.registration.ItemRegistration;
import es.degrassi.mmreborn.common.registration.KeyMappings;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
    if (KeyMappings.STRUCTURE_MODE_CHANGE.get().consumeClick()) {
      Player player = Minecraft.getInstance().player;
      if (player == null) return;
      ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
      if (!stack.is(ItemRegistration.STRUCTURE_CREATOR_ITEM.get())) return;
      StructureCreatorItemMode first = StructureCreatorItem.getCurrentMode(stack);
      StructureCreatorItem.nextMode(stack);
      StructureCreatorItemMode second = StructureCreatorItem.getCurrentMode(stack);
      player.sendSystemMessage(Component.translatable("modular_machinery_reborn.structure_creator.mode.change",
        first.component().withStyle(ChatFormatting.RED), second.component().withStyle(ChatFormatting.GREEN)));
    }
  }
}
